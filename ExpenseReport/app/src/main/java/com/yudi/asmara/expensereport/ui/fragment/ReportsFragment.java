package com.yudi.asmara.expensereport.ui.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yudi.asmara.expensereport.adapters.TransactionAdapter;
import com.yudi.asmara.expensereport.databinding.FragmentReportsBinding;
import com.yudi.asmara.expensereport.databinding.ItemReportCategoryBinding;
import com.yudi.asmara.expensereport.helpers.TimeHelper;
import com.yudi.asmara.expensereport.models.Transaction;
import com.yudi.asmara.expensereport.network.NetworkResult;
import com.yudi.asmara.expensereport.network.services.ExportService;
import com.yudi.asmara.expensereport.network.services.ReportService;
import com.yudi.asmara.expensereport.utils.FormatUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReportsFragment extends Fragment {

    private FragmentReportsBinding binding;
    private ReportService reportService;
    private ExportService exportService;
    private TransactionAdapter dailyAdapter;
    private List<Transaction> dailyList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        reportService = new ReportService(requireContext());
        exportService = new ExportService(requireContext());

        dailyList = new ArrayList<>();
        dailyAdapter = new TransactionAdapter(dailyList, null);
        binding.rvDaily.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDaily.setAdapter(dailyAdapter);

        binding.etDailyDate.setText(TimeHelper.getCurrentDate());
        binding.etMonth.setText(TimeHelper.getCurrentMonth());
        binding.etYear.setText(TimeHelper.getCurrentYear());

        binding.etDailyDate.setOnClickListener(v -> showDatePickerDialog(binding.etDailyDate));
        binding.etStartDate.setOnClickListener(v -> showDatePickerDialog(binding.etStartDate));
        binding.etEndDate.setOnClickListener(v -> showDatePickerDialog(binding.etEndDate));

        binding.btnDailyReport.setOnClickListener(v -> loadDailyReport());
        binding.btnMonthlyReport.setOnClickListener(v -> loadMonthlyReport());
        binding.btnStatsReport.setOnClickListener(v -> loadStatsReport());
        binding.btnExportCsv.setOnClickListener(v -> {
            String start = binding.etStartDate.getText().toString().trim();
            String end = binding.etEndDate.getText().toString().trim();
            exportService.exportCSV("", start, end, new NetworkResult() {
                @Override
                public void onSuccess(String response) {
                    Toast.makeText(requireContext(), "Export berhasil", Toast.LENGTH_LONG).show();
                }
                @Override
                public void onError(String message) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showDatePickerDialog(TextView target) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String date = year + "-" + String.format("%02d", (month + 1)) + "-" + String.format("%02d", dayOfMonth);
            target.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadDailyReport() {
        String date = binding.etDailyDate.getText().toString().trim();
        if (date.isEmpty()) return;
        reportService.getDaily(date, new NetworkResult() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        JSONObject data = json.getJSONObject("data");
                        binding.tvDailyTotal.setText(FormatUtils.rupiah(data.optDouble("total", 0)));
                        JSONArray transactions = data.optJSONArray("transactions");
                        List<Transaction> list = new ArrayList<>();
                        if (transactions != null) {
                            for (int i = 0; i < transactions.length(); i++) {
                                JSONObject obj = transactions.getJSONObject(i);
                                Transaction t = new Transaction();
                                t.setId(obj.getInt("id"));
                                t.setKategoriId(obj.getInt("kategori_id"));
                                t.setNamaKategori(obj.optString("nama_kategori", ""));
                                t.setIcon(obj.optString("icon", ""));
                                t.setTipe(obj.optString("tipe", "expense"));
                                t.setNominal(obj.getDouble("nominal"));
                                t.setKeterangan(obj.optString("keterangan", ""));
                                t.setTanggalTransaksi(obj.optString("tanggal_transaksi", ""));
                                list.add(t);
                            }
                        }
                        dailyAdapter.updateData(list);
                        binding.cardDailyResult.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    Toast.makeText(requireContext(), "Gagal memproses data", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMonthlyReport() {
        String month = binding.etMonth.getText().toString().trim();
        String year = binding.etYear.getText().toString().trim();
        if (month.isEmpty() || year.isEmpty()) return;
        reportService.getMonthly(month, year, new NetworkResult() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        JSONObject data = json.getJSONObject("data");
                        binding.tvMonthlyTotal.setText(FormatUtils.rupiah(data.optDouble("grand_total", 0)));
                        JSONArray categories = data.optJSONArray("categories");
                        binding.layoutMonthlyCategories.removeAllViews();
                        if (categories != null) {
                            for (int i = 0; i < categories.length(); i++) {
                                JSONObject obj = categories.getJSONObject(i);
                                ItemReportCategoryBinding row = ItemReportCategoryBinding.inflate(getLayoutInflater());
                                row.tvReportName.setText(obj.optString("nama_kategori", ""));
                                row.tvReportTotal.setText(FormatUtils.rupiah(obj.optDouble("total", 0)));
                                binding.layoutMonthlyCategories.addView(row.getRoot());
                            }
                        }
                        binding.cardMonthlyResult.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    Toast.makeText(requireContext(), "Gagal memproses data", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStatsReport() {
        String start = binding.etStartDate.getText().toString().trim();
        String end = binding.etEndDate.getText().toString().trim();
        if (start.isEmpty() || end.isEmpty()) return;
        reportService.getStats(start, end, new NetworkResult() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        JSONArray data = json.getJSONArray("data");
                        binding.layoutStats.removeAllViews();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            TextView tv = new TextView(requireContext());
                            tv.setText(obj.optString("date", "") + "  " + FormatUtils.rupiah(obj.optDouble("total", 0)));
                            tv.setPadding(0, 8, 0, 8);
                            binding.layoutStats.addView(tv);
                        }
                        binding.cardStatsResult.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    Toast.makeText(requireContext(), "Gagal memproses data", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
