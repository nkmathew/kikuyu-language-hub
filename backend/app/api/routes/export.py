from typing import Dict, Any, List, Optional
from fastapi import APIRouter, Depends, Query
from fastapi.responses import JSONResponse, Response
from sqlalchemy.orm import Session, joinedload
from sqlalchemy import func
import json
import csv
import io
import xml.etree.ElementTree as ET
from xml.dom import minidom
from ...models.contribution import ContributionStatus, Contribution
from ...models.category import Category
from ...services.contribution_service import ContributionService
from ...schemas.contribution import ContributionExport
from ...db.session import get_db
from ...core.cache import cache, CacheConfig

router = APIRouter(prefix="/export", tags=["export"])


@router.get("/translations.json")
def export_translations_legacy(db: Session = Depends(get_db)):
    """Legacy export format for backward compatibility"""
    # Check cache first
    cache_key = "export_data:translations_legacy"
    cached_result = cache.get(cache_key)
    if cached_result:
        return JSONResponse(content=cached_result, headers={
            "Cache-Control": "public, max-age=3600",
            "ETag": f'"{hash(str(sorted(cached_result["translations"].items())))}"'
        })
    
    # Get all approved contributions
    approved_contributions = ContributionService.get_contributions(
        db, status=ContributionStatus.APPROVED, limit=10000
    )
    
    # Transform to simple key-value format
    translations = {}
    for contribution in approved_contributions:
        # Use source text as key, target text as value
        translations[contribution.source_text] = contribution.target_text
    
    response_data = {
        "translations": translations,
        "count": len(translations),
        "language": "kikuyu"
    }
    
    # Cache the result
    cache.set(cache_key, response_data, CacheConfig.EXPORT_DATA_TTL)
    
    # Add caching headers
    headers = {
        "Cache-Control": "public, max-age=3600",  # Cache for 1 hour
        "ETag": f'"{hash(str(sorted(translations.items())))}"'
    }
    
    return JSONResponse(content=response_data, headers=headers)


@router.get("/flashcards.json", response_model=List[ContributionExport])
def export_for_flashcards(
    category_id: Optional[int] = Query(None, description="Filter by category ID"),
    difficulty: Optional[str] = Query(None, description="Filter by difficulty level"),
    min_quality_score: Optional[float] = Query(0.0, description="Minimum quality score"),
    include_sub_translations: bool = Query(False, description="Include word-level translations"),
    db: Session = Depends(get_db)
):
    """Export translations in flashcard app compatible format"""
    # Generate cache key based on parameters
    cache_key = f"export_data:flashcards:{category_id}:{difficulty}:{min_quality_score}:{include_sub_translations}"
    cached_result = cache.get(cache_key)
    if cached_result:
        return cached_result
    
    query = db.query(Contribution).options(
        joinedload(Contribution.categories),
        joinedload(Contribution.sub_translations)
    ).filter(
        Contribution.status == ContributionStatus.APPROVED,
        Contribution.quality_score >= min_quality_score
    )
    
    # Apply filters
    if category_id:
        query = query.join(Contribution.categories).filter(Category.id == category_id)
    
    if difficulty:
        query = query.filter(Contribution.difficulty_level == difficulty)
    
    contributions = query.order_by(Contribution.difficulty_level, Contribution.source_text).all()
    
    # Transform to flashcard format
    flashcard_data = []
    for contribution in contributions:
        # Get primary category name
        category_name = None
        if contribution.categories:
            category_name = contribution.categories[0].slug
        
        # Parse usage examples
        usage_examples = []
        if contribution.usage_examples:
            try:
                usage_examples = json.loads(contribution.usage_examples)
                if not isinstance(usage_examples, list):
                    usage_examples = [str(usage_examples)]
            except (json.JSONDecodeError, TypeError):
                usage_examples = [contribution.usage_examples] if contribution.usage_examples else []
        
        flashcard_item = ContributionExport(
            english=contribution.target_text,
            kikuyu=contribution.source_text,
            category=category_name,
            difficulty=contribution.difficulty_level.value if contribution.difficulty_level else "beginner",
            pronunciation=contribution.pronunciation_guide,
            cultural_notes=contribution.cultural_notes,
            usage_examples=usage_examples
        )
        
        flashcard_data.append(flashcard_item)
        
        # Add sub-translations if requested
        if include_sub_translations and contribution.sub_translations:
            for sub_trans in contribution.sub_translations:
                sub_category = sub_trans.category.slug if sub_trans.category else category_name
                
                sub_flashcard = ContributionExport(
                    english=sub_trans.target_word,
                    kikuyu=sub_trans.source_word,
                    category=sub_category,
                    difficulty=sub_trans.difficulty_level.value,
                    pronunciation=None,
                    cultural_notes=sub_trans.context,
                    usage_examples=[]
                )
                
                flashcard_data.append(sub_flashcard)
    
    # Cache the result
    cache.set(cache_key, flashcard_data, CacheConfig.EXPORT_DATA_TTL)
    
    return flashcard_data


