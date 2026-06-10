package handlers

import (
	"encoding/csv"
	"expense-api/middleware"
	"expense-api/repository"
	"expense-api/response"
	"fmt"
	"net/http"
	"strconv"
)

func HandleExport(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		response.Error(w, "Method tidak diizinkan", http.StatusMethodNotAllowed)
		return
	}

	claims := middleware.GetClaims(r)
	q := r.URL.Query()
	tipe := q.Get("tipe")
	dateFrom := q.Get("date_from")
	dateTo := q.Get("date_to")

	transactions, err := repository.GetTransactionsExport(claims.UserID, tipe, dateFrom, dateTo)
	if err != nil {
		response.Error(w, "Gagal mengambil data", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "text/csv; charset=utf-8")
	w.Header().Set("Content-Disposition", "attachment; filename=transactions.csv")

	csvWriter := csv.NewWriter(w)
	csvWriter.Write([]string{"ID", "Kategori", "Tipe", "Nominal", "Keterangan", "Tanggal"})

	for _, t := range transactions {
		keterangan := ""
		if t.Keterangan != nil {
			keterangan = *t.Keterangan
		}
		csvWriter.Write([]string{
			strconv.Itoa(t.ID),
			t.NamaKategori,
			t.Tipe,
			fmt.Sprintf("%.0f", t.Nominal),
			keterangan,
			t.TanggalTransaksi,
		})
	}
	csvWriter.Flush()
}
