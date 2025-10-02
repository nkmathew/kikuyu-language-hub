"""
API endpoints for Kikuyu verb morphology system
"""
from typing import List, Optional, Dict, Any
from fastapi import APIRouter, Depends, HTTPException, Query, BackgroundTasks
from sqlalchemy.orm import Session
from sqlalchemy import and_, or_, func

from ...core.security import get_current_user, require_moderator_or_admin
from ...db.session import get_db
from ...models.user import User
from ...models.morphology import (
    Verb, VerbConjugation, NounForm, VerbExample, 
    MorphologicalSubmission, MorphologicalPattern, WordClass
)
from ...schemas.morphology import (
    VerbCreate, VerbUpdate, Verb, VerbDetail, VerbListResponse,
    VerbConjugationCreate, VerbConjugationUpdate, VerbConjugation,
    VerbExampleCreate, VerbExampleUpdate, VerbExample,
    MorphologicalSubmissionCreate, MorphologicalSubmissionUpdate, MorphologicalSubmission,
    VerbSearch, ConjugationSearch, MorphologicalSubmissionResponse,
    VerbExport, MorphologyExport
)
from ...services.morphology_service import MorphologyService
from ...services.nlp_service import NLPService
from ...core.cache import cache_manager
from ...utils.pagination import PaginationParams, paginate

router = APIRouter(prefix="/morphology", tags=["morphology"])