@router.get("/corpus/full.json")
def export_full_corpus(
    format_version: str = Query("v2", description="Export format version"),
    db: Session = Depends(get_db)
):
    """Export complete corpus with all metadata for advanced applications"""
    contributions = db.query(Contribution).options(
        joinedload(Contribution.categories),
        joinedload(Contribution.sub_translations),
        joinedload(Contribution.created_by)
    ).filter(Contribution.status == ContributionStatus.APPROVED).all()
    
    # Get category statistics
    category_stats = db.query(
        Category.id,
        Category.name,
        Category.slug,
        func.count(Contribution.id).label('contribution_count')
    ).outerjoin(
        Contribution.categories
    ).group_by(Category.id, Category.name, Category.slug).all()
    
    categories_data = {
        cat.id: {
            "name": cat.name,
            "slug": cat.slug,
            "contribution_count": cat.contribution_count
        }
        for cat in category_stats
    }
    
    # Transform contributions
    contributions_data = []
    for contrib in contributions:
        contrib_data = {
            "id": contrib.id,
            "source": contrib.source_text,
            "target": contrib.target_text,
            "language": contrib.language,
            "difficulty": contrib.difficulty_level.value if contrib.difficulty_level else "beginner",
            "is_phrase": contrib.is_phrase,
            "word_count": contrib.word_count,
            "quality_score": contrib.quality_score,
            "categories": [cat.slug for cat in contrib.categories],
            "metadata": {
                "context_notes": contrib.context_notes,
                "cultural_notes": contrib.cultural_notes,
                "pronunciation_guide": contrib.pronunciation_guide,
                "usage_examples": contrib.usage_examples,
                "created_at": contrib.created_at.isoformat(),
                "contributor": contrib.created_by.name if contrib.created_by else None
            }
        }
        
        # Add sub-translations
        if contrib.sub_translations:
            contrib_data["sub_translations"] = [
                {
                    "source_word": st.source_word,
                    "target_word": st.target_word,
                    "position": st.word_position,
                    "difficulty": st.difficulty_level.value,
                    "confidence": st.confidence_score,
                    "context": st.context,
                    "category": st.category.slug if st.category else None
                }
                for st in contrib.sub_translations
            ]
        
        contributions_data.append(contrib_data)
    
    response_data = {
        "format_version": format_version,
        "exported_at": func.now(),
        "total_contributions": len(contributions_data),
        "categories": categories_data,
        "contributions": contributions_data,
        "statistics": {
            "total_phrases": sum(1 for c in contributions if c.is_phrase),
            "total_words": sum(c.word_count for c in contributions),
            "avg_quality_score": sum(c.quality_score for c in contributions) / len(contributions) if contributions else 0,
            "difficulty_distribution": {
                level.value: sum(1 for c in contributions if c.difficulty_level == level)
                for level in [None] + list(contrib.difficulty_level.__class__)
                if level is not None
            }
        }
    }
    
    headers = {
        "Content-Disposition": f"attachment; filename=kikuyu_corpus_{format_version}.json",
        "Cache-Control": "public, max-age=1800"  # Cache for 30 minutes
    }
    
    return JSONResponse(content=response_data, headers=headers)


@router.get("/stats")
def export_statistics(db: Session = Depends(get_db)):
    """Get export-related statistics"""
    total_approved = db.query(func.count(Contribution.id)).filter(
        Contribution.status == ContributionStatus.APPROVED
    ).scalar()
    
    total_sub_translations = db.query(func.count()).select_from(
        db.query(Contribution).join(Contribution.sub_translations).filter(
            Contribution.status == ContributionStatus.APPROVED
        ).subquery()
    ).scalar()
    
    categories_with_content = db.query(func.count(func.distinct(Category.id))).select_from(
        Category.__table__.join(Contribution.categories).join(Contribution.__table__)
    ).filter(Contribution.status == ContributionStatus.APPROVED).scalar()
    
    return {
        "total_approved_contributions": total_approved,
        "total_sub_translations": total_sub_translations,
        "categories_with_content": categories_with_content,
        "export_formats": [
            "translations.json (legacy)",
            "flashcards.json (flashcard app)",
            "corpus/full.json (complete corpus)",
            "translations.csv (CSV format)",
            "translations.xml (XML format)",
            "anki.txt (Anki flashcard deck)"
        ]
    }


