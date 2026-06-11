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

	switch {
	case path == "rollover":
		if r.Method != http.MethodPost {
			response.Error(w, "Method tidak diizinkan", http.StatusMethodNotAllowed)
			return
		}
		handleRollover(w, r)
		return
	case path == "allocation":
		handleAllocation(w, r)
		return
	case path == "summary":
		if r.Method != http.MethodGet {
			response.Error(w, "Method tidak diizinkan", http.StatusMethodNotAllowed)
			return
		}
		handleBudgetSummary(w, r)
		return
	}

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
		Priority   int     `json:"priority"`
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
	if input.Priority < 1 {
		input.Priority = 5
	}

	budget, err := repository.UpsertBudget(claims.UserID, input.KategoriID, input.Jumlah, input.Bulan, input.Tahun, input.Priority)
	if err != nil {
		response.Error(w, "Gagal menyimpan budget", http.StatusInternalServerError)
		return
	}
	response.Success(w, budget, "Budget berhasil disimpan")
}

func handleRollover(w http.ResponseWriter, r *http.Request) {
	claims := middleware.GetClaims(r)
	var input struct {
		DariBulan int `json:"dari_bulan"`
		DariTahun int `json:"dari_tahun"`
		KeBulan   int `json:"ke_bulan"`
		KeTahun   int `json:"ke_tahun"`
	}
	if err := json.NewDecoder(r.Body).Decode(&input); err != nil {
		response.Error(w, "Format JSON tidak valid", http.StatusBadRequest)
		return
	}
	if input.DariBulan < 1 || input.DariBulan > 12 {
		input.DariBulan = int(time.Now().Month()) - 1
	}
	if input.DariTahun < 2000 {
		input.DariTahun = time.Now().Year()
	}
	if input.KeBulan < 1 || input.KeBulan > 12 {
		input.KeBulan = int(time.Now().Month())
	}
	if input.KeTahun < 2000 {
		input.KeTahun = time.Now().Year()
	}

	if err := repository.RolloverBudgets(claims.UserID, input.DariBulan, input.DariTahun, input.KeBulan, input.KeTahun); err != nil {
		response.Error(w, "Gagal rollover budget", http.StatusInternalServerError)
		return
	}
	response.Success(w, nil, "Budget berhasil di-rollover")
}

func handleAllocation(w http.ResponseWriter, r *http.Request) {
	claims := middleware.GetClaims(r)

	switch r.Method {
	case http.MethodGet:
		q := r.URL.Query()
		salaryKategoriID, _ := strconv.Atoi(q.Get("salary_kategori_id"))
		allocations := repository.GetAllocations(claims.UserID, salaryKategoriID)
		if allocations == nil {
			response.Success(w, []interface{}{}, "Data alokasi berhasil diambil")
			return
		}
		response.Success(w, allocations, "Data alokasi berhasil diambil")

	case http.MethodPost:
		var input struct {
			KategoriID int     `json:"kategori_id"`
			Persen     float64 `json:"persen"`
		}
		if err := json.NewDecoder(r.Body).Decode(&input); err != nil {
			response.Error(w, "Format JSON tidak valid", http.StatusBadRequest)
			return
		}
		if input.KategoriID <= 0 || input.Persen <= 0 {
			response.Error(w, "Data tidak valid", http.StatusBadRequest)
			return
		}
		if err := repository.SetBudgetAllocation(claims.UserID, input.KategoriID, input.Persen); err != nil {
			response.Error(w, "Gagal menyimpan alokasi", http.StatusInternalServerError)
			return
		}
		response.Success(w, nil, "Alokasi berhasil disimpan")

	default:
		response.Error(w, "Method tidak diizinkan", http.StatusMethodNotAllowed)
	}
}

func handleBudgetSummary(w http.ResponseWriter, r *http.Request) {
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

	summary, err := repository.GetBudgetSummary(claims.UserID, bulan, tahun)
	if err != nil {
		response.Error(w, "Gagal mengambil ringkasan budget", http.StatusInternalServerError)
		return
	}
	response.Success(w, summary, "Ringkasan budget berhasil diambil")
}

func deleteBudget(w http.ResponseWriter, r *http.Request, id int) {
	if err := repository.DeleteBudget(id); err != nil {
		response.Error(w, "Gagal menghapus budget", http.StatusInternalServerError)
		return
	}
	response.Success(w, nil, "Budget berhasil dihapus")
}
