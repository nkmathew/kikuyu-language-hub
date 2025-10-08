@echo off
REM Sync curated content from backend (source of truth) to other apps
REM This ensures consistency across all projects

set BACKEND_SOURCE=backend\curated-content
set NEXTJS_TARGET=flashcards-app\public\data\curated
set MOBILE_TARGET=kikuyu-flashcards-mobile\src\assets\data\curated

echo 🔄 Syncing curated content from backend to other apps...
echo.

REM Sync all category directories
for %%c in (conjugations cultural grammar phrases proverbs vocabulary) do (
    if exist "%BACKEND_SOURCE%\%%c" (
        echo   📁 Syncing %%c...

        REM Sync to Next.js app
        if not exist "%NEXTJS_TARGET%\%%c" mkdir "%NEXTJS_TARGET%\%%c"
        xcopy /E /Y /I /Q "%BACKEND_SOURCE%\%%c" "%NEXTJS_TARGET%\%%c" > nul

        REM Sync to React Native mobile app
        if not exist "%MOBILE_TARGET%\%%c" mkdir "%MOBILE_TARGET%\%%c"
        xcopy /E /Y /I /Q "%BACKEND_SOURCE%\%%c" "%MOBILE_TARGET%\%%c" > nul
    )
)

REM Sync schema file
if exist "%BACKEND_SOURCE%\schema.json" (
    echo   📄 Syncing schema.json...
    copy /Y "%BACKEND_SOURCE%\schema.json" "%NEXTJS_TARGET%\schema.json" > nul
    copy /Y "%BACKEND_SOURCE%\schema.json" "%MOBILE_TARGET%\schema.json" > nul
)

REM Sync markdown documentation files
for %%d in (CURATION_SUMMARY.md EASY_KIKUYU_BATCH_001.md EASY_KIKUYU_PROGRESS.md) do (
    if exist "%BACKEND_SOURCE%\%%d" (
        echo   📝 Syncing %%d...
        copy /Y "%BACKEND_SOURCE%\%%d" "%NEXTJS_TARGET%\%%d" > nul
        copy /Y "%BACKEND_SOURCE%\%%d" "%MOBILE_TARGET%\%%d" > nul
    )
)

echo.
echo ✅ Sync complete!
echo.
echo 📊 Summary:
echo   Source: %BACKEND_SOURCE%
echo   Targets:
echo     - %NEXTJS_TARGET%
echo     - %MOBILE_TARGET%
echo.
