import { CategoryData, Flashcard, CuratedContent, CategoryType } from '../types/flashcard';

// Import all curated content files explicitly
// This ensures they are bundled in production builds
const curatedData = {
  // Conjugations
  'conjugations/easy_kikuyu_011_moments_ago.json': require('../assets/data/curated/conjugations/easy_kikuyu_011_moments_ago.json'),
  'conjugations/easy_kikuyu_012_early_morning.json': require('../assets/data/curated/conjugations/easy_kikuyu_012_early_morning.json'),
  'conjugations/easy_kikuyu_batch_001_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_001_conjugations.json'),
  'conjugations/easy_kikuyu_batch_002_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_002_conjugations.json'),
  'conjugations/easy_kikuyu_batch_004_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_004_conjugations.json'),
  'conjugations/easy_kikuyu_batch_005_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_005_conjugations.json'),
  'conjugations/easy_kikuyu_batch_007_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_007_conjugations.json'),
  'conjugations/easy_kikuyu_batch_009_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_009_conjugations.json'),
  'conjugations/easy_kikuyu_batch_012_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_012_conjugations.json'),
  'conjugations/easy_kikuyu_batch_013_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_013_conjugations.json'),
  'conjugations/easy_kikuyu_batch_014_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_014_conjugations.json'),
  'conjugations/easy_kikuyu_batch_017_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_017_conjugations.json'),
  'conjugations/easy_kikuyu_batch_019_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_019_conjugations.json'),
  'conjugations/easy_kikuyu_batch_020_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_020_conjugations.json'),
  'conjugations/easy_kikuyu_batch_021_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_021_conjugations.json'),
  'conjugations/easy_kikuyu_batch_022_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_022_conjugations.json'),
  'conjugations/easy_kikuyu_batch_023_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_023_conjugations.json'),
  'conjugations/easy_kikuyu_batch_024_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_024_conjugations.json'),
  'conjugations/easy_kikuyu_batch_025_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_025_conjugations.json'),
  'conjugations/easy_kikuyu_batch_026_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_026_conjugations.json'),
  'conjugations/easy_kikuyu_batch_037_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_037_conjugations.json'),
  'conjugations/easy_kikuyu_batch_038_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_038_conjugations.json'),
  'conjugations/easy_kikuyu_batch_039_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_039_conjugations.json'),
  'conjugations/easy_kikuyu_batch_040_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_040_conjugations.json'),
  'conjugations/easy_kikuyu_batch_041_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_041_conjugations.json'),
  'conjugations/easy_kikuyu_batch_042_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_042_conjugations.json'),
  'conjugations/easy_kikuyu_batch_043_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_043_conjugations.json'),
  'conjugations/easy_kikuyu_batch_044_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_044_conjugations.json'),
  'conjugations/easy_kikuyu_batch_045_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_045_conjugations.json'),
  'conjugations/easy_kikuyu_batch_046_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_046_conjugations.json'),
  'conjugations/easy_kikuyu_batch_047_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_047_conjugations.json'),
  'conjugations/easy_kikuyu_batch_048_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_048_conjugations.json'),
  'conjugations/easy_kikuyu_batch_049_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_049_conjugations.json'),
  'conjugations/easy_kikuyu_batch_050_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_050_conjugations.json'),
  'conjugations/easy_kikuyu_batch_051_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_051_conjugations.json'),
  'conjugations/easy_kikuyu_batch_052_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_052_conjugations.json'),
  'conjugations/easy_kikuyu_batch_053_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_053_conjugations.json'),
  'conjugations/wiktionary_basic_verbs.json': require('../assets/data/curated/conjugations/wiktionary_basic_verbs.json'),

  // Cultural
  'cultural/easy_kikuyu_010_quran_fatiha.json': require('../assets/data/curated/cultural/easy_kikuyu_010_quran_fatiha.json'),
  'cultural/easy_kikuyu_batch_001_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_001_cultural.json'),
  'cultural/easy_kikuyu_batch_040_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_040_cultural.json'),
  'cultural/easy_kikuyu_batch_044_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_044_cultural.json'),
  'cultural/easy_kikuyu_batch_045_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_045_cultural.json'),
  'cultural/easy_kikuyu_batch_046_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_046_cultural.json'),
  'cultural/easy_kikuyu_batch_047_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_047_cultural.json'),
  'cultural/easy_kikuyu_batch_048_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_048_cultural.json'),
  'cultural/easy_kikuyu_batch_049_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_049_cultural.json'),
  'cultural/easy_kikuyu_batch_050_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_050_cultural.json'),
  'cultural/easy_kikuyu_batch_051_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_051_cultural.json'),
  'cultural/easy_kikuyu_batch_052_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_052_cultural.json'),
  'cultural/easy_kikuyu_batch_053_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_053_cultural.json'),
  'cultural/easy_kikuyu_batch_054_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_054_cultural.json'),

  // Grammar
  'grammar/easy_kikuyu_batch_002_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_002_grammar.json'),
  'grammar/easy_kikuyu_batch_003_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_003_grammar.json'),
  'grammar/easy_kikuyu_batch_004_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_004_grammar.json'),
  'grammar/easy_kikuyu_batch_005_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_005_grammar.json'),
  'grammar/easy_kikuyu_batch_007_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_007_grammar.json'),
  'grammar/easy_kikuyu_batch_008_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_008_grammar.json'),
  'grammar/easy_kikuyu_batch_009_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_009_grammar.json'),
  'grammar/easy_kikuyu_batch_010_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_010_grammar.json'),
  'grammar/easy_kikuyu_batch_012_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_012_grammar.json'),
  'grammar/easy_kikuyu_batch_013_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_013_grammar.json'),
  'grammar/easy_kikuyu_batch_014_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_014_grammar.json'),
  'grammar/easy_kikuyu_batch_016_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_016_grammar.json'),
  'grammar/easy_kikuyu_batch_017_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_017_grammar.json'),
  'grammar/easy_kikuyu_batch_018_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_018_grammar.json'),
  'grammar/easy_kikuyu_batch_019_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_019_grammar.json'),
  'grammar/easy_kikuyu_batch_020_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_020_grammar.json'),
  'grammar/easy_kikuyu_batch_021_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_021_grammar.json'),
  'grammar/easy_kikuyu_batch_022_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_022_grammar.json'),
  'grammar/easy_kikuyu_batch_023_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_023_grammar.json'),
  'grammar/easy_kikuyu_batch_024_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_024_grammar.json'),
  'grammar/easy_kikuyu_batch_025_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_025_grammar.json'),
  'grammar/easy_kikuyu_batch_027_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_027_grammar.json'),
  'grammar/easy_kikuyu_batch_038_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_038_grammar.json'),
  'grammar/easy_kikuyu_batch_039_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_039_grammar.json'),
  'grammar/easy_kikuyu_batch_040_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_040_grammar.json'),
  'grammar/easy_kikuyu_batch_041_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_041_grammar.json'),
  'grammar/easy_kikuyu_batch_043_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_043_grammar.json'),

  // Phrases
  'phrases/common_greetings.json': require('../assets/data/curated/phrases/common_greetings.json'),
  'phrases/easy_kikuyu_batch_002_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_002_phrases.json'),
  'phrases/easy_kikuyu_batch_003_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_003_phrases.json'),
  'phrases/easy_kikuyu_batch_004_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_004_phrases.json'),
  'phrases/easy_kikuyu_batch_005_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_005_phrases.json'),
  'phrases/easy_kikuyu_batch_006_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_006_phrases.json'),
  'phrases/easy_kikuyu_batch_008_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_008_phrases.json'),
  'phrases/easy_kikuyu_batch_009_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_009_phrases.json'),
  'phrases/easy_kikuyu_batch_010_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_010_phrases.json'),
  'phrases/easy_kikuyu_batch_011_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_011_phrases.json'),
  'phrases/easy_kikuyu_batch_015_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_015_phrases.json'),
  'phrases/easy_kikuyu_batch_017_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_017_phrases.json'),
  'phrases/easy_kikuyu_batch_018_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_018_phrases.json'),
  'phrases/easy_kikuyu_batch_019_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_019_phrases.json'),
  'phrases/easy_kikuyu_batch_020_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_020_phrases.json'),
  'phrases/easy_kikuyu_batch_021_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_021_phrases.json'),
  'phrases/easy_kikuyu_batch_022_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_022_phrases.json'),
  'phrases/easy_kikuyu_batch_023_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_023_phrases.json'),
  'phrases/easy_kikuyu_batch_024_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_024_phrases.json'),
  'phrases/easy_kikuyu_batch_025_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_025_phrases.json'),
  'phrases/easy_kikuyu_batch_026_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_026_phrases.json'),
  'phrases/easy_kikuyu_batch_027_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_027_phrases.json'),
  'phrases/easy_kikuyu_batch_037_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_037_phrases.json'),
  'phrases/easy_kikuyu_batch_038_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_038_phrases.json'),
  'phrases/easy_kikuyu_batch_039_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_039_phrases.json'),
  'phrases/easy_kikuyu_batch_040_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_040_phrases.json'),
  'phrases/easy_kikuyu_batch_041_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_041_phrases.json'),
  'phrases/easy_kikuyu_batch_042_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_042_phrases.json'),
  'phrases/easy_kikuyu_batch_043_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_043_phrases.json'),
  'phrases/easy_kikuyu_batch_044_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_044_phrases.json'),
  'phrases/easy_kikuyu_batch_045_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_045_phrases.json'),
  'phrases/easy_kikuyu_batch_046_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_046_phrases.json'),
  'phrases/easy_kikuyu_batch_047_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_047_phrases.json'),
  'phrases/easy_kikuyu_batch_048_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_048_phrases.json'),
  'phrases/easy_kikuyu_batch_049_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_049_phrases.json'),
  'phrases/easy_kikuyu_batch_050_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_050_phrases.json'),
  'phrases/easy_kikuyu_batch_051_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_051_phrases.json'),
  'phrases/easy_kikuyu_batch_052_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_052_phrases.json'),
  'phrases/easy_kikuyu_batch_053_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_053_phrases.json'),
  'phrases/easy_kikuyu_batch_054_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_054_phrases.json'),

  // Proverbs
  'proverbs/easy_kikuyu_001_proverb.json': require('../assets/data/curated/proverbs/easy_kikuyu_001_proverb.json'),
  'proverbs/easy_kikuyu_014_cleverness.json': require('../assets/data/curated/proverbs/easy_kikuyu_014_cleverness.json'),
  'proverbs/easy_kikuyu_batch_001_proverbs.json': require('../assets/data/curated/proverbs/easy_kikuyu_batch_001_proverbs.json'),
  'proverbs/easy_kikuyu_batch_009_proverbs.json': require('../assets/data/curated/proverbs/easy_kikuyu_batch_009_proverbs.json'),
  'proverbs/easy_kikuyu_batch_014_proverbs.json': require('../assets/data/curated/proverbs/easy_kikuyu_batch_014_proverbs.json'),
  'proverbs/easy_kikuyu_batch_015_proverbs.json': require('../assets/data/curated/proverbs/easy_kikuyu_batch_015_proverbs.json'),
  'proverbs/easy_kikuyu_batch_016_proverbs.json': require('../assets/data/curated/proverbs/easy_kikuyu_batch_016_proverbs.json'),
  'proverbs/easy_kikuyu_batch_017_proverbs.json': require('../assets/data/curated/proverbs/easy_kikuyu_batch_017_proverbs.json'),
  'proverbs/easy_kikuyu_batch_018_proverbs.json': require('../assets/data/curated/proverbs/easy_kikuyu_batch_018_proverbs.json'),
  'proverbs/easy_kikuyu_batch_022_proverbs.json': require('../assets/data/curated/proverbs/easy_kikuyu_batch_022_proverbs.json'),
  'proverbs/easy_kikuyu_batch_024_proverbs.json': require('../assets/data/curated/proverbs/easy_kikuyu_batch_024_proverbs.json'),
  'proverbs/easy_kikuyu_batch_037_proverbs.json': require('../assets/data/curated/proverbs/easy_kikuyu_batch_037_proverbs.json'),
  'proverbs/easy_kikuyu_batch_038_proverbs.json': require('../assets/data/curated/proverbs/easy_kikuyu_batch_038_proverbs.json'),
  'proverbs/easy_kikuyu_batch_039_proverbs.json': require('../assets/data/curated/proverbs/easy_kikuyu_batch_039_proverbs.json'),
  'proverbs/easy_kikuyu_batch_040_proverbs.json': require('../assets/data/curated/proverbs/easy_kikuyu_batch_040_proverbs.json'),
  'proverbs/easy_kikuyu_batch_041_proverbs.json': require('../assets/data/curated/proverbs/easy_kikuyu_batch_041_proverbs.json'),
  'proverbs/easy_kikuyu_batch_042_proverbs.json': require('../assets/data/curated/proverbs/easy_kikuyu_batch_042_proverbs.json'),
  'proverbs/easy_kikuyu_wisdom.json': require('../assets/data/curated/proverbs/easy_kikuyu_wisdom.json'),

  // Vocabulary
  'vocabulary/easy_kikuyu_006_shapes.json': require('../assets/data/curated/vocabulary/easy_kikuyu_006_shapes.json'),
  'vocabulary/easy_kikuyu_007_consonants.json': require('../assets/data/curated/vocabulary/easy_kikuyu_007_consonants.json'),
  'vocabulary/easy_kikuyu_013_market_days.json': require('../assets/data/curated/vocabulary/easy_kikuyu_013_market_days.json'),
  'vocabulary/easy_kikuyu_animals.json': require('../assets/data/curated/vocabulary/easy_kikuyu_animals.json'),
  'vocabulary/easy_kikuyu_batch_001_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_001_vocab.json'),
  'vocabulary/easy_kikuyu_batch_002_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_002_vocab.json'),
  'vocabulary/easy_kikuyu_batch_003_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_003_vocab.json'),
  'vocabulary/easy_kikuyu_batch_004_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_004_vocab.json'),
  'vocabulary/easy_kikuyu_batch_005_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_005_vocab.json'),
  'vocabulary/easy_kikuyu_batch_006_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_006_vocab.json'),
  'vocabulary/easy_kikuyu_batch_007_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_007_vocab.json'),
  'vocabulary/easy_kikuyu_batch_008_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_008_vocab.json'),
  'vocabulary/easy_kikuyu_batch_009_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_009_vocab.json'),
  'vocabulary/easy_kikuyu_batch_010_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_010_vocab.json'),
  'vocabulary/easy_kikuyu_batch_011_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_011_vocab.json'),
  'vocabulary/easy_kikuyu_batch_012_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_012_vocab.json'),
  'vocabulary/easy_kikuyu_batch_013_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_013_vocab.json'),
  'vocabulary/easy_kikuyu_batch_014_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_014_vocab.json'),
  'vocabulary/easy_kikuyu_batch_015_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_015_vocab.json'),
  'vocabulary/easy_kikuyu_batch_016_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_016_vocab.json'),
  'vocabulary/easy_kikuyu_batch_017_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_017_vocab.json'),
  'vocabulary/easy_kikuyu_batch_018_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_018_vocab.json'),
  'vocabulary/easy_kikuyu_batch_019_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_019_vocab.json'),
  'vocabulary/easy_kikuyu_batch_020_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_020_vocab.json'),
  'vocabulary/easy_kikuyu_batch_021_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_021_vocab.json'),
  'vocabulary/easy_kikuyu_batch_022_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_022_vocab.json'),
  'vocabulary/easy_kikuyu_batch_023_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_023_vocab.json'),
  'vocabulary/easy_kikuyu_batch_024_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_024_vocab.json'),
  'vocabulary/easy_kikuyu_batch_025_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_025_vocab.json'),
  'vocabulary/easy_kikuyu_batch_026_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_026_vocab.json'),
  'vocabulary/easy_kikuyu_batch_027_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_027_vocab.json'),
  'vocabulary/easy_kikuyu_batch_028_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_028_vocab.json'),
  'vocabulary/easy_kikuyu_batch_029_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_029_vocab.json'),
  'vocabulary/easy_kikuyu_batch_030_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_030_vocab.json'),
  'vocabulary/easy_kikuyu_batch_031_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_031_vocab.json'),
  'vocabulary/easy_kikuyu_batch_032_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_032_vocab.json'),
  'vocabulary/easy_kikuyu_batch_033_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_033_vocab.json'),
  'vocabulary/easy_kikuyu_batch_034_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_034_vocab.json'),
  'vocabulary/easy_kikuyu_batch_035_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_035_vocab.json'),
  'vocabulary/easy_kikuyu_batch_036_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_036_vocab.json'),
  'vocabulary/easy_kikuyu_batch_037_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_037_vocab.json'),
  'vocabulary/easy_kikuyu_batch_038_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_038_vocab.json'),
  'vocabulary/easy_kikuyu_batch_039_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_039_vocab.json'),
  'vocabulary/easy_kikuyu_batch_040_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_040_vocab.json'),
  'vocabulary/easy_kikuyu_batch_041_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_041_vocab.json'),
  'vocabulary/easy_kikuyu_batch_042_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_042_vocab.json'),
  'vocabulary/easy_kikuyu_batch_043_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_043_vocab.json'),
  'vocabulary/easy_kikuyu_batch_044_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_044_vocab.json'),
  'vocabulary/easy_kikuyu_batch_045_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_045_vocab.json'),
  'vocabulary/easy_kikuyu_batch_046_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_046_vocab.json'),
  'vocabulary/easy_kikuyu_batch_047_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_047_vocab.json'),
  'vocabulary/easy_kikuyu_batch_048_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_048_vocab.json'),
  'vocabulary/easy_kikuyu_batch_049_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_049_vocab.json'),
  'vocabulary/easy_kikuyu_batch_050_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_050_vocab.json'),
  'vocabulary/easy_kikuyu_batch_051_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_051_vocab.json'),
  'vocabulary/easy_kikuyu_batch_052_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_052_vocab.json'),
  'vocabulary/easy_kikuyu_batch_053_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_053_vocab.json'),
  'vocabulary/easy_kikuyu_batch_054_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_054_vocab.json'),
  'vocabulary/easy_kikuyu_household_items.json': require('../assets/data/curated/vocabulary/easy_kikuyu_household_items.json'),

};

