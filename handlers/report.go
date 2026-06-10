package handlers

import (
	"expense-api/middleware"
	"expense-api/models"
	"expense-api/repository"
	"expense-api/response"
	"net/http"
	"strconv"
	"strings"
	"time"
)

func HandleReports(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		response.Error(w, "Method tidak diizinkan", http.StatusMethodNotAllowed)
		return
	}

	path := strings.TrimPrefix(r.URL.Path, "/api/report/")

	switch path {
	case "daily":
		handleDailyReport(w, r)
	case "monthly":
		handleMonthlyReport(w, r)
	case "stats":
		handleStatsReport(w, r)
	default:
		response.Error(w, "Endpoint tidak ditemukan", http.StatusNotFound)
	}
}

func handleDailyReport(w http.ResponseWriter, r *http.Request) {
	date := r.URL.Query().Get("date")
	if date == "" {
		response.Error(w, "Parameter date wajib diisi (YYYY-MM-DD)", http.StatusBadRequest)
		return
	}

	claims := middleware.GetClaims(r)
	report, err := repository.GetDailyReport(claims.UserID, date)
	if err != nil {
		response.Error(w, "Gagal mengambil laporan harian", http.StatusInternalServerError)
		return
	}
	if report.Transactions == nil {
		report.Transactions = []models.Transaction{}
	}
	response.Success(w, report, "Laporan harian berhasil diambil")
}

func handleMonthlyReport(w http.ResponseWriter, r *http.Request) {
	monthStr := r.URL.Query().Get("month")
	yearStr := r.URL.Query().Get("year")

	month, err := strconv.Atoi(monthStr)
	if err != nil || month < 1 || month > 12 {
		month = int(time.Now().Month())
	}
	year, err := strconv.Atoi(yearStr)
	if err != nil || year < 2000 || year > 2100 {
		year = time.Now().Year()
	}

	claims := middleware.GetClaims(r)
	report, err := repository.GetMonthlyReport(claims.UserID, month, year)
	if err != nil {
		response.Error(w, "Gagal mengambil laporan bulanan", http.StatusInternalServerError)
		return
	}
	if report.Categories == nil {
		report.Categories = []models.MonthlyCategoryTotal{}
	}
	response.Success(w, report, "Laporan bulanan berhasil diambil")
}

func handleStatsReport(w http.ResponseWriter, r *http.Request) {
	startDate := r.URL.Query().Get("start_date")
	endDate := r.URL.Query().Get("end_date")

	if startDate == "" {
		startDate = time.Now().Format("2006-01") + "-01"
	}
	if endDate == "" {
		endDate = time.Now().Format("2006-01-02")
	}

	claims := middleware.GetClaims(r)
	stats, err := repository.GetStats(claims.UserID, startDate, endDate)
	if err != nil {
		response.Error(w, "Gagal mengambil statistik", http.StatusInternalServerError)
		return
	}
	if stats == nil {
		stats = []models.StatsItem{}
	}
	response.Success(w, stats, "Statistik berhasil diambil")
}
