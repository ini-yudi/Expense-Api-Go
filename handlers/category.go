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

type categoryInput struct {
	NamaKategori string `json:"nama_kategori"`
	Icon         string `json:"icon"`
}

func HandleCategories(w http.ResponseWriter, r *http.Request) {
	idStr := strings.TrimPrefix(r.URL.Path, "/api/categories")
	idStr = strings.TrimPrefix(idStr, "/")

	if idStr == "" {
		switch r.Method {
		case http.MethodGet:
			listCategories(w, r)
		case http.MethodPost:
			createCategory(w, r)
		default:
			response.Error(w, "Method tidak diizinkan", http.StatusMethodNotAllowed)
		}
		return
	}

	id, err := strconv.Atoi(idStr)
	if err != nil {
		response.Error(w, "ID kategori tidak valid", http.StatusBadRequest)
		return
	}

	switch r.Method {
	case http.MethodGet:
		getCategory(w, r, id)
	case http.MethodPut:
		updateCategory(w, r, id)
	case http.MethodDelete:
		deleteCategory(w, r, id)
	default:
		response.Error(w, "Method tidak diizinkan", http.StatusMethodNotAllowed)
	}
}

func listCategories(w http.ResponseWriter, r *http.Request) {
	claims := middleware.GetClaims(r)
	categories, err := repository.GetAllCategories(claims.UserID)
	if err != nil {
		response.Error(w, "Gagal mengambil data kategori", http.StatusInternalServerError)
		return
	}
	if categories == nil {
		categories = []models.Category{}
	}
	response.Success(w, categories, "Daftar kategori berhasil diambil")
}

func getCategory(w http.ResponseWriter, r *http.Request, id int) {
	category, err := repository.GetCategoryByID(id)
	if err != nil {
		response.Error(w, "Kategori tidak ditemukan", http.StatusNotFound)
		return
	}
	response.Success(w, category, "Detail kategori berhasil diambil")
}

func createCategory(w http.ResponseWriter, r *http.Request) {
	var input categoryInput
	if err := json.NewDecoder(r.Body).Decode(&input); err != nil {
		response.Error(w, "Format JSON tidak valid", http.StatusBadRequest)
		return
	}

	input.NamaKategori = strings.TrimSpace(input.NamaKategori)
	if input.NamaKategori == "" {
		response.Error(w, "Nama kategori wajib diisi", http.StatusBadRequest)
		return
	}
	if input.Icon == "" {
		input.Icon = "category"
	}

	claims := middleware.GetClaims(r)
	category, err := repository.CreateCategory(claims.UserID, input.NamaKategori, input.Icon)
	if err != nil {
		response.Error(w, "Gagal menambahkan kategori", http.StatusInternalServerError)
		return
	}
	response.Created(w, category, "Kategori berhasil ditambahkan")
}

func updateCategory(w http.ResponseWriter, r *http.Request, id int) {
	var input categoryInput
	if err := json.NewDecoder(r.Body).Decode(&input); err != nil {
		response.Error(w, "Format JSON tidak valid", http.StatusBadRequest)
		return
	}

	existing, err := repository.GetCategoryByID(id)
	if err != nil {
		response.Error(w, "Kategori tidak ditemukan", http.StatusNotFound)
		return
	}

	nama := strings.TrimSpace(input.NamaKategori)
	if nama == "" {
		nama = existing.NamaKategori
	}
	icon := input.Icon
	if icon == "" {
		icon = existing.Icon
	}

	category, err := repository.UpdateCategory(id, nama, icon)
	if err != nil {
		response.Error(w, "Gagal memperbarui kategori", http.StatusInternalServerError)
		return
	}
	response.Success(w, category, "Kategori berhasil diperbarui")
}

func deleteCategory(w http.ResponseWriter, r *http.Request, id int) {
	if err := repository.DeleteCategory(id); err != nil {
		response.Error(w, "Gagal menghapus kategori", http.StatusInternalServerError)
		return
	}
	response.Success(w, nil, "Kategori berhasil dihapus")
}
