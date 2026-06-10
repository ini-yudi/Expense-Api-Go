package repository

import (
	"database/sql"
	"expense-api/config"
	"expense-api/models"
)

func FindUserByUsername(username string) (*models.User, error) {
	row := config.DB.QueryRow("SELECT id, username, password FROM users WHERE username = ?", username)
	var u models.User
	if err := row.Scan(&u.ID, &u.Username, &u.Password); err != nil {
		return nil, err
	}
	return &u, nil
}

func CreateUser(username, passwordHash string) (*models.User, error) {
	result, err := config.DB.Exec("INSERT INTO users (username, password) VALUES (?, ?)", username, passwordHash)
	if err != nil {
		return nil, err
	}
	id, _ := result.LastInsertId()
	return &models.User{ID: int(id), Username: username}, nil
}

func UserExists(username string) (bool, error) {
	var count int
	err := config.DB.QueryRow("SELECT COUNT(*) FROM users WHERE username = ?", username).Scan(&count)
	if err != nil {
		return false, err
	}
	return count > 0, nil
}

func GetUserByID(id int) (*models.User, error) {
	row := config.DB.QueryRow("SELECT id, username FROM users WHERE id = ?", id)
	var u models.User
	if err := row.Scan(&u.ID, &u.Username); err != nil {
		if err == sql.ErrNoRows {
			return nil, nil
		}
		return nil, err
	}
	return &u, nil
}
