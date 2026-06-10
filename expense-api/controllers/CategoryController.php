<?php

class CategoryController
{
    public static function index(): void
    {
        $categories = CategoryService::getAll();
        ResponseHandler::success($categories, 'Daftar kategori berhasil diambil');
    }

    public static function show(int $id): void
    {
        $category = CategoryService::getById($id);
        ResponseHandler::success($category, 'Detail kategori berhasil diambil');
    }

    public static function store(): void
    {
        $input = json_decode(file_get_contents('php://input'), true) ?? $_POST;
        $category = CategoryService::create($input);
        ResponseHandler::success($category, 'Kategori berhasil ditambahkan', 201);
    }

    public static function update(int $id): void
    {
        $input = json_decode(file_get_contents('php://input'), true) ?? $_POST;
        $category = CategoryService::update($id, $input);
        ResponseHandler::success($category, 'Kategori berhasil diperbarui');
    }

    public static function destroy(int $id): void
    {
        CategoryService::delete($id);
        ResponseHandler::success(null, 'Kategori berhasil dihapus');
    }
}
