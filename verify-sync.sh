#!/bin/bash

# Verify that curated content is in sync across all apps

set -e

BACKEND_SOURCE="backend/curated-content"
NEXTJS_TARGET="flashcards-app/public/data/curated"
MOBILE_TARGET="kikuyu-flashcards-mobile/src/assets/data/curated"

echo "🔍 Verifying curated content sync..."
echo ""

ALL_SYNCED=true

# Function to check directory sync
check_dir() {
    local category=$1
    local backend_count=$(ls -1 "$BACKEND_SOURCE/$category/" 2>/dev/null | wc -l)
    local nextjs_count=$(ls -1 "$NEXTJS_TARGET/$category/" 2>/dev/null | wc -l)
    local mobile_count=$(ls -1 "$MOBILE_TARGET/$category/" 2>/dev/null | wc -l)

    echo "📁 $category:"
    echo "   Backend: $backend_count files"
    echo "   Next.js: $nextjs_count files"
    echo "   Mobile:  $mobile_count files"

    if [ "$backend_count" -eq "$nextjs_count" ] && [ "$backend_count" -eq "$mobile_count" ]; then
        echo "   ✅ In sync"
    else
        echo "   ❌ Out of sync!"
        ALL_SYNCED=false
    fi
    echo ""
}

# Check all category directories
for category in conjugations cultural grammar phrases proverbs vocabulary; do
    check_dir "$category"
done

# Summary
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if [ "$ALL_SYNCED" = true ]; then
    echo "✅ All content is in sync!"
    echo ""
    echo "📊 Total files per category:"
    for category in conjugations cultural grammar phrases proverbs vocabulary; do
        count=$(ls -1 "$BACKEND_SOURCE/$category/" 2>/dev/null | wc -l)
        printf "   %-15s %3d files\n" "$category:" "$count"
    done
    exit 0
else
    echo "❌ Content is out of sync!"
    echo ""
    echo "💡 Run sync script to fix:"
    echo "   ./sync-curated-content.bat  (Windows)"
    echo "   ./sync-curated-content.sh   (Linux/macOS)"
    exit 1
fi
