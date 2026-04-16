from pathlib import Path

from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.platypus import Paragraph, SimpleDocTemplate, Spacer


def main() -> None:
    base = Path(__file__).resolve().parent
    md_path = base / "Explicacion_Codigo_PersonalBet.md"
    pdf_path = base / "Explicacion_Codigo_PersonalBet.pdf"

    text = md_path.read_text(encoding="utf-8")
    lines = text.splitlines()

    styles = getSampleStyleSheet()
    title_style = ParagraphStyle(
        "TitleCustom",
        parent=styles["Heading1"],
        fontSize=16,
        leading=20,
        spaceAfter=10,
    )
    h2_style = ParagraphStyle(
        "H2Custom",
        parent=styles["Heading2"],
        fontSize=13,
        leading=16,
        spaceBefore=8,
        spaceAfter=6,
    )
    h3_style = ParagraphStyle(
        "H3Custom",
        parent=styles["Heading3"],
        fontSize=11,
        leading=14,
        spaceBefore=6,
        spaceAfter=4,
    )
    body_style = ParagraphStyle(
        "BodyCustom",
        parent=styles["BodyText"],
        fontSize=10,
        leading=13,
        spaceAfter=3,
    )

    story = []
    for raw_line in lines:
        line = raw_line.strip()
        if not line:
            story.append(Spacer(1, 6))
            continue
        if line.startswith("# "):
            story.append(Paragraph(line[2:].strip(), title_style))
            continue
        if line.startswith("## "):
            story.append(Paragraph(line[3:].strip(), h2_style))
            continue
        if line.startswith("### "):
            story.append(Paragraph(line[4:].strip(), h3_style))
            continue
        if line.startswith("---"):
            story.append(Spacer(1, 8))
            continue
        if line.startswith("- "):
            story.append(Paragraph(f"&bull; {line[2:].strip()}", body_style))
            continue

        # Escape simple HTML-sensitive chars for ReportLab Paragraph.
        safe = (
            line.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
        )
        story.append(Paragraph(safe, body_style))

    doc = SimpleDocTemplate(
        str(pdf_path),
        pagesize=A4,
        leftMargin=40,
        rightMargin=40,
        topMargin=36,
        bottomMargin=36,
        title="Explicacion Codigo PersonalBet",
    )
    doc.build(story)
    print(f"PDF generado: {pdf_path}")


if __name__ == "__main__":
    main()
