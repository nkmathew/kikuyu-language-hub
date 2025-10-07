import tkinter as tk
from tkinter import ttk, filedialog
import os
import json
from tkinter import messagebox
import hashlib
import platform
import threading
import ctypes

if platform.system() == "Windows":
    import winsound

CONFIG_DIR = os.path.join(os.path.expanduser('~'), '.config')
CONFIG_PATH = os.path.join(CONFIG_DIR, 'paste_config.json')
HASHES_PATH = os.path.join(CONFIG_DIR, 'paste_hashes.json')

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

def load_hashes():
    if os.path.exists(HASHES_PATH):
        try:
            with open(HASHES_PATH, 'r', encoding='utf-8') as f:
                return set(json.load(f))
        except Exception:
            pass
    return set()

def save_hashes(hashes):
    os.makedirs(CONFIG_DIR, exist_ok=True)
    with open(HASHES_PATH, 'w', encoding='utf-8') as f:
        json.dump(list(hashes), f)

def hash_text(text):
    # Trim and encode before hashing
    return hashlib.sha256(text.strip().encode('utf-8')).hexdigest()

save_folder_path = load_folder()
hashes = load_hashes()

root = tk.Tk()
root.title("Paste into File")

current_index_var = tk.StringVar(value="Current index: ")
folder_var = tk.StringVar(value=f"Save folder: {save_folder_path}")
preview_var = tk.StringVar()
status_var = tk.StringVar(value="")

status_label = ttk.Label(root, textvariable=status_var, anchor="center", font=("Segoe UI", 13), style="TLabel")
status_label.pack(fill="x", pady=(5, 0))

def flash_status(text, color="#ff2222"):
    status_label.config(font=("Segoe UI", 16, "bold"))
    status_label.config(foreground=color)
    status_var.set(text)
    status_label.pack(fill="x", pady=(5, 0))
    def flash(count=0):
        if count < 6:
            status_label.config(foreground=color if count % 2 == 0 else "#222222")
            root.after(200, lambda: flash(count+1))
        else:
            status_label.config(foreground=color)
    flash()

def hide_status():
    status_var.set("")
    status_label.pack_forget()

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
    return os.path.join(directory, f"{next_num:03}.txt"), next_num

def get_file_count(directory):
    if not os.path.exists(directory):
        return 0
    return len([f for f in os.listdir(directory) if f.endswith('.txt')])

def get_line_count(directory):
    count = 0
    if not os.path.exists(directory):
        return 0
    for fname in os.listdir(directory):
        if fname.endswith('.txt'):
            with open(os.path.join(directory, fname), encoding='utf-8') as f:
                count += sum(1 for _ in f)
    return count

file_count_var = tk.StringVar(value=f"File count: {get_file_count(save_folder_path)}")
line_count_var = tk.StringVar(value="Line count: 0")

def choose_folder():
    global save_folder_path
    folder = filedialog.askdirectory(initialdir=save_folder_path)
    if folder:
        save_folder_path = folder
        save_folder(folder)
        folder_var.set(f"Save folder: {folder}")
        file_count_var.set(f"File count: {get_file_count(save_folder_path)}")
        line_count_var.set(f"Line count: {get_line_count(save_folder_path)}")


def update_preview():
    try:
        text = root.clipboard_get()
    except Exception:
        text = ""
    preview_text.config(state="normal")
    preview_text.delete("1.0", tk.END)
    preview_text.tag_delete("strike")
    preview_text.tag_delete("green")
    lines = text.strip().splitlines() if text else []
    line_count_var.set(f"Line count: {len(lines)}")
    hide_status()
    if text:
        trimmed_text = text.strip()
        h = hash_text(trimmed_text)
        if trimmed_text and h in hashes:
            preview_text.insert(tk.END, trimmed_text, ("left", "strike"))
            preview_text.config(bg="#2a2a2a")
            preview_text.tag_configure("strike", foreground="#ff2222", font=("Segoe UI", 11, "overstrike", "bold"))
        else:
            preview_text.insert(tk.END, text, ("left", "green"))
            preview_text.config(bg="#181818")
            preview_text.tag_configure("green", foreground="#44ff44", font=("Segoe UI", 11))
    else:
        preview_text.insert(tk.END, "(Clipboard empty)", "left")
        preview_text.config(bg="#2a2a2a")
    # Dynamically set height to fit text (max 20 lines)
    num_lines = max(1, min(20, len(lines) if lines else 1))
    preview_text.config(height=num_lines)
    preview_text.config(state="disabled")


def paste_clipboard():
    text = root.clipboard_get()
    trimmed_text = text.strip()
    if not trimmed_text:
        status_label.config(font=("Segoe UI", 13))
        status_label.config(foreground="#cccccc")
        status_var.set("Clipboard is empty or not text.")
        status_label.pack(fill="x", pady=(5, 0))
        return
    h = hash_text(trimmed_text)
    if h in hashes:
        flash_status("DUPLICATE!", color="#ff2222")
        preview_text.config(state="normal")
        preview_text.delete("1.0", tk.END)
        preview_text.config(bg="#2a2a2a")
        preview_text.config(state="disabled")
        line_count_var.set("Line count: 0")
        return
    filename, idx = get_next_filename(save_folder_path)
    with open(filename, "w", encoding="utf-8") as f:
        f.write(trimmed_text)
    hashes.add(h)
    save_hashes(hashes)
    if platform.system() == "Windows":
        winsound.MessageBeep(winsound.MB_OK)
    preview_text.config(state="normal")
    preview_text.delete("1.0", tk.END)
    preview_text.config(state="disabled")
    current_index_var.set(f"Current index: {idx:03}")
    file_count_var.set(f"File count: {get_file_count(save_folder_path)}")
    line_count_var.set("Line count: 0")
    status_label.config(font=("Segoe UI", 13))
    status_label.config(foreground="#44ff44")
    status_var.set("Saved successfully.")
    status_label.pack(fill="x", pady=(5, 0))
    update_preview()
    # Delay a second before minimizing
    root.after(1000, root.iconify)