@router.get("/translations.csv")
def export_translations_csv(
    category_id: Optional[int] = Query(None, description="Filter by category ID"),
    difficulty: Optional[str] = Query(None, description="Filter by difficulty level"),
    min_quality_score: Optional[float] = Query(0.0, description="Minimum quality score"),
    db: Session = Depends(get_db)
):
    """Export translations in CSV format"""
    query = db.query(Contribution).options(
        joinedload(Contribution.categories)
    ).filter(
        Contribution.status == ContributionStatus.APPROVED,
        Contribution.quality_score >= min_quality_score
    )
    
    # Apply filters
    if category_id:
        query = query.join(Contribution.categories).filter(Category.id == category_id)
    
    if difficulty:
        query = query.filter(Contribution.difficulty_level == difficulty)
    
    contributions = query.order_by(Contribution.source_text).all()
    
    # Create CSV content
    output = io.StringIO()
    writer = csv.writer(output)
    
    # Write header
    writer.writerow([
        'English', 'Kikuyu', 'Categories', 'Difficulty', 'Quality Score',
        'Context Notes', 'Cultural Notes', 'Pronunciation'
    ])
    
    # Write data
    for contribution in contributions:
        categories = ', '.join([cat.name for cat in contribution.categories])
        writer.writerow([
            contribution.source_text,
            contribution.target_text,
            categories,
            contribution.difficulty_level.value if contribution.difficulty_level else 'beginner',
            contribution.quality_score,
            contribution.context_notes or '',
            contribution.cultural_notes or '',
            contribution.pronunciation_guide or ''
        ])
    
    csv_content = output.getvalue()
    output.close()
    
    headers = {
        "Content-Disposition": "attachment; filename=kikuyu_translations.csv",
        "Content-Type": "text/csv; charset=utf-8"
    }
    
    return Response(content=csv_content, media_type="text/csv", headers=headers)


@router.get("/translations.xml")
def export_translations_xml(
    category_id: Optional[int] = Query(None, description="Filter by category ID"),
    difficulty: Optional[str] = Query(None, description="Filter by difficulty level"),
    min_quality_score: Optional[float] = Query(0.0, description="Minimum quality score"),
    db: Session = Depends(get_db)
):
    """Export translations in XML format"""
    query = db.query(Contribution).options(
        joinedload(Contribution.categories),
        joinedload(Contribution.sub_translations)
    ).filter(
        Contribution.status == ContributionStatus.APPROVED,
        Contribution.quality_score >= min_quality_score
    )
    
    # Apply filters
    if category_id:
        query = query.join(Contribution.categories).filter(Category.id == category_id)
    
    if difficulty:
        query = query.filter(Contribution.difficulty_level == difficulty)
    
    contributions = query.order_by(Contribution.source_text).all()
    
    # Create XML structure
    root = ET.Element("kikuyu_translations")
    root.set("exported_at", func.now().isoformat())
    root.set("total_count", str(len(contributions)))
    
    for contribution in contributions:
        trans_elem = ET.SubElement(root, "translation")
        trans_elem.set("id", str(contribution.id))
        
        # Basic fields
        ET.SubElement(trans_elem, "english").text = contribution.source_text
        ET.SubElement(trans_elem, "kikuyu").text = contribution.target_text
        ET.SubElement(trans_elem, "difficulty").text = contribution.difficulty_level.value if contribution.difficulty_level else "beginner"
        ET.SubElement(trans_elem, "quality_score").text = str(contribution.quality_score)
        
        # Optional fields
        if contribution.context_notes:
            ET.SubElement(trans_elem, "context_notes").text = contribution.context_notes
        if contribution.cultural_notes:
            ET.SubElement(trans_elem, "cultural_notes").text = contribution.cultural_notes
        if contribution.pronunciation_guide:
            ET.SubElement(trans_elem, "pronunciation").text = contribution.pronunciation_guide
        
        # Categories
        if contribution.categories:
            categories_elem = ET.SubElement(trans_elem, "categories")
            for category in contribution.categories:
                cat_elem = ET.SubElement(categories_elem, "category")
                cat_elem.set("slug", category.slug)
                cat_elem.text = category.name
        
        # Sub-translations
        if contribution.sub_translations:
            sub_trans_elem = ET.SubElement(trans_elem, "sub_translations")
            for sub_trans in contribution.sub_translations:
                sub_elem = ET.SubElement(sub_trans_elem, "sub_translation")
                sub_elem.set("position", str(sub_trans.word_position))
                ET.SubElement(sub_elem, "english").text = sub_trans.source_word
                ET.SubElement(sub_elem, "kikuyu").text = sub_trans.target_word
                if sub_trans.context:
                    ET.SubElement(sub_elem, "context").text = sub_trans.context
    
    # Pretty print XML
    xml_str = ET.tostring(root, encoding='unicode')
    dom = minidom.parseString(xml_str)
    pretty_xml = dom.toprettyxml(indent="  ")
    
    headers = {
        "Content-Disposition": "attachment; filename=kikuyu_translations.xml",
        "Content-Type": "application/xml; charset=utf-8"
    }
    
    return Response(content=pretty_xml, media_type="application/xml", headers=headers)


