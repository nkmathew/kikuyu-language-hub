#!/usr/bin/env python3
"""
Extract literal data from Wiktionary files to create hardcoded seed scripts
This will parse all files and output the actual content as Python literals
"""

import os
import sys
import re
from pathlib import Path
import json

# Add the project root to Python path
project_root = Path(__file__).parent.parent
sys.path.insert(0, str(project_root))

class WiktionaryExtractor:
    def __init__(self):
        self.wiktionary_dir = Path(project_root) / "raw-data" / "wiktionary"
        
    def parse_wiktionary_file(self, file_path):
        """Parse a single Wiktionary text file and extract structured data"""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
        except:
            try:
                with open(file_path, 'r', encoding='latin-1') as f:
                    content = f.read()
            except:
                return None
                
        data = {
            'verb': None,
            'infinitive': None,
            'ipa': None,
            'meanings': [],
            'derived_terms': [],
            'proverbs': [],
            'examples': [],
            'related_terms': []
        }
        
        lines = content.split('\n')
        current_section = None
        
        for i, line in enumerate(lines):
            line = line.strip()
            
            # Extract IPA pronunciation
            if line.startswith('/') and line.endswith('/'):
                data['ipa'] = line
                
            # Extract verb and infinitive
            if i < len(lines) - 2:
                next_line = lines[i + 1].strip()
                next_next_line = lines[i + 2].strip() if i + 2 < len(lines) else ""
                
                if (next_line == '(' and 
                    next_next_line.startswith('infinitive')):
                    data['verb'] = line
                    # Extract infinitive from the next few lines
                    for j in range(i + 3, min(i + 8, len(lines))):
                        if lines[j].strip().startswith('kw') or lines[j].strip().startswith('kũ'):
                            data['infinitive'] = lines[j].strip()
                            break
            
            # Extract meanings (lines starting with "to")
            if line.startswith('to '):
                meanings = line.split(', to ')
                for meaning in meanings:
                    if meaning.startswith('to '):
                        meaning = meaning[3:]  # Remove "to " prefix
                    data['meanings'].append(meaning.strip())
            
            # Track sections
            if line in ['Derived terms', 'Related terms', 'Proverbs']:
                current_section = line.lower().replace(' ', '_')
            elif line.startswith('(') and line.endswith(')'):
                continue
            elif current_section == 'derived_terms' and line and not line.startswith('[') and not line.startswith('('):
                if line not in ['edit', 'class', '1', '2', '3', '4', '5']:
                    data['derived_terms'].append(line)
            elif current_section == 'related_terms' and line and not line.startswith('[') and not line.startswith('('):
                if line not in ['edit', 'class', '1', '2', '3', '4', '5']:
                    data['related_terms'].append(line)
            elif current_section == 'proverbs' and line and not line.startswith('[') and not line.startswith('('):
                if line not in ['edit']:
                    data['proverbs'].append(line)
            
            # Extract examples
            if ('.' in line and 
                len(line.split()) > 3 and 
                not line.startswith('[') and 
                not line.startswith('(') and
                line != 'edit' and
                'References' not in line):
                data['examples'].append(line)
        
        return data if data['verb'] else None
    
    def extract_all_data(self):
        """Extract all data and return as structured literals"""
        all_data = []
        
        for wiki_dir in self.wiktionary_dir.iterdir():
            if wiki_dir.is_dir() and wiki_dir.name.startswith('wiki-'):
                txt_files = list(wiki_dir.glob('*.txt'))
                if txt_files:
                    parsed_data = self.parse_wiktionary_file(txt_files[0])
                    if parsed_data:
                        all_data.append(parsed_data)
        
        return all_data

def main():
    extractor = WiktionaryExtractor()
    data = extractor.extract_all_data()
    
    print(f"Extracted {len(data)} entries")
    
    # Organize data by type
    verbs_data = []
    infinitives_data = []
    derived_terms_data = []
    proverbs_data = []
    examples_data = []
    
    for entry in data:
        verb = entry['verb']
        infinitive = entry['infinitive']
        ipa = entry['ipa']
        meanings = entry['meanings']
        
        # Process main verbs
        for meaning in meanings:
            if meaning:
                context_parts = []
                if infinitive:
                    context_parts.append(f"Infinitive: {infinitive}")
                if ipa:
                    context_parts.append(f"IPA: {ipa}")
                
                context_notes = " | ".join(context_parts) if context_parts else f"Wiktionary verb: {verb}"
                
                verbs_data.append((meaning, verb, context_notes))
        
        # Process infinitives
        if infinitive and verb:
            infinitives_data.append((f"infinitive of {verb}", infinitive, f"Infinitive form of the verb '{verb}'"))
        
        # Process derived terms
        for derived_term in entry['derived_terms']:
            if derived_term and len(derived_term) > 2:
                derived_terms_data.append((f"derived from {verb}", derived_term, f"Morphologically derived from '{verb}'"))
        
        # Process proverbs
        for proverb in entry['proverbs']:
            if proverb and len(proverb.split()) >= 3:
                if ' - ' in proverb:
                    kikuyu_part, english_part = proverb.split(' - ', 1)
                else:
                    kikuyu_part = proverb
                    english_part = f"proverb containing {verb}"
                proverbs_data.append((english_part, kikuyu_part, f"Traditional proverb featuring the verb '{verb}'"))
        
        # Process examples
        for example in entry['examples']:
            if example and len(example.split()) >= 3 and '.' in example:
                cleaned_example = example.strip(' .-')
                if cleaned_example:
                    examples_data.append((f"example with {verb}", cleaned_example, f"Usage example featuring the verb '{verb}'"))
    
    # Print counts only to avoid encoding issues
    print("\n# VERBS DATA - Sample (avoiding encoding issues in console)")
    print("verbs_data = [")
    for i, (english, kikuyu, context) in enumerate(verbs_data[:10]):
        try:
            print(f'    # Entry {i+1}: {english[:30]}... -> {kikuyu[:20]}...')
        except:
            print(f'    # Entry {i+1}: [Unicode content]')
    print("]")
    
    print(f"\n# Total extracted:")
    print(f"# Verbs: {len(verbs_data)}")
    print(f"# Infinitives: {len(infinitives_data)}")
    print(f"# Derived terms: {len(derived_terms_data)}")
    print(f"# Proverbs: {len(proverbs_data)}")
    print(f"# Examples: {len(examples_data)}")
    
    # Save to files for creating seed scripts
    with open('wiktionary_literals.json', 'w', encoding='utf-8') as f:
        json.dump({
            'verbs': verbs_data,
            'infinitives': infinitives_data,
            'derived_terms': derived_terms_data,
            'proverbs': proverbs_data,
            'examples': examples_data
        }, f, indent=2, ensure_ascii=False)
    
    print("\nSaved all data to wiktionary_literals.json")

if __name__ == "__main__":
    main()