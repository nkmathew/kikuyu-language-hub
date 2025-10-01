from typing import Dict, Any
from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse
from sqlalchemy.orm import Session
from ...models.contribution import ContributionStatus
from ...services.contribution_service import ContributionService
from ...db.session import get_db

router = APIRouter(prefix="/export", tags=["export"])


@router.get("/translations.json")
def export_translations(db: Session = Depends(get_db)):
    # Get all approved contributions
    approved_contributions = ContributionService.get_contributions(
        db, status=ContributionStatus.APPROVED, limit=10000
    )
    
    # Transform to Android-friendly JSON format
    translations = {}
    for contribution in approved_contributions:
        # Use source text as key, target text as value
        translations[contribution.source_text] = contribution.target_text
    
    response_data = {
        "translations": translations,
        "count": len(translations),
        "language": "kikuyu"
    }
    
    # Add caching headers
    headers = {
        "Cache-Control": "public, max-age=3600",  # Cache for 1 hour
        "ETag": f'"{hash(str(sorted(translations.items())))}"'
    }
    
    return JSONResponse(content=response_data, headers=headers)