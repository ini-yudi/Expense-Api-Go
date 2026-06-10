package com.yudi.asmara.expensereport.ui.fragment;
import com.yudi.asmara.expensereport.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.yudi.asmara.expensereport.databinding.FragmentBudgetBinding;
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

public class BudgetFragment extends Fragment {

    private FragmentBudgetBinding binding;
    private CategoryService categoryService;
    private BudgetService budgetService;
    private List<Category> categories;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBudgetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        categoryService = new CategoryService(requireContext());
        budgetService = new BudgetService(requireContext());
        categories = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        binding.etMonth.setText(String.valueOf(cal.get(Calendar.MONTH) + 1));
        binding.etYear.setText(String.valueOf(cal.get(Calendar.YEAR)));

        loadCategories();
        loadBudgets();

        binding.btnSave.setOnClickListener(v -> saveBudget());
    }

    private void loadCategories() {
        categoryService.getAll(new NetworkResult() {
            @Override
            public void onSuccess(String response) {
                if (!isAdded()) return;
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
                        for (Category c : categories) names.add(c.getNamaKategori());
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, names);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spCategory.setAdapter(adapter);
                    }
                } catch (JSONException e) {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Gagal memuat kategori", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBudgets() {
        budgetService.getAll(new NetworkResult() {
            @Override
            public void onSuccess(String response) {
                if (!isAdded()) return;
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        JSONArray data = json.getJSONArray("data");
                        binding.layoutBudgets.removeAllViews();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            double jumlah = obj.optDouble("jumlah", 0);
                            double terpakai = obj.optDouble("terpakai", 0);
                            double sisa = jumlah - terpakai;
                            double persen = obj.optDouble("persen", 0);

                            ItemBudgetBinding item = ItemBudgetBinding.inflate(LayoutInflater.from(requireContext()));
                            item.tvBudgetName.setText(obj.optString("nama_kategori", ""));
                            item.tvBudgetSisa.setText(FormatUtils.rupiah(sisa));
                            item.tvBudgetDetail.setText("Anggaran: " + FormatUtils.rupiah(jumlah) + " | Terpakai: " + FormatUtils.rupiah(terpakai));

                            int progress = (int) Math.min(persen, 100);
                            item.progressBudget.setProgress(progress);

                            if (sisa < 0) {
                                item.tvBudgetSisa.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                                item.progressBudget.setProgressTintList(ContextCompat.getColorStateList(requireContext(), android.R.color.holo_red_dark));
                            } else if (persen >= 80) {
                                item.tvBudgetSisa.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark));
                                item.progressBudget.setProgressTintList(ContextCompat.getColorStateList(requireContext(), android.R.color.holo_orange_dark));
                            } else {
                                item.tvBudgetSisa.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
                                item.progressBudget.setProgressTintList(ContextCompat.getColorStateList(requireContext(), android.R.color.holo_green_dark));
                            }

                            binding.layoutBudgets.addView(item.getRoot());
                        }
                        binding.cardBudgetList.setVisibility(data.length() > 0 ? View.VISIBLE : View.GONE);
                    }
                } catch (JSONException e) {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Gagal memuat anggaran", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveBudget() {
        String jumlahStr = binding.etJumlah.getText().toString().trim();
        if (jumlahStr.isEmpty() || categories.isEmpty()) {
            Toast.makeText(requireContext(), "Lengkapi data", Toast.LENGTH_SHORT).show();
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
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Anggaran disimpan", Toast.LENGTH_SHORT).show();
                    loadBudgets();
                }
                @Override
                public void onError(String message) {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (JSONException e) {
            Toast.makeText(requireContext(), "Gagal memproses", Toast.LENGTH_SHORT).show();
        }
    }
}
