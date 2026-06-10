package config

import (
	"database/sql"
	"fmt"
	"strings"
	"time"

	_ "github.com/go-sql-driver/mysql"
	"golang.org/x/crypto/bcrypt"
)

var DB *sql.DB

func InitDB() error {
	dsn := fmt.Sprintf("%s:%s@tcp(%s:%s)/%s?charset=utf8mb4&parseTime=true",
		"root",
		"1234",
		"localhost",
		"3306",
		"expense_tracker",
	)

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
