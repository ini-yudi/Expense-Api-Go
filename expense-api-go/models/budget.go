package models

type Budget struct {
	ID           int     `json:"id"`
	UserID       int     `json:"-"`
	KategoriID   int     `json:"kategori_id"`
	NamaKategori string  `json:"nama_kategori,omitempty"`
	Jumlah       float64 `json:"jumlah"`
	Bulan        int     `json:"bulan"`
	Tahun        int     `json:"tahun"`
	Priority     int     `json:"priority"`
	CreatedAt    string  `json:"created_at,omitempty"`
	UpdatedAt    string  `json:"updated_at,omitempty"`
}

type BudgetStatus struct {
	Budget
	Terpakai     float64 `json:"terpakai"`
	TerpakaiNet  float64 `json:"terpakai_net"`
	Sisa         float64 `json:"sisa"`
	Persen       float64 `json:"persen"`
	IncomeTotal  float64 `json:"income_total"`
	ExpenseTotal float64 `json:"expense_total"`
}

type BudgetAllocation struct {
	KategoriID int     `json:"kategori_id"`
	Persen     float64 `json:"persen"`
}
