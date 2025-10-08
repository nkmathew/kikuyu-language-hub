# PowerShell script to sync curated data from web app to mobile app
# Usage: .\sync-mobile-data.ps1

Write-Host "🔄 Syncing curated data from web app to mobile app..." -ForegroundColor Green

# Copy all curated data from web app to mobile app
Write-Host "📁 Copying curated data files..." -ForegroundColor Yellow
xcopy flashcards-app\public\data\curated\* kikuyu-flashcards-mobile\src\assets\data\curated\ /E /Y

# Count files copied
$vocabFiles = (Get-ChildItem kikuyu-flashcards-mobile\src\assets\data\curated\vocabulary\ -Name "*.json").Count
$phraseFiles = (Get-ChildItem kikuyu-flashcards-mobile\src\assets\data\curated\phrases\ -Name "*.json").Count
$culturalFiles = (Get-ChildItem kikuyu-flashcards-mobile\src\assets\data\curated\cultural\ -Name "*.json").Count
$conjugationFiles = (Get-ChildItem kikuyu-flashcards-mobile\src\assets\data\curated\conjugations\ -Name "*.json").Count
$proverbFiles = (Get-ChildItem kikuyu-flashcards-mobile\src\assets\data\curated\proverbs\ -Name "*.json").Count
$grammarFiles = (Get-ChildItem kikuyu-flashcards-mobile\src\assets\data\curated\grammar\ -Name "*.json").Count

$totalFiles = $vocabFiles + $phraseFiles + $culturalFiles + $conjugationFiles + $proverbFiles + $grammarFiles

Write-Host "✅ Sync complete!" -ForegroundColor Green
Write-Host "📊 Files synced:" -ForegroundColor Cyan
Write-Host "   📚 Vocabulary: $vocabFiles files" -ForegroundColor White
Write-Host "   💬 Phrases: $phraseFiles files" -ForegroundColor White
Write-Host "   🏛️ Cultural: $culturalFiles files" -ForegroundColor White
Write-Host "   🔄 Conjugations: $conjugationFiles files" -ForegroundColor White
Write-Host "   💭 Proverbs: $proverbFiles files" -ForegroundColor White
Write-Host "   📖 Grammar: $grammarFiles files" -ForegroundColor White
Write-Host "   📈 Total: $totalFiles files" -ForegroundColor Green

Write-Host "🎉 Mobile app data is now up to date!" -ForegroundColor Green
