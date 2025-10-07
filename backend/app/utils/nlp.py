"""
Advanced NLP utilities for Kikuyu language processing
"""
import re
import unicodedata
from typing import List, Dict, Optional, Tuple, Set
from dataclasses import dataclass
from difflib import SequenceMatcher
import math
from collections import Counter, defaultdict


@dataclass
class KikuyuWord:
    """Represents a Kikuyu word with linguistic metadata"""
    text: str
    normalized: str
    tokens: List[str]
    syllables: List[str]
    tone_pattern: Optional[str] = None
    morphology: Optional[Dict[str, str]] = None
    

@dataclass
class TranslationMatch:
    """Represents a translation memory match"""
    source_text: str
    target_text: str
    similarity_score: float
    match_type: str  # exact, fuzzy, partial
    context: Optional[str] = None


class KikuyuTokenizer:
    """
    Advanced tokenizer for Kikuyu text with support for:
    - Word boundary detection
    - Syllable segmentation
    - Tone mark handling
    - Morphological analysis
    """
    
    # Kikuyu vowels with tone marks
    VOWELS = set('aeiouâêîôûáéíóúàèìòù')
    CONSONANTS = set('bcdfghjklmnpqrstvwxyz')
    
    # Common Kikuyu prefixes and suffixes
    PREFIXES = {
        'wa-', 'we-', 'wi-', 'wo-', 'wu-',  # Person markers
        'ka-', 'ke-', 'ki-', 'ko-', 'ku-',  # Diminutive/gender
        'ma-', 'me-', 'mi-', 'mo-', 'mu-',  # Plural/class markers
        'tha-', 'the-', 'thi-', 'tho-', 'thu-',  # Past tense
        'nga-', 'nge-', 'ngi-', 'ngo-', 'ngu-',  # Conditional
    }
    
    SUFFIXES = {
        '-ire', '-ete', '-aga', '-ete', '-ini',  # Tense markers
        '-ia', '-ea', '-uo', '-io', '-ua',       # Mood markers
        '-ni', '-ta', '-ka', '-ga',              # Particles
    }
    
    def __init__(self):
        self.word_pattern = re.compile(r'\b\w+\b')
        self.syllable_pattern = re.compile(r'[aeiouâêîôûáéíóúàèìòù]+[bcdfghjklmnpqrstvwxyz]*')
    
    def tokenize(self, text: str) -> List[KikuyuWord]:
        """Tokenize Kikuyu text into KikuyuWord objects"""
        words = []
        
        # Basic word extraction
        word_matches = self.word_pattern.findall(text.lower())
        
        for word_text in word_matches:
            normalized = self._normalize_word(word_text)
            tokens = self._sub_tokenize(normalized)
            syllables = self._syllabify(normalized)
            tone_pattern = self._extract_tone_pattern(word_text)
            morphology = self._analyze_morphology(normalized)
            
            words.append(KikuyuWord(
                text=word_text,
                normalized=normalized,
                tokens=tokens,
                syllables=syllables,
                tone_pattern=tone_pattern,
                morphology=morphology
            ))
        
        return words
    
    def _normalize_word(self, word: str) -> str:
        """Normalize Kikuyu word by removing tone marks for analysis"""
        # Remove tone marks but preserve base vowels
        word = unicodedata.normalize('NFD', word)
        word = ''.join(c for c in word if not unicodedata.combining(c))
        return word.lower().strip()
    
    def _sub_tokenize(self, word: str) -> List[str]:
        """Break word into meaningful sub-tokens (morphemes)"""
        tokens = []
        remaining = word
        
        # Check for prefixes
        for prefix in sorted(self.PREFIXES, key=len, reverse=True):
            if remaining.startswith(prefix):
                tokens.append(prefix)
                remaining = remaining[len(prefix):]
                break
        
        # Check for suffixes
        for suffix in sorted(self.SUFFIXES, key=len, reverse=True):
            if remaining.endswith(suffix):
                tokens.append(remaining[:-len(suffix)])
                tokens.append(suffix)
                remaining = ""
                break
        
        # Add remaining as root if not empty
        if remaining:
            tokens.append(remaining)
        
        return tokens if tokens else [word]
    
    def _syllabify(self, word: str) -> List[str]:
        """Break word into syllables using Kikuyu syllable patterns"""
        syllables = []
        remaining = word
        
        while remaining:
            match = self.syllable_pattern.search(remaining)
            if match:
                syllable = match.group()
                syllables.append(syllable)
                remaining = remaining[match.end():]
            else:
                # Handle consonant clusters
                if remaining:
                    syllables.append(remaining)
                break
        
        return syllables if syllables else [word]
    
    def _extract_tone_pattern(self, word: str) -> str:
        """Extract tone pattern from word with tone marks"""
        tone_pattern = ""
        for char in word:
            if char in 'áéíóú':
                tone_pattern += 'H'  # High tone
            elif char in 'àèìòù':
                tone_pattern += 'L'  # Low tone
            elif char in 'âêîôû':
                tone_pattern += 'F'  # Falling tone
            elif char in self.VOWELS:
                tone_pattern += 'M'  # Mid/neutral tone
        
        return tone_pattern if tone_pattern else None
    
    def _analyze_morphology(self, word: str) -> Dict[str, str]:
        """Basic morphological analysis"""
        analysis = {}
        
        # Detect word type based on patterns
        if any(word.startswith(p) for p in ['wa-', 'we-', 'wi-', 'wo-', 'wu-']):
            analysis['type'] = 'personal'
        elif any(word.startswith(p) for p in ['ka-', 'ke-', 'ki-', 'ko-', 'ku-']):
            analysis['type'] = 'diminutive'
        elif any(word.startswith(p) for p in ['ma-', 'me-', 'mi-', 'mo-', 'mu-']):
            analysis['type'] = 'plural'
        elif any(word.endswith(s) for s in ['-ire', '-ete']):
            analysis['tense'] = 'past'
        elif any(word.endswith(s) for s in ['-aga', '-ia']):
            analysis['aspect'] = 'habitual'
        
        return analysis


