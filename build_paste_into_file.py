import subprocess
import sys
import os
import time

def build_exe():
    script = 'paste-into-file.py'
    if not os.path.exists(script):
        print(f"{script} not found.")
        return
    start = time.time()
    cmd = [sys.executable, '-m', 'PyInstaller', '--onefile', '--windowed', script]
    subprocess.run(cmd)
    end = time.time()
    elapsed = end - start
    mins = int(elapsed // 60)
    secs = int(elapsed % 60)
    print(f"Build complete. Check the 'dist' folder for the .exe file.")
    print(f"Build time: {mins} min {secs} sec")

if __name__ == '__main__':
    build_exe()