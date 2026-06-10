<?php

class ResponseHandler
{
    public static function success(mixed $data = [], string $message = 'Success', int $httpCode = 200): void
    {
        http_response_code($httpCode);
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode([
            'status'  => true,
            'message' => $message,
            'data'    => $data,
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }

    public static function error(string $message, int $httpCode = 400): void
    {
        http_response_code($httpCode);
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode([
            'status'  => false,
            'message' => $message,
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }
}
