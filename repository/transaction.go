package repository

import (
	"database/sql"
	"expense-api/config"
	"expense-api/models"
	"fmt"
)

func GetAllTransactions(userID int, tipe, search, dateFrom, dateTo string, kategoriID int) ([]models.Transaction, error) {
	query := `
		SELECT t.id, t.kategori_id, c.nama_kategori, c.icon, t.tipe,
		       t.nominal, t.keterangan, t.tanggal_transaksi
		FROM transactions t
		JOIN categories c ON c.id = t.kategori_id
		WHERE t.user_id = ?`
	args := []interface{}{userID}

	if tipe != "" {
		query += " AND t.tipe = ?"
		args = append(args, tipe)
	}
	if search != "" {
		query += " AND (c.nama_kategori LIKE ? OR t.keterangan LIKE ?)"
		args = append(args, "%"+search+"%", "%"+search+"%")
	}
	if dateFrom != "" {
		query += " AND t.tanggal_transaksi >= ?"
		args = append(args, dateFrom)
	}
	if dateTo != "" {
		query += " AND t.tanggal_transaksi <= ?"
		args = append(args, dateTo)
	}
	if kategoriID > 0 {
		query += " AND t.kategori_id = ?"
		args = append(args, kategoriID)
	}

	query += " ORDER BY t.tanggal_transaksi DESC, t.id DESC"

	rows, err := config.DB.Query(query, args...)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var list []models.Transaction
	for rows.Next() {
		var t models.Transaction
		if err := rows.Scan(&t.ID, &t.KategoriID, &t.NamaKategori, &t.Icon, &t.Tipe,
			&t.Nominal, &t.Keterangan, &t.TanggalTransaksi); err != nil {
			return nil, err
		}
		list = append(list, t)
	}
	return list, nil
}

func GetTransactionByID(id int) (*models.Transaction, error) {
	row := config.DB.QueryRow(`
		SELECT t.id, t.kategori_id, c.nama_kategori, c.icon, t.tipe,
		       t.nominal, t.keterangan, t.tanggal_transaksi
		FROM transactions t
		JOIN categories c ON c.id = t.kategori_id
		WHERE t.id = ?
	`, id)

	var t models.Transaction
	if err := row.Scan(&t.ID, &t.KategoriID, &t.NamaKategori, &t.Icon, &t.Tipe,
		&t.Nominal, &t.Keterangan, &t.TanggalTransaksi); err != nil {
		return nil, err
	}
	return &t, nil
}

func CreateTransaction(userID, kategoriID int, tipe string, nominal float64, keterangan *string, tanggal string) (*models.Transaction, error) {
	result, err := config.DB.Exec(
		"INSERT INTO transactions (user_id, kategori_id, tipe, nominal, keterangan, tanggal_transaksi) VALUES (?, ?, ?, ?, ?, ?)",
		userID, kategoriID, tipe, nominal, keterangan, tanggal,
	)
	if err != nil {
		return nil, err
	}
	id, _ := result.LastInsertId()
	return GetTransactionByID(int(id))
}

func UpdateTransaction(id, kategoriID int, tipe string, nominal float64, keterangan *string, tanggal string) (*models.Transaction, error) {
	_, err := config.DB.Exec(
		"UPDATE transactions SET kategori_id = ?, tipe = ?, nominal = ?, keterangan = ?, tanggal_transaksi = ? WHERE id = ?",
		kategoriID, tipe, nominal, keterangan, tanggal, id,
	)
	if err != nil {
		return nil, err
	}
	return GetTransactionByID(id)
}

func DeleteTransaction(id int) error {
	result, err := config.DB.Exec("DELETE FROM transactions WHERE id = ?", id)
	if err != nil {
		return err
	}
	n, _ := result.RowsAffected()
	if n == 0 {
		return sql.ErrNoRows
	}
	return nil
}

// ----------------------------------------------------------
// Dashboard
// ----------------------------------------------------------

func sumByType(userID int, tipe string, condition string, args ...interface{}) (float64, error) {
	query := fmt.Sprintf("SELECT COALESCE(SUM(nominal), 0) FROM transactions WHERE user_id = ? AND tipe = ? AND %s", condition)
	allArgs := append([]interface{}{userID, tipe}, args...)
	var total float64
	err := config.DB.QueryRow(query, allArgs...).Scan(&total)
	return total, err
}

func sumToday(userID int, tipe string) (float64, error) {
	return sumByType(userID, tipe, "tanggal_transaksi = CURDATE()")
}

func sumThisWeek(userID int, tipe string) (float64, error) {
	return sumByType(userID, tipe, "YEARWEEK(tanggal_transaksi, 1) = YEARWEEK(CURDATE(), 1)")
}

