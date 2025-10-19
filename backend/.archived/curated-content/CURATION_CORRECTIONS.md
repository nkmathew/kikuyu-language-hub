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

#### Batch 017 Phrases
**File**: `backend/curated-content/phrases/easy_kikuyu_batch_017_phrases.json`
**Before**: 3 compound entries
**After**: 21 individual flashcards

Split entries:
- **phrase-017-001** (Nuclear Family Dialogue) → 5 phrases about family
- **phrase-017-002** (Health & Hospital Conversation) → 8 medical/health phrases
- **phrase-017-003** (Greeting & Visitor Dialogue) → 8 visitor greeting phrases

#### Batch 020 Phrases
**File**: `backend/curated-content/phrases/easy_kikuyu_batch_020_phrases.json`
**Before**: 3 compound entries
**After**: 19 individual flashcards

Split entries:
- **phrase-020-001** (Visitor Greeting Dialogue) → 2 unique phrases (duplicates removed)
- **phrase-020-002** (Basic Questions & Identification) → 7 question phrases
- **phrase-020-003** (Meeting Arrangements & Directions) → 10 transportation/meeting phrases

#### Batch 026 Phrases
**File**: `backend/curated-content/phrases/easy_kikuyu_batch_026_phrases.json`
**Before**: 4 compound entries
**After**: 30 individual flashcards

Split entries:
- **phrase-026-001** (Imperative Commands - Household) → 10 command phrases
- **phrase-026-002** (Greetings - Week 2-1) → 8 greeting phrases
- **phrase-026-003** (Multi-topic Dialogue) → 7 conversation phrases
- **phrase-026-004** (Simple Sentence Construction Practice) → 5 practice sentences

## Remaining Work

#### Batch 021 Phrases
**File**: `backend/curated-content/phrases/easy_kikuyu_batch_021_phrases.json`
**Before**: 3 compound entries
**After**: 26 individual flashcards

Split entries:
- **phrase-021-001** (Simple Foundation Questions) → 10 identity/preference questions
- **phrase-021-002** (Handwashing Instructions) → 6 step-by-step hygiene commands
- **phrase-021-003** (School Conversation Dialogue) → 10 school/location conversation phrases

#### Batch 022 Phrases
**File**: `backend/curated-content/phrases/easy_kikuyu_batch_022_phrases.json`
**Before**: 2 compound entries
**After**: 18 individual flashcards

Split entries:
- **phrase-022-001** (Week 2 Greetings Dialogue) → 10 traditional greeting patterns
- **phrase-022-002** (Sound & Feeling Expressions) → 8 advanced idiomatic expressions

#### Batch 024 Phrases
**File**: `backend/curated-content/phrases/easy_kikuyu_batch_024_phrases.json`
**Before**: 1 compound entry
**After**: 10 individual flashcards

Split entries:
- **phrase-024-001** (Week 2 Greetings - Extended Version) → 10 greeting pattern variations

#### Batch 025 Phrases
**File**: `backend/curated-content/phrases/easy_kikuyu_batch_025_phrases.json`
**Before**: 2 compound entries
**After**: 21 individual flashcards

Split entries:
- **phrase-025-001** (Simple Foundation Questions - Week 4) → 7 school/family/interest questions
- **phrase-025-002** (Transport & Payment Dialogue) → 14 matatu conversation phrases

## Remaining Work

### Medium Priority
These entries may be kept as compound cultural notes:

1. **phrase-027-001**: New Month Blessing - July (cultural/seasonal blessing)

### Status: COMPLETED ✓

**Total flashcards created through splitting**:
- Batch 015 phrases: 3 → 11 (+ 8 cards)
- Batch 017 phrases: 3 → 21 (+ 18 cards)
- Batch 020 phrases: 3 → 19 (+ 16 cards)
- Batch 021 phrases: 3 → 26 (+ 23 cards)
- Batch 022 phrases: 2 → 18 (+ 16 cards)
- Batch 024 phrases: 1 → 10 (+ 9 cards)
- Batch 025 phrases: 2 → 21 (+ 19 cards)
- Batch 026 phrases: 4 → 30 (+ 26 cards)
- Batch 027 vocab: 5 → 24 (+ 19 cards)
- Grammar batch 043: 25 → 23 (- 2 invalid cards)
- Vocab batch 050: 60 → 59 (- 1 invalid card)

**Net increase**: +151 usable flashcards (from splitting compounds and removing invalid entries)

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
