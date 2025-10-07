import { CategoryData, Flashcard, CategoryType, CuratedContent } from '@/types/flashcard';
import { DataValidator } from './dataValidation';

class DataLoader {
  private cache: Map<string, CategoryData> = new Map();
  private curatedCache: Map<string, CuratedContent> = new Map();
  private validationEnabled: boolean = true;
  
  async loadCuratedContent(filePath: string): Promise<CuratedContent> {
    if (this.curatedCache.has(filePath)) {
      return this.curatedCache.get(filePath)!;
    }
    
    try {
      const response = await fetch(`/data/curated/${filePath}`);
      if (!response.ok) {
        throw new Error(`Failed to load curated content: ${filePath}`);
      }
      
      const data: CuratedContent = await response.json();
      this.curatedCache.set(filePath, data);
      return data;
    } catch (error) {
      console.error(`Error loading curated content ${filePath}:`, error);
      throw error;
    }
  }
  
  async loadCategory(category: CategoryType): Promise<CategoryData> {
    if (this.cache.has(category)) {
      return this.cache.get(category)!;
    }
    
    // Load curated content only
    try {
      const curatedData = await this.loadCuratedCategory(category);
      if (curatedData) {
        this.cache.set(category, curatedData);
        return curatedData;
      }
      
      // No curated content available for this category
      console.log(`No curated content available for ${category}`);
      const emptyData: CategoryData = {
        category,
        total_count: 0,
        difficulty_counts: { beginner: 0, intermediate: 0, advanced: 0 },
        items: { beginner: [], intermediate: [], advanced: [], all: [] }
      };
      this.cache.set(category, emptyData);
      return emptyData;
    } catch (error) {
      console.error(`Error loading curated content for ${category}:`, error);
      throw new Error(`Failed to load ${category} data`);
    }
  }
  
  private async loadCuratedCategory(category: CategoryType): Promise<CategoryData | null> {
    const curatedFiles: Record<CategoryType, string[]> = {
      vocabulary: [
        'vocabulary/easy_kikuyu_household_items.json',
        'vocabulary/easy_kikuyu_animals.json',
        'vocabulary/easy_kikuyu_batch_001_vocab.json'
      ],
      conjugations: [
        'conjugations/wiktionary_basic_verbs.json',
        'conjugations/easy_kikuyu_batch_001_conjugations.json'
      ],
      proverbs: [
        'proverbs/easy_kikuyu_wisdom.json',
        'proverbs/easy_kikuyu_batch_001_proverbs.json'
      ],
      grammar: [],
      general: ['cultural/easy_kikuyu_batch_001_cultural.json'],
      phrases: ['phrases/common_greetings.json']
    };
    
    const filePaths = curatedFiles[category];
    if (!filePaths || filePaths.length === 0) {
      return null;
    }
    
    try {
      const allCards: Flashcard[] = [];
      
      for (const filePath of filePaths) {
        const curatedContent = await this.loadCuratedContent(filePath);
        allCards.push(...curatedContent.entries);
      }
      
      // Convert curated content to CategoryData format
      const beginnerCards = allCards.filter(card => card.difficulty === 'beginner');
      const intermediateCards = allCards.filter(card => card.difficulty === 'intermediate');
      const advancedCards = allCards.filter(card => card.difficulty === 'advanced');
      
      return {
        category,
        total_count: allCards.length,
        difficulty_counts: {
          beginner: beginnerCards.length,
          intermediate: intermediateCards.length,
          advanced: advancedCards.length
        },
        items: {
          beginner: beginnerCards,
          intermediate: intermediateCards,
          advanced: advancedCards,
          all: allCards
        }
      };
    } catch (error) {
      console.error(`Error loading curated content for ${category}:`, error);
      return null;
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
          // Create empty category data as fallback
          results[category] = {
            category,
            total_count: 0,
            difficulty_counts: { beginner: 0, intermediate: 0, advanced: 0 },
            items: { beginner: [], intermediate: [], advanced: [], all: [] }
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
      // Search in basic fields
      const basicMatch = 
        card.english.toLowerCase().includes(term) ||
        card.kikuyu.toLowerCase().includes(term) ||
        card.context.toLowerCase().includes(term);
      
      // Search in legacy categories field
      const categoryMatch = card.categories?.some(cat => cat.toLowerCase().includes(term)) || false;
      
      // Search in new schema fields
      const tagsMatch = card.tags?.some(tag => tag.toLowerCase().includes(term)) || false;
      const subcategoryMatch = card.subcategory?.toLowerCase().includes(term) || false;
      const culturalNotesMatch = card.cultural_notes?.toLowerCase().includes(term) || false;
      
      return basicMatch || categoryMatch || tagsMatch || subcategoryMatch || culturalNotesMatch;
    });
  }

  /**
   * Apply data validation to filter out problematic cards
   */
  validateAndFilterCards(cards: Flashcard[], strictMode: boolean = false): {
    validCards: Flashcard[];
    invalidCards: Flashcard[];
    stats: ReturnType<typeof DataValidator.getQualityStats>;
  } {
    if (!this.validationEnabled) {
      return {
        validCards: cards,
        invalidCards: [],
        stats: DataValidator.getQualityStats(cards)
      };
    }

    const result = DataValidator.filterValidCards(cards, strictMode);
    const stats = DataValidator.getQualityStats(cards);

    console.log(`Data validation: ${result.validCards.length}/${cards.length} cards passed validation`);
    if (result.invalidCards.length > 0) {
      console.log(`${result.invalidCards.length} cards filtered out due to quality issues`);
    }

    return {
      ...result,
      stats
    };
  }

  /**
   * Set validation mode
   */
  setValidationEnabled(enabled: boolean): void {
    this.validationEnabled = enabled;
  }

  /**
   * Get quality statistics for a category
   */
  async getCategoryQualityStats(category: CategoryType): Promise<ReturnType<typeof DataValidator.getQualityStats>> {
    const categoryData = await this.loadCategory(category);
    return DataValidator.getQualityStats(categoryData.items.all);
  }

  /**
   * Enhanced category loader with validation
   */
  async loadCategoryWithValidation(category: CategoryType, strictMode: boolean = false): Promise<CategoryData> {
    const categoryData = await this.loadCategory(category);
    
    if (!this.validationEnabled) {
      return categoryData;
    }

    // Apply validation to each difficulty level
    const validateDifficulty = (cards: Flashcard[]) => {
      const { validCards } = this.validateAndFilterCards(cards, strictMode);
      return validCards;
    };

    const validatedItems = {
      beginner: validateDifficulty(categoryData.items.beginner),
      intermediate: validateDifficulty(categoryData.items.intermediate),
      advanced: validateDifficulty(categoryData.items.advanced),
      all: validateDifficulty(categoryData.items.all)
    };

    return {
      ...categoryData,
      items: validatedItems,
      total_count: validatedItems.all.length,
      difficulty_counts: {
        beginner: validatedItems.beginner.length,
        intermediate: validatedItems.intermediate.length,
        advanced: validatedItems.advanced.length
      }
    };
  }
}

export const dataLoader = new DataLoader();