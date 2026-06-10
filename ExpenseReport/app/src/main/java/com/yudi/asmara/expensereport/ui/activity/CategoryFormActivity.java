package com.yudi.asmara.expensereport.ui.activity;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yudi.asmara.expensereport.databinding.ActivityCategoryFormBinding;
import com.yudi.asmara.expensereport.models.Category;
import com.yudi.asmara.expensereport.network.NetworkResult;
import com.yudi.asmara.expensereport.network.services.CategoryService;

import org.json.JSONException;
import org.json.JSONObject;

public class CategoryFormActivity extends AppCompatActivity {

    private ActivityCategoryFormBinding binding;
    private CategoryService categoryService;
    private Category existingCategory;
    private String selectedColor = "#FF6B6B";

    private final String[] COLORS = {
        "#FF6B6B", "#FF922B", "#FFD43B", "#51CF66", "#20C997", "#74B9FF",
        "#A29BFE", "#F783AC", "#4DD0E1", "#8BC34A", "#FFB74D", "#CE93D8"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        categoryService = new CategoryService(this);

        if (getIntent().hasExtra("category")) {
            existingCategory = (Category) getIntent().getSerializableExtra("category");
            if (existingCategory != null) {
                binding.etNamaKategori.setText(existingCategory.getNamaKategori());
                binding.btnSave.setText("Update");
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Edit Kategori");
                }
                if (existingCategory.getIcon() != null && !existingCategory.getIcon().isEmpty()) {
                    selectedColor = existingCategory.getIcon();
                }
            }
        }

        setupColorPicker();
        binding.btnSave.setOnClickListener(v -> saveCategory());
    }

    private void setupColorPicker() {
        View[] views = {
            binding.color0, binding.color1, binding.color2, binding.color3,
            binding.color4, binding.color5, binding.color6, binding.color7,
            binding.color8, binding.color9, binding.color10, binding.color11
        };

        for (int i = 0; i < views.length; i++) {
            final int index = i;
            final View v = views[i];
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedColor = COLORS[index];
                    updateSelection(views, index);
                }
            });
        }

        int defaultIndex = 0;
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i].equals(selectedColor)) {
                defaultIndex = i;
                break;
            }
        }
        updateSelection(views, defaultIndex);
    }

    private void updateSelection(View[] views, int selected) {
        for (int i = 0; i < views.length; i++) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(8);
            drawable.setColor(Color.parseColor(COLORS[i]));
            if (i == selected) {
                drawable.setStroke(4, Color.WHITE);
                views[i].setElevation(8);
            } else {
                drawable.setStroke(0, Color.TRANSPARENT);
                views[i].setElevation(2);
            }
            views[i].setBackground(drawable);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void saveCategory() {
        String nama = binding.etNamaKategori.getText().toString().trim();

        if (nama.isEmpty()) {
            binding.etNamaKategori.setError("Nama kategori harus diisi");
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("nama_kategori", nama);
            body.put("icon", selectedColor);

            NetworkResult callback = new NetworkResult() {
                @Override
                public void onSuccess(String response) {
                    Toast.makeText(CategoryFormActivity.this, "Berhasil disimpan", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(CategoryFormActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            };

            if (existingCategory != null) {
                categoryService.update(existingCategory.getId(), body.toString(), callback);
            } else {
                categoryService.create(body.toString(), callback);
            }

        } catch (JSONException e) {
            Toast.makeText(this, "Gagal memproses data", Toast.LENGTH_SHORT).show();
        }
    }
}
