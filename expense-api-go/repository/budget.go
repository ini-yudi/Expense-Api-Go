package repository

import (
	"database/sql"
	"expense-api/config"
	"expense-api/models"
	"time"
)

func GetBudgets(userID, bulan, tahun int) ([]models.BudgetStatus, error) {
	rows, err := config.DB.Query(`
		SELECT b.id, b.kategori_id, c.nama_kategori, b.jumlah, b.bulan, b.tahun, b.priority
		FROM budgets b
		JOIN categories c ON c.id = b.kategori_id
		WHERE b.user_id = ? AND b.bulan = ? AND b.tahun = ?
		ORDER BY b.priority ASC, c.nama_kategori ASC
	`, userID, bulan, tahun)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var list []models.BudgetStatus
	for rows.Next() {
		var b models.BudgetStatus
		if err := rows.Scan(&b.ID, &b.KategoriID, &b.NamaKategori, &b.Jumlah, &b.Bulan, &b.Tahun, &b.Priority); err != nil {
			return nil, err
		}

		expense, _ := sumByCategoryAndType(userID, b.KategoriID, "expense", bulan, tahun)
		income, _ := sumByCategoryAndType(userID, b.KategoriID, "income", bulan, tahun)

		b.ExpenseTotal = expense
		b.IncomeTotal = income
		b.Terpakai = expense
		b.TerpakaiNet = expense - income
		b.Sisa = b.Jumlah - b.TerpakaiNet

		if b.Jumlah > 0 {
			b.Persen = (b.TerpakaiNet / b.Jumlah) * 100
			if b.Persen < 0 {
				b.Persen = 0
			}
		}

		list = append(list, b)
	}

	list = cascadeBudgetSurplus(&list)

	return list, nil
}

func cascadeBudgetSurplus(budgets *[]models.BudgetStatus) []models.BudgetStatus {
	modified := *budgets
	for i := range modified {
		if modified[i].Sisa >= 0 {
			continue
		}
		overBudget := -modified[i].Sisa
		for j := len(modified) - 1; j >= 0; j-- {
			if i == j || modified[j].Sisa <= 0 || modified[j].Priority >= modified[i].Priority {
				continue
			}
			available := modified[j].Sisa
			if available >= overBudget {
				modified[j].Sisa -= overBudget
				modified[i].Sisa += overBudget
				overBudget = 0
				break
			}
			modified[j].Sisa = 0
			modified[i].Sisa += available
			overBudget -= available
		}
	}
	return modified
}

func GetBudgetByCategory(userID, kategoriID, bulan, tahun int) (*models.Budget, error) {
	row := config.DB.QueryRow(`
		SELECT id, kategori_id, jumlah, bulan, tahun, COALESCE(priority, 5)
		FROM budgets
		WHERE user_id = ? AND kategori_id = ? AND bulan = ? AND tahun = ?
	`, userID, kategoriID, bulan, tahun)

	var b models.Budget
	err := row.Scan(&b.ID, &b.KategoriID, &b.Jumlah, &b.Bulan, &b.Tahun, &b.Priority)
	if err != nil {
		return nil, err
	}
	return &b, nil
}

func UpsertBudget(userID, kategoriID int, jumlah float64, bulan, tahun, priority int) (*models.Budget, error) {
	existing, _ := GetBudgetByCategory(userID, kategoriID, bulan, tahun)
	if existing != nil {
		_, err := config.DB.Exec(
			"UPDATE budgets SET jumlah = ?, priority = ? WHERE id = ?",
			jumlah, priority, existing.ID,
		)
		if err != nil {
			return nil, err
		}
		return GetBudgetByCategory(userID, kategoriID, bulan, tahun)
	}

	result, err := config.DB.Exec(
		"INSERT INTO budgets (user_id, kategori_id, jumlah, bulan, tahun, priority) VALUES (?, ?, ?, ?, ?, ?)",
		userID, kategoriID, jumlah, bulan, tahun, priority,
	)
	if err != nil {
		return nil, err
	}
	id, _ := result.LastInsertId()
	return &models.Budget{
		ID:         int(id),
		KategoriID: kategoriID,
		Jumlah:     jumlah,
		Bulan:      bulan,
		Tahun:      tahun,
		Priority:   priority,
	}, nil
}

func DeleteBudget(id int) error {
	_, err := config.DB.Exec("DELETE FROM budgets WHERE id = ?", id)
	return err
}

func GetAllBudgetStatus(userID, bulan, tahun int) ([]models.BudgetStatus, error) {
	return GetBudgets(userID, bulan, tahun)
}

