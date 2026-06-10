-- ============================================================
-- Personal Expense Tracker - Database Schema
-- Database: expense_tracker
-- ============================================================

CREATE DATABASE IF NOT EXISTS expense_tracker
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE expense_tracker;

-- ------------------------------------------------------------
-- Tabel: categories (Master Kategori Pengeluaran)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nama_kategori VARCHAR(100) NOT NULL,
    icon        VARCHAR(50)  NOT NULL DEFAULT 'default',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_nama_kategori (nama_kategori)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Tabel: transactions (Transaksi Pengeluaran)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transactions (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    kategori_id       INT            NOT NULL,
    nominal           DECIMAL(15,2)  NOT NULL,
    keterangan        TEXT           NULL,
    tanggal_transaksi DATE           NOT NULL,
    created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_kategori
        FOREIGN KEY (kategori_id)
        REFERENCES categories (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    INDEX idx_tanggal (tanggal_transaksi),
    INDEX idx_kategori (kategori_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Seed Data: Kategori Default
-- ============================================================
INSERT INTO categories (nama_kategori, icon) VALUES
    ('Makan',       'restaurant'),
    ('Minum',       'local_cafe'),
    ('Jajan',       'cookie'),
    ('Transportasi','directions_bus'),
    ('Belanja',     'shopping_cart'),
    ('Hiburan',     'movie'),
    ('Tagihan',     'receipt'),
    ('Kesehatan',   'local_hospital'),
    ('Pendidikan',  'school'),
    ('Lainnya',     'category');
