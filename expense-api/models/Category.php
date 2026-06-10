<?php

class Category
{
    public static function findAll(): array
    {
        $db = Database::getConnection();
        $result = mysqli_query($db, 'SELECT id, nama_kategori, icon, created_at, updated_at FROM categories ORDER BY id ASC');
        return mysqli_fetch_all($result, MYSQLI_ASSOC);
    }

    public static function findById(int $id): ?array
    {
        $db = Database::getConnection();
        $stmt = mysqli_prepare($db, 'SELECT id, nama_kategori, icon, created_at, updated_at FROM categories WHERE id = ?');
        mysqli_stmt_bind_param($stmt, 'i', $id);
        mysqli_stmt_execute($stmt);
        $result = mysqli_stmt_get_result($stmt);
        $row = mysqli_fetch_assoc($result);
        mysqli_stmt_close($stmt);
        return $row ?: null;
    }

    public static function create(string $namaKategori, string $icon): array
    {
        $db = Database::getConnection();
        $stmt = mysqli_prepare($db, 'INSERT INTO categories (nama_kategori, icon) VALUES (?, ?)');
        mysqli_stmt_bind_param($stmt, 'ss', $namaKategori, $icon);
        mysqli_stmt_execute($stmt);
        $id = (int) mysqli_insert_id($db);
        mysqli_stmt_close($stmt);
        return self::findById($id);
    }

    public static function update(int $id, string $namaKategori, string $icon): ?array
    {
        $db = Database::getConnection();
        $stmt = mysqli_prepare($db, 'UPDATE categories SET nama_kategori = ?, icon = ? WHERE id = ?');
        mysqli_stmt_bind_param($stmt, 'ssi', $namaKategori, $icon, $id);
        mysqli_stmt_execute($stmt);
        mysqli_stmt_close($stmt);
        return self::findById($id);
    }

    public static function delete(int $id): bool
    {
        $db = Database::getConnection();
        $stmt = mysqli_prepare($db, 'DELETE FROM categories WHERE id = ?');
        mysqli_stmt_bind_param($stmt, 'i', $id);
        mysqli_stmt_execute($stmt);
        $affected = mysqli_affected_rows($db);
        mysqli_stmt_close($stmt);
        return $affected > 0;
    }
}
