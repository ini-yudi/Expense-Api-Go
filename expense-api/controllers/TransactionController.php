<?php

class TransactionController
{
    public static function index(): void
    {
        $transactions = TransactionService::getAll();
        ResponseHandler::success($transactions, 'Daftar transaksi berhasil diambil');
    }

    public static function show(int $id): void
    {
        $transaction = TransactionService::getById($id);
        ResponseHandler::success($transaction, 'Detail transaksi berhasil diambil');
    }

    public static function store(): void
    {
        $input = json_decode(file_get_contents('php://input'), true) ?? $_POST;
        $transaction = TransactionService::create($input);
        ResponseHandler::success($transaction, 'Transaksi berhasil ditambahkan', 201);
    }

    public static function update(int $id): void
    {
        $input = json_decode(file_get_contents('php://input'), true) ?? $_POST;
        $transaction = TransactionService::update($id, $input);
        ResponseHandler::success($transaction, 'Transaksi berhasil diperbarui');
    }

    public static function destroy(int $id): void
    {
        TransactionService::delete($id);
        ResponseHandler::success(null, 'Transaksi berhasil dihapus');
    }
}
