<?php

class ReportController
{
    public static function daily(): void
    {
        $date = $_GET['date'] ?? '';
        $data = TransactionService::dailyReport($date);
        ResponseHandler::success($data, 'Laporan harian berhasil diambil');
    }

    public static function monthly(): void
    {
        $month = (int) ($_GET['month'] ?? 0);
        $year  = (int) ($_GET['year'] ?? 0);

        if ($month === 0) {
            $month = (int) date('m');
        }
        if ($year === 0) {
            $year = (int) date('Y');
        }

        $data = TransactionService::monthlyReport($month, $year);
        ResponseHandler::success($data, 'Laporan bulanan berhasil diambil');
    }

    public static function stats(): void
    {
        $startDate = $_GET['start_date'] ?? date('Y-m-01');
        $endDate   = $_GET['end_date']   ?? date('Y-m-t');

        $data = TransactionService::stats($startDate, $endDate);
        ResponseHandler::success($data, 'Statistik berhasil diambil');
    }
}
