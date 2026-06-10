# Personal Expense Tracker API

RESTful API untuk mencatat pengeluaran harian, dibangun dengan **PHP 8** native (tanpa framework) dan **MySQL**.

## Fitur

- Kelola **Kategori** pengeluaran (CRUD)
- Kelola **Transaksi** pengeluaran (CRUD)
- **Dashboard** ringkasan (hari ini, minggu ini, bulan ini, tahun ini)
- **Laporan Harian** berdasarkan tanggal
- **Laporan Bulanan** per kategori
- **Statistik Grafik** (range tanggal)
- CORS enabled

## Persyaratan

- PHP 8.0+
- MySQL 5.7+ / MariaDB 10.2+
- Web Server: IIS (dengan URL Rewrite Module) atau Apache
- Ekstensi PHP: `pdo_mysql`

## Instalasi

### 1. Clone / Copy Project

```
D:\Yudi\development\Personal Expense Tracker\expense-api
```

### 2. Database

Jalankan file `database/schema.sql` di MySQL:

```sql
mysql -u root -p < database/schema.sql
```

Atau import via phpMyAdmin / MySQL Workbench.

### 3. Konfigurasi Database

Sesuaikan koneksi database di `config/database.php`:

```php
private static string $host     = 'localhost';
private static string $port     = '3306';
private static string $dbname   = 'expense_tracker';
private static string $username = 'root';
private static string $password = '1234';
```

### 4. Web Server

#### IIS

1. Install **PHP** di IIS (Handler Mapping: `.php` → `FastCgiModule` → `php-cgi.exe`)
2. Install **URL Rewrite Module** (https://www.iis.net/downloads/microsoft/url-rewrite)
3. Buat Site baru → Physical path指向 `expense-api` folder
4. Beri permission `IUSR` / `IIS_IUSRS` ke folder project

File `web.config` sudah include rewrite rules dan default document.

#### Apache

File `.htaccess` sudah include rewrite rules. Pastikan `mod_rewrite` aktif.

## Struktur Folder

```
expense-api/
├── config/
│   └── database.php        # Koneksi database (PDO)
├── controllers/
│   ├── CategoryController.php
│   ├── DashboardController.php
│   ├── ReportController.php
│   └── TransactionController.php
├── database/
│   └── schema.sql          # Skema database + seed data
├── helpers/
│   └── ResponseHandler.php # JSON response helper
├── models/
│   ├── Category.php
│   └── Transaction.php
├── routes/
│   └── api.php             # Router utama
├── services/
│   ├── CategoryService.php
│   └── TransactionService.php
├── .htaccess               # Apache rewrite rules
├── index.php               # Entry point
├── web.config              # IIS config
└── README.md
```

## API Endpoints

### Dashboard

| Method | Endpoint           | Deskripsi                        |
|--------|--------------------|----------------------------------|
| GET    | `/api/dashboard`   | Ringkasan (hari/minggu/bulan/tahun) |

### Kategori

| Method | Endpoint                | Deskripsi             |
|--------|-------------------------|-----------------------|
| GET    | `/api/categories`       | Semua kategori        |
| GET    | `/api/categories/{id}`  | Detail kategori       |
| POST   | `/api/categories`       | Tambah kategori       |
| PUT    | `/api/categories/{id}`  | Update kategori       |
| DELETE | `/api/categories/{id}`  | Hapus kategori        |

### Transaksi

| Method | Endpoint                  | Deskripsi              |
|--------|---------------------------|------------------------|
| GET    | `/api/transactions`       | Semua transaksi        |
| GET    | `/api/transactions/{id}`  | Detail transaksi       |
| POST   | `/api/transactions`       | Tambah transaksi       |
| PUT    | `/api/transactions/{id}`  | Update transaksi       |
| DELETE | `/api/transactions/{id}`  | Hapus transaksi        |

### Laporan

| Method | Endpoint                         | Deskripsi                        |
|--------|----------------------------------|----------------------------------|
| GET    | `/api/report/daily?date=YYYY-MM-DD` | Laporan harian (default: hari ini) |
| GET    | `/api/report/monthly?month=1&year=2026` | Laporan bulanan per kategori  |
| GET    | `/api/report/stats?start_date=...&end_date=...` | Statistik grafik harian |

## Contoh Request & Response

### Tambah Transaksi

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "kategori_id": 1,
    "nominal": 25000,
    "keterangan": "Nasi Goreng",
    "tanggal_transaksi": "2026-06-09"
  }'
```

Response:

```json
{
  "status": true,
  "message": "Transaksi berhasil ditambahkan",
  "data": {
    "id": 1,
    "kategori_id": 1,
    "nama_kategori": "Makan",
    "icon": "restaurant",
    "nominal": 25000,
    "keterangan": "Nasi Goreng",
    "tanggal_transaksi": "2026-06-09",
    "created_at": "2026-06-09 12:00:00",
    "updated_at": "2026-06-09 12:00:00"
  }
}
```

### Laporan Harian

```bash
curl http://localhost:8080/api/report/daily?date=2026-06-09
```

Response:

```json
{
  "status": true,
  "message": "Laporan harian berhasil diambil",
  "data": {
    "total": 25000,
    "data": [
      {
        "id": 1,
        "kategori_id": 1,
        "nama_kategori": "Makan",
        "icon": "restaurant",
        "nominal": 25000,
        "keterangan": "Nasi Goreng",
        "tanggal_transaksi": "2026-06-09",
        "created_at": "2026-06-09 12:00:00",
        "updated_at": "2026-06-09 12:00:00"
      }
    ]
  }
}
```

## Format Request Body (JSON)

### POST / PUT Transaksi

```json
{
  "kategori_id": 1,
  "nominal": 25000,
  "keterangan": "Opsional",
  "tanggal_transaksi": "2026-06-09"
}
```

### POST / PUT Kategori

```json
{
  "nama_kategori": "Makanan",
  "icon": "restaurant"
}
```
