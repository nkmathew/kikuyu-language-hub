# PowerShell script to update curated content in Android assets
# This script copies all JSON files from backend/curated-content to android-kikuyuflashcards/app/src/main/assets/curated-content

Write-Host "Updating curated content in Android assets..." -ForegroundColor Green

# Source directory (backend/curated-content)
$sourceDir = "../backend/curated-content"

# Target directory (Android assets)
$targetDir = "app/src/main/assets/curated-content"

# Check if source directory exists
if (-not (Test-Path $sourceDir)) {
    Write-Host "Error: Source directory $sourceDir not found!" -ForegroundColor Red
    exit 1
}

# Remove existing curated content
if (Test-Path $targetDir) {
    Write-Host "Removing existing curated content..." -ForegroundColor Yellow
    Remove-Item -Recurse -Force $targetDir
}

# Create target directory
New-Item -ItemType Directory -Path $targetDir -Force | Out-Null

# Copy all curated content files using robocopy
Write-Host "Copying curated content files..." -ForegroundColor Cyan
robocopy $sourceDir $targetDir "*.json" /S

# Count copied files
$fileCount = (Get-ChildItem -Path $targetDir -Recurse -File -Filter "*.json").Count

Write-Host "`nCurated content update completed!" -ForegroundColor Green
Write-Host "Files copied to: $targetDir" -ForegroundColor Yellow
Write-Host "Total files copied: $fileCount" -ForegroundColor Green

# Show directory structure
Write-Host "`nDirectory structure:" -ForegroundColor Blue
Get-ChildItem -Path $targetDir -Directory | ForEach-Object {
    $subDirCount = (Get-ChildItem -Path $_.FullName -File -Filter "*.json").Count
    Write-Host "  $($_.Name)/ ($subDirCount files)" -ForegroundColor White
}
