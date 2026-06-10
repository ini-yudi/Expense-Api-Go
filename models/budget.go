package models

type Budget struct {
	ID           int     `json:"id"`
	UserID       int     `json:"-"`
	KategoriID   int     `json:"kategori_id"`
	NamaKategori string  `json:"nama_kategori,omitempty"`
	Jumlah       float64 `json:"jumlah"`
	Bulan        int     `json:"bulan"`
	Tahun        int     `json:"tahun"`
	CreatedAt    string  `json:"created_at,omitempty"`
	UpdatedAt    string  `json:"updated_at,omitempty"`
}

type BudgetStatus struct {
	Budget
	Terpakai float64 `json:"terpakai"`
	Sisa     float64 `json:"sisa"`
	Persen   float64 `json:"persen"`
}
