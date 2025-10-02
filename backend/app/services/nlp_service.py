"""
NLP service for advanced language processing and analysis
"""
from typing import List, Dict, Optional, Tuple
from sqlalchemy.orm import Session
from sqlalchemy import func, and_
from ..models.contribution import Contribution, ContributionStatus, DifficultyLevel
from ..models.sub_translation import SubTranslation
from ..utils.nlp import (
    kikuyu_tokenizer, 
    translation_memory, 
    spell_checker, 
    difficulty_analyzer,
    TranslationMatch,
    KikuyuWord
)
from ..core.cache import cached, CacheConfig, invalidate_cache_on_change
import json
import logging

logger = logging.getLogger(__name__)


class NLPService:
    """
    Service for advanced NLP operations on contributions
    """
    
    @staticmethod
    def initialize_nlp_models(db: Session):
        """Initialize NLP models with existing data"""
        logger.info("Initializing NLP models with existing data...")
        
        # Load approved contributions for training
        approved_contributions = db.query(Contribution).filter(
            Contribution.status == ContributionStatus.APPROVED
        ).all()
        
        # Train translation memory
        logger.info(f"Training translation memory with {len(approved_contributions)} translations...")
        for contrib in approved_contributions:
            translation_memory.add_translation(
                contrib.source_text,
                contrib.target_text,
                contrib.context_notes
            )
        
        # Build spell checker dictionary
        logger.info("Building spell checker dictionary...")
        kikuyu_words = []
        for contrib in approved_contributions:
            words = kikuyu_tokenizer.tokenize(contrib.source_text)
            kikuyu_words.extend([w.normalized for w in words])
        
        spell_checker.add_to_dictionary(kikuyu_words)
        
        # Train difficulty analyzer
        logger.info("Training difficulty analyzer...")
        kikuyu_texts = [contrib.source_text for contrib in approved_contributions]
        difficulty_analyzer.train_frequency_model(kikuyu_texts)
        
        logger.info("NLP models initialization complete")
    
    @staticmethod
    @cached(ttl=CacheConfig.TRANSLATION_SUGGESTIONS_TTL, key_prefix="nlp_similar_translations")
    def find_similar_translations(
        source_text: str, 
        threshold: float = 0.7,
        limit: int = 5
    ) -> List[Dict[str, any]]:
        """Find similar translations using translation memory"""
        matches = translation_memory.find_matches(source_text, threshold)
        
        return [
            {
                'source_text': match.source_text,
                'target_text': match.target_text,
                'similarity_score': round(match.similarity_score, 3),
                'match_type': match.match_type,
                'context': match.context
            }
            for match in matches[:limit]
        ]
    
    @staticmethod
    def analyze_text_quality(
        source_text: str, 
        target_text: str
    ) -> Dict[str, any]:
        """Analyze quality of a translation pair"""
        analysis = {
            'source_analysis': {},
            'target_analysis': {},
            'translation_quality': {},
            'suggestions': []
        }
        
        # Analyze source text (Kikuyu)
        source_words = kikuyu_tokenizer.tokenize(source_text)
        source_errors = spell_checker.check_text(source_text)
        source_difficulty = difficulty_analyzer.analyze_difficulty(source_text)
        
        analysis['source_analysis'] = {
            'word_count': len(source_words),
            'spelling_errors': len(source_errors),
            'difficulty': source_difficulty,
            'morphological_complexity': sum(
                len(w.tokens) for w in source_words if w.tokens
            ) / max(len(source_words), 1)
        }
        
        # Basic target text analysis (English)
        target_words = target_text.split()
        analysis['target_analysis'] = {
            'word_count': len(target_words),
            'avg_word_length': sum(len(w) for w in target_words) / max(len(target_words), 1)
        }
        
        # Translation quality metrics
        length_ratio = len(source_text) / max(len(target_text), 1)
        word_ratio = len(source_words) / max(len(target_words), 1)
        
        analysis['translation_quality'] = {
            'length_ratio': round(length_ratio, 2),
            'word_ratio': round(word_ratio, 2),
            'balance_score': 1.0 - abs(1.0 - min(length_ratio, 2.0)) * 0.5
        }
        
        # Generate suggestions
        if source_errors:
            analysis['suggestions'].append({
                'type': 'spelling',
                'message': f"Found {len(source_errors)} potential spelling errors in source text",
                'details': source_errors[:3]  # Show first 3 errors
            })
        
        if length_ratio > 3 or length_ratio < 0.3:
            analysis['suggestions'].append({
                'type': 'length_mismatch',
                'message': f"Unusual length ratio ({length_ratio:.1f}) between source and target",
                'details': None
            })
        
        return analysis
    
    @staticmethod
    def suggest_difficulty_level(source_text: str) -> Tuple[DifficultyLevel, float]:
        """Suggest difficulty level for a contribution"""
        difficulty_analysis = difficulty_analyzer.analyze_difficulty(source_text)
        
        level_mapping = {
            'beginner': DifficultyLevel.BEGINNER,
            'intermediate': DifficultyLevel.INTERMEDIATE,
            'advanced': DifficultyLevel.ADVANCED
        }
        
        suggested_level = level_mapping.get(
            difficulty_analysis['level'], 
            DifficultyLevel.BEGINNER
        )
        
        confidence = 1.0 - abs(0.5 - difficulty_analysis['score']) * 2
        
        return suggested_level, confidence
    
    @staticmethod
    def generate_sub_translations(
        db: Session,
        contribution_id: int,
        auto_approve_threshold: float = 0.9
    ) -> List[Dict[str, any]]:
        """Generate sub-word translations for a contribution"""
        contribution = db.query(Contribution).filter(
            Contribution.id == contribution_id
        ).first()
        
        if not contribution:
            return []
        
        source_words = kikuyu_tokenizer.tokenize(contribution.source_text)
        target_words = contribution.target_text.split()
        
        sub_translations = []
        
        # Simple word alignment (can be improved with ML models)
        for i, kikuyu_word in enumerate(source_words):
            # Find potential English matches
            matches = translation_memory.find_matches(kikuyu_word.text, threshold=0.8)
            
            if matches:
                best_match = matches[0]
                
                # Try to find position in target text
                target_position = NLPService._find_word_position(
                    best_match.target_text, target_words
                )
                
                sub_translation = {
                    'source_word': kikuyu_word.text,
                    'target_word': best_match.target_text,
                    'word_position': i,
                    'target_position': target_position,
                    'confidence_score': best_match.similarity_score,
                    'difficulty_level': NLPService.suggest_difficulty_level(
                        kikuyu_word.text
                    )[0],
                    'morphology': kikuyu_word.morphology,
                    'auto_approved': best_match.similarity_score >= auto_approve_threshold
                }
                
                sub_translations.append(sub_translation)
        
        return sub_translations
    
    @staticmethod
    def _find_word_position(target_word: str, target_words: List[str]) -> Optional[int]:
        """Find position of word in target text"""
        target_lower = target_word.lower()
        for i, word in enumerate(target_words):
            if word.lower() == target_lower or target_lower in word.lower():
                return i
        return None
    
    @staticmethod
    @cached(ttl=CacheConfig.TRANSLATION_SUGGESTIONS_TTL, key_prefix="nlp_validate_translation")
    def validate_translation_pair(
        source_text: str, 
        target_text: str
    ) -> Dict[str, any]:
        """Validate a translation pair for common issues"""
        validation = {
            'is_valid': True,
            'warnings': [],
            'errors': [],
            'quality_score': 1.0
        }
        
        # Check for empty texts
        if not source_text.strip() or not target_text.strip():
            validation['errors'].append("Source or target text is empty")
            validation['is_valid'] = False
            validation['quality_score'] = 0.0
            return validation
        
        # Check spelling in source text
        spelling_errors = spell_checker.check_text(source_text)
        if spelling_errors:
            validation['warnings'].append({
                'type': 'spelling',
                'message': f"Found {len(spelling_errors)} potential spelling errors",
                'count': len(spelling_errors)
            })
            validation['quality_score'] -= 0.1 * min(len(spelling_errors), 5)
        
        # Check for similar existing translations
        similar = translation_memory.find_matches(source_text, threshold=0.95)
        if similar:
            exact_matches = [m for m in similar if m.similarity_score >= 0.98]
            if exact_matches:
                validation['warnings'].append({
                    'type': 'duplicate',
                    'message': "Very similar translation already exists",
                    'existing_translations': [
                        {'source': m.source_text, 'target': m.target_text}
                        for m in exact_matches[:2]
                    ]
                })
        
        # Length and complexity checks
        analysis = NLPService.analyze_text_quality(source_text, target_text)
        
        if analysis['translation_quality']['length_ratio'] > 4:
            validation['warnings'].append({
                'type': 'length_imbalance',
                'message': "Source text is much longer than target text"
            })
            validation['quality_score'] -= 0.2
        
        if analysis['translation_quality']['length_ratio'] < 0.25:
            validation['warnings'].append({
                'type': 'length_imbalance',
                'message': "Target text is much longer than source text"
            })
            validation['quality_score'] -= 0.2
        
        # Ensure quality score is not negative
        validation['quality_score'] = max(0.0, validation['quality_score'])
        
        return validation
    
    @staticmethod
    def update_translation_memory(db: Session):
        """Update translation memory with new approved translations"""
        # Get recently approved translations not in memory
        new_contributions = db.query(Contribution).filter(
            Contribution.status == ContributionStatus.APPROVED
        ).order_by(Contribution.updated_at.desc()).limit(100).all()
        
        count = 0
        for contrib in new_contributions:
            # Check if already in memory (simple check)
            existing = translation_memory.find_matches(contrib.source_text, threshold=0.99)
            if not any(m.target_text == contrib.target_text for m in existing):
                translation_memory.add_translation(
                    contrib.source_text,
                    contrib.target_text,
                    contrib.context_notes
                )
                count += 1
        
        logger.info(f"Added {count} new translations to memory")
        return count
    
    @staticmethod
    @cached(ttl=CacheConfig.ANALYTICS_TTL, key_prefix="nlp_corpus_analysis")
    def analyze_corpus_statistics(db: Session) -> Dict[str, any]:
        """Analyze corpus-wide linguistic statistics"""
        approved_contributions = db.query(Contribution).filter(
            Contribution.status == ContributionStatus.APPROVED
        ).all()
        
        if not approved_contributions:
            return {'error': 'No approved contributions found'}
        
        # Collect statistics
        total_words = 0
        total_characters = 0
        difficulty_distribution = {'beginner': 0, 'intermediate': 0, 'advanced': 0}
        word_frequency = {}
        morphology_patterns = {}
        
        for contrib in approved_contributions:
            words = kikuyu_tokenizer.tokenize(contrib.source_text)
            total_words += len(words)
            total_characters += len(contrib.source_text)
            
            # Difficulty distribution
            if contrib.difficulty_level:
                difficulty_distribution[contrib.difficulty_level.value] += 1
            
            # Word frequency and morphology
            for word in words:
                word_frequency[word.normalized] = word_frequency.get(word.normalized, 0) + 1
                
                if word.morphology:
                    for key, value in word.morphology.items():
                        pattern_key = f"{key}:{value}"
                        morphology_patterns[pattern_key] = morphology_patterns.get(pattern_key, 0) + 1
        
        # Calculate statistics
        avg_words_per_contribution = total_words / len(approved_contributions)
        avg_chars_per_contribution = total_characters / len(approved_contributions)
        
        # Top words and patterns
        top_words = sorted(word_frequency.items(), key=lambda x: x[1], reverse=True)[:20]
        top_morphology = sorted(morphology_patterns.items(), key=lambda x: x[1], reverse=True)[:10]
        
        return {
            'corpus_size': len(approved_contributions),
            'total_words': total_words,
            'total_characters': total_characters,
            'avg_words_per_contribution': round(avg_words_per_contribution, 1),
            'avg_chars_per_contribution': round(avg_chars_per_contribution, 1),
            'difficulty_distribution': difficulty_distribution,
            'vocabulary_size': len(word_frequency),
            'top_words': [{'word': word, 'frequency': freq} for word, freq in top_words],
            'morphology_patterns': [
                {'pattern': pattern, 'frequency': freq} 
                for pattern, freq in top_morphology
            ]
        }