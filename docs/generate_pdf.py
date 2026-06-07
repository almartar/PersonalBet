#!/usr/bin/env python3
"""
Genera PDFs bonics des de Markdown.
Ús: python generate_pdf.py

Mètode preferit: HTML + Chrome/Edge (impressió headless).
Alternativa: pandoc. Últim recurs: fpdf2.
"""

from __future__ import annotations

import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

DOCS = Path(__file__).resolve().parent

FILES = [
    ("Full_de_Resum_PersonalBet.md", "Full_de_Resum_PersonalBet.pdf"),
    ("Memoria_del_Projecte_PersonalBet.md", "Memoria_del_Projecte_PersonalBet.pdf"),
    ("Annex_Documentacio_Tecnica.md", "Annex_Documentacio_Tecnica.pdf"),
    ("Esquema_Presentacio.md", "Esquema_Presentacio.pdf"),
]

PRINT_CSS = """
@page { size: A4; margin: 22mm 20mm 24mm 20mm; }
* { box-sizing: border-box; }
body {
  font-family: "Segoe UI", Calibri, "Helvetica Neue", Arial, sans-serif;
  font-size: 11pt;
  line-height: 1.45;
  color: #1a1a1a;
  max-width: 100%;
  margin: 0;
  padding: 0;
  word-wrap: break-word;
  overflow-wrap: anywhere;
}
h1 {
  font-size: 20pt;
  color: #0f3460;
  border-bottom: 2px solid #0f3460;
  padding-bottom: 6px;
  margin: 0 0 14px 0;
  page-break-after: avoid;
}
h2 {
  font-size: 14pt;
  color: #16213e;
  margin: 22px 0 10px 0;
  page-break-after: avoid;
}
h3, h4 {
  font-size: 12pt;
  color: #1a1a2e;
  margin: 16px 0 8px 0;
  page-break-after: avoid;
}
p { margin: 0 0 10px 0; }
ul, ol { margin: 0 0 12px 0; padding-left: 22px; }
li { margin-bottom: 4px; }
strong { color: #0f3460; }
hr {
  border: none;
  border-top: 1px solid #ccc;
  margin: 18px 0;
}
table {
  width: 100%;
  border-collapse: collapse;
  margin: 12px 0 18px 0;
  font-size: 10pt;
  page-break-inside: avoid;
}
th, td {
  border: 1px solid #b8c5d6;
  padding: 8px 10px;
  text-align: left;
  vertical-align: top;
}
th { background: #e8eef5; font-weight: 600; }
tr:nth-child(even) td { background: #f7f9fc; }
code {
  font-family: Consolas, "Courier New", monospace;
  font-size: 9.5pt;
  background: #f0f4f8;
  padding: 1px 5px;
  border-radius: 3px;
}
pre {
  background: #f0f4f8;
  border: 1px solid #d0dae8;
  border-radius: 6px;
  padding: 12px 14px;
  font-size: 9pt;
  line-height: 1.35;
  overflow-x: auto;
  white-space: pre-wrap;
  page-break-inside: avoid;
}
.meta {
  background: #f0f4f8;
  border-left: 4px solid #0f3460;
  padding: 12px 16px;
  margin-bottom: 20px;
  font-size: 10.5pt;
}
.meta p { margin: 4px 0; }
blockquote {
  margin: 12px 0;
  padding: 10px 16px;
  border-left: 4px solid #94a3b8;
  background: #f8fafc;
  color: #334155;
  font-style: italic;
}
.cover-title { font-size: 24pt; margin-top: 40px; }
"""

SLIDES_CSS = PRINT_CSS + """
body { font-size: 12pt; }
.slide {
  page-break-after: always;
  min-height: 240mm;
  padding: 8mm 0 0 0;
  border-bottom: none;
}
.slide:last-child { page-break-after: auto; }
.slide h2 {
  font-size: 16pt;
  background: #0f3460;
  color: white;
  padding: 12px 16px;
  margin: 0 0 20px 0;
  border-radius: 4px;
}
.slide ul { font-size: 13pt; line-height: 1.6; }
.slide ul li { margin-bottom: 10px; }
.slide p { font-size: 11pt; color: #475569; }
.intro-note {
  font-size: 10pt;
  color: #64748b;
  margin-bottom: 24px;
  padding: 10px 14px;
  background: #f8fafc;
  border-radius: 6px;
}
"""


