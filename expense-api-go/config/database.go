package config

import (
	"database/sql"
	"fmt"
	"net/url"
	"os"
	"strings"
	"time"

	_ "github.com/go-sql-driver/mysql"
	"golang.org/x/crypto/bcrypt"
)

var DB *sql.DB

func getEnv(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}

func InitDB() error {
	mysqlURL := getEnv("MYSQL_PUBLIC_URL", "")
	if mysqlURL == "" {
		mysqlURL = getEnv("MYSQL_URL", "")
	}
	if mysqlURL == "" {
		mysqlURL = getEnv("DATABASE_URL", "")
	}

	var dsn string
	if mysqlURL != "" {
		u, err := url.Parse(mysqlURL)
		if err != nil {
			return fmt.Errorf("gagal parse database URL: %w", err)
		}
		pass, _ := u.User.Password()
		host := u.Hostname()
		port := u.Port()
		if port == "" {
			port = "3306"
		}
		dbname := strings.TrimPrefix(u.Path, "/")
		dsn = fmt.Sprintf("%s:%s@tcp(%s:%s)/%s?charset=utf8mb4&parseTime=true",
			u.User.Username(), pass, host, port, dbname,
		)
	} else {
		host := getEnv("MYSQLHOST", getEnv("MYSQL_HOST", "localhost"))
		port := getEnv("MYSQLPORT", getEnv("MYSQL_PORT", "3306"))
		user := getEnv("MYSQLUSER", getEnv("MYSQL_USER", "root"))
		pass := getEnv("MYSQLPASSWORD", getEnv("MYSQL_PASSWORD", "1234"))
		dbname := getEnv("MYSQLDATABASE", getEnv("MYSQL_DATABASE", "expense_tracker"))
		dsn = fmt.Sprintf("%s:%s@tcp(%s:%s)/%s?charset=utf8mb4&parseTime=true",
			user, pass, host, port, dbname,
		)
	}

	var err error
	DB, err = sql.Open("mysql", dsn)
	if err != nil {
		return fmt.Errorf("gagal buka koneksi: %w", err)
	}

	DB.SetMaxOpenConns(10)
	DB.SetMaxIdleConns(5)
	DB.SetConnMaxLifetime(5 * time.Minute)

	if err = DB.Ping(); err != nil {
		return fmt.Errorf("gagal ping database: %w", err)
	}

	if err = autoMigrate(); err != nil {
		return fmt.Errorf("gagal migrasi database: %w", err)
	}

	return nil
}

func autoMigrate() error {
	migrate := []string{
		`CREATE TABLE IF NOT EXISTS users (
			id INT AUTO_INCREMENT PRIMARY KEY,
			username VARCHAR(100) NOT NULL UNIQUE,
			password VARCHAR(255) NOT NULL,
			created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
			updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
		)`,
		`CREATE TABLE IF NOT EXISTS budgets (
			id INT AUTO_INCREMENT PRIMARY KEY,
			user_id INT NOT NULL,
			kategori_id INT NOT NULL,
			jumlah DOUBLE NOT NULL DEFAULT 0,
			bulan INT NOT NULL,
			tahun INT NOT NULL,
			created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
			updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
			UNIQUE KEY unique_budget (user_id, kategori_id, bulan, tahun)
		)`,
		`CREATE TABLE IF NOT EXISTS categories (
			id INT AUTO_INCREMENT PRIMARY KEY,
			user_id INT NOT NULL DEFAULT 0,
			nama_kategori VARCHAR(100) NOT NULL,
			icon VARCHAR(50) NOT NULL DEFAULT '',
			created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
			updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
		)`,
		`CREATE TABLE IF NOT EXISTS transactions (
			id INT AUTO_INCREMENT PRIMARY KEY,
			user_id INT NOT NULL DEFAULT 0,
			kategori_id INT NOT NULL,
			nominal DOUBLE NOT NULL,
			keterangan TEXT,
			tanggal_transaksi DATE NOT NULL,
			tipe VARCHAR(10) NOT NULL DEFAULT 'expense',
			created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
			updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
		)`,
	}
	for _, q := range migrate {
		if _, err := DB.Exec(q); err != nil {
			return err
		}
	}

	columns := map[string]map[string]string{
		"categories":   {"user_id": "INT NOT NULL DEFAULT 0"},
		"transactions": {"user_id": "INT NOT NULL DEFAULT 0", "tipe": "VARCHAR(10) NOT NULL DEFAULT 'expense'"},
	}
	for table, cols := range columns {
		for col, def := range cols {
			if err := addColumnIfNotExists(table, col, def); err != nil {
				return err
			}
		}
	}

	DB.Exec("UPDATE transactions SET tipe = 'expense' WHERE tipe IS NULL OR tipe = ''")

	if err := seedDefaultUser(); err != nil {
		return err
	}
	return migrateOldData()
}

func addColumnIfNotExists(table, column, definition string) error {
	_, err := DB.Exec("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition)
	if err != nil {
		if strings.Contains(err.Error(), "Duplicate column") {
			return nil
		}
		return err
	}
	return nil
}

func migrateOldData() error {
	DB.Exec("UPDATE categories SET user_id = 1 WHERE user_id = 0")
	DB.Exec("UPDATE transactions SET user_id = 1 WHERE user_id = 0")
	return nil
}

func seedDefaultUser() error {
	var count int
	err := DB.QueryRow("SELECT COUNT(*) FROM users").Scan(&count)
	if err != nil {
		return err
	}
	if count > 0 {
		return nil
	}
	hash, err := bcrypt.GenerateFromPassword([]byte("123456"), bcrypt.DefaultCost)
	if err != nil {
		return err
	}
	_, err = DB.Exec("INSERT INTO users (username, password) VALUES (?, ?)", "admin", string(hash))
	if err != nil {
		return err
	}
	return nil
}
