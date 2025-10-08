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
        'vocabulary/easy_kikuyu_batch_001_vocab.json',
        'vocabulary/easy_kikuyu_batch_002_vocab.json',
        'vocabulary/easy_kikuyu_batch_003_vocab.json',
        'vocabulary/easy_kikuyu_batch_004_vocab.json',
        'vocabulary/easy_kikuyu_batch_005_vocab.json',
        'vocabulary/easy_kikuyu_batch_006_vocab.json',
        'vocabulary/easy_kikuyu_batch_007_vocab.json',
        'vocabulary/easy_kikuyu_batch_008_vocab.json',
        'vocabulary/easy_kikuyu_batch_009_vocab.json',
        'vocabulary/easy_kikuyu_batch_010_vocab.json',
        'vocabulary/easy_kikuyu_batch_011_vocab.json',
        'vocabulary/easy_kikuyu_batch_012_vocab.json',
        'vocabulary/easy_kikuyu_batch_013_vocab.json',
        'vocabulary/easy_kikuyu_batch_014_vocab.json',
        'vocabulary/easy_kikuyu_batch_015_vocab.json',
        'vocabulary/easy_kikuyu_batch_016_vocab.json',
        'vocabulary/easy_kikuyu_batch_017_vocab.json',
        'vocabulary/easy_kikuyu_batch_018_vocab.json',
        'vocabulary/easy_kikuyu_batch_019_vocab.json',
        'vocabulary/easy_kikuyu_batch_020_vocab.json',
        'vocabulary/easy_kikuyu_batch_021_vocab.json',
        'vocabulary/easy_kikuyu_batch_022_vocab.json',
        'vocabulary/easy_kikuyu_batch_023_vocab.json',
        'vocabulary/easy_kikuyu_batch_024_vocab.json',
        'vocabulary/easy_kikuyu_batch_025_vocab.json',
        'vocabulary/easy_kikuyu_batch_026_vocab.json',
        'vocabulary/easy_kikuyu_batch_027_vocab.json',
        'vocabulary/easy_kikuyu_batch_028_vocab.json',
        'vocabulary/easy_kikuyu_batch_029_vocab.json',
        'vocabulary/easy_kikuyu_batch_030_vocab.json',
        'vocabulary/easy_kikuyu_batch_031_vocab.json',
        'vocabulary/easy_kikuyu_batch_032_vocab.json',
        'vocabulary/easy_kikuyu_batch_033_vocab.json',
        'vocabulary/easy_kikuyu_batch_034_vocab.json',
        'vocabulary/easy_kikuyu_batch_035_vocab.json',
        'vocabulary/easy_kikuyu_batch_036_vocab.json',
        'vocabulary/easy_kikuyu_batch_037_vocab.json',
        'vocabulary/easy_kikuyu_batch_038_vocab.json',
        'vocabulary/easy_kikuyu_batch_039_vocab.json',
        'vocabulary/easy_kikuyu_batch_040_vocab.json',
        'vocabulary/easy_kikuyu_batch_041_vocab.json',
        'vocabulary/easy_kikuyu_batch_042_vocab.json',
        'vocabulary/easy_kikuyu_batch_043_vocab.json',
        'vocabulary/easy_kikuyu_batch_044_vocab.json',
        'vocabulary/easy_kikuyu_batch_045_vocab.json',
        'vocabulary/easy_kikuyu_batch_046_vocab.json',
        'vocabulary/easy_kikuyu_batch_047_vocab.json',
        'vocabulary/easy_kikuyu_batch_048_vocab.json',
        'vocabulary/easy_kikuyu_batch_049_vocab.json',
        'vocabulary/easy_kikuyu_batch_050_vocab.json'
      ],
      conjugations: [
        'conjugations/wiktionary_basic_verbs.json',
        'conjugations/easy_kikuyu_batch_001_conjugations.json',
        'conjugations/easy_kikuyu_batch_002_conjugations.json',
        'conjugations/easy_kikuyu_batch_004_conjugations.json',
        'conjugations/easy_kikuyu_batch_005_conjugations.json',
        'conjugations/easy_kikuyu_batch_007_conjugations.json',
        'conjugations/easy_kikuyu_batch_009_conjugations.json',
        'conjugations/easy_kikuyu_batch_012_conjugations.json',
        'conjugations/easy_kikuyu_batch_013_conjugations.json',
        'conjugations/easy_kikuyu_batch_014_conjugations.json',
        'conjugations/easy_kikuyu_batch_017_conjugations.json',
        'conjugations/easy_kikuyu_batch_019_conjugations.json',
        'conjugations/easy_kikuyu_batch_020_conjugations.json',
        'conjugations/easy_kikuyu_batch_021_conjugations.json',
        'conjugations/easy_kikuyu_batch_022_conjugations.json',
        'conjugations/easy_kikuyu_batch_023_conjugations.json',
        'conjugations/easy_kikuyu_batch_024_conjugations.json',
        'conjugations/easy_kikuyu_batch_025_conjugations.json',
        'conjugations/easy_kikuyu_batch_026_conjugations.json',
        'conjugations/easy_kikuyu_batch_037_conjugations.json',
        'conjugations/easy_kikuyu_batch_038_conjugations.json',
        'conjugations/easy_kikuyu_batch_039_conjugations.json',
        'conjugations/easy_kikuyu_batch_040_conjugations.json',
        'conjugations/easy_kikuyu_batch_041_conjugations.json',
        'conjugations/easy_kikuyu_batch_042_conjugations.json',
        'conjugations/easy_kikuyu_batch_043_conjugations.json',
        'conjugations/easy_kikuyu_batch_044_conjugations.json',
        'conjugations/easy_kikuyu_batch_045_conjugations.json',
        'conjugations/easy_kikuyu_batch_046_conjugations.json',
        'conjugations/easy_kikuyu_batch_047_conjugations.json',
        'conjugations/easy_kikuyu_batch_048_conjugations.json',
        'conjugations/easy_kikuyu_batch_049_conjugations.json',
        'conjugations/easy_kikuyu_batch_050_conjugations.json'
      ],
      proverbs: [
        'proverbs/easy_kikuyu_wisdom.json',
        'proverbs/easy_kikuyu_batch_001_proverbs.json',
        'proverbs/easy_kikuyu_batch_009_proverbs.json',
        'proverbs/easy_kikuyu_batch_014_proverbs.json',
        'proverbs/easy_kikuyu_batch_015_proverbs.json',
        'proverbs/easy_kikuyu_batch_016_proverbs.json',
        'proverbs/easy_kikuyu_batch_017_proverbs.json',
        'proverbs/easy_kikuyu_batch_018_proverbs.json',
        'proverbs/easy_kikuyu_batch_022_proverbs.json',
        'proverbs/easy_kikuyu_batch_024_proverbs.json',
        'proverbs/easy_kikuyu_batch_037_proverbs.json',
        'proverbs/easy_kikuyu_batch_038_proverbs.json',
        'proverbs/easy_kikuyu_batch_039_proverbs.json',
        'proverbs/easy_kikuyu_batch_040_proverbs.json',
        'proverbs/easy_kikuyu_batch_041_proverbs.json',
        'proverbs/easy_kikuyu_batch_042_proverbs.json'
      ],
      grammar: [
        'grammar/easy_kikuyu_batch_002_grammar.json',
        'grammar/easy_kikuyu_batch_003_grammar.json',
        'grammar/easy_kikuyu_batch_004_grammar.json',
        'grammar/easy_kikuyu_batch_005_grammar.json',
        'grammar/easy_kikuyu_batch_007_grammar.json',
        'grammar/easy_kikuyu_batch_008_grammar.json',
        'grammar/easy_kikuyu_batch_009_grammar.json',
        'grammar/easy_kikuyu_batch_010_grammar.json',
        'grammar/easy_kikuyu_batch_012_grammar.json',
        'grammar/easy_kikuyu_batch_013_grammar.json',
        'grammar/easy_kikuyu_batch_014_grammar.json',
        'grammar/easy_kikuyu_batch_016_grammar.json',
        'grammar/easy_kikuyu_batch_017_grammar.json',
        'grammar/easy_kikuyu_batch_018_grammar.json',
        'grammar/easy_kikuyu_batch_019_grammar.json',
        'grammar/easy_kikuyu_batch_020_grammar.json',
        'grammar/easy_kikuyu_batch_021_grammar.json',
        'grammar/easy_kikuyu_batch_022_grammar.json',
        'grammar/easy_kikuyu_batch_023_grammar.json',
        'grammar/easy_kikuyu_batch_024_grammar.json',
        'grammar/easy_kikuyu_batch_025_grammar.json',
        'grammar/easy_kikuyu_batch_027_grammar.json',
        'grammar/easy_kikuyu_batch_038_grammar.json',
        'grammar/easy_kikuyu_batch_039_grammar.json',
        'grammar/easy_kikuyu_batch_040_grammar.json',
        'grammar/easy_kikuyu_batch_041_grammar.json',
        'grammar/easy_kikuyu_batch_043_grammar.json'
      ],
      general: [], // Will be populated with all categories combined
      phrases: [
        'phrases/common_greetings.json',
        'phrases/easy_kikuyu_batch_002_phrases.json',
        'phrases/easy_kikuyu_batch_003_phrases.json',
        'phrases/easy_kikuyu_batch_004_phrases.json',
        'phrases/easy_kikuyu_batch_005_phrases.json',
        'phrases/easy_kikuyu_batch_006_phrases.json',
        'phrases/easy_kikuyu_batch_008_phrases.json',
        'phrases/easy_kikuyu_batch_009_phrases.json',
        'phrases/easy_kikuyu_batch_010_phrases.json',
        'phrases/easy_kikuyu_batch_011_phrases.json',
        'phrases/easy_kikuyu_batch_015_phrases.json',
        'phrases/easy_kikuyu_batch_017_phrases.json',
        'phrases/easy_kikuyu_batch_018_phrases.json',
        'phrases/easy_kikuyu_batch_019_phrases.json',
        'phrases/easy_kikuyu_batch_020_phrases.json',
        'phrases/easy_kikuyu_batch_021_phrases.json',
        'phrases/easy_kikuyu_batch_022_phrases.json',
        'phrases/easy_kikuyu_batch_023_phrases.json',
        'phrases/easy_kikuyu_batch_024_phrases.json',
        'phrases/easy_kikuyu_batch_025_phrases.json',
        'phrases/easy_kikuyu_batch_026_phrases.json',
        'phrases/easy_kikuyu_batch_027_phrases.json',
        'phrases/easy_kikuyu_batch_037_phrases.json',
        'phrases/easy_kikuyu_batch_038_phrases.json',
        'phrases/easy_kikuyu_batch_039_phrases.json',
        'phrases/easy_kikuyu_batch_040_phrases.json',
        'phrases/easy_kikuyu_batch_041_phrases.json',
        'phrases/easy_kikuyu_batch_042_phrases.json',
        'phrases/easy_kikuyu_batch_043_phrases.json',
        'phrases/easy_kikuyu_batch_044_phrases.json',
        'phrases/easy_kikuyu_batch_045_phrases.json',
        'phrases/easy_kikuyu_batch_046_phrases.json',
        'phrases/easy_kikuyu_batch_047_phrases.json',
        'phrases/easy_kikuyu_batch_048_phrases.json',
        'phrases/easy_kikuyu_batch_049_phrases.json',
        'phrases/easy_kikuyu_batch_050_phrases.json'
      ],
      cultural: [
        'cultural/easy_kikuyu_batch_040_cultural.json',
        'cultural/easy_kikuyu_batch_044_cultural.json',
        'cultural/easy_kikuyu_batch_045_cultural.json',
        'cultural/easy_kikuyu_batch_046_cultural.json',
        'cultural/easy_kikuyu_batch_047_cultural.json',
        'cultural/easy_kikuyu_batch_048_cultural.json',
        'cultural/easy_kikuyu_batch_049_cultural.json',
        'cultural/easy_kikuyu_batch_050_cultural.json'
      ]
    };

    // Special handling for 'general' - combine all categories
    if (category === 'general') {
      const allFiles: string[] = [
        ...curatedFiles.vocabulary,
        ...curatedFiles.conjugations,
        ...curatedFiles.proverbs,
        ...curatedFiles.grammar,
        ...curatedFiles.phrases
      ];
      const filePaths = allFiles;

      if (filePaths.length === 0) {
        return null;
      }

      try {
        const allCards: Flashcard[] = [];

        for (const filePath of filePaths) {
          const curatedContent = await this.loadCuratedContent(filePath);
          // Handle both formats: batch_info/flashcards or metadata/entries
          const cards = curatedContent.flashcards || curatedContent.entries || [];

          // Enrich each card with batch-level source metadata
          const enrichedCards = cards.map(card => ({
            ...card,
            source: {
              origin: curatedContent.source || card.source?.origin || 'Easy Kikuyu',
              attribution: curatedContent.author || card.source?.attribution,
              created_date: curatedContent.created_date || card.source?.created_date,
              last_updated: curatedContent.last_updated || card.source?.last_updated
            }
          }));

          allCards.push(...enrichedCards);
        }

        // Convert curated content to CategoryData format
        const beginnerCards = allCards.filter(card => card.difficulty === 'beginner');
        const intermediateCards = allCards.filter(card => card.difficulty === 'intermediate');
        const advancedCards = allCards.filter(card => card.difficulty === 'advanced');

        return {
          category: 'general',
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
        console.error(`Error loading general content:`, error);
        return null;
      }
    }

    const filePaths = curatedFiles[category];
    if (!filePaths || filePaths.length === 0) {
      return null;
    }
    
    try {
      const allCards: Flashcard[] = [];

      for (const filePath of filePaths) {
        const curatedContent = await this.loadCuratedContent(filePath);
        // Handle both formats: batch_info/flashcards or metadata/entries
        const cards = curatedContent.flashcards || curatedContent.entries || [];

        // Enrich each card with batch-level source metadata
        const enrichedCards = cards.map(card => ({
          ...card,
          source: {
            origin: curatedContent.source || card.source?.origin || 'Easy Kikuyu',
            attribution: curatedContent.author || card.source?.attribution,
            created_date: curatedContent.created_date || card.source?.created_date,
            last_updated: curatedContent.last_updated || card.source?.last_updated
          }
        }));

        allCards.push(...enrichedCards);
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