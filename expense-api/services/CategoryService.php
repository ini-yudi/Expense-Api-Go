<?php

class CategoryService
{
    public static function getAll(): array
    {
        return Category::findAll();
    }

    public static function getById(int $id): array
    {
        $category = Category::findById($id);
        if (!$category) {
            ResponseHandler::error('Kategori tidak ditemukan', 404);
        }
        return $category;
    }

    public static function create(array $input): array
    {
        $namaKategori = trim($input['nama_kategori'] ?? '');
        $icon         = trim($input['icon'] ?? 'default');

        if ($namaKategori === '') {
            ResponseHandler::error('Nama kategori wajib diisi');
        }

        return Category::create($namaKategori, $icon);
    }

    public static function update(int $id, array $input): array
    {
        $category = Category::findById($id);
        if (!$category) {
            ResponseHandler::error('Kategori tidak ditemukan', 404);
        }

        $namaKategori = trim($input['nama_kategori'] ?? $category['nama_kategori']);
        $icon         = trim($input['icon'] ?? $category['icon']);

        if ($namaKategori === '') {
            ResponseHandler::error('Nama kategori wajib diisi');
        }

        $updated = Category::update($id, $namaKategori, $icon);
        if (!$updated) {
            ResponseHandler::error('Gagal memperbarui kategori');
        }
        return $updated;
    }

    public static function delete(int $id): void
    {
        $category = Category::findById($id);
        if (!$category) {
            ResponseHandler::error('Kategori tidak ditemukan', 404);
        }

        if (!Category::delete($id)) {
            ResponseHandler::error('Gagal menghapus kategori');
        }
    }
}
