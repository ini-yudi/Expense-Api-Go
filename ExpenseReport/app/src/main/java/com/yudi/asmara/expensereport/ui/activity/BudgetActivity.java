package com.yudi.asmara.expensereport.ui.activity;
import com.yudi.asmara.expensereport.R;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.yudi.asmara.expensereport.databinding.ActivityBudgetBinding;
import com.yudi.asmara.expensereport.databinding.ItemBudgetBinding;
import com.yudi.asmara.expensereport.models.Category;
import com.yudi.asmara.expensereport.network.NetworkResult;
import com.yudi.asmara.expensereport.network.services.BudgetService;
import com.yudi.asmara.expensereport.network.services.CategoryService;
import com.yudi.asmara.expensereport.utils.FormatUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BudgetActivity extends AppCompatActivity {

    private ActivityBudgetBinding binding;
    private CategoryService categoryService;
    private BudgetService budgetService;
    private List<Category> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBudgetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        categoryService = new CategoryService(this);
        budgetService = new BudgetService(this);
        categories = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        binding.etMonth.setText(String.valueOf(cal.get(Calendar.MONTH) + 1));
        binding.etYear.setText(String.valueOf(cal.get(Calendar.YEAR)));

        loadCategories();
        loadBudgets();

        binding.btnSave.setOnClickListener(v -> saveBudget());
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
                        categories.clear();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            Category c = new Category();
                            c.setId(obj.getInt("id"));
                            c.setNamaKategori(obj.getString("nama_kategori"));
                            c.setIcon(obj.optString("icon", ""));
                            categories.add(c);
                        }
                        List<String> names = new ArrayList<>();
                        for (Category c : categories) {
                            names.add(c.getNamaKategori());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                BudgetActivity.this,
                                android.R.layout.simple_spinner_item, names);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spCategory.setAdapter(adapter);
                    }
                } catch (JSONException e) {
                    Toast.makeText(BudgetActivity.this, "Gagal memuat kategori", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(BudgetActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBudgets() {
        budgetService.getAll(new NetworkResult() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        JSONArray data = json.getJSONArray("data");
                        binding.layoutBudgets.removeAllViews();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            String nama = obj.optString("nama_kategori", "");
                            double jumlah = obj.optDouble("jumlah", 0);
                            double terpakai = obj.optDouble("terpakai", 0);
                            double sisa = jumlah - terpakai;
                            double persen = obj.optDouble("persen", 0);

                            ItemBudgetBinding item = ItemBudgetBinding.inflate(getLayoutInflater());
                            item.tvBudgetName.setText(nama);
                            item.tvBudgetSisa.setText(FormatUtils.rupiah(sisa));
                            item.tvBudgetDetail.setText("Anggaran: " + FormatUtils.rupiah(jumlah) + " | Terpakai: " + FormatUtils.rupiah(terpakai));

                            int progress = (int) Math.min(persen, 100);
                            item.progressBudget.setProgress(progress);

                            if (sisa < 0) {
                                item.tvBudgetSisa.setTextColor(ContextCompat.getColor(BudgetActivity.this, android.R.color.holo_red_dark));
                                item.progressBudget.setProgressTintList(ContextCompat.getColorStateList(BudgetActivity.this, android.R.color.holo_red_dark));
                            } else if (persen >= 80) {
                                item.tvBudgetSisa.setTextColor(ContextCompat.getColor(BudgetActivity.this, android.R.color.holo_orange_dark));
                                item.progressBudget.setProgressTintList(ContextCompat.getColorStateList(BudgetActivity.this, android.R.color.holo_orange_dark));
                            } else {
                                item.tvBudgetSisa.setTextColor(ContextCompat.getColor(BudgetActivity.this, android.R.color.holo_green_dark));
                                item.progressBudget.setProgressTintList(ContextCompat.getColorStateList(BudgetActivity.this, android.R.color.holo_green_dark));
                            }

                            binding.layoutBudgets.addView(item.getRoot());
                        }
                        binding.cardBudgetList.setVisibility(data.length() > 0 ? android.view.View.VISIBLE : android.view.View.GONE);
                    }
                } catch (JSONException e) {
                    Toast.makeText(BudgetActivity.this, "Gagal memuat anggaran", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(BudgetActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveBudget() {
        String jumlahStr = binding.etJumlah.getText().toString().trim();
        if (jumlahStr.isEmpty() || categories.isEmpty()) {
            Toast.makeText(this, "Lengkapi data", Toast.LENGTH_SHORT).show();
            return;
        }

        Category selected = categories.get(binding.spCategory.getSelectedItemPosition());
        int bulan = Integer.parseInt(binding.etMonth.getText().toString().trim());
        int tahun = Integer.parseInt(binding.etYear.getText().toString().trim());

        try {
            JSONObject body = new JSONObject();
            body.put("kategori_id", selected.getId());
            body.put("jumlah", Double.parseDouble(jumlahStr));
            body.put("bulan", bulan);
            body.put("tahun", tahun);

            budgetService.save(body.toString(), new NetworkResult() {
                @Override
                public void onSuccess(String response) {
                    Toast.makeText(BudgetActivity.this, "Anggaran disimpan", Toast.LENGTH_SHORT).show();
                    loadBudgets();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(BudgetActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (JSONException e) {
            Toast.makeText(this, "Gagal memproses", Toast.LENGTH_SHORT).show();
        }
    }
}
