#!/usr/bin/env python3
"""
Easy Kikuyu Content Extractor
Extracts structured linguistic data from categorized lesson files
using pattern recognition and native speaker content analysis.
"""

import os
import re
import json
from pathlib import Path
from typing import List, Dict, Tuple, Optional, Set
from dataclasses import dataclass
from collections import defaultdict

@dataclass
class ExtractedContent:
    """Structured extracted content"""
    english: str
    kikuyu: str
    context: str
    content_type: str
    difficulty: str = "INTERMEDIATE"
    cultural_notes: str = ""
    quality_score: float = 4.0

class EasyKikuyuExtractor:
    def __init__(self, analysis_file: str = "easy_kikuyu_analysis.json"):
        self.analysis_file = analysis_file
        self.analysis_data = self.load_analysis_data()
        self.extracted_content = defaultdict(list)
        
        # Difficulty mapping based on content complexity
        self.difficulty_mapping = {
            'proverb': 'ADVANCED',
            'vocabulary': 'BEGINNER',
            'conjugations': 'INTERMEDIATE', 
            'grammar': 'INTERMEDIATE',
            'cultural': 'ADVANCED',
            'religious': 'ADVANCED',
            'mixed': 'INTERMEDIATE'
        }
    
    def load_analysis_data(self) -> Dict:
        """Load analysis results"""
        try:
            with open(self.analysis_file, 'r', encoding='utf-8') as f:
                return json.load(f)
        except FileNotFoundError:
            print(f"Analysis file {self.analysis_file} not found. Run analyzer first.")
            return {}
    
    def clean_text(self, text: str) -> str:
        """Clean and normalize text"""
        # Remove markdown formatting
        text = re.sub(r'```[a-z]*\n?', '', text)
        text = re.sub(r'```', '', text)
        
        # Remove excessive whitespace
        text = re.sub(r'\n\s*\n', '\n', text)
        text = re.sub(r'\s+', ' ', text)
        
        # Remove copyright and footer text
        text = re.sub(r'Learn Kikuyu.*?Emmanuel Kariuki', '', text, flags=re.DOTALL)
        text = re.sub(r'© Emmanuel.*', '', text)
        text = re.sub(r'https://www\.facebook\.com/EasyKikuyu', '', text)
        text = re.sub(r'Rũngai kana mwongerere haha.*', '', text, flags=re.DOTALL)
        
        return text.strip()
    
    def extract_proverbs(self, content: str, file_info: Dict) -> List[ExtractedContent]:
        """Extract proverbs and their explanations"""
        extracted = []
        content = self.clean_text(content)
        
        # Pattern for proverb with translation
        proverb_pattern = r'([A-ZŨĀĒĪŌŪ][^–\n]*)[\s–]+([a-z][^(]+?)(?:\s*\(|$)'
        matches = re.findall(proverb_pattern, content, re.MULTILINE)
        
        for kikuyu_text, english_text in matches:
            kikuyu_text = kikuyu_text.strip()
            english_text = english_text.strip()
            
            # Skip if too short or not meaningful
            if len(kikuyu_text) < 10 or len(english_text) < 5:
                continue
            
            # Skip if it looks like a header
            if kikuyu_text.upper() == kikuyu_text or 'PROVERB' in kikuyu_text.upper():
                continue
            
            # Extract cultural context
            cultural_notes = f"Traditional Kikuyu proverb from Easy Kikuyu lessons by Emmanuel Kariuki. "
            
            # Look for explanations in parentheses or following lines
            explanation_pattern = rf'{re.escape(english_text)}.*?\((.*?)\)'
            explanation_match = re.search(explanation_pattern, content, re.DOTALL)
            if explanation_match:
                cultural_notes += explanation_match.group(1).strip()
            
            extracted.append(ExtractedContent(
                english=english_text,
                kikuyu=kikuyu_text,
                context=f"Traditional proverb with cultural wisdom - File {file_info.get('file_number', 'unknown')}",
                content_type="proverb",
                difficulty="ADVANCED",
                cultural_notes=cultural_notes,
                quality_score=4.8
            ))
        
        # Also extract standalone proverbs (kikuyu only)
        standalone_pattern = r'^([A-ZŨĀĒĪŌŪ][^.!?\n]*[.!?]?)$'
        lines = content.split('\n')
        for line in lines:
            line = line.strip()
            if re.match(standalone_pattern, line) and len(line) > 15:
                # Skip if already extracted
                if any(ext.kikuyu == line for ext in extracted):
                    continue
                
                extracted.append(ExtractedContent(
                    english=f"Traditional Kikuyu saying",
                    kikuyu=line,
                    context=f"Traditional proverb - File {file_info.get('file_number', 'unknown')}",
                    content_type="proverb",
                    difficulty="ADVANCED",
                    cultural_notes="Traditional Kikuyu wisdom from Easy Kikuyu lessons",
                    quality_score=4.5
                ))
        
        return extracted
    
    def extract_vocabulary(self, content: str, file_info: Dict) -> List[ExtractedContent]:
        """Extract vocabulary items with translations"""
        extracted = []
        content = self.clean_text(content)
        
        # Pattern 1: Table format with pipes
        if '|' in content:
            lines = content.split('\n')
            for line in lines:
                if '|' in line and not line.startswith('|:'):  # Skip table headers
                    parts = [p.strip() for p in line.split('|') if p.strip()]
                    
                    # Process pairs of Kikuyu-English
                    for i in range(0, len(parts) - 1, 2):
                        if i + 1 < len(parts):
                            kikuyu = parts[i].strip('*')
                            english = parts[i + 1].strip('*')
                            
                            if len(kikuyu) > 2 and len(english) > 2:
                                extracted.append(ExtractedContent(
                                    english=english,
                                    kikuyu=kikuyu,
                                    context=f"Vocabulary from structured lesson - File {file_info.get('file_number', 'unknown')}",
                                    content_type="vocabulary",
                                    difficulty="BEGINNER",
                                    cultural_notes="Native speaker vocabulary from Easy Kikuyu lessons",
                                    quality_score=4.6
                                ))
        
        # Pattern 2: Direct translation format (Word - Translation)
        translation_pattern = r'([A-ZŨĀĒĪŌŪ][a-zũāēīōū\s\']+?)\s*[–-]\s*([a-zA-Z][a-zA-Z\s,/()]+?)(?:\n|$|\s*\()'
        matches = re.findall(translation_pattern, content, re.MULTILINE)
        
        for kikuyu, english in matches:
            kikuyu = kikuyu.strip()
            english = english.strip()
            
            # Clean up common suffixes
            english = re.sub(r'\s*\([^)]*\)$', '', english)
            
            if len(kikuyu) > 2 and len(english) > 2 and kikuyu != english:
                extracted.append(ExtractedContent(
                    english=english,
                    kikuyu=kikuyu,
                    context=f"Direct translation vocabulary - File {file_info.get('file_number', 'unknown')}",
                    content_type="vocabulary", 
                    difficulty="BEGINNER",
                    cultural_notes="Native speaker translation from Easy Kikuyu lessons",
                    quality_score=4.5
                ))
        
        # Pattern 3: Section headers with vocabulary
        if 'METHODS OF COOKING' in content or 'Directions' in content or 'wild animals' in content:
            # Extract thematic vocabulary
            lines = content.split('\n')
            topic = "general vocabulary"
            
            if 'COOKING' in content:
                topic = "cooking methods"
            elif 'Directions' in content:
                topic = "directions and geography"
            elif 'animals' in content:
                topic = "wild animals"
            
            for line in lines:
                if re.search(r'[A-ZŨĀĒĪŌŪ][a-zũāēīōū]+\s*[–-]\s*[a-z]', line):
                    parts = re.split(r'\s*[–-]\s*', line, 1)
                    if len(parts) == 2:
                        kikuyu, english = parts
                        kikuyu = kikuyu.strip()
                        english = english.strip()
                        
                        if len(kikuyu) > 2 and len(english) > 2:
                            extracted.append(ExtractedContent(
                                english=english,
                                kikuyu=kikuyu,
                                context=f"Thematic vocabulary: {topic} - File {file_info.get('file_number', 'unknown')}",
                                content_type="vocabulary",
                                difficulty="BEGINNER",
                                cultural_notes=f"Native speaker {topic} vocabulary from Easy Kikuyu lessons",
                                quality_score=4.7
                            ))
        
        return extracted
    
    def extract_conjugations(self, content: str, file_info: Dict) -> List[ExtractedContent]:
        """Extract verb conjugations and examples"""
        extracted = []
        content = self.clean_text(content)
        
        # Pattern 1: Sentence examples with clear structure
        sentence_pattern = r'(Nd[a-z]+[^–\n]*?)\s*[–-]\s*([A-Z][^(]+?)(?:\s*\(|$|\n)'
        matches = re.findall(sentence_pattern, content, re.MULTILINE)
        
        for kikuyu, english in matches:
            kikuyu = kikuyu.strip()
            english = english.strip()
            
            if len(kikuyu) > 5 and len(english) > 5:
                # Determine tense context
                tense_context = "conjugation example"
                if "moments ago" in content.lower():
                    tense_context = "recent past tense"
                elif "early today" in content.lower():
                    tense_context = "earlier today"
                elif "1st person" in content:
                    tense_context = "first person conjugation"
                
                extracted.append(ExtractedContent(
                    english=english,
                    kikuyu=kikuyu,
                    context=f"Verb conjugation: {tense_context} - File {file_info.get('file_number', 'unknown')}",
                    content_type="conjugation",
                    difficulty="INTERMEDIATE",
                    cultural_notes="Native speaker verb conjugation examples from Easy Kikuyu lessons",
                    quality_score=4.6
                ))
        
        # Pattern 2: Verb pattern examples (Verb - Pattern1 | Pattern2)
        pattern_examples = r'([A-Z][a-z]+)\s*-\s*([A-Z][a-z]+)\s*\|\s*([A-Z][a-z]+)'
        pattern_matches = re.findall(pattern_examples, content)
        
        for base_verb, pattern1, pattern2 in pattern_matches:
            # Create two conjugation entries
            extracted.append(ExtractedContent(
                english=f"conjugated form of {base_verb.lower()}",
                kikuyu=pattern1,
                context=f"Verb conjugation pattern - File {file_info.get('file_number', 'unknown')}",
                content_type="conjugation",
                difficulty="INTERMEDIATE",
                cultural_notes=f"Morphological pattern for verb '{base_verb}' from Easy Kikuyu lessons",
                quality_score=4.4
            ))
            
            extracted.append(ExtractedContent(
                english=f"past form of {base_verb.lower()}",
                kikuyu=pattern2,
                context=f"Verb conjugation pattern - File {file_info.get('file_number', 'unknown')}",
                content_type="conjugation",
                difficulty="INTERMEDIATE", 
                cultural_notes=f"Morphological pattern for verb '{base_verb}' from Easy Kikuyu lessons",
                quality_score=4.4
            ))
        
        return extracted
    
    def extract_grammar(self, content: str, file_info: Dict) -> List[ExtractedContent]:
        """Extract grammar rules and examples"""
        extracted = []
        content = self.clean_text(content)
        
        # Extract noun class information
        if 'Class' in content and 'nouns' in content:
            class_pattern = r'Class ([IVX]+) nouns[^.]*?([^.]+\.)'
            class_matches = re.findall(class_pattern, content, re.DOTALL)
            
            for class_num, description in class_matches:
                extracted.append(ExtractedContent(
                    english=f"Kikuyu Class {class_num} nouns",
                    kikuyu=f"Kirĩmu gĩa {class_num}",
                    context=f"Noun class grammar rule - File {file_info.get('file_number', 'unknown')}",
                    content_type="grammar",
                    difficulty="INTERMEDIATE",
                    cultural_notes=f"Grammatical explanation: {description.strip()}",
                    quality_score=4.5
                ))
        
        # Extract grammar examples
        example_pattern = r'Example:\s*([^–\n]+?)\s*[–-]\s*([^(\n]+?)(?:\s*\(|$|\n)'
        example_matches = re.findall(example_pattern, content, re.MULTILINE)
        
        for kikuyu, english in example_matches:
            kikuyu = kikuyu.strip()
            english = english.strip()
            
            if len(kikuyu) > 3 and len(english) > 3:
                extracted.append(ExtractedContent(
                    english=english,
                    kikuyu=kikuyu,
                    context=f"Grammar example - File {file_info.get('file_number', 'unknown')}",
                    content_type="grammar",
                    difficulty="INTERMEDIATE",
                    cultural_notes="Grammatical example from Easy Kikuyu lessons",
                    quality_score=4.3
                ))
        
        return extracted
    
    def extract_cultural_content(self, content: str, file_info: Dict) -> List[ExtractedContent]:
        """Extract cultural notes and context"""
        extracted = []
        content = self.clean_text(content)
        
        # Extract cultural explanations in parentheses
        cultural_pattern = r'\(([^)]{20,})\)'
        cultural_matches = re.findall(cultural_pattern, content)
        
        for explanation in cultural_matches:
            if len(explanation) > 20:
                extracted.append(ExtractedContent(
                    english="cultural context note",
                    kikuyu=f"Mũrĩre wa gĩkũyũ",
                    context=f"Cultural explanation - File {file_info.get('file_number', 'unknown')}",
                    content_type="cultural",
                    difficulty="ADVANCED",
                    cultural_notes=explanation.strip(),
                    quality_score=4.2
                ))
        
        return extracted
    
    def extract_from_file(self, file_info: Dict) -> List[ExtractedContent]:
        """Extract content from a single file based on its type"""
        file_path = file_info.get('file_path', '')
        content_type = file_info.get('content_type', 'mixed')
        
        if not os.path.exists(file_path):
            return []
        
        # Read file content
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
        except UnicodeDecodeError:
            try:
                with open(file_path, 'r', encoding='latin-1') as f:
                    content = f.read()
            except:
                return []
        
        extracted = []
        
        # Route to appropriate extractor based on content type
        if content_type == 'proverb':
            extracted.extend(self.extract_proverbs(content, file_info))
        elif content_type == 'vocabulary':
            extracted.extend(self.extract_vocabulary(content, file_info))
        elif content_type == 'conjugations':
            extracted.extend(self.extract_conjugations(content, file_info))
        elif content_type == 'grammar':
            extracted.extend(self.extract_grammar(content, file_info))
        elif content_type == 'cultural':
            extracted.extend(self.extract_cultural_content(content, file_info))
        else:  # mixed content
            # Try all extractors for mixed content
            extracted.extend(self.extract_proverbs(content, file_info))
            extracted.extend(self.extract_vocabulary(content, file_info))
            extracted.extend(self.extract_conjugations(content, file_info))
            extracted.extend(self.extract_grammar(content, file_info))
            extracted.extend(self.extract_cultural_content(content, file_info))
        
        return extracted
    
    def extract_all_content(self) -> Dict[str, List[ExtractedContent]]:
        """Extract content from all analyzed files"""
        if not self.analysis_data:
            print("No analysis data available. Run analyzer first.")
            return {}
        
        analysis_results = self.analysis_data.get('analysis_results', [])
        
        print(f"Extracting content from {len(analysis_results)} files...")
        
        # Filter high-quality files
        high_quality_files = [
            file_info for file_info in analysis_results 
            if file_info.get('quality_score', 0) >= 2.5
        ]
        
        print(f"Processing {len(high_quality_files)} high-quality files...")
        
        for i, file_info in enumerate(high_quality_files):
            if i % 50 == 0 and i > 0:
                print(f"Processed {i}/{len(high_quality_files)} files...")
            
            extracted = self.extract_from_file(file_info)
            content_type = file_info.get('content_type', 'mixed')
            
            self.extracted_content[content_type].extend(extracted)
        
        return dict(self.extracted_content)
    
    def deduplicate_content(self):
        """Remove duplicate content across categories"""
        seen_pairs = set()
        
        for content_type, items in self.extracted_content.items():
            deduplicated = []
            
            for item in items:
                # Create a key for deduplication
                key = (item.english.lower().strip(), item.kikuyu.lower().strip())
                
                if key not in seen_pairs and len(item.english) > 2 and len(item.kikuyu) > 2:
                    seen_pairs.add(key)
                    deduplicated.append(item)
            
            self.extracted_content[content_type] = deduplicated
    
    def generate_extraction_summary(self) -> Dict:
        """Generate summary of extracted content"""
        summary = {}
        total_items = 0
        
        for content_type, items in self.extracted_content.items():
            summary[content_type] = {
                'count': len(items),
                'avg_quality': sum(item.quality_score for item in items) / len(items) if items else 0,
                'difficulty_distribution': {}
            }
            
            # Difficulty distribution
            for item in items:
                difficulty = item.difficulty
                if difficulty not in summary[content_type]['difficulty_distribution']:
                    summary[content_type]['difficulty_distribution'][difficulty] = 0
                summary[content_type]['difficulty_distribution'][difficulty] += 1
            
            total_items += len(items)
        
        summary['total_extracted'] = total_items
        return summary
    
    def save_extracted_content(self, output_file: str = "easy_kikuyu_extracted.json"):
        """Save extracted content to JSON file"""
        # Convert to serializable format
        output_data = {}
        
        for content_type, items in self.extracted_content.items():
            output_data[content_type] = []
            
            for item in items:
                output_data[content_type].append({
                    'english': item.english,
                    'kikuyu': item.kikuyu,
                    'context': item.context,
                    'content_type': item.content_type,
                    'difficulty': item.difficulty,
                    'cultural_notes': item.cultural_notes,
                    'quality_score': item.quality_score
                })
        
        # Add summary
        output_data['extraction_summary'] = self.generate_extraction_summary()
        
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(output_data, f, indent=2, ensure_ascii=False)
        
        print(f"Extracted content saved to: {output_file}")

def main():
    """Main extraction function"""
    print("Starting Easy Kikuyu Content Extraction...")
    
    extractor = EasyKikuyuExtractor()
    
    if not extractor.analysis_data:
        print("Analysis data not found. Please run easy_kikuyu_analyzer.py first.")
        return
    
    # Extract all content
    extracted = extractor.extract_all_content()
    
    if not extracted:
        print("No content was extracted!")
        return
    
    # Deduplicate
    print("Removing duplicates...")
    extractor.deduplicate_content()
    
    # Generate summary
    summary = extractor.generate_extraction_summary()
    
    print("\n" + "=" * 60)
    print("EXTRACTION SUMMARY")
    print("=" * 60)
    
    for content_type, stats in summary.items():
        if content_type != 'total_extracted':
            print(f"\n{content_type.upper()}:")
            print(f"  Items: {stats['count']}")
            print(f"  Avg Quality: {stats['avg_quality']:.2f}")
            print(f"  Difficulties: {stats['difficulty_distribution']}")
    
    print(f"\nTOTAL EXTRACTED ITEMS: {summary['total_extracted']}")
    
    # Save results
    extractor.save_extracted_content()
    
    print("\nExtraction complete! Ready for seed generation.")

if __name__ == "__main__":
    main()