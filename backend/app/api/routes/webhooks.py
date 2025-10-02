from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from ...db.session import get_db
from ...models.user import User
from ...services.webhook_service import WebhookService
from ...schemas.webhook import (
    WebhookCreate, WebhookUpdate, WebhookResponse, 
    WebhookDeliveryResponse, WebhookStatsResponse
)
from ...models.webhook import WebhookEvent, WebhookStatus
from ...core.security import get_current_user, require_role
from ...models.user import UserRole

router = APIRouter(prefix="/webhooks", tags=["webhooks"])


@router.post("/", response_model=WebhookResponse)
def create_webhook(
    webhook_data: WebhookCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Create a new webhook subscription"""
    # Check if user can create webhooks (contributors and above)
    if current_user.role == UserRole.VIEWER:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Insufficient permissions to create webhooks"
        )
    
    webhook = WebhookService.create_webhook(
        db=db,
        name=webhook_data.name,
        url=str(webhook_data.url),
        events=webhook_data.events,
        user=current_user,
        secret=webhook_data.secret,
        headers=webhook_data.headers,
        timeout_seconds=webhook_data.timeout_seconds,
        retry_count=webhook_data.retry_count
    )
    
    return webhook


@router.get("/", response_model=List[WebhookResponse])
def list_webhooks(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Get all webhooks for the current user"""
    webhooks = WebhookService.get_user_webhooks(db, current_user)
    return webhooks


@router.get("/{webhook_id}", response_model=WebhookResponse)
def get_webhook(
    webhook_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Get a specific webhook by ID"""
    webhook = WebhookService.get_webhook_by_id(db, webhook_id, current_user)
    if not webhook:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Webhook not found"
        )
    return webhook


@router.put("/{webhook_id}", response_model=WebhookResponse)
def update_webhook(
    webhook_id: int,
    webhook_data: WebhookUpdate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Update a webhook"""
    webhook = WebhookService.get_webhook_by_id(db, webhook_id, current_user)
    if not webhook:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Webhook not found"
        )
    
    # Update webhook fields
    update_data = webhook_data.dict(exclude_unset=True)
    for field, value in update_data.items():
        if field == "events" and value:
            # Convert events to JSON string
            import json
            setattr(webhook, field, json.dumps([event.value for event in value]))
        elif field == "headers" and value:
            # Convert headers to JSON string
            import json
            setattr(webhook, field, json.dumps(value))
        elif field == "url":
            setattr(webhook, field, str(value))
        else:
            setattr(webhook, field, value)
    
    db.commit()
    db.refresh(webhook)
    return webhook


@router.delete("/{webhook_id}")
def delete_webhook(
    webhook_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Delete a webhook"""
    success = WebhookService.delete_webhook(db, webhook_id, current_user)
    if not success:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Webhook not found"
        )
    return {"message": "Webhook deleted successfully"}


@router.get("/{webhook_id}/deliveries", response_model=List[WebhookDeliveryResponse])
def get_webhook_deliveries(
    webhook_id: int,
    limit: int = 50,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Get recent webhook deliveries"""
    deliveries = WebhookService.get_webhook_deliveries(
        db, webhook_id, current_user, limit
    )
    return deliveries


@router.get("/{webhook_id}/stats", response_model=WebhookStatsResponse)
def get_webhook_stats(
    webhook_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Get webhook statistics"""
    stats = WebhookService.get_webhook_stats(db, webhook_id, current_user)
    if not stats:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Webhook not found"
        )
    return stats


@router.post("/{webhook_id}/test")
def test_webhook(
    webhook_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Send a test webhook delivery"""
    webhook = WebhookService.get_webhook_by_id(db, webhook_id, current_user)
    if not webhook:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Webhook not found"
        )
    
    # Send test webhook
    import asyncio
    test_payload = {
        "test": True,
        "webhook_id": webhook_id,
        "user_id": current_user.id,
        "message": "This is a test webhook delivery"
    }
    
    # Create async task to send webhook
    async def send_test():
        await WebhookService._deliver_webhook(db, webhook, {
            "event": "webhook.test",
            "timestamp": "2024-01-01T00:00:00Z",
            "data": test_payload
        })
    
    # Run in background
    import threading
    thread = threading.Thread(target=lambda: asyncio.run(send_test()))
    thread.start()
    
    return {"message": "Test webhook sent"}


@router.get("/events/available")
def get_available_events(
    current_user: User = Depends(get_current_user)
):
    """Get list of available webhook events"""
    events = []
    for event in WebhookEvent:
        events.append({
            "value": event.value,
            "description": _get_event_description(event)
        })
    
    return {"events": events}


def _get_event_description(event: WebhookEvent) -> str:
    """Get human-readable description for webhook events"""
    descriptions = {
        WebhookEvent.CONTRIBUTION_CREATED: "Triggered when a new contribution is submitted",
        WebhookEvent.CONTRIBUTION_APPROVED: "Triggered when a contribution is approved by a moderator",
        WebhookEvent.CONTRIBUTION_REJECTED: "Triggered when a contribution is rejected",
        WebhookEvent.CONTRIBUTION_UPDATED: "Triggered when a contribution is modified",
        WebhookEvent.USER_REGISTERED: "Triggered when a new user registers",
        WebhookEvent.QUALITY_THRESHOLD_REACHED: "Triggered when a contribution reaches high quality score",
        WebhookEvent.DAILY_STATS_UPDATE: "Triggered daily with platform statistics"
    }
    return descriptions.get(event, "No description available")