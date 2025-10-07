import subprocess
import sys
import os
import time
from rich.console import Console
from rich.panel import Panel

console = Console()

def build_exe():
    script = 'paste-into-file.py'
    if not os.path.exists(script):
        console.print(Panel(f"{script} not found.", style="bold red"))
        return
    start = time.time()
    console.print(Panel("Starting build...", style="cyan"))
    cmd = [sys.executable, '-m', 'PyInstaller', '--onefile', '--windowed', script]
    subprocess.run(cmd)
    end = time.time()
    elapsed = end - start
    mins = int(elapsed // 60)
    secs = int(elapsed % 60)
    console.print(Panel("Build complete. Check the 'dist' folder for the .exe file.", style="bold green"))
    console.print(Panel(f"Build time: {mins} min {secs} sec", style="yellow"))

if __name__ == '__main__':
    build_exe()