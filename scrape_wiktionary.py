import asyncio
import httpx
from bs4 import BeautifulSoup
from urllib.parse import urljoin, urlparse
import os
from rich.console import Console
from rich import print
import re
import random

console = Console()

start_url = "https://en.wiktionary.org/wiki/Category:Kikuyu_verbs"
visited = set()

async def fetch_kikuyu_section(url, client, word):
    console.print(f"[cyan]Fetching URL:[/cyan] {url} for word '{word}'")
    try:
        r = await client.get(url, timeout=10)
        console.print(f"[magenta]Status code for {url}:[/magenta] {r.status_code}")
        console.print(f"[magenta]Response length for {url}:[/magenta] {len(r.text)}")
        preview = r.text[:200].replace('\n', ' ')
        console.print(f"[magenta]Response preview for {url}:[/magenta] {preview}")
        r.raise_for_status()
    except Exception as e:
        console.print(f"[bold red]Failed to fetch {url}: {e}[/bold red]")
        return None
    soup = BeautifulSoup(r.text, "html.parser")
    console.print(f"[cyan]Parsing HTML for word '{word}'[/cyan]")
    # Find Kikuyu section
    h2 = None
    for tag in soup.find_all("h2"):
        if tag.get_text(strip=True) == "Kikuyu":
            h2 = tag
            break
    if not h2:
        console.print(f"[yellow]No Kikuyu section found for {word}[/yellow]")
        return None
    # Get parent div of h2 (section wrapper)
    section_div = h2.find_parent("div", class_="mw-heading mw-heading2")
    if not section_div:
        console.print(f"[yellow]No section wrapper found for Kikuyu in {word}[/yellow]")
        return None
    console.print(f"[green]Found Kikuyu section for {word}[/green]")
    # Collect siblings of section_div until next mw-heading2 div
    content = []
    for sib in section_div.find_next_siblings():
        if sib.name == "div" and "mw-heading2" in sib.get("class", []):
            break
        content.append(sib.get_text("\n", strip=True))
    merged = "\n\n".join(content).strip()
    console.print(f"[cyan]Extracted content for {word} (length: {len(merged)})[/cyan]")
    return merged

async def scrape():
    console.print(f"[bold cyan]Starting scrape from:[/bold cyan] {start_url}")
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"
    }
    async with httpx.AsyncClient(follow_redirects=True, headers=headers) as client:
        r = await client.get(start_url, timeout=10)
        console.print(f"[magenta]Status code for start URL:[/magenta] {r.status_code}")
        r.raise_for_status()
        soup = BeautifulSoup(r.text, "html.parser")
        console.print("[cyan]Finding all word links in the category...[/cyan]")
        # Find all word links in the category page (handle multiple structures)
        links = []
        mw_category = soup.find("div", class_="mw-category")
        if mw_category:
            links.extend(mw_category.find_all("a", href=re.compile(r"^/wiki/[^:]+$")))
        mw_pages = soup.find("div", id="mw-pages")
        if mw_pages:
            for group in mw_pages.find_all("div", class_="mw-category-group"):
                links.extend(group.find_all("a", href=re.compile(r"^/wiki/[^:]+$")))
        console.print(f"[cyan]Found {len(links)} word links.[/cyan]")
        tasks = []
        for a in links:
            href = urljoin(start_url, a["href"])
            word = a.get("title") or a.get_text(strip=True)
            if not word:
                console.print(f"[yellow]Skipping link with no word: {href}[/yellow]")
                continue
            if href in visited:
                console.print(f"[yellow]Already visited: {href}[/yellow]")
                continue
            visited.add(href)
            console.print(f"[blue]Queueing word:[/blue] {word} -> {href}")
            tasks.append((href, word))
        console.print(f"[bold cyan]Processing up to 1 word sequentially...[/bold cyan]")
        if tasks:
            href, word = tasks[0]
            await process_word(href, client, word)
        else:
            console.print("[yellow]No words to process.[/yellow]")

async def process_word(url, client, word):
    pause = 10 + random.random() * 5
    console.print(f"[yellow]Pausing for {pause:.1f} seconds before processing {word}[/yellow]")
    await asyncio.sleep(pause)
    console.print(f"[bold blue]Processing:[/bold blue] {word} -> {url}")
    text = await fetch_kikuyu_section(url, client, word)
    if text:
        dest_dir = f"raw-data/wiktionary/wiki-{word}"
        os.makedirs(dest_dir, exist_ok=True)
        dest = os.path.join(dest_dir, f"{word}.txt")
        console.print(f"[cyan]Saving content to:[/cyan] {dest}")
        try:
            with open(dest, "w", encoding="utf-8") as f:
                f.write(text)
            console.print(f"[green]Saved:[/green] {dest}")
        except Exception as e:
            console.print(f"[bold red]Failed to save {dest}: {e}[/bold red]")
    else:
        console.print(f"[yellow]No content for {word}[/yellow]")

if __name__ == "__main__":
    console.print("[bold cyan]Script started.[/bold cyan]")
    asyncio.run(scrape())
    console.print("[bold cyan]Script finished.[/bold cyan]")
