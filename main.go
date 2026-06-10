package main

import (
	"expense-api/config"
	"expense-api/handlers"
	"expense-api/middleware"
	"fmt"
	"log"
	"net/http"
)

func corsMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
		w.Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization")

		if r.Method == http.MethodOptions {
			w.WriteHeader(http.StatusOK)
			return
		}

		next.ServeHTTP(w, r)
	})
}

func main() {
	if err := config.InitDB(); err != nil {
		log.Fatalf("Database error: %v", err)
	}
	defer config.DB.Close()

	mux := http.NewServeMux()

	mux.HandleFunc("/api/auth/login", handlers.HandleLogin)
	mux.HandleFunc("/api/auth/register", handlers.HandleRegister)

	protected := http.NewServeMux()
	protected.HandleFunc("/api/dashboard", handlers.HandleDashboard)
	protected.HandleFunc("/api/categories", handlers.HandleCategories)
	protected.HandleFunc("/api/categories/", handlers.HandleCategories)
	protected.HandleFunc("/api/transactions", handlers.HandleTransactions)
	protected.HandleFunc("/api/transactions/", handlers.HandleTransactions)
	protected.HandleFunc("/api/report/", handlers.HandleReports)
	protected.HandleFunc("/api/chart", handlers.HandleChart)
	protected.HandleFunc("/api/export", handlers.HandleExport)
	protected.HandleFunc("/api/budgets", handlers.HandleBudgets)
	protected.HandleFunc("/api/budgets/", handlers.HandleBudgets)
	protected.HandleFunc("/api/budget", handlers.HandleBudgetUpsert)

	mux.Handle("/api/", middleware.AuthMiddleware(protected))

	addr := ":8080"
	fmt.Println("Expense Tracker API running on http://0.0.0.0" + addr)
	log.Fatal(http.ListenAndServe(addr, corsMiddleware(mux)))
}
