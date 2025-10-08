#!/bin/bash

# Sync curated content from backend (source of truth) to other apps
# This ensures consistency across all projects

set -e

BACKEND_SOURCE="backend/curated-content"
NEXTJS_TARGET="flashcards-app/public/data/curated"
MOBILE_TARGET="kikuyu-flashcards-mobile/src/assets/data/curated"

echo "🔄 Syncing curated content from backend to other apps..."

# Function to sync directory
sync_dir() {
    local category=$1

    if [ -d "$BACKEND_SOURCE/$category" ]; then
        echo "  📁 Syncing $category..."

        # Sync to Next.js app
        mkdir -p "$NEXTJS_TARGET/$category"
        rsync -av --delete "$BACKEND_SOURCE/$category/" "$NEXTJS_TARGET/$category/"

        # Sync to React Native mobile app
        mkdir -p "$MOBILE_TARGET/$category"
        rsync -av --delete "$BACKEND_SOURCE/$category/" "$MOBILE_TARGET/$category/"
    fi
}

# Sync all category directories
for category in conjugations cultural grammar phrases proverbs vocabulary; do
    sync_dir "$category"
done

# Sync schema file
if [ -f "$BACKEND_SOURCE/schema.json" ]; then
    echo "  📄 Syncing schema.json..."
    cp "$BACKEND_SOURCE/schema.json" "$NEXTJS_TARGET/schema.json"
    cp "$BACKEND_SOURCE/schema.json" "$MOBILE_TARGET/schema.json"
fi

# Sync markdown documentation files
for doc in CURATION_SUMMARY.md EASY_KIKUYU_BATCH_001.md EASY_KIKUYU_PROGRESS.md; do
    if [ -f "$BACKEND_SOURCE/$doc" ]; then
        echo "  📝 Syncing $doc..."
        cp "$BACKEND_SOURCE/$doc" "$NEXTJS_TARGET/$doc"
        cp "$BACKEND_SOURCE/$doc" "$MOBILE_TARGET/$doc"
    fi
done

echo "✅ Sync complete!"
echo ""
echo "📊 Summary:"
echo "  Source: $BACKEND_SOURCE"
echo "  Targets:"
echo "    - $NEXTJS_TARGET"
echo "    - $MOBILE_TARGET"