def md_to_html_body(md: str, slides_mode: bool = False) -> str:
    try:
        import markdown

        extensions = ["tables", "fenced_code", "nl2br", "sane_lists"]
        body = markdown.markdown(md, extensions=extensions)
    except ImportError:
        body = _simple_md_to_html(md)

    if slides_mode:
        body = _wrap_slides(body)
    return body


def _simple_md_to_html(md: str) -> str:
    """Fallback sense paquet markdown."""
    lines = md.splitlines()
    out: list[str] = []
    in_pre = False
    for line in lines:
        if line.strip().startswith("```"):
            if in_pre:
                out.append("</pre>")
                in_pre = False
            else:
                out.append("<pre>")
                in_pre = True
            continue
        if in_pre:
            out.append(line.replace("<", "&lt;"))
            continue
        if line.startswith("# "):
            out.append(f"<h1>{_inline(line[2:])}</h1>")
        elif line.startswith("## "):
            out.append(f"<h2>{_inline(line[3:])}</h2>")
        elif line.startswith("### "):
            out.append(f"<h3>{_inline(line[4:])}</h3>")
        elif line.strip() == "---":
            out.append("<hr>")
        elif line.startswith("- "):
            if not out or not out[-1].endswith("</li>"):
                if out and out[-1] != "<ul>":
                    out.append("<ul>")
            out.append(f"<li>{_inline(line[2:])}</li>")
        elif line.startswith("|"):
            continue
        elif line.strip():
            if out and out[-1] == "<ul>":
                pass
            elif out and out[-1].endswith("</li>"):
                out.append("</ul>")
            out.append(f"<p>{_inline(line)}</p>")
        else:
            if out and out[-1].endswith("</li>"):
                out.append("</ul>")
    if out and out[-1].endswith("</li>"):
        out.append("</ul>")
    return "\n".join(out)


def _inline(text: str) -> str:
    text = re.sub(r"\*\*(.+?)\*\*", r"<strong>\1</strong>", text)
    text = re.sub(r"`(.+?)`", r"<code>\1</code>", text)
    text = re.sub(r"\[([^\]]+)\]\([^)]+\)", r"\1", text)
    return text


def _wrap_slides(html: str) -> str:
    """Cada h2 = una diapositiva."""
    parts = re.split(r"(<h2[^>]*>.*?</h2>)", html, flags=re.DOTALL)
    if len(parts) <= 1:
        return f'<div class="slide">{html}</div>'
    slides: list[str] = []
    i = 0
    while i < len(parts):
        chunk = parts[i].strip()
        if chunk.startswith("<h2"):
            title = chunk
            content = parts[i + 1].strip() if i + 1 < len(parts) else ""
            slides.append(f'<div class="slide">{title}{content}</div>')
            i += 2
        elif chunk:
            slides.insert(0, f'<div class="intro-note">{chunk}</div>')
            i += 1
        else:
            i += 1
    return "\n".join(slides)


def _extract_meta(md: str) -> tuple[str, str]:
    """Línies **Camp:** valor després del títol (#) com a caixa meta."""
    lines = md.splitlines()
    meta_lines: list[str] = []
    rest: list[str] = []
    past_title = False
    for line in lines:
        s = line.strip()
        if s.startswith("# "):
            rest.append(line)
            past_title = True
            continue
        if past_title and s.startswith("**") and ":" in s:
            t = re.sub(r"\*\*", "", s).strip()
            k, _, v = t.partition(":")
            meta_lines.append(f"<p><strong>{k.strip()}:</strong> {v.strip()}</p>")
            continue
        if meta_lines and s in ("", "---"):
            if s == "---":
                rest.append(line)
            continue
        if meta_lines and not s.startswith("**"):
            rest.append(line)
        elif not meta_lines:
            rest.append(line)
    if not meta_lines:
        return "", md
    return '<div class="meta">' + "".join(meta_lines) + "</div>", "\n".join(rest)


def build_html(md_path: Path, slides_mode: bool = False) -> str:
    raw = md_path.read_text(encoding="utf-8")
    meta_html, body_md = _extract_meta(raw)
    body_html = md_to_html_body(body_md, slides_mode=slides_mode)
    css = SLIDES_CSS if slides_mode else PRINT_CSS
    title = md_path.stem.replace("_", " ")
    return f"""<!DOCTYPE html>
<html lang="ca">
<head>
  <meta charset="UTF-8">
  <title>{title}</title>
  <style>{css}</style>
</head>
<body>
{meta_html}
{body_html}
</body>
</html>"""


