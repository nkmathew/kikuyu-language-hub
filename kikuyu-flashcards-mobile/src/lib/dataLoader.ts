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
  'conjugations/easy_kikuyu_batch_006_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_006_conjugations.json'),
  'conjugations/easy_kikuyu_batch_007_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_007_conjugations.json'),
  'conjugations/easy_kikuyu_batch_008_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_008_conjugations.json'),
  'conjugations/easy_kikuyu_batch_009_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_009_conjugations.json'),
  'conjugations/easy_kikuyu_batch_010_conjugations.json': require('../assets/data/curated/conjugations/easy_kikuyu_batch_010_conjugations.json'),

  // Cultural
  'cultural/easy_kikuyu_batch_001_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_001_cultural.json'),
  'cultural/easy_kikuyu_batch_002_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_002_cultural.json'),
  'cultural/easy_kikuyu_batch_003_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_003_cultural.json'),
  'cultural/easy_kikuyu_batch_004_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_004_cultural.json'),
  'cultural/easy_kikuyu_batch_005_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_005_cultural.json'),
  'cultural/easy_kikuyu_batch_006_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_006_cultural.json'),
  'cultural/easy_kikuyu_batch_007_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_007_cultural.json'),
  'cultural/easy_kikuyu_batch_008_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_008_cultural.json'),
  'cultural/easy_kikuyu_batch_009_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_009_cultural.json'),
  'cultural/easy_kikuyu_batch_010_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_010_cultural.json'),
  'cultural/easy_kikuyu_batch_011_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_011_cultural.json'),
  'cultural/easy_kikuyu_batch_012_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_012_cultural.json'),
  'cultural/easy_kikuyu_batch_013_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_013_cultural.json'),
  'cultural/easy_kikuyu_batch_014_cultural.json': require('../assets/data/curated/cultural/easy_kikuyu_batch_014_cultural.json'),

  // Grammar
  'grammar/easy_kikuyu_batch_001_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_001_grammar.json'),
  'grammar/easy_kikuyu_batch_002_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_002_grammar.json'),
  'grammar/easy_kikuyu_batch_003_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_003_grammar.json'),
  'grammar/easy_kikuyu_batch_004_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_004_grammar.json'),
  'grammar/easy_kikuyu_batch_005_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_005_grammar.json'),
  'grammar/easy_kikuyu_batch_006_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_006_grammar.json'),
  'grammar/easy_kikuyu_batch_043_grammar.json': require('../assets/data/curated/grammar/easy_kikuyu_batch_043_grammar.json'),

  // Phrases
  'phrases/easy_kikuyu_batch_002_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_002_phrases.json'),
  'phrases/easy_kikuyu_batch_003_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_003_phrases.json'),
  'phrases/easy_kikuyu_batch_004_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_004_phrases.json'),
  'phrases/easy_kikuyu_batch_005_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_005_phrases.json'),
  'phrases/easy_kikuyu_batch_006_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_006_phrases.json'),
  'phrases/easy_kikuyu_batch_007_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_007_phrases.json'),
  'phrases/easy_kikuyu_batch_008_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_008_phrases.json'),
  'phrases/easy_kikuyu_batch_009_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_009_phrases.json'),
  'phrases/easy_kikuyu_batch_010_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_010_phrases.json'),
  'phrases/easy_kikuyu_batch_011_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_011_phrases.json'),
  'phrases/easy_kikuyu_batch_012_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_012_phrases.json'),
  'phrases/easy_kikuyu_batch_013_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_013_phrases.json'),
  'phrases/easy_kikuyu_batch_014_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_014_phrases.json'),
  'phrases/easy_kikuyu_batch_015_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_015_phrases.json'),
  'phrases/easy_kikuyu_batch_016_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_016_phrases.json'),
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
  'phrases/easy_kikuyu_batch_028_phrases.json': require('../assets/data/curated/phrases/easy_kikuyu_batch_028_phrases.json'),

  // Proverbs
  'proverbs/easy_kikuyu_batch_001_proverbs.json': require('../assets/data/curated/proverbs/easy_kikuyu_batch_001_proverbs.json'),
  'proverbs/easy_kikuyu_batch_002_proverbs.json': require('../assets/data/curated/proverbs/easy_kikuyu_batch_002_proverbs.json'),
  'proverbs/easy_kikuyu_batch_003_proverbs.json': require('../assets/data/curated/proverbs/easy_kikuyu_batch_003_proverbs.json'),
  'proverbs/easy_kikuyu_batch_004_proverbs.json': require('../assets/data/curated/proverbs/easy_kikuyu_batch_004_proverbs.json'),
  'proverbs/easy_kikuyu_batch_005_proverbs.json': require('../assets/data/curated/proverbs/easy_kikuyu_batch_005_proverbs.json'),
  'proverbs/easy_kikuyu_batch_006_proverbs.json': require('../assets/data/curated/proverbs/easy_kikuyu_batch_006_proverbs.json'),

  // Vocabulary
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
  'vocabulary/easy_kikuyu_batch_027_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_027_vocab.json'),
  'vocabulary/easy_kikuyu_batch_050_vocab.json': require('../assets/data/curated/vocabulary/easy_kikuyu_batch_050_vocab.json'),
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