class TranslationMemory:
    """
    Translation memory system for storing and retrieving similar translations
    """
    
    def __init__(self):
        self.tokenizer = KikuyuTokenizer()
        self.memory: List[TranslationMatch] = []
        self.source_index: Dict[str, List[int]] = defaultdict(list)
        self.target_index: Dict[str, List[int]] = defaultdict(list)
    
    def add_translation(self, source: str, target: str, context: str = None):
        """Add a translation pair to memory"""
        match = TranslationMatch(
            source_text=source,
            target_text=target,
            similarity_score=1.0,
            match_type='exact',
            context=context
        )
        
        idx = len(self.memory)
        self.memory.append(match)
        
        # Index source and target words
        source_words = self._extract_keywords(source)
        target_words = self._extract_keywords(target)
        
        for word in source_words:
            self.source_index[word].append(idx)
        
        for word in target_words:
            self.target_index[word].append(idx)
    
    def find_matches(self, query: str, threshold: float = 0.7) -> List[TranslationMatch]:
        """Find translation matches for a query"""
        matches = []
        query_words = set(self._extract_keywords(query))
        
        # Find candidate translations
        candidates = set()
        for word in query_words:
            candidates.update(self.source_index.get(word, []))
        
        # Calculate similarity scores
        for idx in candidates:
            stored_match = self.memory[idx]
            similarity = self._calculate_similarity(query, stored_match.source_text)
            
            if similarity >= threshold:
                match = TranslationMatch(
                    source_text=stored_match.source_text,
                    target_text=stored_match.target_text,
                    similarity_score=similarity,
                    match_type=self._classify_match(similarity),
                    context=stored_match.context
                )
                matches.append(match)
        
        # Sort by similarity score
        matches.sort(key=lambda x: x.similarity_score, reverse=True)
        return matches[:10]  # Return top 10 matches
    
    def _extract_keywords(self, text: str) -> List[str]:
        """Extract keywords from text for indexing"""
        words = self.tokenizer.tokenize(text)
        keywords = []
        
        for word in words:
            # Add original word
            keywords.append(word.normalized)
            
            # Add morphological roots
            if word.tokens:
                keywords.extend(word.tokens)
        
        return list(set(keywords))  # Remove duplicates
    
    def _calculate_similarity(self, text1: str, text2: str) -> float:
        """Calculate similarity between two texts"""
        # Exact match
        if text1.lower() == text2.lower():
            return 1.0
        
        # Token-based similarity
        words1 = set(self._extract_keywords(text1))
        words2 = set(self._extract_keywords(text2))
        
        if not words1 or not words2:
            return 0.0
        
        intersection = words1 & words2
        union = words1 | words2
        
        jaccard_similarity = len(intersection) / len(union)
        
        # Sequence similarity
        sequence_similarity = SequenceMatcher(None, text1.lower(), text2.lower()).ratio()
        
        # Combined score
        return (jaccard_similarity * 0.6) + (sequence_similarity * 0.4)
    
    def _classify_match(self, similarity: float) -> str:
        """Classify match type based on similarity score"""
        if similarity >= 0.95:
            return 'exact'
        elif similarity >= 0.8:
            return 'fuzzy'
        else:
            return 'partial'


