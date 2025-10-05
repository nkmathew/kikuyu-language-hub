import tkinter as tk
from tkinter import ttk
import os
from tkinter import messagebox

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
    directory = os.path.join(os.path.dirname(__file__), "raw-data", "easy-kikuyu")
    filename = get_next_filename(directory)
    with open(filename, "w", encoding="utf-8") as f:
        f.write(text)
    messagebox.showinfo("Saved", f"Saved to {filename}")
    update_preview()


root = tk.Tk()
root.title("Paste into File")

# Center window
window_width = 400
window_height = 200
screen_width = root.winfo_screenwidth()
screen_height = root.winfo_screenheight()
x = (screen_width // 2) - (window_width // 2)
y = (screen_height // 2) - (window_height // 2)
root.geometry(f"{window_width}x{window_height}+{x}+{y}")

# Dark theme
style = ttk.Style(root)
style.theme_use("clam")
style.configure("TButton", foreground="white", background="#333333")
style.configure("TLabel", foreground="white", background="#222222")
root.configure(bg="#222222")

main_frame = ttk.Frame(root, padding=20)
main_frame.pack(expand=True)

paste_btn = ttk.Button(main_frame, text="Paste", command=paste_clipboard, width=20)
paste_btn.pack(pady=(0, 10))

preview_var = tk.StringVar()
preview_label = ttk.Label(main_frame, textvariable=preview_var, wraplength=350, anchor="center", justify="center")
preview_label.pack(fill="x")

update_preview()
root.after(1000, update_preview)  # Update preview every second

root.mainloop()