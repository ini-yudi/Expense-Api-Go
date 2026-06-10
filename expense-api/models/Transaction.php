<?php

class Transaction
{
    private static function fetchAllHelper(mysqli_stmt $stmt): array
    {
        $result = mysqli_stmt_get_result($stmt);
        $rows = mysqli_fetch_all($result, MYSQLI_ASSOC);
        mysqli_stmt_close($stmt);
        return $rows;
    }

    private static function fetchOneHelper(mysqli_stmt $stmt): ?array
    {
        $result = mysqli_stmt_get_result($stmt);
        $row = mysqli_fetch_assoc($result);
        mysqli_stmt_close($stmt);
        return $row ?: null;
    }

    public static function findAll(): array
    {
        $db = Database::getConnection();
        $stmt = mysqli_prepare($db, '
            SELECT t.id, t.kategori_id, c.nama_kategori, c.icon,
                   t.nominal, t.keterangan, t.tanggal_transaksi, t.created_at, t.updated_at
            FROM transactions t
            JOIN categories c ON c.id = t.kategori_id
            ORDER BY t.tanggal_transaksi DESC, t.id DESC
        ');
        mysqli_stmt_execute($stmt);
        return self::fetchAllHelper($stmt);
    }

    public static function findById(int $id): ?array
    {
        $db = Database::getConnection();
        $stmt = mysqli_prepare($db, '
            SELECT t.id, t.kategori_id, c.nama_kategori, c.icon,
                   t.nominal, t.keterangan, t.tanggal_transaksi, t.created_at, t.updated_at
            FROM transactions t
            JOIN categories c ON c.id = t.kategori_id
            WHERE t.id = ?
        ');
        mysqli_stmt_bind_param($stmt, 'i', $id);
        mysqli_stmt_execute($stmt);
        return self::fetchOneHelper($stmt);
    }

    public static function create(int $kategoriId, float $nominal, ?string $keterangan, string $tanggalTransaksi): array
    {
        $db = Database::getConnection();
        $stmt = mysqli_prepare($db, '
            INSERT INTO transactions (kategori_id, nominal, keterangan, tanggal_transaksi)
            VALUES (?, ?, ?, ?)
        ');
        mysqli_stmt_bind_param($stmt, 'idss', $kategoriId, $nominal, $keterangan, $tanggalTransaksi);
        mysqli_stmt_execute($stmt);
        $id = (int) mysqli_insert_id($db);
        mysqli_stmt_close($stmt);
        return self::findById($id);
    }

    public static function update(int $id, int $kategoriId, float $nominal, ?string $keterangan, string $tanggalTransaksi): ?array
    {
        $db = Database::getConnection();
        $stmt = mysqli_prepare($db, '
            UPDATE transactions
            SET kategori_id = ?, nominal = ?, keterangan = ?, tanggal_transaksi = ?
            WHERE id = ?
        ');
        mysqli_stmt_bind_param($stmt, 'idssi', $kategoriId, $nominal, $keterangan, $tanggalTransaksi, $id);
        mysqli_stmt_execute($stmt);
        mysqli_stmt_close($stmt);
        return self::findById($id);
    }

    public static function delete(int $id): bool
    {
        $db = Database::getConnection();
        $stmt = mysqli_prepare($db, 'DELETE FROM transactions WHERE id = ?');
        mysqli_stmt_bind_param($stmt, 'i', $id);
        mysqli_stmt_execute($stmt);
        $affected = mysqli_affected_rows($db);
        mysqli_stmt_close($stmt);
        return $affected > 0;
    }

    // ----------------------------------------------------------
    // Dashboard
    // ----------------------------------------------------------
    public static function sumToday(): float
    {
        $db = Database::getConnection();
        $result = mysqli_query($db, 'SELECT COALESCE(SUM(nominal), 0) AS total FROM transactions WHERE tanggal_transaksi = CURDATE()');
        return (float) mysqli_fetch_assoc($result)['total'];
    }

    public static function sumThisWeek(): float
    {
        $db = Database::getConnection();
        $result = mysqli_query($db, "
            SELECT COALESCE(SUM(nominal), 0) AS total
            FROM transactions
            WHERE YEARWEEK(tanggal_transaksi, 1) = YEARWEEK(CURDATE(), 1)
        ");
        return (float) mysqli_fetch_assoc($result)['total'];
    }

    public static function sumThisMonth(): float
    {
        $db = Database::getConnection();
        $result = mysqli_query($db, "
            SELECT COALESCE(SUM(nominal), 0) AS total
            FROM transactions
            WHERE YEAR(tanggal_transaksi) = YEAR(CURDATE())
              AND MONTH(tanggal_transaksi) = MONTH(CURDATE())
        ");
        return (float) mysqli_fetch_assoc($result)['total'];
    }

    public static function sumThisYear(): float
    {
        $db = Database::getConnection();
        $result = mysqli_query($db, "
            SELECT COALESCE(SUM(nominal), 0) AS total
            FROM transactions
            WHERE YEAR(tanggal_transaksi) = YEAR(CURDATE())
        ");
        return (float) mysqli_fetch_assoc($result)['total'];
    }

    public static function countAll(): int
    {
        $db = Database::getConnection();
        $result = mysqli_query($db, 'SELECT COUNT(*) AS total FROM transactions');
        return (int) mysqli_fetch_assoc($result)['total'];
    }

    // ----------------------------------------------------------
    // Laporan Harian
    // ----------------------------------------------------------
    public static function findByDate(string $date): array
    {
        $db = Database::getConnection();
        $stmt = mysqli_prepare($db, '
            SELECT t.id, t.kategori_id, c.nama_kategori, c.icon,
                   t.nominal, t.keterangan, t.tanggal_transaksi, t.created_at, t.updated_at
            FROM transactions t
            JOIN categories c ON c.id = t.kategori_id
            WHERE t.tanggal_transaksi = ?
            ORDER BY t.id DESC
        ');
        mysqli_stmt_bind_param($stmt, 's', $date);
        mysqli_stmt_execute($stmt);
        return self::fetchAllHelper($stmt);
    }

    public static function sumByDate(string $date): float
    {
        $db = Database::getConnection();
        $stmt = mysqli_prepare($db, 'SELECT COALESCE(SUM(nominal), 0) AS total FROM transactions WHERE tanggal_transaksi = ?');
        mysqli_stmt_bind_param($stmt, 's', $date);
        mysqli_stmt_execute($stmt);
        $result = self::fetchOneHelper($stmt);
        return (float) ($result['total'] ?? 0);
    }

    // ----------------------------------------------------------
    // Laporan Bulanan per Kategori
    // ----------------------------------------------------------
    public static function sumPerCategoryByMonth(int $month, int $year): array
    {
        $db = Database::getConnection();
        $stmt = mysqli_prepare($db, '
            SELECT c.nama_kategori AS category, COALESCE(SUM(t.nominal), 0) AS total
            FROM transactions t
            JOIN categories c ON c.id = t.kategori_id
            WHERE YEAR(t.tanggal_transaksi) = ?
              AND MONTH(t.tanggal_transaksi) = ?
            GROUP BY c.id, c.nama_kategori
            ORDER BY total DESC
        ');
        mysqli_stmt_bind_param($stmt, 'ii', $year, $month);
        mysqli_stmt_execute($stmt);
        return self::fetchAllHelper($stmt);
    }

    public static function sumByMonth(int $month, int $year): float
    {
        $db = Database::getConnection();
        $stmt = mysqli_prepare($db, "
            SELECT COALESCE(SUM(nominal), 0) AS total
            FROM transactions
            WHERE YEAR(tanggal_transaksi) = ? AND MONTH(tanggal_transaksi) = ?
        ");
        mysqli_stmt_bind_param($stmt, 'ii', $year, $month);
        mysqli_stmt_execute($stmt);
        $result = self::fetchOneHelper($stmt);
        return (float) ($result['total'] ?? 0);
    }

    // ----------------------------------------------------------
    // Statistik Harian (untuk grafik)
    // ----------------------------------------------------------
    public static function statsDaily(string $startDate, string $endDate): array
    {
        $db = Database::getConnection();
        $stmt = mysqli_prepare($db, '
            SELECT tanggal_transaksi AS date, SUM(nominal) AS total
            FROM transactions
            WHERE tanggal_transaksi BETWEEN ? AND ?
            GROUP BY tanggal_transaksi
            ORDER BY tanggal_transaksi ASC
        ');
        mysqli_stmt_bind_param($stmt, 'ss', $startDate, $endDate);
        mysqli_stmt_execute($stmt);
        return self::fetchAllHelper($stmt);
    }
}
