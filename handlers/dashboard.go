package handlers

import (
	"expense-api/middleware"
	"expense-api/repository"
	"expense-api/response"
	"net/http"
)

func HandleDashboard(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		response.Error(w, "Method tidak diizinkan", http.StatusMethodNotAllowed)
		return
	}

	claims := middleware.GetClaims(r)
	dashboard, err := repository.GetDashboard(claims.UserID)
	if err != nil {
		response.Error(w, "Gagal mengambil data dashboard", http.StatusInternalServerError)
		return
	}
	response.Success(w, dashboard, "Dashboard berhasil diambil")
}
