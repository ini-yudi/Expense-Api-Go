package models

type Category struct {
	ID          int    `json:"id"`
	NamaKategori string `json:"nama_kategori"`
	Icon        string `json:"icon"`
	CreatedAt   string `json:"created_at,omitempty"`
	UpdatedAt   string `json:"updated_at,omitempty"`
}
