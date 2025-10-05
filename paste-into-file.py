import tkinter as tk
from tkinter import ttk, filedialog
import os
import json
from tkinter import messagebox

CONFIG_DIR = os.path.join(os.path.expanduser('~'), '.config')
CONFIG_PATH = os.path.join(CONFIG_DIR, 'paste_config.json')

def load_folder():
    if os.path.exists(CONFIG_PATH):
        try:
            with open(CONFIG_PATH, 'r', encoding='utf-8') as f:
                data = json.load(f)
                return data.get('save_folder')
        except Exception:
            pass
    return os.path.join(os.path.dirname(__file__), "raw-data", "easy-kikuyu")

def save_folder(path):
    os.makedirs(CONFIG_DIR, exist_ok=True)
    with open(CONFIG_PATH, 'w', encoding='utf-8') as f:
        json.dump({'save_folder': path}, f)

save_folder_path = load_folder()

def get_next_filename(directory):
    os.makedirs(directory, exist_ok=True)
    files = [f for f in os.listdir(directory) if f.endswith('.txt')]
    numbers = []
    for f in files:
        try:
            num = int(os.path.splitext(f)[0])
            numbers.append(num)
        except ValueError:
            continue
    next_num = max(numbers) + 1 if numbers else 1
    return os.path.join(directory, f"{next_num}.txt")


def choose_folder():
    global save_folder_path
    folder = filedialog.askdirectory(initialdir=save_folder_path)
    if folder:
        save_folder_path = folder
        save_folder(folder)
        folder_var.set(f"Save folder: {folder}")


def update_preview():
    try:
        text = root.clipboard_get()
    except Exception:
        text = ""
    preview_var.set(text if text else "(Clipboard empty)")


def paste_clipboard():
    text = root.clipboard_get()
    if not text.strip():
        messagebox.showwarning("Empty Clipboard", "Clipboard is empty or not text.")
        return
    filename = get_next_filename(save_folder_path)
    with open(filename, "w", encoding="utf-8") as f:
        f.write(text)
    messagebox.showinfo("Saved", f"Saved to {filename}")
    update_preview()


root = tk.Tk()
root.title("Paste into File")

# Center window on start
def center_window(win, width=600, height=400):
    win.update_idletasks()
    screen_width = win.winfo_screenwidth()
    screen_height = win.winfo_screenheight()
    x = (screen_width // 2) - (width // 2)
    y = (screen_height // 2) - (height // 2)
    win.geometry(f"{width}x{height}+{x}+{y}")

center_window(root)

# Dark theme
style = ttk.Style(root)
style.theme_use("clam")
style.configure("TButton", foreground="white", background="#333333")
style.configure("TLabel", foreground="white", background="#222222")
root.configure(bg="#222222")

main_frame = ttk.Frame(root, padding=20)
main_frame.pack(expand=True, fill="both")
main_frame.configure(style="TFrame")
style.configure("TFrame", background="#222222")

folder_var = tk.StringVar(value=f"Save folder: {save_folder_path}")
folder_label = ttk.Label(main_frame, textvariable=folder_var, style="TLabel")
folder_label.pack(pady=(0, 5))
choose_btn = ttk.Button(main_frame, text="Choose Folder", command=choose_folder)
choose_btn.pack(pady=(0, 10))

paste_btn = ttk.Button(main_frame, text="Paste", command=paste_clipboard, width=20)
paste_btn.pack(pady=(0, 10))

preview_var = tk.StringVar()
preview_label = ttk.Label(main_frame, textvariable=preview_var, wraplength=350, anchor="center", justify="center", style="TLabel")
preview_label.pack(fill="both", expand=True)

update_preview()
root.after(1000, update_preview)  # Update preview every second

root.mainloop()