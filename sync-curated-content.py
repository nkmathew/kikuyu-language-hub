#!/usr/bin/env python3
"""
Sync curated content from backend (source of truth) to other apps.
This ensures consistency across all projects.
"""

import os
import sys
import shutil
from pathlib import Path
from datetime import datetime
from rich.console import Console
from rich.table import Table
from rich.panel import Panel
from rich.progress import Progress, BarColumn, TextColumn, TimeElapsedColumn
from rich import box

# Fix Windows console encoding
if sys.platform == "win32":
    sys.stdout.reconfigure(encoding='utf-8')

console = Console()

# Paths
BACKEND_SOURCE = Path("backend/curated-content")
NEXTJS_TARGET = Path("flashcards-app/public/data/curated")
MOBILE_TARGET = Path("kikuyu-flashcards-mobile/src/assets/data/curated")

# Categories to sync
CATEGORIES = ["conjugations", "cultural", "grammar", "phrases", "proverbs", "vocabulary"]
DOCS = ["CURATION_SUMMARY.md", "EASY_KIKUYU_BATCH_001.md", "EASY_KIKUYU_PROGRESS.md"]


def count_files(path: Path) -> dict:
    """Count files in each category."""
    counts = {}
    for category in CATEGORIES:
        cat_path = path / category
        if cat_path.exists():
            counts[category] = len(list(cat_path.glob("*.json")))
        else:
            counts[category] = 0
    return counts


def sync_directory(src: Path, dest: Path, name: str) -> int:
    """Sync a directory from source to destination."""
    if not src.exists():
        return 0

    # Create destination if it doesn't exist
    dest.mkdir(parents=True, exist_ok=True)

    # Remove all files in destination
    for item in dest.glob("*"):
        if item.is_file():
            item.unlink()
        elif item.is_dir():
            shutil.rmtree(item)

    # Copy all files from source
    files_copied = 0
    for item in src.glob("*"):
        if item.is_file():
            shutil.copy2(item, dest / item.name)
            files_copied += 1
        elif item.is_dir():
            shutil.copytree(item, dest / item.name)
            files_copied += len(list(item.rglob("*")))

    return files_copied


def sync_file(src: Path, dest_paths: list[Path]) -> bool:
    """Sync a single file to multiple destinations."""
    if not src.exists():
        return False

    for dest in dest_paths:
        dest.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dest)

    return True


def main():
    console.print()
    console.print(
        Panel.fit(
            "[bold cyan]Curated Content Sync[/bold cyan]\n"
            "[dim]Syncing from backend (source of truth) to other apps[/dim]",
            border_style="cyan"
        )
    )
    console.print()

    # Check if backend source exists
    if not BACKEND_SOURCE.exists():
        console.print("[bold red]✗[/bold red] Backend source directory not found!")
        console.print(f"  Expected: {BACKEND_SOURCE}")
        return 1

    # Count files before sync
    backend_counts = count_files(BACKEND_SOURCE)
    total_backend_files = sum(backend_counts.values())

    # Sync with progress
    with Progress(
        TextColumn("[progress.description]{task.description}"),
        BarColumn(),
        TextColumn("[progress.percentage]{task.percentage:>3.0f}%"),
        TimeElapsedColumn(),
        console=console,
    ) as progress:

        # Sync categories
        task = progress.add_task("[cyan]Syncing categories...", total=len(CATEGORIES))

        synced_categories = {}
        for category in CATEGORIES:
            src = BACKEND_SOURCE / category
            nextjs_dest = NEXTJS_TARGET / category
            mobile_dest = MOBILE_TARGET / category

            nextjs_count = sync_directory(src, nextjs_dest, f"Next.js/{category}")
            mobile_count = sync_directory(src, mobile_dest, f"Mobile/{category}")

            synced_categories[category] = {
                "backend": backend_counts[category],
                "nextjs": nextjs_count,
                "mobile": mobile_count,
            }

            progress.update(task, advance=1)

        # Sync schema
        progress.add_task("[cyan]Syncing schema.json...", total=None)
        schema_src = BACKEND_SOURCE / "schema.json"
        sync_file(schema_src, [NEXTJS_TARGET / "schema.json", MOBILE_TARGET / "schema.json"])

        # Sync documentation
        progress.add_task("[cyan]Syncing documentation...", total=None)
        docs_synced = 0
        for doc in DOCS:
            doc_src = BACKEND_SOURCE / doc
            if sync_file(doc_src, [NEXTJS_TARGET / doc, MOBILE_TARGET / doc]):
                docs_synced += 1

    console.print()
    console.print("[bold green]✓[/bold green] Sync complete!")
    console.print()

    # Create summary table
    table = Table(
        title="📊 Sync Summary",
        box=box.ROUNDED,
        header_style="bold cyan",
        show_lines=True,
    )

    table.add_column("Category", style="cyan", no_wrap=True)
    table.add_column("Backend\n(Source)", justify="right", style="yellow")
    table.add_column("Next.js\n(Synced)", justify="right", style="green")
    table.add_column("Mobile\n(Synced)", justify="right", style="green")
    table.add_column("Status", justify="center")

    all_synced = True
    for category, counts in synced_categories.items():
        backend = counts["backend"]
        nextjs = counts["nextjs"]
        mobile = counts["mobile"]

        if backend == nextjs == mobile:
            status = "[green]✓[/green]"
        else:
            status = "[red]✗[/red]"
            all_synced = False

        table.add_row(
            category.capitalize(),
            str(backend),
            str(nextjs),
            str(mobile),
            status,
        )

    # Add totals row
    total_nextjs = sum(c["nextjs"] for c in synced_categories.values())
    total_mobile = sum(c["mobile"] for c in synced_categories.values())

    table.add_section()
    table.add_row(
        "[bold]TOTAL[/bold]",
        f"[bold yellow]{total_backend_files}[/bold yellow]",
        f"[bold green]{total_nextjs}[/bold green]",
        f"[bold green]{total_mobile}[/bold green]",
        "[bold green]✓[/bold green]" if all_synced else "[bold red]✗[/bold red]",
    )

    console.print(table)
    console.print()

    # Additional files synced
    console.print(Panel(
        f"[cyan]schema.json[/cyan] - Synced to both apps\n"
        f"[cyan]{docs_synced} documentation files[/cyan] - Synced to both apps",
        title="Additional Files",
        border_style="dim",
    ))
    console.print()

    # Target directories info
    console.print("[dim]📁 Target directories:[/dim]")
    console.print(f"[dim]   • Next.js:  {NEXTJS_TARGET}[/dim]")
    console.print(f"[dim]   • Mobile:   {MOBILE_TARGET}[/dim]")
    console.print()

    # Final message
    if all_synced:
        console.print("[bold green]🎉 All content is in sync![/bold green]")
    else:
        console.print("[bold yellow]⚠️  Some categories may have sync issues[/bold yellow]")

    console.print()
    console.print(f"[dim]Synced at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}[/dim]")
    console.print()

    return 0 if all_synced else 1


if __name__ == "__main__":
    try:
        exit(main())
    except KeyboardInterrupt:
        console.print("\n[yellow]Sync cancelled by user[/yellow]")
        exit(1)
    except Exception as e:
        console.print(f"\n[bold red]Error:[/bold red] {e}")
        exit(1)
