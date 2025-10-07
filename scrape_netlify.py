import asyncio
import httpx
from bs4 import BeautifulSoup
from urllib.parse import urljoin, urlparse
import json
from rich.console import Console
from rich import print
import os
import time

console = Console()

start_url = "https://learn-kikuyu.netlify.app/"
visited = set()
results = []

async def scrape(url, client):
    if url in visited:
        return
    visited.add(url)
    console.print(f"[bold blue]Visiting:[/bold blue] {url}")

    try:
        r = await client.get(url, timeout=10)
        r.raise_for_status()
    except Exception as e:
        console.print(f"[bold red]Failed to fetch {url}: {e}[/bold red]")
        return

    soup = BeautifulSoup(r.text, "html.parser")

    # improved card extraction
    for card in soup.select(".card"):
        card_data = {"url": url}
        # image src
        img = card.select_one(".card-image img")
        card_data["image_src"] = urljoin(url, img["src"].strip()) if img and img.get("src") else None
        # columns
        columns = card.select(".columns .column")
        if len(columns) >= 2:
            # English word
            eng_p = columns[0].select_one(".mb-1 p")
            card_data["english"] = eng_p.get_text(strip=True) if eng_p else None
            # Kikuyu word
            kikuyu_p = columns[0].select_one(".pt-5 p")
            card_data["kikuyu"] = kikuyu_p.get_text(strip=True) if kikuyu_p else None
            # audio src
            audio = columns[1].find("audio")
            source = audio.find("source") if audio else None
            card_data["audio_src"] = urljoin(url, source["src"].strip()) if source and source.get("src") else None
        # card link (if any)
        a = card.find("a")
        card_data["link"] = urljoin(url, a["href"]) if a and a.get("href") else None
        results.append(card_data)
        console.print(f"[green]Extracted card:[/green] {card_data}")

    # schedule new links
    tasks = []
    for a in soup.find_all("a", href=True):
        next_url = urljoin(url, a["href"])
        if urlparse(next_url).netloc == urlparse(start_url).netloc:
            if next_url not in visited:
                console.print(f"[yellow]Scheduling link:[/yellow] {next_url}")
                tasks.append(scrape(next_url, client))
    await asyncio.gather(*tasks)

async def main():
  start_time = time.time()
  try:
    async with httpx.AsyncClient(follow_redirects=True) as client:
      await scrape(start_url, client)
    dest = 'raw-data/learn-kikuyu.netlify.app.json'
    dest_dir = os.path.dirname(dest)
    if dest_dir and not os.path.exists(dest_dir):
      os.makedirs(dest_dir, exist_ok=True)
    try:
      with open(dest, "w", encoding="utf-8") as f:
        json.dump(results, f, ensure_ascii=False, indent=2)
      console.print(f"[bold magenta]Scraped {len(results)} card contents into {dest}[/bold magenta]")
    except Exception as file_err:
      console.print(f"[bold red]Failed to write results to {dest}: {file_err}[/bold red]")
  except Exception as err:
    console.print(f"[bold red]Fatal error in main: {err}[/bold red]")
  elapsed = time.time() - start_time
  console.print(f"[bold cyan]Elapsed time: {elapsed:.2f} seconds[/bold cyan]")

if __name__ == "__main__":
    asyncio.run(main())
