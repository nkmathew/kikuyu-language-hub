import subprocess
import sys
import os
import time
from rich.console import Console
from rich.progress import Progress

console = Console()

def build_exe():
    script = 'paste-into-file.py'
    if not os.path.exists(script):
        console.print(f"[bold red]{script} not found.[/bold red]")
        return
    start = time.time()
    cmd = [sys.executable, '-m', 'PyInstaller', '--onefile', '--windowed', script]
    with Progress() as progress:
        task = progress.add_task("[cyan]Building executable...", total=1)
        subprocess.run(cmd)
        progress.update(task, advance=1)
    end = time.time()
    elapsed = end - start
    mins = int(elapsed // 60)
    secs = int(elapsed % 60)
    console.print("[bold green]Build complete. Check the 'dist' folder for the .exe file.[/bold green]")
    console.print(f"[yellow]Build time: {mins} min {secs} sec[/yellow]")

if __name__ == '__main__':
    build_exe()