@router.get("/anki.txt")
def export_anki_deck(
    category_id: Optional[int] = Query(None, description="Filter by category ID"),
    difficulty: Optional[str] = Query(None, description="Filter by difficulty level"),
    min_quality_score: Optional[float] = Query(0.0, description="Minimum quality score"),
    include_pronunciation: bool = Query(True, description="Include pronunciation guide"),
    include_context: bool = Query(True, description="Include context notes"),
    db: Session = Depends(get_db)
):
    """Export translations in Anki flashcard format (tab-separated)"""
    query = db.query(Contribution).options(
        joinedload(Contribution.categories)
    ).filter(
        Contribution.status == ContributionStatus.APPROVED,
        Contribution.quality_score >= min_quality_score
    )
    
    # Apply filters
    if category_id:
        query = query.join(Contribution.categories).filter(Category.id == category_id)
    
    if difficulty:
        query = query.filter(Contribution.difficulty_level == difficulty)
    
    contributions = query.order_by(Contribution.source_text).all()
    
    # Create Anki format content (tab-separated)
    lines = []
    
    for contribution in contributions:
        # Front side (English)
        front = contribution.source_text
        
        # Back side (Kikuyu + optional details)
        back_parts = [contribution.target_text]
        
        if include_pronunciation and contribution.pronunciation_guide:
            back_parts.append(f"<br><i>Pronunciation: {contribution.pronunciation_guide}</i>")
        
        if include_context and contribution.context_notes:
            back_parts.append(f"<br><small>{contribution.context_notes}</small>")
        
        # Add categories as tags
        categories = [cat.slug for cat in contribution.categories]
        category_tags = ' '.join(categories) if categories else 'general'
        
        back = ''.join(back_parts)
        
        # Anki format: Front\tBack\tTags
        line = f"{front}\t{back}\t{category_tags}"
        lines.append(line)
    
    anki_content = '\n'.join(lines)
    
    headers = {
        "Content-Disposition": "attachment; filename=kikuyu_anki_deck.txt",
        "Content-Type": "text/plain; charset=utf-8"
    }
    
    return Response(content=anki_content, media_type="text/plain", headers=headers)


@router.get("/formats")
def get_export_formats():
    """Get list of available export formats with descriptions"""
    formats = [
        {
            "format": "json",
            "endpoint": "/export/translations.json",
            "description": "Legacy JSON format for backward compatibility",
            "mime_type": "application/json",
            "use_case": "Simple key-value translations for basic apps"
        },
        {
            "format": "flashcards_json",
            "endpoint": "/export/flashcards.json",
            "description": "Enhanced JSON format optimized for flashcard applications",
            "mime_type": "application/json",
            "use_case": "Flashcard apps, language learning platforms"
        },
        {
            "format": "corpus_json",
            "endpoint": "/export/corpus/full.json",
            "description": "Complete corpus with all metadata and sub-translations",
            "mime_type": "application/json",
            "use_case": "Academic research, advanced language processing"
        },
        {
            "format": "csv",
            "endpoint": "/export/translations.csv",
            "description": "Comma-separated values format for spreadsheets",
            "mime_type": "text/csv",
            "use_case": "Excel analysis, data processing, simple imports"
        },
        {
            "format": "xml",
            "endpoint": "/export/translations.xml",
            "description": "Structured XML format with hierarchical data",
            "mime_type": "application/xml",
            "use_case": "Enterprise systems, XML-based workflows"
        },
        {
            "format": "anki",
            "endpoint": "/export/anki.txt",
            "description": "Tab-separated format for Anki flashcard import",
            "mime_type": "text/plain",
            "use_case": "Anki spaced repetition system, personal study"
        }
    ]
    
    return {
        "formats": formats,
        "total_formats": len(formats),
        "common_parameters": [
            "category_id: Filter by category ID",
            "difficulty: Filter by difficulty level (beginner/intermediate/advanced)",
            "min_quality_score: Minimum quality score (0.0-5.0)"
        ]
    }