import { CategoryData, Flashcard, CuratedContent, CategoryType } from '../types/flashcard';

// Import bundled data files
import batch001Vocab from '../assets/data/curated/vocabulary/easy_kikuyu_batch_001_vocab.json';
import batch002Phrases from '../assets/data/curated/phrases/easy_kikuyu_batch_002_phrases.json';
import batch002Grammar from '../assets/data/curated/grammar/easy_kikuyu_batch_002_grammar.json';

class DataLoader {
  private cache: Map<string, CategoryData> = new Map();

  // Map of bundled data files
  private bundledData: Record<string, Record<string, CuratedContent>> = {
    vocabulary: {
      'vocabulary/easy_kikuyu_batch_001_vocab.json': batch001Vocab as CuratedContent,
    },
    phrases: {
      'phrases/easy_kikuyu_batch_002_phrases.json': batch002Phrases as CuratedContent,
    },
    grammar: {
      'grammar/easy_kikuyu_batch_002_grammar.json': batch002Grammar as CuratedContent,
    },
    conjugations: {},
    proverbs: {},
    general: {},
  };

  async loadCategory(category: CategoryType): Promise<CategoryData> {
    if (this.cache.has(category)) {
      return this.cache.get(category)!;
    }

    try {
      const categoryFiles = this.bundledData[category];
      const allCards: Flashcard[] = [];

      // Load all files for this category
      for (const [path, content] of Object.entries(categoryFiles)) {
        const cards = content.flashcards || content.entries || [];
        allCards.push(...cards);
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

      return basicMatch || categoryMatch || tagsMatch || subcategoryMatch || culturalNotesMatch;
    });
  }
}

export const dataLoader = new DataLoader();
