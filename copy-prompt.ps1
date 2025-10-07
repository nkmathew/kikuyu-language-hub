$files = git status --porcelain | Select-String ".txt" | ForEach-Object {
    "@" + ($_.Line -split '\s+')[1]
}

$text = @"

Get translations from these files and add to seed:

$($files -join "`n")

"@

$text | Set-Clipboard
Write-Host $text