func sumThisMonth(userID int, tipe string) (float64, error) {
	return sumByType(userID, tipe, "YEAR(tanggal_transaksi) = YEAR(CURDATE()) AND MONTH(tanggal_transaksi) = MONTH(CURDATE())")
}

func sumThisYear(userID int, tipe string) (float64, error) {
	return sumByType(userID, tipe, "YEAR(tanggal_transaksi) = YEAR(CURDATE())")
}

func countAll(userID int) (int, error) {
	var total int
	err := config.DB.QueryRow("SELECT COUNT(*) FROM transactions WHERE user_id = ?", userID).Scan(&total)
	return total, err
}

func GetDashboard(userID int) (*models.Dashboard, error) {
	todayIncome, _ := sumToday(userID, "income")
	todayExpense, _ := sumToday(userID, "expense")
	weekIncome, _ := sumThisWeek(userID, "income")
	weekExpense, _ := sumThisWeek(userID, "expense")
	monthIncome, _ := sumThisMonth(userID, "income")
	monthExpense, _ := sumThisMonth(userID, "expense")
	yearIncome, _ := sumThisYear(userID, "income")
	yearExpense, _ := sumThisYear(userID, "expense")
	count, _ := countAll(userID)

	return &models.Dashboard{
		TodayIncome:      todayIncome,
		TodayExpense:     todayExpense,
		WeekIncome:       weekIncome,
		WeekExpense:      weekExpense,
		MonthIncome:      monthIncome,
		MonthExpense:     monthExpense,
		YearIncome:       yearIncome,
		YearExpense:      yearExpense,
		TotalTransaction: count,
		Today:            todayExpense,
		Week:             weekExpense,
		Month:            monthExpense,
		Year:             yearExpense,
		Income:           monthIncome,
		Expense:          monthExpense,
		Net:              monthIncome - monthExpense,
	}, nil
}

// ----------------------------------------------------------
// Reports - Daily
// ----------------------------------------------------------

func FindTransactionsByDate(userID int, date string) ([]models.Transaction, error) {
	rows, err := config.DB.Query(`
		SELECT t.id, t.kategori_id, c.nama_kategori, c.icon, t.tipe,
		       t.nominal, t.keterangan, t.tanggal_transaksi
		FROM transactions t
		JOIN categories c ON c.id = t.kategori_id
		WHERE t.user_id = ? AND t.tanggal_transaksi = ?
		ORDER BY t.id DESC
	`, userID, date)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var list []models.Transaction
	for rows.Next() {
		var t models.Transaction
		if err := rows.Scan(&t.ID, &t.KategoriID, &t.NamaKategori, &t.Icon, &t.Tipe,
			&t.Nominal, &t.Keterangan, &t.TanggalTransaksi); err != nil {
			return nil, err
		}
		list = append(list, t)
	}
	return list, nil
}

func GetDailyReport(userID int, date string) (*models.DailyReport, error) {
	transactions, err := FindTransactionsByDate(userID, date)
	if err != nil {
		return nil, err
	}
	var totalIncome, totalExpense float64
	for _, t := range transactions {
		if t.Tipe == "income" {
			totalIncome += t.Nominal
		} else {
			totalExpense += t.Nominal
		}
	}
	return &models.DailyReport{
		TotalIncome:  totalIncome,
		TotalExpense: totalExpense,
		Transactions: transactions,
	}, nil
}

// ----------------------------------------------------------
// Reports - Monthly
// ----------------------------------------------------------

func SumPerCategoryByMonth(userID, month, year int) ([]models.MonthlyCategoryTotal, error) {
	rows, err := config.DB.Query(`
		SELECT c.nama_kategori, t.tipe, COALESCE(SUM(t.nominal), 0) AS total
		FROM transactions t
		JOIN categories c ON c.id = t.kategori_id
		WHERE t.user_id = ? AND YEAR(t.tanggal_transaksi) = ? AND MONTH(t.tanggal_transaksi) = ?
		GROUP BY c.id, c.nama_kategori, t.tipe
		ORDER BY total DESC
	`, userID, year, month)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var list []models.MonthlyCategoryTotal
	for rows.Next() {
		var m models.MonthlyCategoryTotal
		if err := rows.Scan(&m.NamaKategori, &m.Tipe, &m.Total); err != nil {
			return nil, err
		}
		list = append(list, m)
	}
	return list, nil
}

func GetMonthlyReport(userID, month, year int) (*models.MonthlyReport, error) {
	categories, err := SumPerCategoryByMonth(userID, month, year)
	if err != nil {
		return nil, err
	}
	var grandIncome, grandExpense float64
	for _, c := range categories {
		if c.Tipe == "income" {
			grandIncome += c.Total
		} else {
			grandExpense += c.Total
		}
	}
	return &models.MonthlyReport{
		GrandTotalIncome:  grandIncome,
		GrandTotalExpense: grandExpense,
		Categories:        categories,
	}, nil
}

