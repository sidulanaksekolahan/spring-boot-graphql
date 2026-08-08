# Spring Boot GraphQL: Input Object

Contoh REST API berbasis **Spring Boot**, **GraphQL**, **Spring Data JPA**, dan **PostgreSQL**. Proyek ini mengelola data produk dan terutama memperlihatkan penggunaan **GraphQL input object** (`ProductReqDto`) pada mutation `createProduct` dan `updateProduct`.

> Endpoint aplikasi adalah `POST http://localhost:8080/graphql`.

## Daftar isi

- [Teknologi](#teknologi)
- [Struktur dan alur aplikasi](#struktur-dan-alur-aplikasi)
- [Prasyarat](#prasyarat)
- [Menjalankan aplikasi](#menjalankan-aplikasi)
- [Skema GraphQL](#skema-graphql)
- [Testing dengan Postman](#testing-dengan-postman)
- [Catatan perilaku API](#catatan-perilaku-api)
- [Troubleshooting](#troubleshooting)

## Teknologi

| Komponen | Kegunaan |
| --- | --- |
| Java 17 | Bahasa pemrograman aplikasi. |
| Spring Boot 4.1.0 | Framework aplikasi. |
| Spring for GraphQL | Menyediakan endpoint GraphQL serta binding skema ke controller. |
| Spring Data JPA / Hibernate | Akses dan pemetaan data `Product` ke database. |
| PostgreSQL 16 | Database relasional. |
| Maven Wrapper | Menjalankan Maven tanpa instalasi Maven global. |
| Docker Compose | Menjalankan PostgreSQL secara lokal. |

## Struktur dan alur aplikasi

```text
Postman / GraphQL client
          |
          | POST /graphql
          v
schema.graphqls  <-->  ProductGraphqlController
                              |
                              v
                        ProductService
                              |
                              v
                    ProductRepository (JPA)
                              |
                              v
                         PostgreSQL: products
```

File penting:

| Lokasi | Peran |
| --- | --- |
| `src/main/resources/graphql/schema.graphqls` | Kontrak GraphQL: type, input, query, dan mutation. |
| `controller/ProductGraphqlController.java` | Resolver GraphQL dengan `@QueryMapping` dan `@MutationMapping`. |
| `dto/ProductReqDto.java` | DTO masukan mutation; dipetakan dari `ProductReqDto` pada skema. |
| `dto/ProductResDto.java` | DTO respons yang diekspos GraphQL. |
| `service/ProductServiceImpl.java` | Logika baca, simpan, ubah, dan hapus produk. |
| `entity/Product.java` | Entity JPA tabel `products`. |
| `repository/ProductRepository.java` | Repository JPA. |
| `docker-compose.yml` dan `.env` | Konfigurasi PostgreSQL lokal. |

## Prasyarat

- JDK 17.
- Docker Desktop (disarankan untuk PostgreSQL), atau PostgreSQL yang berjalan di komputer Anda.
- Postman untuk pengujian manual.

## Menjalankan aplikasi

### 1. Siapkan database PostgreSQL

Proyek menyediakan nilai berikut pada file `.env`:

```env
POSTGRES_DB=mydb_graphql
POSTGRES_USER=myuser
POSTGRES_PASSWORD=mypassword
POSTGRES_PORT=5432
```

Jalankan PostgreSQL dari direktori root proyek:

```powershell
docker compose up -d
```

Pastikan kontainer sudah aktif:

```powershell
docker compose ps
```

Jika memakai PostgreSQL yang tidak dijalankan Docker, buat database dan sesuaikan environment variable berikut sebelum aplikasi dijalankan:

```powershell
$env:POSTGRES_DB = "mydb_graphql"
$env:POSTGRES_USER = "myuser"
$env:POSTGRES_PASSWORD = "mypassword"
```

Koneksi aplikasi secara default adalah `jdbc:postgresql://localhost:5432/${POSTGRES_DB}`. Port database aplikasi saat ini selalu `5432`; variabel `POSTGRES_PORT` di `.env` hanya dipakai Docker Compose untuk memetakan port host ke kontainer.

### 2. Jalankan Spring Boot

Windows/PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Atau jalankan test konteks Spring:

```powershell
.\mvnw.cmd test
```

Setelah startup berhasil, GraphQL dapat diakses melalui `http://localhost:8080/graphql`.

### 3. Hentikan database (opsional)

```powershell
docker compose down
```

Perintah tersebut menghentikan dan menghapus kontainer, tetapi volume Docker tetap dipertahankan. Data sementara dari aplikasi tetap akan hilang ketika aplikasi dihentikan karena konfigurasi Hibernate menggunakan `spring.jpa.hibernate.ddl-auto=create-drop`.

## Skema GraphQL

```graphql
input ProductReqDto {
  name: String!
  description: String
  price: Float!
}

type ProductResDto {
  name: String!
  description: String
  price: Float!
}

type Query {
  products: [ProductResDto!]!
  product(id: ID!): ProductResDto
}

type Mutation {
  createProduct(input: ProductReqDto!): ProductResDto!
  updateProduct(id: ID!, reqDto: ProductReqDto!): ProductResDto!
  deleteProduct(id: ID!): Boolean!
}
```

Keterangan:

- Tanda `!` berarti nilai wajib dan tidak boleh `null`.
- `ProductReqDto` adalah **input object**. Ia mengelompokkan `name`, `description`, dan `price` menjadi satu argumen.
- `description` boleh tidak dikirim atau bernilai `null`.
- `ID` dapat dikirim sebagai angka atau string dalam GraphQL. Aplikasi mengikatnya ke `Integer`.
- Nilai `price` pada GraphQL bertipe `Float`; di Java ditangani sebagai `BigDecimal`.

## Testing dengan Postman

### Konfigurasi dasar request

1. Buat **Collection** baru, misalnya `Spring Boot GraphQL`.
2. Tambahkan collection variable berikut agar URL tidak ditulis berulang:

   | Variable | Initial value | Current value |
   | --- | --- | --- |
   | `baseUrl` | `http://localhost:8080` | `http://localhost:8080` |

3. Buat request baru dengan konfigurasi:

   | Pengaturan | Nilai |
   | --- | --- |
   | Method | `POST` |
   | URL | `{{baseUrl}}/graphql` |
   | Header | `Content-Type: application/json` |
   | Body | Pilih **raw**, lalu pilih tipe **JSON**. |

Setiap body Postman mengirim objek JSON dengan dua properti utama:

```json
{
  "query": "query atau mutation GraphQL",
  "variables": {}
}
```

`variables` bersifat opsional. Gunakan saat query GraphQL mendeklarasikan variabel, misalnya `$id` atau `$input`.

### A. Query tanpa variable dan tanpa input object

#### 1. Ambil seluruh produk

Request ini tidak memiliki argumen, sehingga tidak memakai variable maupun input object.

```json
{
  "query": "query { products { name description price } }"
}
```

Contoh respons sukses:

```json
{
  "data": {
    "products": [
      {
        "name": "Keyboard Mechanical",
        "description": "Keyboard switch brown",
        "price": 750000.0
      }
    ]
  }
}
```

#### 2. Ambil satu produk — nilai literal di query

Contoh ini menggunakan argumen `id` secara langsung, jadi **tanpa GraphQL variable** dan **tanpa input object**.

```json
{
  "query": "query { product(id: 1) { name description price } }"
}
```

### B. Query menggunakan GraphQL variable (tanpa input object)

Gunakan variable untuk menghindari penulisan nilai `id` di dalam string query dan agar request mudah dipakai ulang.

```json
{
  "query": "query GetProduct($id: ID!) { product(id: $id) { name description price } }",
  "variables": {
    "id": 1
  }
}
```

Keterangan:

- `$id: ID!` mendeklarasikan variable wajib bernama `id`.
- `product(id: $id)` meneruskan variable tersebut sebagai argumen resolver.
- Objek `variables` adalah JSON biasa; jangan menulis `$id` sebagai key di sana.

### C. Mutation menggunakan input object tanpa variable

#### 3. Buat produk (`createProduct`)

Di sini `input` adalah `ProductReqDto`, tetapi nilainya ditulis langsung di query. Ini menggunakan **input object**, namun **tanpa variable**.

```json
{
  "query": "mutation { createProduct(input: { name: \"Keyboard Mechanical\", description: \"Keyboard switch brown\", price: 750000 }) { name description price } }"
}
```

Jika `description` tidak diperlukan, properti tersebut dapat dihilangkan:

```json
{
  "query": "mutation { createProduct(input: { name: \"Mouse Wireless\", price: 250000 }) { name description price } }"
}
```

### D. Mutation menggunakan input object dan GraphQL variable

#### 4. Buat produk dengan variable `input`

Ini adalah pola yang paling disarankan untuk mutation. Struktur data produk dipisahkan dari operasi GraphQL.

```json
{
  "query": "mutation CreateProduct($input: ProductReqDto!) { createProduct(input: $input) { name description price } }",
  "variables": {
    "input": {
      "name": "Monitor 24 Inch",
      "description": "IPS Full HD",
      "price": 1850000
    }
  }
}
```

#### 5. Ubah produk dengan dua variable: `id` dan `reqDto`

Nama argument input untuk `updateProduct` adalah **`reqDto`**, bukan `input`; nama ini harus sama persis dengan skema.

```json
{
  "query": "mutation UpdateProduct($id: ID!, $reqDto: ProductReqDto!) { updateProduct(id: $id, reqDto: $reqDto) { name description price } }",
  "variables": {
    "id": 1,
    "reqDto": {
      "name": "Keyboard Mechanical Pro",
      "description": "Keyboard hot-swappable",
      "price": 900000
    }
  }
}
```

### E. Mutation tanpa input object

#### 6. Hapus produk dengan nilai literal

`deleteProduct` hanya memerlukan scalar `id`, sehingga tidak membutuhkan input object.

```json
{
  "query": "mutation { deleteProduct(id: 1) }"
}
```

#### 7. Hapus produk dengan variable

```json
{
  "query": "mutation DeleteProduct($id: ID!) { deleteProduct(id: $id) }",
  "variables": {
    "id": 1
  }
}
```

### F. Menggunakan variable dari Postman Environment/Collection

GraphQL variable dan variable Postman adalah dua mekanisme berbeda yang dapat dipakai bersamaan:

- **Variable Postman** seperti `{{baseUrl}}` atau `{{productId}}` diganti oleh Postman sebelum request dikirim.
- **GraphQL variable** seperti `$id` dikirim di properti JSON `variables` dan diproses GraphQL.

Tambahkan collection variable ini setelah membuat produk:

| Variable | Current value |
| --- | --- |
| `productId` | `1` |
| `productName` | `Mouse Wireless` |
| `productPrice` | `250000` |

Kemudian jalankan request berikut. Postman akan mengganti `{{productId}}`, `{{productName}}`, dan `{{productPrice}}` terlebih dahulu; GraphQL berikutnya memproses `$id` dan `$input`.

```json
{
  "query": "mutation UpdateProduct($id: ID!, $input: ProductReqDto!) { updateProduct(id: $id, reqDto: $input) { name description price } }",
  "variables": {
    "id": "{{productId}}",
    "input": {
      "name": "{{productName}}",
      "description": "Diubah melalui Postman variable",
      "price": {{productPrice}}
    }
  }
}
```

Perhatikan `price` tidak diberi tanda kutip karena harus dikirim sebagai angka JSON. Untuk `ID`, nilai string seperti `"{{productId}}"` tetap valid untuk scalar `ID` dan akan dikonversi ke `Integer` oleh resolver ini.

### G. Urutan pengujian yang disarankan

1. Jalankan `products` untuk memastikan endpoint dapat diakses. Respons awal normalnya adalah array kosong.
2. Jalankan `createProduct` dengan input object dan variable.
3. Jalankan `products` untuk melihat data yang baru dibuat.
4. Jalankan `product` menggunakan variable `id`.
5. Jalankan `updateProduct` menggunakan variable `id` dan `reqDto`.
6. Jalankan `deleteProduct`, lalu verifikasi lagi dengan `products`.

## Catatan perilaku API

- Respons `ProductResDto` saat ini hanya berisi `name`, `description`, dan `price`; **`id` tidak diekspos**. Karena itu, catat ID saat pengujian atau lihat tabel `products` di PostgreSQL untuk menentukan ID yang digunakan pada `product`, `updateProduct`, dan `deleteProduct`.
- Saat produk tidak ditemukan, service memakai `orElseThrow()`. GraphQL akan membalas dengan field `errors` dan HTTP request tetap dapat berstatus `200`, sesuai pola respons GraphQL.
- `name` dan `price` wajib ada pada `createProduct` maupun `updateProduct`. Jika salah satu tidak dikirim, GraphQL mengembalikan validation error sebelum resolver dijalankan.
- Pada proses update, semua field pada `reqDto` digunakan untuk mengganti nilai lama. Karena `name` dan `price` wajib, mutation ini lebih menyerupai **full update** daripada partial update.
- Tabel dibuat saat aplikasi mulai dan dihapus saat aplikasi berhenti (`create-drop`). Konfigurasi ini tepat untuk demo/development, bukan untuk data produksi.

## Contoh respons error

Jika variable wajib tidak dikirim:

```json
{
  "query": "query GetProduct($id: ID!) { product(id: $id) { name } }",
  "variables": {}
}
```

GraphQL akan menghasilkan respons dengan `errors`, karena `$id` bertipe non-null (`ID!`). Periksa bagian `errors[].message` di Postman untuk detail validasi atau exception dari aplikasi.

## Troubleshooting

| Gejala | Penyebab umum | Solusi |
| --- | --- | --- |
| `Connection refused` ke PostgreSQL | Kontainer/database belum aktif. | Jalankan `docker compose up -d`, lalu periksa `docker compose ps`. |
| Gagal autentikasi database | Username, password, atau database tidak cocok. | Samakan `POSTGRES_DB`, `POSTGRES_USER`, dan `POSTGRES_PASSWORD` antara Docker dan environment aplikasi. |
| `404` pada `/graphql` | Aplikasi belum berjalan atau URL salah. | Jalankan `.\mvnw.cmd spring-boot:run` dan gunakan `POST {{baseUrl}}/graphql`. |
| Error `Validation error` | Nama field/argument tidak sesuai skema, atau field wajib kosong. | Cocokkan request dengan `schema.graphqls`; khusus update gunakan `reqDto`. |
| Error JSON di Postman | Body bukan JSON valid atau kutip di string query tidak di-escape. | Pilih raw/JSON dan gunakan `\"` untuk kutip di dalam `query` JSON. |
| Produk tidak ditemukan | ID tidak ada atau data hilang setelah restart. | Gunakan ID yang ada; ingat konfigurasi `create-drop` menghapus tabel saat shutdown. |

## Ringkasan pola request

| Kebutuhan | Input object | GraphQL variable | Contoh operasi |
| --- | --- | --- | --- |
| Baca semua data | Tidak | Tidak | `products` |
| Baca satu data dengan nilai langsung | Tidak | Tidak | `product(id: 1)` |
| Baca satu data yang dapat dipakai ulang | Tidak | Ya | `product(id: $id)` |
| Simpan data secara langsung | Ya | Tidak | `createProduct(input: { ... })` |
| Simpan/ubah data secara reusable | Ya | Ya | `createProduct(input: $input)`, `updateProduct(..., reqDto: $reqDto)` |
| Hapus data | Tidak | Opsional | `deleteProduct(id: 1)` atau `deleteProduct(id: $id)` |
