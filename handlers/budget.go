package handlers

import (
	"encoding/json"
	"expense-api/middleware"
	"expense-api/repository"
	"expense-api/response"
	"net/http"
	"strconv"
	"strings"
	"time"
)

func HandleBudgets(w http.ResponseWriter, r *http.Request) {
	path := strings.TrimPrefix(r.URL.Path, "/api/budgets")
	path = strings.TrimPrefix(path, "/")

	if path == "" {
		switch r.Method {
		case http.MethodGet:
			listBudgets(w, r)
		default:
			response.Error(w, "Method tidak diizinkan", http.StatusMethodNotAllowed)
		}
		return
	}

	id, err := strconv.Atoi(path)
	if err != nil {
		response.Error(w, "ID tidak valid", http.StatusBadRequest)
		return
	}

	switch r.Method {
	case http.MethodDelete:
		deleteBudget(w, r, id)
	default:
		response.Error(w, "Method tidak diizinkan", http.StatusMethodNotAllowed)
	}
}

func listBudgets(w http.ResponseWriter, r *http.Request) {
	claims := middleware.GetClaims(r)
	q := r.URL.Query()
	bulan, _ := strconv.Atoi(q.Get("bulan"))
	tahun, _ := strconv.Atoi(q.Get("tahun"))
	if bulan < 1 || bulan > 12 {
		bulan = int(time.Now().Month())
	}
	if tahun < 2000 {
		tahun = time.Now().Year()
	}

	budgets, err := repository.GetAllBudgetStatus(claims.UserID, bulan, tahun)
	if err != nil {
		response.Error(w, "Gagal mengambil budget", http.StatusInternalServerError)
		return
	}
	response.Success(w, budgets, "Data budget berhasil diambil")
}

func HandleBudgetUpsert(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		response.Error(w, "Method tidak diizinkan", http.StatusMethodNotAllowed)
		return
	}

	claims := middleware.GetClaims(r)
	var input struct {
		KategoriID int     `json:"kategori_id"`
		Jumlah     float64 `json:"jumlah"`
		Bulan      int     `json:"bulan"`
		Tahun      int     `json:"tahun"`
	}
	if err := json.NewDecoder(r.Body).Decode(&input); err != nil {
		response.Error(w, "Format JSON tidak valid", http.StatusBadRequest)
		return
	}
	if input.KategoriID <= 0 || input.Jumlah <= 0 {
		response.Error(w, "Kategori dan jumlah wajib diisi", http.StatusBadRequest)
		return
	}
	if input.Bulan < 1 || input.Bulan > 12 {
		input.Bulan = int(time.Now().Month())
	}
	if input.Tahun < 2000 {
		input.Tahun = time.Now().Year()
	}

	budget, err := repository.UpsertBudget(claims.UserID, input.KategoriID, input.Jumlah, input.Bulan, input.Tahun)
	if err != nil {
		response.Error(w, "Gagal menyimpan budget", http.StatusInternalServerError)
		return
	}
	response.Success(w, budget, "Budget berhasil disimpan")
}

func deleteBudget(w http.ResponseWriter, r *http.Request, id int) {
	if err := repository.DeleteBudget(id); err != nil {
		response.Error(w, "Gagal menghapus budget", http.StatusInternalServerError)
		return
	}
	response.Success(w, nil, "Budget berhasil dihapus")
}