// ----------------------------------------------------------
// Reports - Stats
// ----------------------------------------------------------

func GetStats(userID int, startDate, endDate string) ([]models.StatsItem, error) {
	rows, err := config.DB.Query(`
		SELECT tanggal_transaksi AS date,
		       COALESCE(SUM(CASE WHEN tipe = 'income' THEN nominal ELSE 0 END), 0) AS income,
		       COALESCE(SUM(CASE WHEN tipe = 'expense' THEN nominal ELSE 0 END), 0) AS expense,
		       COALESCE(SUM(nominal), 0) AS total
		FROM transactions
		WHERE user_id = ? AND tanggal_transaksi BETWEEN ? AND ?
		GROUP BY tanggal_transaksi
		ORDER BY tanggal_transaksi ASC
	`, userID, startDate, endDate)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var list []models.StatsItem
	for rows.Next() {
		var s models.StatsItem
		if err := rows.Scan(&s.Date, &s.Income, &s.Expense, &s.Total); err != nil {
			return nil, err
		}
		list = append(list, s)
	}
	return list, nil
}

// ----------------------------------------------------------
// Chart Data
// ----------------------------------------------------------

func GetChartDataByType(userID int, tipe string, month, year int) (*models.ChartData, error) {
	rows, err := config.DB.Query(`
		SELECT c.nama_kategori, COALESCE(SUM(t.nominal), 0) AS total
		FROM transactions t
		JOIN categories c ON c.id = t.kategori_id
		WHERE t.user_id = ? AND t.tipe = ? AND YEAR(t.tanggal_transaksi) = ? AND MONTH(t.tanggal_transaksi) = ?
		GROUP BY c.id, c.nama_kategori
		ORDER BY total DESC
	`, userID, tipe, year, month)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var data models.ChartData
	for rows.Next() {
		var label string
		var value float64
		if err := rows.Scan(&label, &value); err != nil {
			return nil, err
		}
		data.Labels = append(data.Labels, label)
		data.Values = append(data.Values, value)
	}
	if len(data.Labels) == 0 {
		data.Labels = []string{}
		data.Values = []float64{}
	}
	return &data, nil
}

// ----------------------------------------------------------
// Budget helpers
// ----------------------------------------------------------

func sumByCategoryAndType(userID, kategoriID int, tipe string, month, year int) (float64, error) {
	var total float64
	err := config.DB.QueryRow(`
		SELECT COALESCE(SUM(nominal), 0) FROM transactions
		WHERE user_id = ? AND kategori_id = ? AND tipe = ? AND YEAR(tanggal_transaksi) = ? AND MONTH(tanggal_transaksi) = ?
	`, userID, kategoriID, tipe, year, month).Scan(&total)
	return total, err
}

func CheckBudgetStatus(userID, kategoriID int, month, year int) (float64, float64, error) {
	budget, err := GetBudgetByCategory(userID, kategoriID, month, year)
	if err != nil || budget == nil {
		return 0, 0, err
	}
	spent, err := sumByCategoryAndType(userID, kategoriID, "expense", month, year)
	if err != nil {
		return 0, 0, err
	}
	return budget.Jumlah, spent, nil
}

// ----------------------------------------------------------
// Transactions - Update tipe
// ----------------------------------------------------------

func UpdateTransactionTipe(oldTipe, newTipe string) error {
	_, err := config.DB.Exec("UPDATE transactions SET tipe = ? WHERE tipe = ?", newTipe, oldTipe)
	return err
}

func GetTransactionsExport(userID int, tipe, dateFrom, dateTo string) ([]models.Transaction, error) {
	if tipe == "all" {
		tipe = ""
	}
	query := `
		SELECT t.id, t.kategori_id, c.nama_kategori, c.icon, t.tipe,
		       t.nominal, t.keterangan, t.tanggal_transaksi
		FROM transactions t
		JOIN categories c ON c.id = t.kategori_id
		WHERE t.user_id = ?`
	args := []interface{}{userID}

	if tipe != "" {
		query += " AND t.tipe = ?"
		args = append(args, tipe)
	}
	if dateFrom != "" {
		query += " AND t.tanggal_transaksi >= ?"
		args = append(args, dateFrom)
	}
	if dateTo != "" {
		query += " AND t.tanggal_transaksi <= ?"
		args = append(args, dateTo)
	}
	query += " ORDER BY t.tanggal_transaksi ASC"

	rows, err := config.DB.Query(query, args...)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var list []models.Transaction
	for rows.Next() {
		var t models.Transaction
		if err := rows.Scan(&t.ID, &t.KategoriID, &t.NamaKategori, &t.Icon, &t.Tipe,
			&t.Nominal, &t.Keterangan, &t.TanggalTransaksi); err != nil {
			return nil, err
		}
		list = append(list, t)
	}
	return list, nil
}


