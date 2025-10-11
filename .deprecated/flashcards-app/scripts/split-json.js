const fs = require('fs');
const path = require('path');

const DATA_DIR = path.join(__dirname, '../public/data');
const CHUNK_SIZE = 50; // Cards per chunk

function splitCategoryJSON(category) {
  const filePath = path.join(DATA_DIR, `${category}.json`);
  
  if (!fs.existsSync(filePath)) {
    console.log(`File ${category}.json not found, skipping...`);
    return;
  }
  
  const data = JSON.parse(fs.readFileSync(filePath, 'utf8'));
  
  // Create chunked directory
  const chunkDir = path.join(DATA_DIR, 'chunks', category);
  if (!fs.existsSync(chunkDir)) {
    fs.mkdirSync(chunkDir, { recursive: true });
  }
  
  // Split each difficulty level into chunks
  const difficulties = ['beginner', 'intermediate', 'advanced'];
  const chunkInfo = {
    category: data.category,
    total_count: data.total_count,
    difficulty_counts: data.difficulty_counts,
    chunks: {}
  };
  
  difficulties.forEach(difficulty => {
    const cards = data.items[difficulty] || [];
    const chunks = [];
    
    for (let i = 0; i < cards.length; i += CHUNK_SIZE) {
      const chunk = cards.slice(i, i + CHUNK_SIZE);
      const chunkIndex = Math.floor(i / CHUNK_SIZE);
      const chunkFile = `${difficulty}_${chunkIndex}.json`;
      
      fs.writeFileSync(
        path.join(chunkDir, chunkFile),
        JSON.stringify(chunk, null, 2),
        'utf8'
      );
      
      chunks.push({
        file: chunkFile,
        count: chunk.length,
        startIndex: i,
        endIndex: i + chunk.length - 1
      });
    }
    
    chunkInfo.chunks[difficulty] = chunks;
  });
  
  // Save chunk info
  fs.writeFileSync(
    path.join(chunkDir, 'index.json'),
    JSON.stringify(chunkInfo, null, 2),
    'utf8'
  );
  
  console.log(`✅ Split ${category}: ${data.total_count} cards into ${Object.values(chunkInfo.chunks).flat().length} chunks`);
}

function createLightweightSummary() {
  const categories = ['vocabulary', 'proverbs', 'conjugations', 'grammar', 'general'];
  const summary = {};
  
  categories.forEach(category => {
    const filePath = path.join(DATA_DIR, `${category}.json`);
    if (fs.existsSync(filePath)) {
      const data = JSON.parse(fs.readFileSync(filePath, 'utf8'));
      summary[category] = {
        total_count: data.total_count,
        difficulty_counts: data.difficulty_counts
      };
    }
  });
  
  fs.writeFileSync(
    path.join(DATA_DIR, 'summary.json'),
    JSON.stringify(summary, null, 2),
    'utf8'
  );
  
  console.log('✅ Created lightweight summary.json');
}

// Main execution
console.log('🚀 Starting JSON optimization...');

// Create chunks directory
const chunksDir = path.join(DATA_DIR, 'chunks');
if (!fs.existsSync(chunksDir)) {
  fs.mkdirSync(chunksDir, { recursive: true });
}

// Split large files
const categoriesToSplit = ['vocabulary', 'proverbs', 'conjugations', 'general'];
categoriesToSplit.forEach(splitCategoryJSON);

// Create summary
createLightweightSummary();

console.log('✅ JSON optimization complete!');
console.log('\nFile structure:');
console.log('- summary.json (lightweight category info)');
console.log('- chunks/[category]/index.json (chunk metadata)');
console.log('- chunks/[category]/[difficulty]_[index].json (card chunks)');