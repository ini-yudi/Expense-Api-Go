package com.yudi.asmara.expensereport.ui.activity;
import com.yudi.asmara.expensereport.R;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yudi.asmara.expensereport.databinding.ActivityTransactionFormBinding;
import com.yudi.asmara.expensereport.models.Category;
import com.yudi.asmara.expensereport.network.NetworkResult;
import com.yudi.asmara.expensereport.network.services.CategoryService;
import com.yudi.asmara.expensereport.network.services.TransactionService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TransactionFormActivity extends AppCompatActivity {

    private ActivityTransactionFormBinding binding;
    private CategoryService categoryService;
    private TransactionService transactionService;
    private List<Category> categories;
    private String selectedDate = "";
    private Integer editId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        categoryService = new CategoryService(this);
        transactionService = new TransactionService(this);
        categories = new ArrayList<>();

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        selectedDate = sdf.format(Calendar.getInstance().getTime());
        binding.tvDate.setText(selectedDate);

        binding.tvDate.setOnClickListener(v -> showDatePicker());

        loadCategories();

        binding.btnSave.setOnClickListener(v -> saveTransaction());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate = year + "-" + String.format("%02d", (month + 1)) + "-" + String.format("%02d", dayOfMonth);
            binding.tvDate.setText(selectedDate);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
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
                                TransactionFormActivity.this,
                                android.R.layout.simple_spinner_item, names);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spCategory.setAdapter(adapter);
                    }
                } catch (JSONException e) {
                    Toast.makeText(TransactionFormActivity.this, "Gagal memuat kategori", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(TransactionFormActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveTransaction() {
        String nominalStr = binding.etNominal.getText().toString().trim();
        String keterangan = binding.etKeterangan.getText().toString().trim();

        if (nominalStr.isEmpty()) {
            binding.etNominal.setError("Nominal harus diisi");
            return;
        }
        if (categories.isEmpty()) {
            Toast.makeText(this, "Kategori belum tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        Category selectedCategory = categories.get(binding.spCategory.getSelectedItemPosition());

        String tipe = binding.rbExpense.isChecked() ? "expense" : "income";

        try {
            JSONObject body = new JSONObject();
            body.put("kategori_id", selectedCategory.getId());
            body.put("nominal", Double.parseDouble(nominalStr));
            body.put("keterangan", keterangan);
            body.put("tanggal_transaksi", selectedDate);
            body.put("tipe", tipe);

            NetworkResult callback = new NetworkResult() {
                @Override
                public void onSuccess(String response) {
                    Toast.makeText(TransactionFormActivity.this, "Berhasil disimpan", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(TransactionFormActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            };

            if (editId != null) {
                transactionService.update(editId, body.toString(), callback);
            } else {
                transactionService.create(body.toString(), callback);
            }

        } catch (JSONException e) {
            Toast.makeText(this, "Gagal memproses data", Toast.LENGTH_SHORT).show();
        }
    }
}
