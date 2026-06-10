package handlers

import (
	"encoding/json"
	"expense-api/middleware"
	"expense-api/models"
	"expense-api/repository"
	"expense-api/response"
	"net/http"
	"strconv"
	"strings"
)

type transactionInput struct {
	KategoriID       int     `json:"kategori_id"`
	Tipe             string  `json:"tipe"`
	Nominal          float64 `json:"nominal"`
	Keterangan       *string `json:"keterangan"`
	TanggalTransaksi string  `json:"tanggal_transaksi"`
}

func HandleTransactions(w http.ResponseWriter, r *http.Request) {
	idStr := strings.TrimPrefix(r.URL.Path, "/api/transactions")
	idStr = strings.TrimPrefix(idStr, "/")

	if idStr == "" {
		switch r.Method {
		case http.MethodGet:
			listTransactions(w, r)
		case http.MethodPost:
			createTransaction(w, r)
		default:
			response.Error(w, "Method tidak diizinkan", http.StatusMethodNotAllowed)
		}
		return
	}

	id, err := strconv.Atoi(idStr)
	if err != nil {
		response.Error(w, "ID transaksi tidak valid", http.StatusBadRequest)
		return
	}

	switch r.Method {
	case http.MethodGet:
		getTransaction(w, r, id)
	case http.MethodPut:
		updateTransaction(w, r, id)
	case http.MethodDelete:
		deleteTransaction(w, r, id)
	default:
		response.Error(w, "Method tidak diizinkan", http.StatusMethodNotAllowed)
	}
}

func listTransactions(w http.ResponseWriter, r *http.Request) {
	claims := middleware.GetClaims(r)
	q := r.URL.Query()
	transactions, err := repository.GetAllTransactions(
		claims.UserID,
		q.Get("tipe"),
		q.Get("search"),
		q.Get("date_from"),
		q.Get("date_to"),
		0,
	)
	if err != nil {
		response.Error(w, "Gagal mengambil data transaksi", http.StatusInternalServerError)
		return
	}
	if transactions == nil {
		transactions = []models.Transaction{}
	}
	response.Success(w, transactions, "Daftar transaksi berhasil diambil")
}

func getTransaction(w http.ResponseWriter, r *http.Request, id int) {
	transaction, err := repository.GetTransactionByID(id)
	if err != nil {
		response.Error(w, "Transaksi tidak ditemukan", http.StatusNotFound)
		return
	}
	response.Success(w, transaction, "Detail transaksi berhasil diambil")
}

func createTransaction(w http.ResponseWriter, r *http.Request) {
	var input transactionInput
	if err := json.NewDecoder(r.Body).Decode(&input); err != nil {
		response.Error(w, "Format JSON tidak valid", http.StatusBadRequest)
		return
	}

	if input.KategoriID <= 0 {
		response.Error(w, "Kategori wajib dipilih", http.StatusBadRequest)
		return
	}
	if input.Tipe != "income" && input.Tipe != "expense" {
		response.Error(w, "Tipe harus income atau expense", http.StatusBadRequest)
		return
	}
	if input.Nominal <= 0 {
		response.Error(w, "Nominal wajib lebih dari 0", http.StatusBadRequest)
		return
	}
	if input.TanggalTransaksi == "" {
		response.Error(w, "Tanggal transaksi wajib diisi", http.StatusBadRequest)
		return
	}

	_, err := repository.GetCategoryByID(input.KategoriID)
	if err != nil {
		response.Error(w, "Kategori tidak ditemukan", http.StatusNotFound)
		return
	}

	claims := middleware.GetClaims(r)
	transaction, err := repository.CreateTransaction(claims.UserID, input.KategoriID, input.Tipe, input.Nominal, input.Keterangan, input.TanggalTransaksi)
	if err != nil {
		response.Error(w, "Gagal menambahkan transaksi", http.StatusInternalServerError)
		return
	}
	response.Created(w, transaction, "Transaksi berhasil ditambahkan")
}

func updateTransaction(w http.ResponseWriter, r *http.Request, id int) {
	existing, err := repository.GetTransactionByID(id)
	if err != nil {
		response.Error(w, "Transaksi tidak ditemukan", http.StatusNotFound)
		return
	}

	var input transactionInput
	if err := json.NewDecoder(r.Body).Decode(&input); err != nil {
		response.Error(w, "Format JSON tidak valid", http.StatusBadRequest)
		return
	}

	kategoriID := input.KategoriID
	if kategoriID <= 0 {
		kategoriID = existing.KategoriID
	}
	tipe := input.Tipe
	if tipe != "income" && tipe != "expense" {
		tipe = existing.Tipe
	}
	nominal := input.Nominal
	if nominal <= 0 {
		nominal = existing.Nominal
	}
	tanggal := input.TanggalTransaksi
	if tanggal == "" {
		tanggal = existing.TanggalTransaksi
	}
	keterangan := input.Keterangan
	if keterangan == nil {
		keterangan = existing.Keterangan
	}

	transaction, err := repository.UpdateTransaction(id, kategoriID, tipe, nominal, keterangan, tanggal)
	if err != nil {
		response.Error(w, "Gagal memperbarui transaksi", http.StatusInternalServerError)
		return
	}
	response.Success(w, transaction, "Transaksi berhasil diperbarui")
}

func deleteTransaction(w http.ResponseWriter, r *http.Request, id int) {
	if err := repository.DeleteTransaction(id); err != nil {
		response.Error(w, "Gagal menghapus transaksi", http.StatusInternalServerError)
		return
	}
	response.Success(w, nil, "Transaksi berhasil dihapus")
}