class KikuyuSpellChecker:
    """
    Spell checker for Kikuyu text with suggestions
    """
    
    def __init__(self):
        self.tokenizer = KikuyuTokenizer()
        self.dictionary: Set[str] = set()
        self.word_frequency: Counter = Counter()
        self.common_errors: Dict[str, str] = {}
    
    def add_to_dictionary(self, words: List[str]):
        """Add words to the dictionary"""
        for word in words:
            normalized = self.tokenizer._normalize_word(word)
            self.dictionary.add(normalized)
            self.word_frequency[normalized] += 1
    
    def add_common_error(self, error: str, correction: str):
        """Add common error-correction pair"""
        self.common_errors[error.lower()] = correction.lower()
    
    def check_text(self, text: str) -> List[Dict[str, any]]:
        """Check text for spelling errors"""
        words = self.tokenizer.tokenize(text)
        errors = []
        
        for i, word in enumerate(words):
            if not self.is_correct(word.normalized):
                suggestions = self.get_suggestions(word.normalized)
                errors.append({
                    'word': word.text,
                    'position': i,
                    'suggestions': suggestions,
                    'confidence': max([s['score'] for s in suggestions]) if suggestions else 0.0
                })
        
        return errors
    
    def is_correct(self, word: str) -> bool:
        """Check if word is correctly spelled"""
        normalized = self.tokenizer._normalize_word(word)
        return normalized in self.dictionary
    
    def get_suggestions(self, word: str, max_suggestions: int = 5) -> List[Dict[str, any]]:
        """Get spelling suggestions for a word"""
        suggestions = []
        
        # Check common errors first
        if word in self.common_errors:
            suggestions.append({
                'word': self.common_errors[word],
                'score': 0.95,
                'reason': 'common_error'
            })
        
        # Find similar words in dictionary
        for dict_word in self.dictionary:
            if abs(len(word) - len(dict_word)) <= 2:  # Similar length
                similarity = SequenceMatcher(None, word, dict_word).ratio()
                if similarity >= 0.7:
                    frequency_score = math.log(self.word_frequency[dict_word] + 1) / 10
                    combined_score = (similarity * 0.8) + (frequency_score * 0.2)
                    
                    suggestions.append({
                        'word': dict_word,
                        'score': combined_score,
                        'reason': 'similarity'
                    })
        
        # Sort by score and return top suggestions
        suggestions.sort(key=lambda x: x['score'], reverse=True)
        return suggestions[:max_suggestions]


class TextDifficulty:
    """
    Analyzes text difficulty for language learners
    """
    
    def __init__(self):
        self.tokenizer = KikuyuTokenizer()
        self.word_frequency: Counter = Counter()
        self.complexity_factors = {
            'avg_word_length': 0.2,
            'syllable_complexity': 0.3,
            'morphological_complexity': 0.2,
            'vocabulary_rarity': 0.3
        }
    
    def train_frequency_model(self, texts: List[str]):
        """Train word frequency model from corpus"""
        for text in texts:
            words = self.tokenizer.tokenize(text)
            for word in words:
                self.word_frequency[word.normalized] += 1
    
    def analyze_difficulty(self, text: str) -> Dict[str, any]:
        """Analyze text difficulty and return metrics"""
        words = self.tokenizer.tokenize(text)
        
        if not words:
            return {'level': 'beginner', 'score': 0.0, 'factors': {}}
        
        # Calculate complexity factors
        avg_word_length = sum(len(w.text) for w in words) / len(words)
        
        syllable_count = sum(len(w.syllables) for w in words)
        avg_syllables = syllable_count / len(words)
        
        morphological_complexity = sum(
            len(w.tokens) for w in words if w.tokens
        ) / len(words)
        
        rare_words = sum(
            1 for w in words 
            if self.word_frequency.get(w.normalized, 0) < 10
        )
        vocabulary_rarity = rare_words / len(words)
        
        # Calculate overall difficulty score
        factors = {
            'avg_word_length': min(avg_word_length / 10, 1.0),
            'syllable_complexity': min(avg_syllables / 5, 1.0),
            'morphological_complexity': min(morphological_complexity / 3, 1.0),
            'vocabulary_rarity': vocabulary_rarity
        }
        
        difficulty_score = sum(
            factors[factor] * weight 
            for factor, weight in self.complexity_factors.items()
        )
        
        # Classify difficulty level
        if difficulty_score < 0.3:
            level = 'beginner'
        elif difficulty_score < 0.6:
            level = 'intermediate'
        else:
            level = 'advanced'
        
        return {
            'level': level,
            'score': difficulty_score,
            'factors': factors,
            'metrics': {
                'word_count': len(words),
                'avg_word_length': avg_word_length,
                'avg_syllables': avg_syllables,
                'morphological_complexity': morphological_complexity,
                'vocabulary_rarity': vocabulary_rarity
            }
        }


# Global instances for easy import
kikuyu_tokenizer = KikuyuTokenizer()
translation_memory = TranslationMemory()
spell_checker = KikuyuSpellChecker()
difficulty_analyzer = TextDifficulty()