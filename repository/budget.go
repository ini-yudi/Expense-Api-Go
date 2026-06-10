package repository

import (
	"expense-api/config"
	"expense-api/models"
)

func GetBudgets(userID, bulan, tahun int) ([]models.BudgetStatus, error) {
	rows, err := config.DB.Query(`
		SELECT b.id, b.kategori_id, c.nama_kategori, b.jumlah, b.bulan, b.tahun
		FROM budgets b
		JOIN categories c ON c.id = b.kategori_id
		WHERE b.user_id = ? AND b.bulan = ? AND b.tahun = ?
		ORDER BY c.nama_kategori ASC
	`, userID, bulan, tahun)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var list []models.BudgetStatus
	for rows.Next() {
		var b models.BudgetStatus
		if err := rows.Scan(&b.ID, &b.KategoriID, &b.NamaKategori, &b.Jumlah, &b.Bulan, &b.Tahun); err != nil {
			return nil, err
		}
		terpakai, _ := sumByCategoryAndType(userID, b.KategoriID, "expense", bulan, tahun)
		b.Terpakai = terpakai
		b.Sisa = b.Jumlah - terpakai
		if b.Jumlah > 0 {
			b.Persen = (terpakai / b.Jumlah) * 100
		}
		list = append(list, b)
	}
	return list, nil
}

func GetBudgetByCategory(userID, kategoriID, bulan, tahun int) (*models.Budget, error) {
	row := config.DB.QueryRow(`
		SELECT id, kategori_id, jumlah, bulan, tahun FROM budgets
		WHERE user_id = ? AND kategori_id = ? AND bulan = ? AND tahun = ?
	`, userID, kategoriID, bulan, tahun)

	var b models.Budget
	err := row.Scan(&b.ID, &b.KategoriID, &b.Jumlah, &b.Bulan, &b.Tahun)
	if err != nil {
		return nil, err
	}
	return &b, nil
}

func UpsertBudget(userID, kategoriID int, jumlah float64, bulan, tahun int) (*models.Budget, error) {
	existing, _ := GetBudgetByCategory(userID, kategoriID, bulan, tahun)
	if existing != nil {
		_, err := config.DB.Exec(
			"UPDATE budgets SET jumlah = ? WHERE id = ?",
			jumlah, existing.ID,
		)
		if err != nil {
			return nil, err
		}
		return GetBudgetByCategory(userID, kategoriID, bulan, tahun)
	}

	result, err := config.DB.Exec(
		"INSERT INTO budgets (user_id, kategori_id, jumlah, bulan, tahun) VALUES (?, ?, ?, ?, ?)",
		userID, kategoriID, jumlah, bulan, tahun,
	)
	if err != nil {
		return nil, err
	}
	id, _ := result.LastInsertId()
	return &models.Budget{ID: int(id), KategoriID: kategoriID, Jumlah: jumlah, Bulan: bulan, Tahun: tahun}, nil
}

func DeleteBudget(id int) error {
	_, err := config.DB.Exec("DELETE FROM budgets WHERE id = ?", id)
	return err
}

func GetAllBudgetStatus(userID, bulan, tahun int) ([]models.BudgetStatus, error) {
	return GetBudgets(userID, bulan, tahun)
}
