# PowerShell script to copy curated content from backend to Android assets
# This script copies all JSON files from backend/curated-content to android-kikuyuflashcards/app/src/main/assets/curated-content

Write-Host "Copying curated content files to Android assets..." -ForegroundColor Green

# Source directory (backend/curated-content)
$sourceDir = "../backend/curated-content"

# Target directory (Android assets)
$targetDir = "app/src/main/assets/curated-content"

# Check if source directory exists
if (-not (Test-Path $sourceDir)) {
    Write-Host "Error: Source directory $sourceDir not found!" -ForegroundColor Red
    exit 1
}

# Create target directory if it doesn't exist
if (-not (Test-Path $targetDir)) {
    New-Item -ItemType Directory -Path $targetDir -Force
    Write-Host "Created target directory: $targetDir" -ForegroundColor Yellow
}

# Function to copy files recursively
function Copy-CuratedFiles {
    param(
        [string]$SourcePath,
        [string]$TargetPath
    )
    
    $items = Get-ChildItem -Path $SourcePath -Recurse -File -Filter "*.json"
    
    foreach ($item in $items) {
        # Get relative path from source
        $relativePath = $item.FullName.Substring($SourcePath.Length + 1)
        
        # Create target file path
        $targetFilePath = Join-Path $TargetPath $relativePath
        
        # Create target directory if it doesn't exist
        $targetFileDir = Split-Path $targetFilePath -Parent
        if (-not (Test-Path $targetFileDir)) {
            New-Item -ItemType Directory -Path $targetFileDir -Force | Out-Null
        }
        
        # Copy the file
        Copy-Item -Path $item.FullName -Destination $targetFilePath -Force
        Write-Host "Copied: $relativePath" -ForegroundColor Cyan
    }
}

# Copy all curated content files
Copy-CuratedFiles -SourcePath $sourceDir -TargetPath $targetDir

Write-Host "`nCurated content copy completed!" -ForegroundColor Green
Write-Host "Files copied to: $targetDir" -ForegroundColor Yellow

# List the copied files
Write-Host "`nCopied files:" -ForegroundColor Blue
Get-ChildItem -Path $targetDir -Recurse -File -Filter "*.json" | ForEach-Object {
    $relativePath = $_.FullName.Substring((Resolve-Path $targetDir).Path.Length + 1)
    Write-Host "  $relativePath" -ForegroundColor White
}

Write-Host "`nTotal files copied: $((Get-ChildItem -Path $targetDir -Recurse -File -Filter '*.json').Count)" -ForegroundColor Green
