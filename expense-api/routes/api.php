<?php

/**
 * API Router
 *
 * Pola URL:
 *   GET    /api/categories          -> index
 *   GET    /api/categories/{id}     -> show
 *   POST   /api/categories          -> store
 *   PUT    /api/categories/{id}     -> update
 *   DELETE /api/categories/{id}     -> destroy
 *
 *   GET    /api/transactions          -> index
 *   GET    /api/transactions/{id}     -> show
 *   POST   /api/transactions          -> store
 *   PUT    /api/transactions/{id}     -> update
 *   DELETE /api/transactions/{id}     -> destroy
 *
 *   GET    /api/dashboard           -> index
 *   GET    /api/report/daily        -> daily
 *   GET    /api/report/monthly      -> monthly
 *   GET    /api/report/stats        -> stats
 */

class Router
{
    private string $method;
    private string $uri;
    private array  $segments;

    public function __construct()
    {
        $this->method = $_SERVER['REQUEST_METHOD'];

        // Ambil URI tanpa query string
        $fullUri = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);
        // Hapus base path (/expense-api) jika ada
        $basePath = dirname($_SERVER['SCRIPT_NAME']);
        if ($basePath !== '/' && str_starts_with($fullUri, $basePath)) {
            $fullUri = substr($fullUri, strlen($basePath));
        }
        $fullUri = '/' . trim($fullUri, '/');
        $this->uri = $fullUri;
        $this->segments = explode('/', trim($this->uri, '/'));
    }

    public function dispatch(): void
    {
        try {
            $this->handle();
        } catch (PDOException $e) {
            ResponseHandler::error('Terjadi kesalahan database: ' . $e->getMessage(), 500);
        } catch (Throwable $e) {
            ResponseHandler::error($e->getMessage(), 500);
        }
    }

    private function handle(): void
    {
        $segments = $this->segments;

        // Pastikan diawali dengan 'api' -> segments[0] == 'api'
        if (($segments[0] ?? '') !== 'api') {
            ResponseHandler::error('Endpoint tidak ditemukan', 404);
        }

        $resource = $segments[1] ?? '';
        $id       = isset($segments[2]) && is_numeric($segments[2]) ? (int) $segments[2] : null;
        $action   = $segments[2] ?? null;

        // ----- Route: Dashboard -----
        if ($resource === 'dashboard') {
            if ($this->method === 'GET') {
                DashboardController::index();
            }
            ResponseHandler::error('Method tidak diizinkan', 405);
        }

        // ----- Route: Report -----
        if ($resource === 'report') {
            $reportType = $segments[2] ?? '';
            if ($this->method === 'GET') {
                if ($reportType === 'daily') {
                    ReportController::daily();
                } elseif ($reportType === 'monthly') {
                    ReportController::monthly();
                } elseif ($reportType === 'stats') {
                    ReportController::stats();
                }
            }
            ResponseHandler::error('Endpoint tidak ditemukan', 404);
        }

        // ----- Route: Categories -----
        if ($resource === 'categories') {
            switch ($this->method) {
                case 'GET':
                    if ($id !== null) {
                        CategoryController::show($id);
                    } else {
                        CategoryController::index();
                    }
                    break;
                case 'POST':
                    CategoryController::store();
                    break;
                case 'PUT':
                    if ($id === null) ResponseHandler::error('ID kategori wajib diisi');
                    CategoryController::update($id);
                    break;
                case 'DELETE':
                    if ($id === null) ResponseHandler::error('ID kategori wajib diisi');
                    CategoryController::destroy($id);
                    break;
            }
            ResponseHandler::error('Method tidak diizinkan', 405);
        }

        // ----- Route: Transactions -----
        if ($resource === 'transactions') {
            switch ($this->method) {
                case 'GET':
                    if ($id !== null) {
                        TransactionController::show($id);
                    } else {
                        TransactionController::index();
                    }
                    break;
                case 'POST':
                    TransactionController::store();
                    break;
                case 'PUT':
                    if ($id === null) ResponseHandler::error('ID transaksi wajib diisi');
                    TransactionController::update($id);
                    break;
                case 'DELETE':
                    if ($id === null) ResponseHandler::error('ID transaksi wajib diisi');
                    TransactionController::destroy($id);
                    break;
            }
            ResponseHandler::error('Method tidak diizinkan', 405);
        }

        // ----- Not Found -----
        ResponseHandler::error('Endpoint tidak ditemukan', 404);
    }
}
