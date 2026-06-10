<?php

class TransactionService
{
    public static function getAll(): array
    {
        return Transaction::findAll();
    }

    public static function getById(int $id): array
    {
        $transaction = Transaction::findById($id);
        if (!$transaction) {
            ResponseHandler::error('Transaksi tidak ditemukan', 404);
        }
        return $transaction;
    }

    public static function create(array $input): array
    {
        $kategoriId       = (int) ($input['kategori_id'] ?? 0);
        $nominal          = (float) ($input['nominal'] ?? 0);
        $keterangan       = trim($input['keterangan'] ?? '');
        $tanggalTransaksi = trim($input['tanggal_transaksi'] ?? '');

        // Validasi kategori
        if ($kategoriId <= 0) {
            ResponseHandler::error('Kategori wajib dipilih');
        }
        $category = Category::findById($kategoriId);
        if (!$category) {
            ResponseHandler::error('Kategori tidak ditemukan', 404);
        }

        // Validasi nominal
        if ($nominal <= 0) {
            ResponseHandler::error('Nominal wajib lebih dari 0');
        }

        // Validasi tanggal
        if (!self::isValidDate($tanggalTransaksi)) {
            ResponseHandler::error('Format tanggal tidak valid (YYYY-MM-DD)');
        }

        return Transaction::create($kategoriId, $nominal, $keterangan ?: null, $tanggalTransaksi);
    }

    public static function update(int $id, array $input): array
    {
        $transaction = Transaction::findById($id);
        if (!$transaction) {
            ResponseHandler::error('Transaksi tidak ditemukan', 404);
        }

        $kategoriId       = (int) ($input['kategori_id'] ?? $transaction['kategori_id']);
        $nominal          = (float) ($input['nominal'] ?? $transaction['nominal']);
        $keterangan       = trim($input['keterangan'] ?? $transaction['keterangan'] ?? '');
        $tanggalTransaksi = trim($input['tanggal_transaksi'] ?? $transaction['tanggal_transaksi']);

        if ($kategoriId <= 0) {
            ResponseHandler::error('Kategori wajib dipilih');
        }
        $category = Category::findById($kategoriId);
        if (!$category) {
            ResponseHandler::error('Kategori tidak ditemukan', 404);
        }

        if ($nominal <= 0) {
            ResponseHandler::error('Nominal wajib lebih dari 0');
        }

        if (!self::isValidDate($tanggalTransaksi)) {
            ResponseHandler::error('Format tanggal tidak valid (YYYY-MM-DD)');
        }

        $updated = Transaction::update($id, $kategoriId, $nominal, $keterangan ?: null, $tanggalTransaksi);
        if (!$updated) {
            ResponseHandler::error('Gagal memperbarui transaksi');
        }
        return $updated;
    }

    public static function delete(int $id): void
    {
        $transaction = Transaction::findById($id);
        if (!$transaction) {
            ResponseHandler::error('Transaksi tidak ditemukan', 404);
        }

        if (!Transaction::delete($id)) {
            ResponseHandler::error('Gagal menghapus transaksi');
        }
    }

    // ----------------------------------------------------------
    // Dashboard
    // ----------------------------------------------------------
    public static function dashboard(): array
    {
        return [
            'today'             => Transaction::sumToday(),
            'week'              => Transaction::sumThisWeek(),
            'month'             => Transaction::sumThisMonth(),
            'year'              => Transaction::sumThisYear(),
            'total_transaction' => Transaction::countAll(),
        ];
    }

    // ----------------------------------------------------------
    // Laporan Harian
    // ----------------------------------------------------------
    public static function dailyReport(string $date): array
    {
        if (!self::isValidDate($date)) {
            ResponseHandler::error('Format tanggal tidak valid (YYYY-MM-DD)');
        }

        return [
            'total'        => Transaction::sumByDate($date),
            'transactions' => Transaction::findByDate($date),
        ];
    }

    // ----------------------------------------------------------
    // Laporan Bulanan
    // ----------------------------------------------------------
    public static function monthlyReport(int $month, int $year): array
    {
        if ($month < 1 || $month > 12) {
            ResponseHandler::error('Bulan harus antara 1-12');
        }
        if ($year < 2000 || $year > 2100) {
            ResponseHandler::error('Tahun tidak valid');
        }

        return [
            'grand_total' => Transaction::sumByMonth($month, $year),
            'categories'  => Transaction::sumPerCategoryByMonth($month, $year),
        ];
    }

    // ----------------------------------------------------------
    // Statistik Harian
    // ----------------------------------------------------------
    public static function stats(string $startDate, string $endDate): array
    {
        if (!self::isValidDate($startDate) || !self::isValidDate($endDate)) {
            ResponseHandler::error('Format tanggal tidak valid (YYYY-MM-DD)');
        }

        return Transaction::statsDaily($startDate, $endDate);
    }

    // ----------------------------------------------------------
    // Helper
    // ----------------------------------------------------------
    private static function isValidDate(string $date): bool
    {
        $d = DateTime::createFromFormat('Y-m-d', $date);
        return $d && $d->format('Y-m-d') === $date;
    }
}
