const fs = require('fs');
const path = require('path');

// Find all JSON files in curated directory
function findJsonFiles(dir, fileList = []) {
  const files = fs.readdirSync(dir);

  files.forEach(file => {
    const filePath = path.join(dir, file);
    const stat = fs.statSync(filePath);

    if (stat.isDirectory()) {
      findJsonFiles(filePath, fileList);
    } else if (file.endsWith('.json') && file !== 'schema.json') {
      fileList.push(filePath);
    }
  });

  return fileList;
}

const curatedDir = path.join(__dirname, 'src', 'assets', 'data', 'curated');
const jsonFiles = findJsonFiles(curatedDir);

// Generate require statements grouped by category
const requireStatements = {};

jsonFiles.forEach(filePath => {
  const relativePath = path.relative(curatedDir, filePath).replace(/\\/g, '/');
  const category = relativePath.split('/')[0];

  if (!requireStatements[category]) {
    requireStatements[category] = [];
  }

  requireStatements[category].push(relativePath);
});

// Sort each category
Object.keys(requireStatements).forEach(category => {
  requireStatements[category].sort();
});

// Generate the TypeScript code
let output = `import { CategoryData, Flashcard, CuratedContent, CategoryType } from '../types/flashcard';

// Import all curated content files explicitly
// This ensures they are bundled in production builds
const curatedData = {
`;

// Add each category
Object.keys(requireStatements).sort().forEach(category => {
  output += `  // ${category.charAt(0).toUpperCase() + category.slice(1)}\n`;

  requireStatements[category].forEach(filePath => {
    output += `  '${filePath}': require('../assets/data/curated/${filePath}'),\n`;
  });

  output += '\n';
});

output += `};

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
        this.allData.set(\`./\${key}\`, data as CuratedContent);
      } catch (error) {
        console.error(\`Error loading \${key}:\`, error);
      }
    });

    console.log(\`Loaded \${this.allData.size} data files\`);
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
        const categoryPath = \`./\${category}/\`;
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
      console.error(\`Error loading category \${category}:\`, error);
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
          console.error(\`Failed to load \${category}:\`, error);
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
`;

// Write the generated file
fs.writeFileSync(path.join(__dirname, 'src', 'lib', 'dataLoader.ts'), output, 'utf8');

console.log(`✅ Generated dataLoader.ts with ${jsonFiles.length} files`);
console.log(`\nFiles per category:`);
Object.keys(requireStatements).sort().forEach(category => {
  console.log(`  ${category}: ${requireStatements[category].length} files`);
});
