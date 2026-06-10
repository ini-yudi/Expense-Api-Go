<?php
/**
 * Router for PHP built-in development server
 *
 * Usage:
 *   php -S 0.0.0.0:8080 -t . router.php
 *
 * File exists? Serve it directly. Otherwise, route to index.php.
 */

$uri = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);

// Jika file/folder benar-benar ada di doc root, serving langsung
if ($uri !== '/' && file_exists(__DIR__ . $uri)) {
    return false;
}

// Selainnya, serahkan ke index.php
require __DIR__ . '/index.php';
