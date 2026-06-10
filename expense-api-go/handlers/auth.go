package handlers

import (
	"encoding/json"
	"expense-api/auth"
	"expense-api/models"
	"expense-api/repository"
	"expense-api/response"
	"net/http"
	"strings"

	"golang.org/x/crypto/bcrypt"
)

func HandleLogin(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		response.Error(w, "Method tidak diizinkan", http.StatusMethodNotAllowed)
		return
	}

	var req models.LoginRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response.Error(w, "Format JSON tidak valid", http.StatusBadRequest)
		return
	}

	req.Username = strings.TrimSpace(req.Username)
	if req.Username == "" || req.Password == "" {
		response.Error(w, "Username dan password wajib diisi", http.StatusBadRequest)
		return
	}

	user, err := repository.FindUserByUsername(req.Username)
	if err != nil {
		response.Error(w, "Username atau password salah", http.StatusUnauthorized)
		return
	}

	if err := bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(req.Password)); err != nil {
		response.Error(w, "Username atau password salah", http.StatusUnauthorized)
		return
	}

	token, err := auth.GenerateToken(user)
	if err != nil {
		response.Error(w, "Gagal membuat token", http.StatusInternalServerError)
		return
	}

	resp := models.LoginResponse{
		Token: token,
		User:  *user,
	}
	resp.User.Password = ""
	response.Success(w, resp, "Login berhasil")
}

func HandleRegister(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		response.Error(w, "Method tidak diizinkan", http.StatusMethodNotAllowed)
		return
	}

	var req models.RegisterRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response.Error(w, "Format JSON tidak valid", http.StatusBadRequest)
		return
	}

	req.Username = strings.TrimSpace(req.Username)
	if req.Username == "" {
		response.Error(w, "Username wajib diisi", http.StatusBadRequest)
		return
	}
	if len(req.Password) < 6 {
		response.Error(w, "Password minimal 6 karakter", http.StatusBadRequest)
		return
	}

	exists, err := repository.UserExists(req.Username)
	if err != nil {
		response.Error(w, "Gagal memeriksa username", http.StatusInternalServerError)
		return
	}
	if exists {
		response.Error(w, "Username sudah digunakan", http.StatusConflict)
		return
	}

	hash, err := bcrypt.GenerateFromPassword([]byte(req.Password), bcrypt.DefaultCost)
	if err != nil {
		response.Error(w, "Gagal memproses password", http.StatusInternalServerError)
		return
	}

	user, err := repository.CreateUser(req.Username, string(hash))
	if err != nil {
		response.Error(w, "Gagal mendaftarkan user", http.StatusInternalServerError)
		return
	}

	token, err := auth.GenerateToken(user)
	if err != nil {
		response.Error(w, "Gagal membuat token", http.StatusInternalServerError)
		return
	}

	resp := models.LoginResponse{
		Token: token,
		User:  *user,
	}
	response.Created(w, resp, "Registrasi berhasil")
}