class DataLoader {
  private cache: Map<string, CategoryData> = new Map();
  private allData: Map<string, CuratedContent> = new Map();

  constructor() {
    this.loadAllDataFiles();
  }

  private loadAllDataFiles() {
    // Load all JSON files from explicit imports
    Object.entries(curatedData).forEach(([key, data]) => {
      try {
        this.allData.set(`./${key}`, data as CuratedContent);
      } catch (error) {
        console.error(`Error loading ${key}:`, error);
      }
    });

    console.log(`Loaded ${this.allData.size} data files`);
  }

  async loadCategory(category: CategoryType): Promise<CategoryData> {
    if (this.cache.has(category)) {
      return this.cache.get(category)!;
    }

    try {
      const allCards: Flashcard[] = [];

      // If category is 'general' or 'all', combine all categories
      if (category === 'general' || category === 'all') {
        this.allData.forEach((content, path) => {
          if (!path.includes('schema.json')) {
            const cards = content.flashcards || content.entries || [];
            allCards.push(...cards);
          }
        });
      } else {
        // Load specific category
        const categoryPath = `./${category}/`;
        this.allData.forEach((content, path) => {
          if (path.startsWith(categoryPath)) {
            const cards = content.flashcards || content.entries || [];
            allCards.push(...cards);
          }
        });
      }

      // Categorize by difficulty
      const beginnerCards = allCards.filter(card => card.difficulty === 'beginner');
      const intermediateCards = allCards.filter(card => card.difficulty === 'intermediate');
      const advancedCards = allCards.filter(card => card.difficulty === 'advanced');

      const categoryData: CategoryData = {
        category,
        total_count: allCards.length,
        difficulty_counts: {
          beginner: beginnerCards.length,
          intermediate: intermediateCards.length,
          advanced: advancedCards.length,
        },
        items: {
          beginner: beginnerCards,
          intermediate: intermediateCards,
          advanced: advancedCards,
          all: allCards,
        },
      };

      this.cache.set(category, categoryData);
      return categoryData;
    } catch (error) {
      console.error(`Error loading category ${category}:`, error);
      // Return empty category data
      const emptyData: CategoryData = {
        category,
        total_count: 0,
        difficulty_counts: { beginner: 0, intermediate: 0, advanced: 0 },
        items: { beginner: [], intermediate: [], advanced: [], all: [] },
      };
      return emptyData;
    }
  }

