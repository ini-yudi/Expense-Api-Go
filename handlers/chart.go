package handlers

import (
	"expense-api/middleware"
	"expense-api/repository"
	"expense-api/response"
	"net/http"
	"strconv"
	"time"
)

func HandleChart(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		response.Error(w, "Method tidak diizinkan", http.StatusMethodNotAllowed)
		return
	}

	claims := middleware.GetClaims(r)
	q := r.URL.Query()
	tipe := q.Get("tipe")
	if tipe != "income" && tipe != "expense" {
		tipe = "expense"
	}
	month, _ := strconv.Atoi(q.Get("bulan"))
	year, _ := strconv.Atoi(q.Get("tahun"))
	if month < 1 || month > 12 {
		month = int(time.Now().Month())
	}
	if year < 2000 {
		year = time.Now().Year()
	}

	data, err := repository.GetChartDataByType(claims.UserID, tipe, month, year)
	if err != nil {
		response.Error(w, "Gagal mengambil data grafik", http.StatusInternalServerError)
		return
	}
	response.Success(w, data, "Data grafik berhasil diambil")
}
