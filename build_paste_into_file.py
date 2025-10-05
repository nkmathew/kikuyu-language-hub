import subprocess
import sys
import os

def build_exe():
    script = 'paste-into-file.py'
    if not os.path.exists(script):
        print(f"{script} not found.")
        return
    cmd = [sys.executable, '-m', 'PyInstaller', '--onefile', '--windowed', script]
    subprocess.run(cmd)
    print("Build complete. Check the 'dist' folder for the .exe file.")

if __name__ == '__main__':
    build_exe()