package response

import (
	"encoding/json"
	"net/http"
)

type SuccessResponse struct {
	Status  bool        `json:"status"`
	Message string      `json:"message"`
	Data    interface{} `json:"data"`
}

type ErrorResponse struct {
	Status  bool   `json:"status"`
	Message string `json:"message"`
}

func JSON(w http.ResponseWriter, status int, data interface{}) {
	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	w.WriteHeader(status)
	json.NewEncoder(w).Encode(data)
}

func Success(w http.ResponseWriter, data interface{}, msg string) {
	JSON(w, http.StatusOK, SuccessResponse{
		Status:  true,
		Message: msg,
		Data:    data,
	})
}

func Created(w http.ResponseWriter, data interface{}, msg string) {
	JSON(w, http.StatusCreated, SuccessResponse{
		Status:  true,
		Message: msg,
		Data:    data,
	})
}

func Error(w http.ResponseWriter, msg string, code int) {
	JSON(w, code, ErrorResponse{
		Status:  false,
		Message: msg,
	})
}
