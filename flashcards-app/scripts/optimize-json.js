const fs = require('fs');
const path = require('path');

const DATA_DIR = path.join(__dirname, '../public/data');
const PAGE_SIZE = 100; // Cards per page

function splitJsonByPages(category) {
  const filePath = path.join(DATA_DIR, `${category}.json`);
  
  if (!fs.existsSync(filePath)) {
    console.log(`File ${category}.json not found, skipping...`);
    return;
  }
  
  const data = JSON.parse(fs.readFileSync(filePath, 'utf8'));
  const allCards = data.items.all || [];
  
  if (allCards.length === 0) {
    console.log(`No cards found in ${category}.json`);
    return;
  }
  
  // Create paginated directory
  const pagesDir = path.join(DATA_DIR, 'pages', category);
  if (!fs.existsSync(pagesDir)) {
    fs.mkdirSync(pagesDir, { recursive: true });
  }
  
  // Split into pages
  const totalPages = Math.ceil(allCards.length / PAGE_SIZE);
  
  for (let page = 0; page < totalPages; page++) {
    const startIndex = page * PAGE_SIZE;
    const endIndex = Math.min(startIndex + PAGE_SIZE, allCards.length);
    const pageCards = allCards.slice(startIndex, endIndex);
    
    const pageData = {
      page,
      totalPages,
      totalCards: allCards.length,
      startIndex,
      endIndex: endIndex - 1,
      cards: pageCards
    };
    
    fs.writeFileSync(
      path.join(pagesDir, `page-${page}.json`),
      JSON.stringify(pageData, null, 2),
      'utf8'
    );
  }
  
  // Create metadata file
  const metaData = {
    category: data.category,
    total_count: data.total_count,
    difficulty_counts: data.difficulty_counts,
    page_size: PAGE_SIZE,
    total_pages: totalPages,
    pages: Array.from({ length: totalPages }, (_, i) => ({
      page: i,
      file: `page-${i}.json`,
      start: i * PAGE_SIZE,
      end: Math.min((i + 1) * PAGE_SIZE - 1, allCards.length - 1),
      count: Math.min(PAGE_SIZE, allCards.length - (i * PAGE_SIZE))
    }))
  };
  
  fs.writeFileSync(
    path.join(pagesDir, 'meta.json'),
    JSON.stringify(metaData, null, 2),
    'utf8'
  );
  
  console.log(`✅ Split ${category}: ${allCards.length} cards into ${totalPages} pages of ${PAGE_SIZE} cards each`);
  
  // Calculate total size
  const originalSize = fs.statSync(filePath).size;
  let newTotalSize = 0;
  for (let i = 0; i < totalPages; i++) {
    newTotalSize += fs.statSync(path.join(pagesDir, `page-${i}.json`)).size;
  }
  newTotalSize += fs.statSync(path.join(pagesDir, 'meta.json')).size;
  
  console.log(`   Original: ${(originalSize / 1024).toFixed(1)}KB → Paginated: ${(newTotalSize / 1024).toFixed(1)}KB`);
  console.log(`   Typical page load: ${(fs.statSync(path.join(pagesDir, 'page-0.json')).size / 1024).toFixed(1)}KB`);
}

function createLightweightIndex() {
  const categories = ['vocabulary', 'proverbs', 'conjugations', 'grammar', 'general'];
  const index = {};
  
  categories.forEach(category => {
    const filePath = path.join(DATA_DIR, `${category}.json`);
    if (fs.existsSync(filePath)) {
      const data = JSON.parse(fs.readFileSync(filePath, 'utf8'));
      index[category] = {
        total_count: data.total_count,
        difficulty_counts: data.difficulty_counts,
        has_pages: fs.existsSync(path.join(DATA_DIR, 'pages', category, 'meta.json'))
      };
    }
  });
  
  fs.writeFileSync(
    path.join(DATA_DIR, 'index.json'),
    JSON.stringify(index, null, 2),
    'utf8'
  );
  
  console.log('✅ Created lightweight index.json');
}

// Main execution
console.log('🚀 Starting JSON optimization for performance...');

// Create pages directory
const pagesDir = path.join(DATA_DIR, 'pages');
if (!fs.existsSync(pagesDir)) {
  fs.mkdirSync(pagesDir, { recursive: true });
}

// Split large files into pages
const categoriesToOptimize = ['vocabulary', 'proverbs', 'conjugations'];
categoriesToOptimize.forEach(splitJsonByPages);

// Create index
createLightweightIndex();

console.log('✅ JSON optimization complete!');
console.log('');
console.log('Performance benefits:');
console.log('- Initial page load only requires index.json (~1KB)');
console.log('- Study sessions load one page at a time (~20-30KB)');
console.log('- Users can browse without downloading entire datasets');
console.log('- Improved mobile performance on slower connections');