# Verb endpoints
@router.post("/verbs", response_model=Verb, status_code=201)
def create_verb(
    verb_data: VerbCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Create a new verb with its conjugations and forms"""
    return MorphologyService.create_verb(db, verb_data, current_user.id)


@router.get("/verbs", response_model=VerbListResponse)
def list_verbs(
    search: Optional[str] = Query(None, description="Search in base form or English meaning"),
    verb_class: Optional[str] = Query(None, description="Filter by verb class"),
    semantic_field: Optional[str] = Query(None, description="Filter by semantic field"),
    is_transitive: Optional[bool] = Query(None, description="Filter by transitivity"),
    has_conjugations: Optional[bool] = Query(None, description="Filter by conjugation completeness"),
    limit: int = Query(20, ge=1, le=100),
    offset: int = Query(0, ge=0),
    db: Session = Depends(get_db)
):
    """List verbs with optional filtering and pagination"""
    cache_key = f"verbs:list:{search}:{verb_class}:{semantic_field}:{is_transitive}:{has_conjugations}:{limit}:{offset}"
    
    cached_result = cache_manager.get(cache_key)
    if cached_result:
        return cached_result
    
    query = db.query(Verb)
    
    # Apply filters
    if search:
        query = query.filter(
            or_(
                Verb.base_form.ilike(f"%{search}%"),
                Verb.english_meaning.ilike(f"%{search}%")
            )
        )
    
    if verb_class:
        query = query.filter(Verb.verb_class == verb_class)
    
    if semantic_field:
        query = query.filter(Verb.semantic_field == semantic_field)
    
    if is_transitive is not None:
        query = query.filter(Verb.is_transitive == is_transitive)
    
    if has_conjugations is not None:
        if has_conjugations:
            query = query.join(VerbConjugation).distinct()
        else:
            query = query.outerjoin(VerbConjugation).filter(VerbConjugation.id.is_(None))
    
    total = query.count()
    verbs = query.offset(offset).limit(limit).all()
    
    result = paginate_response(verbs, total, limit, offset, VerbListResponse)
    cache_manager.set(cache_key, result, ttl=300)  # 5 minutes
    
    return result


@router.get("/verbs/{verb_id}", response_model=VerbDetail)
def get_verb(
    verb_id: int,
    include_conjugations: bool = Query(True, description="Include full conjugation data"),
    include_examples: bool = Query(True, description="Include example sentences"),
    db: Session = Depends(get_db)
):
    """Get detailed verb information"""
    cache_key = f"verb:detail:{verb_id}:{include_conjugations}:{include_examples}"
    
    cached_result = cache_manager.get(cache_key)
    if cached_result:
        return cached_result
    
    verb = db.query(Verb).filter(Verb.id == verb_id).first()
    if not verb:
        raise HTTPException(status_code=404, detail="Verb not found")
    
    # Build response with optional includes
    verb_dict = VerbDetail.from_orm(verb).dict()
    
    if include_conjugations:
        conjugations = db.query(VerbConjugation).filter(VerbConjugation.verb_id == verb_id).all()
        verb_dict["conjugations"] = [VerbConjugation.from_orm(c).dict() for c in conjugations]
    
    if include_examples:
        examples = db.query(VerbExample).filter(VerbExample.verb_id == verb_id).all()
        verb_dict["examples"] = [VerbExample.from_orm(e).dict() for e in examples]
    
    cache_manager.set(cache_key, verb_dict, ttl=600)  # 10 minutes
    
    return verb_dict


@router.put("/verbs/{verb_id}", response_model=Verb)
def update_verb(
    verb_id: int,
    verb_data: VerbUpdate,
    current_user: User = Depends(require_moderator_or_admin),
    db: Session = Depends(get_db)
):
    """Update verb information"""
    verb = db.query(Verb).filter(Verb.id == verb_id).first()
    if not verb:
        raise HTTPException(status_code=404, detail="Verb not found")
    
    return MorphologyService.update_verb(db, verb, verb_data, current_user.id)


@router.delete("/verbs/{verb_id}", status_code=204)
def delete_verb(
    verb_id: int,
    current_user: User = Depends(require_moderator_or_admin),
    db: Session = Depends(get_db)
):
    """Delete a verb and all its related data"""
    verb = db.query(Verb).filter(Verb.id == verb_id).first()
    if not verb:
        raise HTTPException(status_code=404, detail="Verb not found")
    
    db.delete(verb)
    db.commit()
    
    # Clear related cache
    cache_manager.delete_pattern(f"verb:detail:{verb_id}:*")
    cache_manager.delete_pattern("verbs:list:*")


# Conjugation endpoints
@router.get("/conjugations", response_model=List[VerbConjugation])
def list_conjugations(
    verb_id: Optional[int] = Query(None, description="Filter by verb ID"),
    base_form: Optional[str] = Query(None, description="Filter by verb base form"),
    tense: Optional[str] = Query(None, description="Filter by tense"),
    aspect: Optional[str] = Query(None, description="Filter by aspect"),
    mood: Optional[str] = Query(None, description="Filter by mood"),
    polarity: Optional[str] = Query(None, description="Filter by polarity"),
    person: Optional[str] = Query(None, description="Filter by person"),
    number: Optional[str] = Query(None, description="Filter by number"),
    is_common: Optional[bool] = Query(None, description="Filter by common usage"),
    limit: int = Query(50, ge=1, le=200),
    offset: int = Query(0, ge=0),
    db: Session = Depends(get_db)
):
    """List verb conjugations with comprehensive filtering"""
    cache_key = f"conjugations:list:{verb_id}:{base_form}:{tense}:{aspect}:{mood}:{polarity}:{person}:{number}:{is_common}:{limit}:{offset}"
    
    cached_result = cache_manager.get(cache_key)
    if cached_result:
        return cached_result
    
    query = db.query(VerbConjugation)
    
    # Apply filters
    if verb_id:
        query = query.filter(VerbConjugation.verb_id == verb_id)
    
    if base_form:
        query = query.join(Verb).filter(Verb.base_form.ilike(f"%{base_form}%"))
    
    if tense:
        query = query.filter(VerbConjugation.tense == tense)
    
    if aspect:
        query = query.filter(VerbConjugation.aspect == aspect)
    
    if mood:
        query = query.filter(VerbConjugation.mood == mood)
    
    if polarity:
        query = query.filter(VerbConjugation.polarity == polarity)
    
    if person:
        query = query.filter(VerbConjugation.person == person)
    
    if number:
        query = query.filter(VerbConjugation.number == number)
    
    if is_common is not None:
        query = query.filter(VerbConjugation.is_common == is_common)
    
    conjugations = query.offset(offset).limit(limit).all()
    
    result = [VerbConjugation.from_orm(c).dict() for c in conjugations]
    cache_manager.set(cache_key, result, ttl=300)  # 5 minutes
    
    return result


@router.post("/conjugations", response_model=VerbConjugation, status_code=201)
def create_conjugation(
    conjugation_data: VerbConjugationCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Add a new conjugation to an existing verb"""
    return MorphologyService.create_conjugation(db, conjugation_data, current_user.id)


# Morphological submission endpoints
@router.post("/submissions", response_model=MorphologicalSubmissionResponse, status_code=201)
def submit_morphology(
    submission_data: MorphologicalSubmissionCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
    background_tasks: BackgroundTasks = BackgroundTasks()
):
    """Submit a new verb or morphological data for review"""
    # Validate the submission
    validation = MorphologyService.validate_submission(submission_data)
    
    # Check for similar existing entries
    similar_existing = MorphologyService.find_similar_verbs(db, submission_data.base_form, submission_data.english_meaning)
    
    # Create the submission
    submission = MorphologySubmission(
        **submission_data.dict(),
        created_by_id=current_user.id,
        status="pending"
    )
    db.add(submission)
    db.commit()
    db.refresh(submission)
    
    # Schedule background processing
    background_tasks.add_task(
        MorphologyService.process_submission_background,
        submission.id,
        validation.confidence_score
    )
    
    return MorphologicalSubmissionResponse(
        submission=submission,
        validation=validation,
        similar_existing=similar_existing,
        confidence_score=validation.confidence_score
    )


@router.get("/submissions", response_model=List[MorphologicalSubmission])
def list_submissions(
    status: Optional[str] = Query(None, description="Filter by status"),
    submission_type: Optional[str] = Query(None, description="Filter by submission type"),
    created_by_id: Optional[int] = Query(None, description="Filter by submitter"),
    limit: int = Query(20, ge=1, le=100),
    offset: int = Query(0, ge=0),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """List morphological submissions (moderator/admin can see all)"""
    query = db.query(MorphologicalSubmission)
    
    # Non-moderators can only see their own submissions
    if not (current_user.role in ["moderator", "admin"]):
        query = query.filter(MorphologicalSubmission.created_by_id == current_user.id)
    
    # Apply filters
    if status:
        query = query.filter(MorphologicalSubmission.status == status)
    
    if submission_type:
        query = query.filter(MorphologicalSubmission.submission_type == submission_type)
    
    if created_by_id:
        query = query.filter(MorphologicalSubmission.created_by_id == created_by_id)
    
    submissions = query.order_by(MorphologicalSubmission.created_at.desc()).offset(offset).limit(limit).all()
    
    return [MorphologicalSubmission.from_orm(s).dict() for s in submissions]


@router.post("/submissions/{submission_id}/approve", status_code=200)
def approve_submission(
    submission_id: int,
    review_notes: Optional[str] = None,
    current_user: User = Depends(require_moderator_or_admin),
    db: Session = Depends(get_db)
):
    """Approve a morphological submission and create the actual verb/form"""
    submission = db.query(MorphologicalSubmission).filter(MorphologicalSubmission.id == submission_id).first()
    if not submission:
        raise HTTPException(status_code=404, detail="Submission not found")
    
    if submission.status != "pending":
        raise HTTPException(status_code=400, detail=f"Submission is {submission.status}, cannot approve")
    
    # Process the approval
    result = MorphologyService.approve_submission(db, submission, current_user.id, review_notes)
    
    return {"message": "Submission approved successfully", "created": result}


@router.post("/submissions/{submission_id}/reject", status_code=200)
def reject_submission(
    submission_id: int,
    review_notes: str = Query(..., description="Reason for rejection"),
    current_user: User = Depends(require_moderator_or_admin),
    db: Session = Depends(get_db)
):
    """Reject a morphological submission"""
    submission = db.query(MorphologicalSubmission).filter(MorphologicalSubmission.id == submission_id).first()
    if not submission:
        raise HTTPException(status_code=404, detail="Submission not found")
    
    if submission.status != "pending":
        raise HTTPException(status_code=400, detail=f"Submission is {submission.status}, cannot reject")
    
    submission.status = "rejected"
    submission.reviewed_by_id = current_user.id
    submission.review_notes = review_notes
    db.commit()
    
    return {"message": "Submission rejected"}


# Advanced search and analysis endpoints
@router.post("/verbs/search/advanced", response_model=VerbListResponse)
def advanced_verb_search(
    search_params: VerbSearch,
    db: Session = Depends(get_db)
):
    """Advanced verb search with multiple criteria"""
    cache_key = f"verbs:advanced:{hash(str(search_params.dict()))}"
    
    cached_result = cache_manager.get(cache_key)
    if cached_result:
        return cached_result
    
    query = db.query(Verb)
    
    # Apply all search filters
    if search_params.query:
        query = query.filter(
            or_(
                Verb.base_form.ilike(f"%{search_params.query}%"),
                Verb.english_meaning.ilike(f"%{search_params.query}%")
            )
        )
    
    if search_params.english_meaning:
        query = query.filter(Verb.english_meaning.ilike(f"%{search_params.english_meaning}%"))
    
    if search_params.verb_class:
        query = query.filter(Verb.verb_class == search_params.verb_class)
    
    if search_params.semantic_field:
        query = query.filter(Verb.semantic_field == search_params.semantic_field)
    
    if search_params.is_transitive is not None:
        query = query.filter(Verb.is_transitive == search_params.is_transitive)
    
    if search_params.is_stative is not None:
        query = query.filter(Verb.is_stative == search_params.is_stative)
    
    if search_params.register:
        query = query.filter(Verb.register == search_params.register)
    
    # Tense/aspect/mood filters via conjugations
    if any([search_params.tense, search_params.aspect, search_params.mood, search_params.polarity]):
        query = query.join(VerbConjugation)
        
        if search_params.tense:
            query = query.filter(VerbConjugation.tense == search_params.tense)
        if search_params.aspect:
            query = query.filter(VerbConjugation.aspect == search_params.aspect)
        if search_params.mood:
            query = query.filter(VerbConjugation.mood == search_params.mood)
        if search_params.polarity:
            query = query.filter(VerbConjugation.polarity == search_params.polarity)
    
    # Presence filters
    if search_params.has_conjugations is not None:
        if search_params.has_conjugations:
            query = query.join(VerbConjugation).distinct()
        else:
            query = query.outerjoin(VerbConjugation).filter(VerbConjugation.id.is_(None))
    
    if search_params.has_examples is not None:
        if search_params.has_examples:
            query = query.join(VerbExample).distinct()
        else:
            query = query.outerjoin(VerbExample).filter(VerbExample.id.is_(None))
    
    total = query.count()
    verbs = query.offset(search_params.offset).limit(search_params.limit).all()
    
    result = paginate_response(verbs, total, search_params.limit, search_params.offset, VerbListResponse)
    cache_manager.set(cache_key, result, ttl=600)  # 10 minutes
    
    return result


@router.get("/verbs/{verb_id}/conjugations/table", response_model=Dict[str, Any])
def get_conjugation_table(
    verb_id: int,
    tense: Optional[str] = Query(None, description="Filter by tense"),
    aspect: Optional[str] = Query(None, description="Filter by aspect"),
    polarity: Optional[str] = Query(None, description="Filter by polarity"),
    db: Session = Depends(get_db)
):
    """Get conjugations in a structured table format (person/number grid)"""
    cache_key = f"verb:conjugation_table:{verb_id}:{tense}:{aspect}:{polarity}"
    
    cached_result = cache_manager.get(cache_key)
    if cached_result:
        return cached_result
    
    verb = db.query(Verb).filter(Verb.id == verb_id).first()
    if not verb:
        raise HTTPException(status_code=404, detail="Verb not found")
    
    conjugations = db.query(VerbConjugation).filter(VerbConjugation.verb_id == verb_id)
    
    if tense:
        conjugations = conjugations.filter(VerbConjugation.tense == tense)
    if aspect:
        conjugations = conjugations.filter(VerbConjugation.aspect == aspect)
    if polarity:
        conjugations = conjugations.filter(VerbConjugation.polarity == polarity)
    
    conjugations = conjugations.all()
    
    # Organize into table structure
    table = {
        "verb": Verb.from_orm(verb).dict(),
        "conjugations": {}
    }
    
    for conj in conjugations:
        key = f"{conj.tense}_{conj.aspect}_{conj.mood}_{conj.polarity}"
        if key not in table["conjugations"]:
            table["conjugations"][key] = {}
        
        person_num = f"{conj.person}_{conj.number}"
        table["conjugations"][key][person_num] = VerbConjugation.from_orm(conj).dict()
    
    cache_manager.set(cache_key, table, ttl=900)  # 15 minutes
    
    return table


# Export endpoints
@router.post("/verbs/export", response_model=MorphologyExport)
def export_verbs(
    verb_ids: Optional[List[int]] = Query(None, description="Specific verb IDs to export"),
    format: str = Query("json", description="Export format"),
    include_conjugations: bool = Query(True, description="Include conjugations"),
    include_examples: bool = Query(True, description="Include examples"),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Export verbs with all morphological data"""
    if verb_ids:
        verbs = db.query(Verb).filter(Verb.id.in_(verb_ids)).all()
    else:
        # Export all verbs that user has access to
        verbs = db.query(Verb).all()
    
    export_data = []
    for verb in verbs:
        verb_export = VerbExport(
            base_form=verb.base_form,
            english_meaning=verb.english_meaning,
            verb_class=verb.verb_class,
            pronunciation_guide=verb.pronunciation_guide
        )
        
        if include_conjugations:
            conjugations = db.query(VerbConjugation).filter(VerbConjugation.verb_id == verb.id).all()
            conj_dict = {}
            for conj in conjugations:
                key = f"{conj.tense}_{conj.aspect}_{conj.mood}_{conj.polarity}"
                if key not in conj_dict:
                    conj_dict[key] = []
                conj_dict[key].append({
                    "person": conj.person,
                    "number": conj.number,
                    "form": conj.conjugated_form,
                    "morphology": conj.morphological_breakdown
                })
            verb_export.all_conjugations = conj_dict
        
        if include_examples:
            examples = db.query(VerbExample).filter(VerbExample.verb_id == verb.id).all()
            verb_export.examples = [
                {
                    "kikuyu": ex.kikuyu_sentence,
                    "english": ex.english_translation,
                    "context": ex.context_description
                }
                for ex in examples
            ]
        
        export_data.append(verb_export)
    
    from datetime import datetime
    return MorphologyExport(
        verbs=export_data,
        export_date=datetime.utcnow(),
        total_count=len(export_data),
        export_format=format
    )


# Statistics and analytics endpoints
@router.get("/verbs/stats", response_model=Dict[str, Any])
def get_verb_statistics(
    db: Session = Depends(get_db)
):
    """Get statistics about the verb database"""
    cache_key = "verbs:statistics"
    
    cached_result = cache_manager.get(cache_key)
    if cached_result:
        return cached_result
    
    stats = {
        "total_verbs": db.query(Verb).count(),
        "total_conjugations": db.query(VerbConjugation).count(),
        "total_examples": db.query(VerbExample).count(),
        "verb_classes": db.query(Verb.verb_class, func.count(Verb.id)).group_by(Verb.verb_class).all(),
        "semantic_fields": db.query(Verb.semantic_field, func.count(Verb.id)).group_by(Verb.semantic_field).all(),
        "transitive_ratio": {
            "transitive": db.query(Verb).filter(Verb.is_transitive == True).count(),
            "intransitive": db.query(Verb).filter(Verb.is_transitive == False).count()
        },
        "tense_distribution": db.query(VerbConjugation.tense, func.count(VerbConjugation.id)).group_by(VerbConjugation.tense).all(),
        "aspect_distribution": db.query(VerbConjugation.aspect, func.count(VerbConjugation.id)).group_by(VerbConjugation.aspect).all(),
        "recent_submissions": db.query(MorphologicalSubmission).filter(MorphologicalSubmission.status == "pending").count()
    }
    
    cache_manager.set(cache_key, stats, ttl=3600)  # 1 hour
    
    return stats