func RecalculateBudgetFromTransaction(userID, kategoriID int, tipe string, nominal float64, tanggal string) error {
	t, _ := time.Parse("2006-01-02", tanggal)
	bulan := int(t.Month())
	tahun := t.Year()

	budget, err := GetBudgetByCategory(userID, kategoriID, bulan, tahun)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil
		}
		return err
	}

	_ = budget

	expense, _ := sumByCategoryAndType(userID, kategoriID, "expense", bulan, tahun)
	income, _ := sumByCategoryAndType(userID, kategoriID, "income", bulan, tahun)
	_ = expense
	_ = income

	if tipe == "income" && income > 0 {
		autoAllocateFromSalary(userID, kategoriID, nominal, bulan, tahun)
	}

	return nil
}

func autoAllocateFromSalary(userID, salaryKategoriID int, salaryAmount float64, bulan, tahun int) {
	allocations := getBudgetAllocations(userID, salaryKategoriID)
	if len(allocations) == 0 {
		return
	}

	totalPersen := 0.0
	for _, a := range allocations {
		totalPersen += a.Persen
	}
	if totalPersen <= 0 {
		return
	}

	for _, a := range allocations {
		allocAmount := salaryAmount * (a.Persen / 100)
		if allocAmount <= 0 {
			continue
		}
		existing, _ := GetBudgetByCategory(userID, a.KategoriID, bulan, tahun)
		if existing != nil {
			config.DB.Exec("UPDATE budgets SET jumlah = jumlah + ? WHERE id = ?", allocAmount, existing.ID)
		} else {
			config.DB.Exec(
				"INSERT INTO budgets (user_id, kategori_id, jumlah, bulan, tahun, priority) VALUES (?, ?, ?, ?, ?, 5)",
				userID, a.KategoriID, allocAmount, bulan, tahun,
			)
		}
	}
}

func getBudgetAllocations(userID, salaryKategoriID int) []models.BudgetAllocation {
	rows, err := config.DB.Query(`
		SELECT kategori_id, persen FROM budget_allocations
		WHERE user_id = ? AND kategori_id != ?
		ORDER BY id ASC
	`, userID, salaryKategoriID)
	if err != nil {
		return nil
	}
	defer rows.Close()

	var list []models.BudgetAllocation
	for rows.Next() {
		var a models.BudgetAllocation
		if err := rows.Scan(&a.KategoriID, &a.Persen); err != nil {
			continue
		}
		list = append(list, a)
	}
	return list
}

func GetAllocations(userID, salaryKategoriID int) []models.BudgetAllocation {
	return getBudgetAllocations(userID, salaryKategoriID)
}

func SetBudgetAllocation(userID, kategoriID int, persen float64) error {
	_, err := config.DB.Exec(`
		INSERT INTO budget_allocations (user_id, kategori_id, persen)
		VALUES (?, ?, ?)
		ON DUPLICATE KEY UPDATE persen = VALUES(persen)
	`, userID, kategoriID, persen)
	return err
}

func RolloverBudgets(userID, fromBulan, fromTahun, toBulan, toTahun int) error {
	budgets, err := GetBudgets(userID, fromBulan, fromTahun)
	if err != nil {
		return err
	}

	for _, b := range budgets {
		if b.Sisa <= 0 {
			continue
		}
		existing, _ := GetBudgetByCategory(userID, b.KategoriID, toBulan, toTahun)
		if existing != nil {
			config.DB.Exec("UPDATE budgets SET jumlah = jumlah + ? WHERE id = ?", b.Sisa, existing.ID)
		} else {
			config.DB.Exec(
				"INSERT INTO budgets (user_id, kategori_id, jumlah, bulan, tahun, priority) VALUES (?, ?, ?, ?, ?, ?)",
				userID, b.KategoriID, b.Sisa, toBulan, toTahun, b.Priority,
			)
		}
	}
	return nil
}

func GetBudgetSummary(userID, bulan, tahun int) (map[string]float64, error) {
	budgets, err := GetBudgets(userID, bulan, tahun)
	if err != nil {
		return nil, err
	}

	summary := map[string]float64{
		"total_budget":      0,
		"total_terpakai":    0,
		"total_terpakai_net": 0,
		"total_sisa":        0,
		"total_income":      0,
		"total_expense":     0,
	}
	for _, b := range budgets {
		summary["total_budget"] += b.Jumlah
		summary["total_terpakai"] += b.Terpakai
		summary["total_terpakai_net"] += b.TerpakaiNet
		summary["total_sisa"] += b.Sisa
		summary["total_income"] += b.IncomeTotal
		summary["total_expense"] += b.ExpenseTotal
	}
	return summary, nil
}