  async loadAllCategories(): Promise<Record<CategoryType, CategoryData>> {
    const categories: CategoryType[] = ['vocabulary', 'proverbs', 'conjugations', 'grammar', 'general', 'phrases'];
    const results: Record<string, CategoryData> = {};

    await Promise.all(
      categories.map(async (category) => {
        try {
          results[category] = await this.loadCategory(category);
        } catch (error) {
          console.error(`Failed to load ${category}:`, error);
          results[category] = {
            category,
            total_count: 0,
            difficulty_counts: { beginner: 0, intermediate: 0, advanced: 0 },
            items: { beginner: [], intermediate: [], advanced: [], all: [] },
          };
        }
      })
    );

    return results as Record<CategoryType, CategoryData>;
  }

  getCardsByDifficulty(categoryData: CategoryData, difficulties: string[]): Flashcard[] {
    const cards: Flashcard[] = [];

    // If "all" is in the difficulties array, return all cards
    if (difficulties.includes('all')) {
      return categoryData.items.all;
    }

    difficulties.forEach(difficulty => {
      switch (difficulty.toLowerCase()) {
        case 'beginner':
          cards.push(...categoryData.items.beginner);
          break;
        case 'intermediate':
          cards.push(...categoryData.items.intermediate);
          break;
        case 'advanced':
          cards.push(...categoryData.items.advanced);
          break;
        default:
          break;
      }
    });

    return cards.length > 0 ? cards : categoryData.items.all;
  }

