<?php

class Database
{
    private static ?mysqli $instance = null;

    private static string $host = 'localhost';
    private static string $port = '3306';
    private static string $dbname = 'expense_tracker';
    private static string $username = 'root';
    private static string $password = '1234';

    public static function getConnection(): mysqli
    {
        if (self::$instance === null) {
            $conn = mysqli_connect(
                self::$host,
                self::$username,
                self::$password,
                self::$dbname,
                (int) self::$port
            );

            if (!$conn) {
                throw new RuntimeException(
                    'Koneksi database gagal: ' . mysqli_connect_error()
                );
            }

            mysqli_set_charset($conn, 'utf8mb4');
            self::$instance = $conn;
        }

        return self::$instance;
    }
}
