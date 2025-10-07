# Seed Files Archive

## Overview
This archive contains all the original seed files used to populate the Kikuyu Language Hub database. The parsing was not optimal due to inconsistent raw data formats from various sources.

## Archive Date
October 7, 2025

## Issues with Current Parsing
1. **Non-uniform data structures** - Different sources had varying formats
2. **Inconsistent difficulty categorization** - All difficulties showing as 0
3. **Mixed content types** - Some files mixed vocabulary, grammar, and cultural notes
4. **Hardcoded approaches** - Used literal strings instead of proper parsing

## Files Archived

### Easy Kikuyu Sources (Facebook Page Extraction)
- `easy_kikuyu_vocabulary_seed.py` - Vocabulary parsing (automated)
- `easy_kikuyu_vocabulary_literal_seed.py` - Manual vocabulary data
- `easy_kikuyu_proverbs_seed.py` - Proverbs parsing (automated)  
- `easy_kikuyu_proverbs_literal_seed.py` - Manual proverbs data
- `easy_kikuyu_conjugations_seed.py` - Conjugations parsing (automated)
- `easy_kikuyu_conjugations_literal_seed.py` - Manual conjugations data
- `easy_kikuyu_comprehensive_seed.py` - Combined parsing (automated)
- `easy_kikuyu_comprehensive_literal_seed.py` - Manual comprehensive data

### Other Language Sources
- `wikipedia_seed.py` - Wikipedia Kikuyu language content
- `quizlet_seed.py` - Quizlet flashcard sets
- `verbs_seed.py` - Verb conjugation patterns
- `numbers_seed.py` - Number system
- `facebook_cultural_seed.py` - Cultural expressions from Facebook
- `linguistic_verb_seed.py` - Linguistic verb analysis
- `linguistic_grammar_seed.py` - Grammar rules and structures
- `learn_kikuyu_app_seed.py` - Mobile app content
- `lughayangu_vocabulary_seed.py` - LughaYangu vocabulary
- `wiktionary_comprehensive_seed.py` - Wiktionary data
- `wiktionary_verbs_literal_seed.py` - Manual Wiktionary verbs
- `wiktionary_proverbs_literal_seed.py` - Manual Wiktionary proverbs
- `wiktionary_derivatives_literal_seed.py` - Word derivatives
- `wisdomafrica_translations_seed.py` - WisdomAfrica content
- `additional_sources_seed.py` - Miscellaneous sources
- `additional_vocabulary_seed.py` - Additional vocabulary
- `comprehensive_materials_seed.py` - Comprehensive learning materials
- `cooking_methods_seed.py` - Cooking and food terminology
- `proverbs_collection_seed.py` - Proverb collections

### Support Files
- `run_easy_kikuyu_literal_seeds.py` - Batch runner for literal seeds
- `wikipedia_kikuyu_extracted.py` - Wikipedia extraction script
- `wikipedia_kikuyu_language_structured.txt` - Structured Wikipedia data

## Current Database Status
**Successfully populated with 3,393 contributions:**
- Vocabulary: 1,747 cards
- Proverbs: 608 cards  
- Conjugations: 692 cards
- Grammar: 51 cards
- General: 295 cards

## Recommendations for Future Improvement

### 1. Manual Content Review Approach
Instead of automated parsing, manually review each source:

```bash
# Create structured review process
mkdir archive/manual-review/
mkdir archive/manual-review/vocabulary/
mkdir archive/manual-review/proverbs/
mkdir archive/manual-review/grammar/
mkdir archive/manual-review/conjugations/
```

### 2. Standardized JSON Schema
Create consistent structure for all content:

```json
{
  "source": "easy_kikuyu_facebook",
  "category": "vocabulary|proverbs|grammar|conjugations",
  "difficulty": "beginner|intermediate|advanced",
  "english": "English text",
  "kikuyu": "Kikuyu text", 
  "context": "Usage context",
  "cultural_notes": "Cultural significance",
  "pronunciation": "IPA or simplified",
  "examples": ["usage examples"],
  "tags": ["subject", "theme"],
  "quality_verified": true|false,
  "native_speaker_reviewed": true|false
}
```

### 3. Quality Control Process
1. **Source Verification** - Verify content with native speakers
2. **Difficulty Classification** - Proper beginner/intermediate/advanced categorization
3. **Cultural Context** - Add proper cultural explanations
4. **Pronunciation Guides** - Add pronunciation for learners
5. **Usage Examples** - Provide context sentences

### 4. Raw Data Files Reference
Original raw data files are in:
- `raw-data/easy-kikuyu/` - 540+ text files from Facebook page
- Other source materials referenced in seed files

## Next Steps for Data Improvement

### Phase 1: Content Audit
1. Review each seed file's source material
2. Identify highest quality content sources
3. Flag content needing native speaker review

### Phase 2: Manual Curation  
1. Create curated JSON files for each category
2. Proper difficulty classification by native speakers
3. Add cultural context and pronunciation

### Phase 3: Enhanced Features
1. Audio pronunciation files
2. Interactive grammar exercises  
3. Cultural immersion content
4. Regional dialect variations

## Archive Structure
```
archive/
├── SEED_FILES_ARCHIVE_README.md (this file)
└── seed-files-original/
    ├── [all 30 original .py seed files]
    ├── wikipedia_kikuyu_language_structured.txt
    └── run_easy_kikuyu_literal_seeds.py
```

## Usage Notes
- These files were created between Oct 2-7, 2025
- Main focus was on Easy Kikuyu Facebook page content (540 files)
- Parsing attempts were made but resulted in non-uniform data
- Current static website uses this data successfully
- Future improvements should focus on manual curation over automated parsing

## Contact for Native Speaker Review
Content should be reviewed by native Kikuyu speakers for:
- Accuracy verification
- Cultural appropriateness  
- Difficulty level classification
- Pronunciation guidance
- Regional variations