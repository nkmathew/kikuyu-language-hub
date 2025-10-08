# Curated Content Corrections Log

## Date: 2025-01-09

### Summary
Reviewed and corrected improperly parsed curated content entries. Focus on entries that were too broad, contained multiple phrases, or had parsing errors.

## Corrections Made

### 1. Removed Invalid Single-Letter Entries
**Reason**: Monosyllabic entries too ambiguous for standalone flashcards

- **ek_b43_grammar_015**: Removed standalone "A" (Which people)
  - Kept the phrase "Nĩ a?" (Who are they?) instead
  - File: `backend/curated-content/grammar/easy_kikuyu_batch_043_grammar.json`

- **ek_b43_grammar_018**: Removed standalone "E" (Him/her)
  - Kept the phrase "E ha?" (Where is he/she?) instead
  - File: `backend/curated-content/grammar/easy_kikuyu_batch_043_grammar.json`

- **ek_b50_vocab_010**: Removed standalone "o" (they/those ones)
  - File: `backend/curated-content/vocabulary/easy_kikuyu_batch_050_vocab.json`

**Impact**:
- Batch 43 Grammar: 25 → 23 flashcards
- Batch 50 Vocabulary: 60 → 59 flashcards

### 2. Fixed Incorrect Transcription
- **ek_phrase_batch002_001**: "dire na mugunda" → "Ndire na mũgũnda"
  - **Reason**: Missing subject marker "N-" at beginning
  - **Translation**: "I don't have a farm"
  - File: `backend/curated-content/phrases/easy_kikuyu_batch_002_phrases.json`

### 3. Split Multi-Phrase Entries

#### Batch 015 Phrases
**File**: `backend/curated-content/phrases/easy_kikuyu_batch_015_phrases.json`
**Before**: 3 compound entries
**After**: 11 individual flashcards

Split entries:
- **phrase-015-001** (Location & Place Phrases) → Split into 4 phrases:
  - Ũyũ nĩ mũciĩ mũnene wa Nairobi (This is the city of Nairobi)
  - Taũni nene ya Nairobi (The big town of Nairobi)
  - Gaũdencia e Ol-jororok (Gaudencia is in Ol-jororok)
  - We wĩkũ? (Where are you?)

- **phrase-015-002** (Weather Inquiry Conversation) → Split into 4 phrases:
  - Kwanyu kũhana atĩa? (How is your place?)
  - Kwĩna heho kana mbura? (Is it cold or rainy?)
  - Kana kwĩna riũwa? (Or is it sunny?)
  - Gwitũ kũrĩ na rũhuho (Our place is windy)

- **phrase-015-003** (Telling Time - Complete System) → Split into 3 essential phrases:
  - Nĩ thaa cĩgana? (What time is it?)
  - Nĩ thaa igĩrĩ na nuthu (It is 8:30)
  - Nĩ thaa ĩthatũ itigarĩtie ndagĩka ikũmi na ithano (It is 9:45)

## Remaining Work

### High Priority
These entries still need to be split (flagged by users):

1. **phrase-017-001**: Nuclear Family Dialogue (5+ phrases)
2. **phrase-017-003**: Greeting & Visitor Dialogue (11+ phrases)
3. **phrase-020-001**: Visitor Greeting Dialogue (duplicate of 017-003, needs review)
4. **phrase-020-002**: Basic Questions & Identification (7+ phrases)
5. **phrase-021-001**: Simple Foundation Questions (10+ phrases)
6. **phrase-022-001**: Week 2 Greetings Dialogue (9+ phrases)
7. **phrase-024-001**: Week 2 Greetings - Extended Version (10+ phrases)
8. **phrase-025-001**: Simple Foundation Questions - Week 4 (7+ phrases)
9. **phrase-026-001**: Imperative Commands - Household (10+ phrases)
10. **phrase-026-002**: Greetings - Week 2-1 (8+ phrases)
11. **phrase-026-004**: Simple Sentence Construction Practice (13+ phrases)
12. **phrase-027-001**: New Month Blessing - July (can keep as compound cultural note)

### Vocabulary Entries to Split

1. **vocab-027-003**: Clothing & Accessories - Complete (20+ items)
   - File: `backend/curated-content/vocabulary/easy_kikuyu_batch_027_vocab.json`
   - Should be individual vocabulary cards

2. **vocab-027-005**: Character Descriptors (3+ items)
   - File: `backend/curated-content/vocabulary/easy_kikuyu_batch_027_vocab.json`
   - Should be individual vocabulary cards

## Benefits of Splitting

1. **Better Learning Experience**: Individual cards are easier to study and remember
2. **Proper Spaced Repetition**: Each concept can be reviewed independently
3. **Accurate Progress Tracking**: Users can mark individual phrases as known/unknown
4. **Easier Flagging**: Users can flag specific problematic translations
5. **Better Searchability**: Individual entries are easier to find and reference

## Notes

- All split entries maintain:
  - Original source attribution
  - Quality scores
  - Cultural notes
  - Proper difficulty levels
  - Batch numbering consistency

- Date format updated to ISO 8601 for split entries
- IDs maintained for traceability where possible
- Compound entries that represent cohesive dialogues or cultural practices may be kept together if educationally valuable

## Next Steps

1. Complete splitting remaining flagged phrase entries (items 1-12 above)
2. Split vocabulary compound entries
3. Run validation script to check for duplicates
4. Run `python sync-curated-content.py` to sync to all apps
5. Update `EASY_KIKUYU_PROGRESS.md` with new totals
