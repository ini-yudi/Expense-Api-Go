<?php

/**
 * Personal Expense Tracker API
 * Entry Point
 */

// -------------------- CORS Headers --------------------
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

// Handle preflight request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

// -------------------- Autoload --------------------
spl_autoload_register(function (string $class) {
    $dirs = [__DIR__ . '/config', __DIR__ . '/helpers', __DIR__ . '/models', __DIR__ . '/services', __DIR__ . '/controllers'];
    foreach ($dirs as $dir) {
        $file = $dir . '/' . $class . '.php';
        if (file_exists($file)) {
            require_once $file;
            return;
        }
    }
});

// -------------------- Dispatch Router --------------------
require_once __DIR__ . '/routes/api.php';

$router = new Router();
$router->dispatch();
