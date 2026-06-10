package com.yudi.asmara.expensereport.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yudi.asmara.expensereport.adapters.CategoryAdapter;
import com.yudi.asmara.expensereport.databinding.ActivityCategoriesBinding;
import com.yudi.asmara.expensereport.models.Category;
import com.yudi.asmara.expensereport.network.NetworkResult;
import com.yudi.asmara.expensereport.network.services.CategoryService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity {
    private ActivityCategoriesBinding binding;
    private CategoryAdapter adapter;
    private List<Category> categoryList;
    private CategoryService categoryService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        categoryService = new CategoryService(this);
        categoryList = new ArrayList<>();
        adapter = new CategoryAdapter(categoryList, new CategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onEdit(Category category) {
                Intent intent = new Intent(CategoriesActivity.this, CategoryFormActivity.class);
                intent.putExtra("category", category);
                startActivity(intent);
            }

            @Override
            public void onDelete(Category category) {
                deleteCategory(category.getId());
            }
        });

        binding.rvCategories.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCategories.setAdapter(adapter);

        binding.fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CategoryFormActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadCategories() {
        categoryService.getAll(new NetworkResult() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        JSONArray data = json.getJSONArray("data");
                        List<Category> list = new ArrayList<>();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            Category c = new Category();
                            c.setId(obj.getInt("id"));
                            c.setNamaKategori(obj.getString("nama_kategori"));
                            c.setIcon(obj.optString("icon", ""));
                            list.add(c);
                        }
                        adapter.updateData(list);
                    }
                } catch (JSONException e) {
                    Toast.makeText(CategoriesActivity.this, "Gagal memproses data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(CategoriesActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteCategory(int id) {
        categoryService.delete(id, new NetworkResult() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(CategoriesActivity.this, "Berhasil dihapus", Toast.LENGTH_SHORT).show();
                loadCategories();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(CategoriesActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
