<?php

class DashboardController
{
    public static function index(): void
    {
        $data = TransactionService::dashboard();
        ResponseHandler::success($data, 'Ringkasan dashboard berhasil diambil');
    }
}
