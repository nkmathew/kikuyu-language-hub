#!/usr/bin/env python3
"""
Easy Kikuyu Lesson Analyzer
Analyzes 538 lesson files from native speaker's Facebook page to categorize content
and prepare for structured extraction and seed generation.
"""

import os
import re
from pathlib import Path
from collections import defaultdict, Counter
from dataclasses import dataclass
from typing import List, Dict, Set, Optional, Tuple
import json

@dataclass
class FileAnalysis:
    """Analysis result for a single file"""
    file_path: str
    file_number: str
    content_type: str
    word_count: int
    has_translations: bool
    has_proverb: bool
    has_vocabulary: bool
    has_grammar: bool
    has_conjugations: bool
    has_cultural_notes: bool
    quality_score: float
    key_patterns: List[str]
    sample_content: str

class EasyKikuyuAnalyzer:
    def __init__(self):
        self.raw_data_dir = Path(__file__).parent.parent / "raw-data" / "easy-kikuyu"
        self.results = []
        self.content_patterns = {
            'proverb': [
                r'PROVERB OF THE WEEK',
                r'proverb',
                r'saying',
                r'wisdom',
                r'traditional'
            ],
            'vocabulary': [
                r'METHODS OF COOKING',
                r'Directions',
                r'wild animals',
                r'Nyamû cia',
                r'Class III nouns',
                r'\|.*\|.*\|',  # Table format
                r'[A-Z][a-z]+ - [A-Z][a-z]+',  # Word - Translation
            ],
            'grammar': [
                r'Past tenses',
                r'present tense',
                r'future tense',
                r'Class [IVX]+ nouns',
                r'noun form',
                r'plural',
                r'singular',
                r'pattern'
            ],
            'conjugations': [
                r'Moments ago',
                r'Early today',
                r'Note the pattern in verbs',
                r'1st person',
                r'2nd person',
                r'3rd person',
                r'Nd[aeo][a-z]+',  # First person patterns
                r'[A-Z][a-z]+ - Nd[a-z]+ \| [A-Z][a-z]+'  # Verb conjugation patterns
            ],
            'cultural': [
                r'cultural',
                r'traditional',
                r'custom',
                r'ceremony',
                r'ritual',
                r'Emmanuel Kariuki',
                r'Learn Kikuyu',
                r'© Emmanuel'
            ],
            'religious': [
                r'Arabic Text',
                r'Bismillah',
                r'Surah',
                r'Quran',
                r'prayer',
                r'God',
                r'Ngai'
            ]
        }
    
    def clean_content(self, content: str) -> str:
        """Clean and normalize file content"""
        # Remove markdown formatting
        content = re.sub(r'```[a-z]*\n?', '', content)
        content = re.sub(r'```', '', content)
        
        # Remove excessive whitespace
        content = re.sub(r'\n\s*\n', '\n\n', content)
        content = content.strip()
        
        # Remove NO CONTENT FOUND markers
        content = re.sub(r'\[NO CONTENT FOUND\].*?`', '', content, flags=re.DOTALL)
        
        return content
    
    def extract_file_number(self, file_path: str) -> str:
        """Extract file number from filename"""
        filename = Path(file_path).stem
        # Handle various numbering formats: 01, 02, 5, 006, etc.
        match = re.search(r'(\d+)', filename)
        return match.group(1) if match else '0'
    
    def detect_content_type(self, content: str) -> Tuple[str, List[str]]:
        """Detect primary content type and matching patterns"""
        content_lower = content.lower()
        pattern_matches = defaultdict(list)
        
        for content_type, patterns in self.content_patterns.items():
            for pattern in patterns:
                if re.search(pattern, content, re.IGNORECASE):
                    pattern_matches[content_type].append(pattern)
        
        # Determine primary content type
        if pattern_matches['proverb']:
            return 'proverb', pattern_matches['proverb']
        elif pattern_matches['conjugations']:
            return 'conjugations', pattern_matches['conjugations']
        elif pattern_matches['grammar']:
            return 'grammar', pattern_matches['grammar']
        elif pattern_matches['vocabulary']:
            return 'vocabulary', pattern_matches['vocabulary']
        elif pattern_matches['religious']:
            return 'religious', pattern_matches['religious']
        elif pattern_matches['cultural']:
            return 'cultural', pattern_matches['cultural']
        else:
            return 'mixed', []
    
    def calculate_quality_score(self, content: str, analysis: Dict) -> float:
        """Calculate content quality score based on various factors"""
        score = 0.0
        
        # Base score for having content
        if len(content.strip()) > 10:
            score += 2.0
        
        # Word count factor
        word_count = len(content.split())
        if word_count > 50:
            score += 2.0
        elif word_count > 20:
            score += 1.5
        elif word_count > 10:
            score += 1.0
        
        # Translation pairs
        if analysis['has_translations']:
            score += 1.5
        
        # Structured content
        if '|' in content or re.search(r'[A-Z][a-z]+ - [A-Z][a-z]+', content):
            score += 1.0
        
        # Educational value
        if analysis['has_grammar'] or analysis['has_conjugations']:
            score += 1.0
        
        # Cultural authenticity
        if analysis['has_cultural_notes']:
            score += 0.5
        
        # Proverb bonus
        if analysis['has_proverb']:
            score += 1.0
        
        return min(score, 5.0)  # Cap at 5.0
    
    def analyze_content_features(self, content: str) -> Dict:
        """Analyze specific content features"""
        return {
            'has_translations': bool(re.search(r'[A-Z][a-z]+ - [A-Z][a-z]+', content)) or '|' in content,
            'has_proverb': bool(re.search(r'proverb|saying|wisdom', content, re.IGNORECASE)),
            'has_vocabulary': bool(re.search(r'[A-Z][a-z]+ - [A-Z][a-z]+|Class.*nouns', content)),
            'has_grammar': bool(re.search(r'tense|plural|singular|pattern|Class [IVX]+', content, re.IGNORECASE)),
            'has_conjugations': bool(re.search(r'1st person|2nd person|3rd person|Nd[aeo]', content)),
            'has_cultural_notes': bool(re.search(r'Emmanuel|traditional|cultural|Learn Kikuyu', content, re.IGNORECASE))
        }
    
    def analyze_file(self, file_path: Path) -> Optional[FileAnalysis]:
        """Analyze a single file"""
        try:
            # Try different encodings
            content = None
            for encoding in ['utf-8', 'latin-1', 'cp1252']:
                try:
                    with open(file_path, 'r', encoding=encoding) as f:
                        content = f.read()
                    break
                except UnicodeDecodeError:
                    continue
            
            if content is None:
                return None
            
            # Clean content
            cleaned_content = self.clean_content(content)
            
            # Skip if essentially empty
            if len(cleaned_content.strip()) < 5:
                return None
            
            # Extract file number
            file_number = self.extract_file_number(str(file_path))
            
            # Detect content type
            content_type, key_patterns = self.detect_content_type(cleaned_content)
            
            # Analyze features
            features = self.analyze_content_features(cleaned_content)
            
            # Calculate quality score
            quality_score = self.calculate_quality_score(cleaned_content, features)
            
            # Create sample content (first 200 chars)
            sample_content = cleaned_content[:200] + "..." if len(cleaned_content) > 200 else cleaned_content
            
            return FileAnalysis(
                file_path=str(file_path),
                file_number=file_number,
                content_type=content_type,
                word_count=len(cleaned_content.split()),
                quality_score=quality_score,
                key_patterns=key_patterns,
                sample_content=sample_content,
                **features
            )
            
        except Exception as e:
            print(f"Error analyzing {file_path}: {e}")
            return None
    
    def analyze_all_files(self) -> List[FileAnalysis]:
        """Analyze all files in the easy-kikuyu directory"""
        print(f"Analyzing files in: {self.raw_data_dir}")
        
        txt_files = list(self.raw_data_dir.glob("*.txt"))
        print(f"Found {len(txt_files)} text files")
        
        results = []
        for i, file_path in enumerate(txt_files, 1):
            if i % 50 == 0:
                print(f"Processed {i}/{len(txt_files)} files...")
            
            analysis = self.analyze_file(file_path)
            if analysis:
                results.append(analysis)
        
        self.results = results
        return results
    
    def generate_summary_report(self) -> Dict:
        """Generate comprehensive summary report"""
        if not self.results:
            return {}
        
        # Content type distribution
        content_types = Counter(r.content_type for r in self.results)
        
        # Quality distribution
        quality_ranges = {
            'high_quality': len([r for r in self.results if r.quality_score >= 4.0]),
            'good_quality': len([r for r in self.results if 3.0 <= r.quality_score < 4.0]),
            'medium_quality': len([r for r in self.results if 2.0 <= r.quality_score < 3.0]),
            'low_quality': len([r for r in self.results if r.quality_score < 2.0])
        }
        
        # Feature analysis
        feature_counts = {
            'has_translations': len([r for r in self.results if r.has_translations]),
            'has_proverbs': len([r for r in self.results if r.has_proverb]),
            'has_vocabulary': len([r for r in self.results if r.has_vocabulary]),
            'has_grammar': len([r for r in self.results if r.has_grammar]),
            'has_conjugations': len([r for r in self.results if r.has_conjugations]),
            'has_cultural_notes': len([r for r in self.results if r.has_cultural_notes])
        }
        
        # Word count statistics
        word_counts = [r.word_count for r in self.results]
        word_stats = {
            'total_words': sum(word_counts),
            'avg_words_per_file': sum(word_counts) / len(word_counts) if word_counts else 0,
            'min_words': min(word_counts) if word_counts else 0,
            'max_words': max(word_counts) if word_counts else 0
        }
        
        return {
            'total_files_analyzed': len(self.results),
            'content_type_distribution': dict(content_types),
            'quality_distribution': quality_ranges,
            'feature_analysis': feature_counts,
            'word_statistics': word_stats,
            'estimated_contributions': self.estimate_contributions()
        }
    
    def estimate_contributions(self) -> Dict:
        """Estimate potential database contributions"""
        estimates = defaultdict(int)
        
        for result in self.results:
            # Estimate based on content type and quality
            if result.content_type == 'proverb' and result.quality_score >= 3.0:
                estimates['proverbs'] += 1
            elif result.content_type == 'vocabulary' and result.quality_score >= 2.5:
                # Estimate vocabulary items based on word count and structure
                if '|' in result.sample_content:  # Table format
                    estimates['vocabulary_items'] += min(10, result.word_count // 5)
                else:
                    estimates['vocabulary_items'] += min(5, result.word_count // 10)
            elif result.content_type == 'conjugations' and result.quality_score >= 3.0:
                estimates['conjugation_examples'] += min(8, result.word_count // 15)
            elif result.content_type == 'grammar' and result.quality_score >= 2.5:
                estimates['grammar_examples'] += min(5, result.word_count // 20)
            elif result.content_type == 'cultural' and result.quality_score >= 2.0:
                estimates['cultural_notes'] += 1
            elif result.content_type == 'religious' and result.quality_score >= 3.0:
                estimates['religious_content'] += min(3, result.word_count // 20)
        
        estimates['total_estimated'] = sum(estimates.values())
        return dict(estimates)
    
    def save_analysis_results(self, output_file: str = "easy_kikuyu_analysis.json"):
        """Save analysis results to JSON file"""
        # Convert dataclass objects to dictionaries
        results_dict = []
        for result in self.results:
            result_dict = {
                'file_path': result.file_path,
                'file_number': result.file_number,
                'content_type': result.content_type,
                'word_count': result.word_count,
                'has_translations': result.has_translations,
                'has_proverb': result.has_proverb,
                'has_vocabulary': result.has_vocabulary,
                'has_grammar': result.has_grammar,
                'has_conjugations': result.has_conjugations,
                'has_cultural_notes': result.has_cultural_notes,
                'quality_score': result.quality_score,
                'key_patterns': result.key_patterns,
                'sample_content': result.sample_content
            }
            results_dict.append(result_dict)
        
        analysis_data = {
            'analysis_results': results_dict,
            'summary_report': self.generate_summary_report(),
            'analysis_timestamp': str(Path(__file__).stat().st_mtime)
        }
        
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(analysis_data, f, indent=2, ensure_ascii=False)
        
        print(f"Analysis results saved to: {output_file}")
    
    def print_summary_report(self):
        """Print a formatted summary report"""
        summary = self.generate_summary_report()
        
        print("\n" + "=" * 80)
        print("EASY KIKUYU LESSON ANALYSIS SUMMARY")
        print("=" * 80)
        
        print(f"\nFiles Processed: {summary['total_files_analyzed']}")
        
        print(f"\nContent Type Distribution:")
        for content_type, count in summary['content_type_distribution'].items():
            print(f"  {content_type.capitalize()}: {count}")
        
        print(f"\nQuality Distribution:")
        for quality, count in summary['quality_distribution'].items():
            print(f"  {quality.replace('_', ' ').title()}: {count}")
        
        print(f"\nFeature Analysis:")
        for feature, count in summary['feature_analysis'].items():
            print(f"  {feature.replace('_', ' ').title()}: {count}")
        
        print(f"\nWord Statistics:")
        stats = summary['word_statistics']
        print(f"  Total Words: {stats['total_words']:,}")
        print(f"  Average Words per File: {stats['avg_words_per_file']:.1f}")
        print(f"  Range: {stats['min_words']} - {stats['max_words']} words")
        
        print(f"\nEstimated Database Contributions:")
        for contribution_type, count in summary['estimated_contributions'].items():
            print(f"  {contribution_type.replace('_', ' ').title()}: {count}")

def main():
    """Main analysis function"""
    print("Starting Easy Kikuyu Lesson Analysis...")
    
    analyzer = EasyKikuyuAnalyzer()
    
    # Analyze all files
    results = analyzer.analyze_all_files()
    
    if not results:
        print("No files were successfully analyzed!")
        return
    
    # Print summary report
    analyzer.print_summary_report()
    
    # Save detailed results
    analyzer.save_analysis_results()
    
    print(f"\nAnalysis complete! Processed {len(results)} files successfully.")
    print("Use the generated JSON file for extraction and seed generation.")

if __name__ == "__main__":
    main()