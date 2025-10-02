import json
import hmac
import hashlib
import asyncio
from typing import List, Dict, Any, Optional
from datetime import datetime, timedelta
import httpx
from sqlalchemy.orm import Session
from sqlalchemy import and_, desc
from ..models.webhook import Webhook, WebhookDelivery, WebhookEvent, WebhookStatus
from ..models.user import User
from ..core.config import settings
import logging

logger = logging.getLogger(__name__)


class WebhookService:
    
    @staticmethod
    def create_webhook(
        db: Session,
        name: str,
        url: str,
        events: List[WebhookEvent],
        user: User,
        secret: Optional[str] = None,
        headers: Optional[Dict[str, str]] = None,
        timeout_seconds: int = 30,
        retry_count: int = 3
    ) -> Webhook:
        """Create a new webhook subscription"""
        webhook = Webhook(
            name=name,
            url=url,
            events=json.dumps([event.value for event in events]),
            secret=secret,
            headers=json.dumps(headers) if headers else None,
            timeout_seconds=timeout_seconds,
            retry_count=retry_count,
            created_by_id=user.id
        )
        
        db.add(webhook)
        db.commit()
        db.refresh(webhook)
        
        logger.info(f"Created webhook {webhook.id} for user {user.id}")
        return webhook
    
    @staticmethod
    def get_user_webhooks(db: Session, user: User) -> List[Webhook]:
        """Get all webhooks for a user"""
        return db.query(Webhook).filter(
            Webhook.created_by_id == user.id
        ).order_by(desc(Webhook.created_at)).all()
    
    @staticmethod
    def get_webhook_by_id(db: Session, webhook_id: int, user: User) -> Optional[Webhook]:
        """Get webhook by ID (user must own it)"""
        return db.query(Webhook).filter(
            and_(
                Webhook.id == webhook_id,
                Webhook.created_by_id == user.id
            )
        ).first()
    
    @staticmethod
    def update_webhook_status(db: Session, webhook_id: int, status: WebhookStatus) -> None:
        """Update webhook status"""
        db.query(Webhook).filter(Webhook.id == webhook_id).update({
            "status": status,
            "updated_at": datetime.utcnow()
        })
        db.commit()
    
    @staticmethod
    def delete_webhook(db: Session, webhook_id: int, user: User) -> bool:
        """Delete a webhook (user must own it)"""
        webhook = WebhookService.get_webhook_by_id(db, webhook_id, user)
        if webhook:
            db.delete(webhook)
            db.commit()
            logger.info(f"Deleted webhook {webhook_id} by user {user.id}")
            return True
        return False
    
    @staticmethod
    async def trigger_webhooks(
        db: Session,
        event_type: WebhookEvent,
        payload: Dict[str, Any]
    ) -> None:
        """Trigger all active webhooks for a specific event"""
        # Get all active webhooks that subscribe to this event
        webhooks = db.query(Webhook).filter(
            and_(
                Webhook.status == WebhookStatus.ACTIVE,
                Webhook.events.contains(f'"{event_type.value}"')
            )
        ).all()
        
        if not webhooks:
            return
        
        # Prepare payload
        webhook_payload = {
            "event": event_type.value,
            "timestamp": datetime.utcnow().isoformat(),
            "data": payload
        }
        
        # Send webhooks concurrently
        tasks = []
        for webhook in webhooks:
            task = asyncio.create_task(
                WebhookService._deliver_webhook(db, webhook, webhook_payload)
            )
            tasks.append(task)
        
        await asyncio.gather(*tasks, return_exceptions=True)
    
    @staticmethod
    async def _deliver_webhook(
        db: Session,
        webhook: Webhook,
        payload: Dict[str, Any]
    ) -> None:
        """Deliver a single webhook with retry logic"""
        payload_json = json.dumps(payload)
        
        # Prepare headers
        headers = {
            "Content-Type": "application/json",
            "User-Agent": f"KikuyuHub-Webhook/1.0",
            "X-Webhook-Event": payload["event"],
            "X-Webhook-Delivery": str(datetime.utcnow().timestamp())
        }
        
        # Add custom headers
        if webhook.headers:
            try:
                custom_headers = json.loads(webhook.headers)
                headers.update(custom_headers)
            except json.JSONDecodeError:
                logger.warning(f"Invalid custom headers for webhook {webhook.id}")
        
        # Add signature if secret is provided
        if webhook.secret:
            signature = hmac.new(
                webhook.secret.encode(),
                payload_json.encode(),
                hashlib.sha256
            ).hexdigest()
            headers["X-Webhook-Signature"] = f"sha256={signature}"
        
        # Attempt delivery with retries
        for attempt in range(1, webhook.retry_count + 1):
            delivery = WebhookDelivery(
                webhook_id=webhook.id,
                event_type=payload["event"],
                payload=payload_json,
                headers_sent=json.dumps(headers),
                attempt_number=attempt
            )
            
            try:
                start_time = datetime.utcnow()
                
                async with httpx.AsyncClient(timeout=webhook.timeout_seconds) as client:
                    response = await client.post(
                        webhook.url,
                        json=payload,
                        headers=headers
                    )
                
                end_time = datetime.utcnow()
                duration_ms = int((end_time - start_time).total_seconds() * 1000)
                
                # Record delivery details
                delivery.status_code = response.status_code
                delivery.response_body = response.text[:1000]  # Limit size
                delivery.response_headers = json.dumps(dict(response.headers))
                delivery.delivered_at = end_time
                delivery.duration_ms = duration_ms
                delivery.is_successful = 200 <= response.status_code < 300
                
                db.add(delivery)
                
                # Update webhook stats
                webhook.total_calls += 1
                webhook.last_triggered_at = end_time
                
                if delivery.is_successful:
                    webhook.successful_calls += 1
                    logger.info(f"Webhook {webhook.id} delivered successfully (attempt {attempt})")
                    break
                else:
                    webhook.failed_calls += 1
                    delivery.error_message = f"HTTP {response.status_code}: {response.text[:200]}"
                    logger.warning(f"Webhook {webhook.id} failed with status {response.status_code} (attempt {attempt})")
                
            except Exception as e:
                # Record failed delivery
                delivery.error_message = str(e)[:500]
                delivery.delivered_at = datetime.utcnow()
                delivery.is_successful = False
                
                db.add(delivery)
                webhook.total_calls += 1
                webhook.failed_calls += 1
                
                logger.error(f"Webhook {webhook.id} delivery failed (attempt {attempt}): {e}")
                
                if attempt < webhook.retry_count:
                    # Wait before retry (exponential backoff)
                    await asyncio.sleep(min(2 ** attempt, 60))
        
        # Disable webhook if it keeps failing
        if webhook.failed_calls >= 10 and webhook.successful_calls == 0:
            webhook.status = WebhookStatus.FAILED
            logger.warning(f"Disabled webhook {webhook.id} due to repeated failures")
        
        db.commit()
    
    @staticmethod
    def get_webhook_deliveries(
        db: Session,
        webhook_id: int,
        user: User,
        limit: int = 50
    ) -> List[WebhookDelivery]:
        """Get recent deliveries for a webhook"""
        webhook = WebhookService.get_webhook_by_id(db, webhook_id, user)
        if not webhook:
            return []
        
        return db.query(WebhookDelivery).filter(
            WebhookDelivery.webhook_id == webhook_id
        ).order_by(desc(WebhookDelivery.created_at)).limit(limit).all()
    
    @staticmethod
    def get_webhook_stats(db: Session, webhook_id: int, user: User) -> Optional[Dict[str, Any]]:
        """Get webhook statistics"""
        webhook = WebhookService.get_webhook_by_id(db, webhook_id, user)
        if not webhook:
            return None
        
        # Get recent deliveries for success rate
        recent_deliveries = db.query(WebhookDelivery).filter(
            and_(
                WebhookDelivery.webhook_id == webhook_id,
                WebhookDelivery.created_at >= datetime.utcnow() - timedelta(days=7)
            )
        ).all()
        
        successful_recent = sum(1 for d in recent_deliveries if d.is_successful)
        
        return {
            "webhook_id": webhook.id,
            "name": webhook.name,
            "status": webhook.status.value,
            "total_calls": webhook.total_calls,
            "successful_calls": webhook.successful_calls,
            "failed_calls": webhook.failed_calls,
            "success_rate": webhook.successful_calls / max(webhook.total_calls, 1) * 100,
            "recent_success_rate": successful_recent / max(len(recent_deliveries), 1) * 100,
            "last_triggered": webhook.last_triggered_at.isoformat() if webhook.last_triggered_at else None,
            "avg_response_time": WebhookService._calculate_avg_response_time(recent_deliveries)
        }
    
    @staticmethod
    def _calculate_avg_response_time(deliveries: List[WebhookDelivery]) -> Optional[float]:
        """Calculate average response time from deliveries"""
        successful_deliveries = [d for d in deliveries if d.is_successful and d.duration_ms]
        if not successful_deliveries:
            return None
        
        return sum(d.duration_ms for d in successful_deliveries) / len(successful_deliveries)