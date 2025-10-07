import { CategoryData, Flashcard, CategoryType, CuratedContent } from '@/types/flashcard';

class DataLoader {
  private cache: Map<string, CategoryData> = new Map();
  private curatedCache: Map<string, CuratedContent> = new Map();
  
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
    
    // Try to load curated content first
    try {
      const curatedData = await this.loadCuratedCategory(category);
      if (curatedData) {
        this.cache.set(category, curatedData);
        return curatedData;
      }
    } catch (error) {
      console.log(`No curated content for ${category}, falling back to original data`);
    }
    
    // Fall back to original paginated format
    try {
      const response = await fetch(`/data/${category}.json`);
      if (!response.ok) {
        throw new Error(`Failed to load ${category} data`);
      }
      
      const data: CategoryData = await response.json();
      this.cache.set(category, data);
      return data;
    } catch (error) {
      console.error(`Error loading ${category} data:`, error);
      throw error;
    }
  }
  
  private async loadCuratedCategory(category: CategoryType): Promise<CategoryData | null> {
    const curatedFiles: Record<CategoryType, string[]> = {
      vocabulary: ['vocabulary/easy_kikuyu_household_items.json', 'vocabulary/easy_kikuyu_animals.json'],
      conjugations: ['conjugations/wiktionary_basic_verbs.json'],
      proverbs: ['proverbs/easy_kikuyu_wisdom.json'],
      grammar: [],
      general: []
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
    const categories: CategoryType[] = ['vocabulary', 'proverbs', 'conjugations', 'grammar', 'general'];
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
}

export const dataLoader = new DataLoader();