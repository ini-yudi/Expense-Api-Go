package repository

import (
	"database/sql"
	"expense-api/config"
	"expense-api/models"
)

func GetAllCategories(userID int) ([]models.Category, error) {
	rows, err := config.DB.Query("SELECT id, nama_kategori, icon FROM categories WHERE user_id = ? ORDER BY id ASC", userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var list []models.Category
	for rows.Next() {
		var c models.Category
		if err := rows.Scan(&c.ID, &c.NamaKategori, &c.Icon); err != nil {
			return nil, err
		}
		list = append(list, c)
	}
	return list, nil
}

func GetCategoryByID(id int) (*models.Category, error) {
	row := config.DB.QueryRow("SELECT id, nama_kategori, icon FROM categories WHERE id = ?", id)
	var c models.Category
	if err := row.Scan(&c.ID, &c.NamaKategori, &c.Icon); err != nil {
		return nil, err
	}
	return &c, nil
}

func CreateCategory(userID int, nama, icon string) (*models.Category, error) {
	result, err := config.DB.Exec("INSERT INTO categories (user_id, nama_kategori, icon) VALUES (?, ?, ?)", userID, nama, icon)
	if err != nil {
		return nil, err
	}
	id, _ := result.LastInsertId()
	return GetCategoryByID(int(id))
}

func UpdateCategory(id int, nama, icon string) (*models.Category, error) {
	_, err := config.DB.Exec("UPDATE categories SET nama_kategori = ?, icon = ? WHERE id = ?", nama, icon, id)
	if err != nil {
		return nil, err
	}
	return GetCategoryByID(id)
}

func DeleteCategory(id int) error {
	result, err := config.DB.Exec("DELETE FROM categories WHERE id = ?", id)
	if err != nil {
		return err
	}
	n, _ := result.RowsAffected()
	if n == 0 {
		return sql.ErrNoRows
	}
	return nil
}
