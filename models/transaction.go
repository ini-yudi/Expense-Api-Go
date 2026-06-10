package models

type Transaction struct {
	ID               int     `json:"id"`
	UserID           int     `json:"-"`
	KategoriID       int     `json:"kategori_id"`
	NamaKategori     string  `json:"nama_kategori,omitempty"`
	Icon             string  `json:"icon,omitempty"`
	Tipe             string  `json:"tipe"`
	Nominal          float64 `json:"nominal"`
	Keterangan       *string `json:"keterangan"`
	TanggalTransaksi string  `json:"tanggal_transaksi"`
	CreatedAt        string  `json:"created_at,omitempty"`
	UpdatedAt        string  `json:"updated_at,omitempty"`
}

type Dashboard struct {
	TodayIncome      float64 `json:"today_income"`
	TodayExpense     float64 `json:"today_expense"`
	WeekIncome       float64 `json:"week_income"`
	WeekExpense      float64 `json:"week_expense"`
	MonthIncome      float64 `json:"month_income"`
	MonthExpense     float64 `json:"month_expense"`
	YearIncome       float64 `json:"year_income"`
	YearExpense      float64 `json:"year_expense"`
	TotalTransaction int     `json:"total_transaction"`
	Today            float64 `json:"today"`
	Week             float64 `json:"week"`
	Month            float64 `json:"month"`
	Year             float64 `json:"year"`
	Income           float64 `json:"income"`
	Expense          float64 `json:"expense"`
	Net              float64 `json:"net"`
}

type DailyReport struct {
	TotalIncome  float64       `json:"total_income"`
	TotalExpense float64       `json:"total_expense"`
	Transactions []Transaction `json:"transactions"`
}

type MonthlyCategoryTotal struct {
	NamaKategori string  `json:"nama_kategori"`
	Tipe         string  `json:"tipe"`
	Total        float64 `json:"total"`
}

type MonthlyReport struct {
	GrandTotalIncome  float64                `json:"grand_total_income"`
	GrandTotalExpense float64                `json:"grand_total_expense"`
	Categories        []MonthlyCategoryTotal `json:"categories"`
}

type StatsItem struct {
	Date       string  `json:"date"`
	Total      float64 `json:"total"`
	Income     float64 `json:"income"`
	Expense    float64 `json:"expense"`
}

type ChartData struct {
	Labels []string  `json:"labels"`
	Values []float64 `json:"values"`
}