def on_button_hover(event):
    event.widget.state(['active'])

def on_button_leave(event):
    event.widget.state(['!active'])


# Center window on start
def center_window(win, width=600, height=None):
    win.update_idletasks()
    screen_width = win.winfo_screenwidth()
    screen_height = win.winfo_screenheight()
    # Use 90% of screen height, top at y=0
    height = int(screen_height * 0.9)
    x = (screen_width // 2) - (width // 2)
    y = 0
    win.geometry(f"{width}x{height}+{x}+{y}")

center_window(root)

# Dark theme
style = ttk.Style(root)
style.theme_use("clam")
style.configure("TButton", foreground="white", background="#333333")
style.map("TButton",
    background=[("active", "#444444"), ("!active", "#333333")],
    foreground=[("active", "white"), ("!active", "white")]
)
style.configure("TLabel", foreground="white", background="#222222")
style.configure("Preview.TLabel", foreground="white", background="#181818", font=("Segoe UI", 11))
root.configure(bg="#222222")

main_frame = ttk.Frame(root, padding=(20, 0, 20, 20))  # Remove top padding
main_frame.pack(expand=True, fill="both")
main_frame.configure(style="TFrame")
style.configure("TFrame", background="#222222")

folder_label = ttk.Label(main_frame, textvariable=folder_var, style="TLabel")
folder_label.pack(fill="x", pady=(0, 5))  # No top margin

button_frame = ttk.Frame(main_frame, style="TFrame")
button_frame.pack(fill="x", pady=(0, 10))
choose_btn = ttk.Button(button_frame, text="Choose Folder", command=choose_folder, style="TButton")
choose_btn.pack(side="left", padx=(0, 10))
choose_btn.bind("<Enter>", on_button_hover)
choose_btn.bind("<Leave>", on_button_leave)
paste_btn = ttk.Button(button_frame, text="Write to File", command=paste_clipboard, width=20, style="TButton")
paste_btn.pack(side="left", padx=(0, 10))
paste_btn.bind("<Enter>", on_button_hover)
paste_btn.bind("<Leave>", on_button_leave)
current_index_label = ttk.Label(button_frame, textvariable=current_index_var, style="TLabel")
current_index_label.pack(side="left", padx=(0, 10))
file_count_label = ttk.Label(button_frame, textvariable=file_count_var, style="TLabel")
file_count_label.pack(side="left")
line_count_label = ttk.Label(button_frame, textvariable=line_count_var, style="TLabel")
line_count_label.pack(side="left", padx=(10, 0))

preview_frame = ttk.Frame(main_frame, style="TFrame")
preview_frame.pack(fill="both", expand=True)

# Remove scrollbar if not needed
# preview_scrollbar = tk.Scrollbar(preview_frame)
# preview_scrollbar.pack(side="right", fill="y")

preview_text = tk.Text(
    preview_frame,
    wrap="word",
    font=("Segoe UI", 11),
    bg="#181818",
    fg="white",
    borderwidth=4,  # Thicker border for raised effect
    relief="raised",  # Raised effect
    padx=10,
    pady=10,
    height=1
)
preview_text.pack(fill="x", expand=False)
preview_text.tag_configure("left", justify="left")

# Move status bar below preview box
status_var = tk.StringVar(value="")
status_label = ttk.Label(preview_frame, textvariable=status_var, anchor="center", font=("Segoe UI", 13), style="TLabel")
status_label.pack(fill="x", pady=(5, 0))

# Add click-to-refresh clipboard preview
def on_preview_click(event):
    update_preview()

preview_text.bind("<Button-1>", on_preview_click)

update_preview()
root.after(1000, update_preview)  # Update preview every second
root.bind('<FocusIn>', lambda e: update_preview())

def bring_window_to_front():
    root.deiconify()
    root.lift()
    root.focus_force()
    # On Windows, use ctypes to force focus and restore window
    if platform.system() == "Windows":
        try:
            root.update_idletasks()
            root_id = int(root.winfo_id())
            # Restore window if minimized
            SW_RESTORE = 9
            ctypes.windll.user32.ShowWindow(root_id, SW_RESTORE)
            ctypes.windll.user32.SetForegroundWindow(root_id)
            ctypes.windll.user32.BringWindowToTop(root_id)
        except Exception:
            pass

def poll_clipboard():
    try:
        text = root.clipboard_get()
        if isinstance(text, str):
            trimmed_text = text.strip()
            h = hash_text(trimmed_text)
            # Only activate if clipboard has non-empty text and is not duplicate
            if trimmed_text and h not in hashes:
                root.after(0, bring_window_to_front)
    except Exception:
        pass
    root.after(3000, poll_clipboard)

poll_clipboard()  # Start polling clipboard

root.mainloop()