def _find_browser() -> list[str] | None:
    paths = [
        Path(os.environ.get("PROGRAMFILES", r"C:\Program Files"))
        / "Google/Chrome/Application/chrome.exe",
        Path(os.environ.get("PROGRAMFILES(X86)", r"C:\Program Files (x86)"))
        / "Google/Chrome/Application/chrome.exe",
        Path(os.environ.get("PROGRAMFILES", r"C:\Program Files"))
        / "Microsoft/Edge/Application/msedge.exe",
        Path(os.environ.get("PROGRAMFILES(X86)", r"C:\Program Files (x86)"))
        / "Microsoft/Edge/Application/msedge.exe",
    ]
    for p in paths:
        if p.is_file():
            return [str(p)]
    return None


def html_to_pdf_browser(html_path: Path, pdf_path: Path) -> bool:
    browser = _find_browser()
    if not browser:
        return False
    url = html_path.resolve().as_uri()
    cmd = browser + [
        "--headless=new",
        "--disable-gpu",
        "--no-pdf-header-footer",
        f"--print-to-pdf={pdf_path.resolve()}",
        url,
    ]
    try:
        subprocess.run(cmd, check=True, capture_output=True, timeout=60)
        return pdf_path.is_file() and pdf_path.stat().st_size > 1000
    except (subprocess.CalledProcessError, subprocess.TimeoutExpired, FileNotFoundError):
        return False


def generate_with_pandoc(md_path: Path, pdf_path: Path) -> bool:
    pandoc = shutil.which("pandoc")
    if not pandoc:
        return False
    for engine_args in (
        ["--pdf-engine=xelatex", "-V", "mainfont=Segoe UI"],
        [],
    ):
        cmd = [pandoc, str(md_path), "-o", str(pdf_path), "-V", "geometry:margin=2.5cm"] + engine_args
        try:
            subprocess.run(cmd, check=True, capture_output=True, text=True, timeout=120)
            return pdf_path.is_file()
        except (subprocess.CalledProcessError, subprocess.TimeoutExpired):
            continue
    return False


def generate_pdf(md_path: Path, pdf_path: Path) -> tuple[bool, str]:
    slides = "Presentacio" in md_path.name or "presentacio" in md_path.name
    html_path = md_path.with_suffix(".html")
    html_path.write_text(build_html(md_path, slides_mode=slides), encoding="utf-8")

    if generate_with_pandoc(md_path, pdf_path):
        return True, "pandoc"

    if html_to_pdf_browser(html_path, pdf_path):
        return True, "navegador (Chrome/Edge)"

    return False, "html"


def main() -> int:
    print("Generant documents a:", DOCS)
    try:
        import markdown  # noqa: F401
    except ImportError:
        print("Instal·lant markdown...")
        subprocess.run([sys.executable, "-m", "pip", "install", "markdown", "-q"], check=False)

    browser = _find_browser()
    if browser:
        print("Navegador per PDF:", browser[0])
    else:
        print("AVÍS: No s'ha trobat Chrome/Edge. Obre els .html i Ctrl+P → Guardar PDF.")

    ok = 0
    for md_name, pdf_name in FILES:
        md_path = DOCS / md_name
        pdf_path = DOCS / pdf_name
        if not md_path.exists():
            continue
        print(f"  {md_name} ...", end=" ")
        success, method = generate_pdf(md_path, pdf_path)
        if success:
            print(f"OK ({method})")
            ok += 1
        else:
            html = md_path.with_suffix(".html")
            print(f"HTML creat: {html.name} — obre'l al navegador → Imprimir → PDF")

    print(f"\n{ok}/{len(FILES)} PDFs generats.")
    if ok < len(FILES):
        print("\nPer PDFs perfectes sense script:")
        print("  1. Obre docs/*.html al Chrome")
        print("  2. Ctrl+P → Destinació: Guardar como PDF")
        print("  3. Márgenes: predeterminado · Activar gráficos de fondo")
    return 0 if ok > 0 else 1


if __name__ == "__main__":
    sys.exit(main())
