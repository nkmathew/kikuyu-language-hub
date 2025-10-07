import { CategoryData, Flashcard, CategoryType } from '@/types/flashcard';

class DataLoader {
  private cache: Map<string, CategoryData> = new Map();
  
  async loadCategory(category: CategoryType): Promise<CategoryData> {
    if (this.cache.has(category)) {
      return this.cache.get(category)!;
    }
    
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
    return cards.filter(card => 
      card.english.toLowerCase().includes(term) ||
      card.kikuyu.toLowerCase().includes(term) ||
      card.context.toLowerCase().includes(term) ||
      card.categories.some(cat => cat.toLowerCase().includes(term))
    );
  }
}

export const dataLoader = new DataLoader();