from pathlib import Path
from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import cm
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, Preformatted

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "output" / "pdf" / "dokumentasi-project-spring-boot-graphql.pdf"
OUT.parent.mkdir(parents=True, exist_ok=True)

styles = getSampleStyleSheet()
styles.add(ParagraphStyle(name="TitleCustom", parent=styles["Title"], fontName="Helvetica-Bold", fontSize=23, leading=28, textColor=colors.HexColor("#12355B"), alignment=TA_CENTER, spaceAfter=12))
styles.add(ParagraphStyle(name="SubTitle", parent=styles["Normal"], fontName="Helvetica", fontSize=10, leading=14, textColor=colors.HexColor("#486581"), alignment=TA_CENTER))
styles.add(ParagraphStyle(name="H1Custom", parent=styles["Heading1"], fontName="Helvetica-Bold", fontSize=15, leading=19, textColor=colors.HexColor("#12355B"), spaceBefore=16, spaceAfter=8))
styles.add(ParagraphStyle(name="H2Custom", parent=styles["Heading2"], fontName="Helvetica-Bold", fontSize=11.5, leading=15, textColor=colors.HexColor("#1D4E89"), spaceBefore=10, spaceAfter=5))
styles.add(ParagraphStyle(name="BodyCustom", parent=styles["BodyText"], fontName="Helvetica", fontSize=9.2, leading=13.2, spaceAfter=6))
styles.add(ParagraphStyle(name="CodeCustom", parent=styles["Code"], fontName="Courier", fontSize=7.1, leading=9.1, leftIndent=5, rightIndent=5, borderColor=colors.HexColor("#D9E2EC"), borderWidth=0.5, borderPadding=6, backColor=colors.HexColor("#F7FAFC"), spaceBefore=3, spaceAfter=8))

def p(text, style="BodyCustom"):
    return Paragraph(text, styles[style])

def code(text):
    return Preformatted(text.strip(), styles["CodeCustom"])

def table(rows, widths):
    t = Table(rows, colWidths=widths, repeatRows=1, hAlign="LEFT")
    t.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#12355B")),
        ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
        ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
        ("FONTNAME", (0, 1), (-1, -1), "Helvetica"),
        ("FONTSIZE", (0, 0), (-1, -1), 7.6),
        ("LEADING", (0, 0), (-1, -1), 9.5),
        ("GRID", (0, 0), (-1, -1), 0.35, colors.HexColor("#BCCCDC")),
        ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#F0F4F8")]),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 5), ("RIGHTPADDING", (0, 0), (-1, -1), 5),
        ("TOPPADDING", (0, 0), (-1, -1), 5), ("BOTTOMPADDING", (0, 0), (-1, -1), 5),
    ]))
    return t

def footer(canvas, doc):
    canvas.saveState()
    canvas.setStrokeColor(colors.HexColor("#D9E2EC"))
    canvas.line(doc.leftMargin, 1.35*cm, A4[0]-doc.rightMargin, 1.35*cm)
    canvas.setFont("Helvetica", 8)
    canvas.setFillColor(colors.HexColor("#486581"))
    canvas.drawString(doc.leftMargin, 0.85*cm, "Spring Boot GraphQL - Dokumentasi Project")
    canvas.drawRightString(A4[0]-doc.rightMargin, 0.85*cm, f"Halaman {doc.page}")
    canvas.restoreState()