  shuffleCards(cards: Flashcard[]): Flashcard[] {
    const shuffled = [...cards];
    for (let i = shuffled.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
    }
    return shuffled;
  }

  filterCardsBySearch(cards: Flashcard[], searchTerm: string): Flashcard[] {
    if (!searchTerm.trim()) return cards;

    const term = searchTerm.toLowerCase();
    return cards.filter(card => {
      const basicMatch =
        card.english.toLowerCase().includes(term) ||
        card.kikuyu.toLowerCase().includes(term) ||
        (card.context && card.context.toLowerCase().includes(term));

      const categoryMatch = card.categories?.some(cat => cat.toLowerCase().includes(term)) || false;
      const tagsMatch = card.tags?.some(tag => tag.toLowerCase().includes(term)) || false;
      const subcategoryMatch = card.subcategory?.toLowerCase().includes(term) || false;
      const culturalNotesMatch = card.cultural_notes?.toLowerCase().includes(term) || false;
      const notesMatch = card.notes?.toLowerCase().includes(term) || false;

      return basicMatch || categoryMatch || tagsMatch || subcategoryMatch || culturalNotesMatch || notesMatch;
    });
  }

  getTotalCardCount(): number {
    return this.allData.size;
  }
}

export const dataLoader = new DataLoader();