story = [p("DOKUMENTASI PROJECT", "TitleCustom"), p("Spring Boot GraphQL Product API", "TitleCustom"), p("Panduan setup, API, dan pengujian melalui Postman", "SubTitle"), Spacer(1, 0.45*cm)]
story += [p("1. Ringkasan", "H1Custom"), p("Project ini adalah GraphQL API untuk mengelola data produk. Teknologi yang digunakan: Java 17, Spring Boot, Spring for GraphQL, Spring Data JPA, dan PostgreSQL 16. Operasi utama mencakup membaca seluruh produk atau satu produk, serta membuat, mengubah, dan menghapus produk.")]
story += [p("2. Prasyarat dan konfigurasi", "H1Custom"), p("Siapkan Java 17+, Docker Desktop, dan Postman. Konfigurasi default pada .env menggunakan database mydb_graphql, user myuser, password mypassword, dan port PostgreSQL 5432.")]
story += [p("Catatan penting", "H2Custom"), p("Aplikasi memakai spring.jpa.hibernate.ddl-auto=create-drop. Tabel dan data dibuat saat aplikasi berjalan dan dihapus ketika aplikasi berhenti. Data uji tidak persisten antar restart.")]
story += [p("3. Menjalankan aplikasi", "H1Custom"), p("Dari direktori root project, jalankan database kemudian aplikasi."), code("docker compose up -d\n.\\mvnw.cmd spring-boot:run"), p("Endpoint GraphQL default:") , code("POST http://localhost:8080/graphql\nContent-Type: application/json")]
story += [p("4. Skema GraphQL", "H1Custom"), table([["Field", "Tipe", "Keterangan"], ["id", "ID!", "ID produk dari database"], ["name", "String!", "Nama produk"], ["description", "String", "Deskripsi, boleh null"], ["price", "Float!", "Harga produk"], ["createdAt", "String!", "Waktu pembuatan"], ["updatedAt", "String!", "Waktu perubahan terakhir"]], [2.8*cm, 3*cm, 10.2*cm]), Spacer(1, 0.25*cm), table([["Jenis", "Nama", "Parameter", "Hasil"], ["Query", "products", "-", "Daftar seluruh produk"], ["Query", "product", "id: ID!", "Satu produk"], ["Mutation", "createProduct", "name, description, price", "Produk baru"], ["Mutation", "updateProduct", "id, name, description, price", "Produk berubah"], ["Mutation", "deleteProduct", "id", "Boolean"]], [2.1*cm, 3.2*cm, 6.6*cm, 4.1*cm])]
story += [PageBreak(), p("5. Postman - setup request", "H1Custom"), p("Buat request POST ke http://localhost:8080/graphql. Pilih Body > raw > JSON dan set header Content-Type: application/json. Tempel payload JSON di bawah ini sesuai kebutuhan.")]
story += [p("A. Query tanpa variables", "H2Custom"), p("Mengambil seluruh produk:"), code('{\n  "query": "query { products { id name description price createdAt updatedAt } }"\n}'), p("Mengambil satu produk (ID ditulis langsung):"), code('{\n  "query": "query { product(id: 1) { id name description price } }"\n}')]
story += [p("B. Mutation tanpa variables", "H2Custom"), p("Membuat produk:"), code('{\n  "query": "mutation { createProduct(name: \\"Keyboard Mechanical\\", description: \\"Switch tactile\\", price: 850000) { id name description price } }"\n}'), p("Memperbarui produk (gunakan ID dari respons createProduct):"), code('{\n  "query": "mutation { updateProduct(id: 1, name: \\"Keyboard Pro\\", description: \\"Hot-swappable\\", price: 950000) { id name description price updatedAt } }"\n}'), p("Menghapus produk:"), code('{\n  "query": "mutation { deleteProduct(id: 1) }"\n}')]
story += [PageBreak(), p("6. Postman - memakai GraphQL variables", "H1Custom"), p("Variables memisahkan nilai data dari dokumen GraphQL. Metode ini membuat request dapat dipakai ulang dan lebih aman untuk string/nilai dinamis.")]
story += [p("Query dengan variables", "H2Custom"), code('{\n  "query": "query GetProduct($id: ID!) { product(id: $id) { id name description price createdAt updatedAt } }",\n  "variables": { "id": 1 }\n}')]
story += [p("Create mutation dengan variables", "H2Custom"), code('{\n  "query": "mutation CreateProduct($name: String!, $description: String, $price: Float!) { createProduct(name: $name, description: $description, price: $price) { id name description price createdAt updatedAt } }",\n  "variables": {\n    "name": "Mouse Wireless",\n    "description": "Mouse Bluetooth ergonomis",\n    "price": 275000\n  }\n}')]
story += [p("Update dan delete dengan variables", "H2Custom"), code('{\n  "query": "mutation UpdateProduct($id: ID!, $name: String!, $description: String, $price: Float!) { updateProduct(id: $id, name: $name, description: $description, price: $price) { id name description price updatedAt } }",\n  "variables": { "id": 1, "name": "Mouse Wireless Pro", "description": "Baterai tahan lama", "price": 325000 }\n}\n\n{\n  "query": "mutation DeleteProduct($id: ID!) { deleteProduct(id: $id) }",\n  "variables": { "id": 1 }\n}')]
story += [p("7. Environment Variable Postman", "H1Custom"), p("Buat environment bernama Local GraphQL dengan baseUrl = http://localhost:8080 dan productId = 1. Gunakan URL {{baseUrl}}/graphql. Untuk ID dinamis, payload dapat memakai nilai environment di dalam object variables:"), code('{\n  "query": "query GetProduct($id: ID!) { product(id: $id) { id name price } }",\n  "variables": { "id": {{productId}} }\n}')]
story += [p("8. Verifikasi dan troubleshooting", "H1Custom"), p("Urutan test yang disarankan: createProduct, products, product/updateProduct, deleteProduct, lalu products lagi. Catat ID hasil create untuk operasi berikutnya."), p("Periksa field errors pada body respons, karena GraphQL dapat mengembalikan HTTP 200 walaupun eksekusi operasi gagal. Pada kode saat ini, ID yang tidak ada memicu error karena service menggunakan orElseThrow(). Jika database gagal terhubung, pastikan Docker aktif, container PostgreSQL berjalan, dan port 5432 tersedia.")]

doc = SimpleDocTemplate(str(OUT), pagesize=A4, leftMargin=1.65*cm, rightMargin=1.65*cm, topMargin=1.55*cm, bottomMargin=1.8*cm, title="Dokumentasi Spring Boot GraphQL")
doc.build(story, onFirstPage=footer, onLaterPages=footer)
print(OUT)
