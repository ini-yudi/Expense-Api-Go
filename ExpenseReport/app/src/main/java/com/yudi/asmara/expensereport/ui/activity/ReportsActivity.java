package com.yudi.asmara.expensereport.ui.activity;
import com.yudi.asmara.expensereport.R;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yudi.asmara.expensereport.adapters.TransactionAdapter;
import com.yudi.asmara.expensereport.databinding.ActivityReportsBinding;
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

public class ReportsActivity extends AppCompatActivity {

    private ActivityReportsBinding binding;
    private ReportService reportService;
    private ExportService exportService;
    private TransactionAdapter dailyAdapter;
    private List<Transaction> dailyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        reportService = new ReportService(this);
        exportService = new ExportService(this);

        dailyList = new ArrayList<>();
        dailyAdapter = new TransactionAdapter(dailyList, null);
        binding.rvDaily.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDaily.setAdapter(dailyAdapter);

        binding.etDailyDate.setText(TimeHelper.getCurrentDate());
        binding.etMonth.setText(TimeHelper.getCurrentMonth());
        binding.etYear.setText(TimeHelper.getCurrentYear());

        binding.etDailyDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(binding.etDailyDate);
            }
        });

        binding.etStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(binding.etStartDate);
            }
        });

        binding.etEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(binding.etEndDate);
            }
        });

        binding.btnDailyReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDailyReport();
            }
        });

        binding.btnMonthlyReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMonthlyReport();
            }
        });

        binding.btnStatsReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadStatsReport();
            }
        });

        binding.btnExportCsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String start = binding.etStartDate.getText().toString().trim();
                String end = binding.etEndDate.getText().toString().trim();
                exportService.exportCSV("", start, end, new NetworkResult() {
                    @Override
                    public void onSuccess(String response) {
                        Toast.makeText(ReportsActivity.this, "Export berhasil", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(ReportsActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reports, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_export_csv) {
            String date = binding.etDailyDate.getText().toString().trim();
            exportService.exportCSV("", date, date, new NetworkResult() {
                @Override
                public void onSuccess(String response) {
                    Toast.makeText(ReportsActivity.this, "Export berhasil", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(ReportsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDatePickerDialog(TextView target) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
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
                        double total = data.optDouble("total", 0);
                        binding.tvDailyTotal.setText(FormatUtils.rupiah(total));

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
                    Toast.makeText(ReportsActivity.this, "Gagal memproses data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ReportsActivity.this, message, Toast.LENGTH_SHORT).show();
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
                        double grandTotal = data.optDouble("grand_total", 0);
                        binding.tvMonthlyTotal.setText(FormatUtils.rupiah(grandTotal));

                        JSONArray categories = data.optJSONArray("categories");
                        binding.layoutMonthlyCategories.removeAllViews();
                        if (categories != null) {
                            for (int i = 0; i < categories.length(); i++) {
                                JSONObject obj = categories.getJSONObject(i);
                                String nama = obj.optString("nama_kategori", "");
                                double total = obj.optDouble("total", 0);

                                ItemReportCategoryBinding rowBinding = ItemReportCategoryBinding.inflate(getLayoutInflater());
                                rowBinding.tvReportName.setText(nama);
                                rowBinding.tvReportTotal.setText(FormatUtils.rupiah(total));
                                binding.layoutMonthlyCategories.addView(rowBinding.getRoot());
                            }
                        }

                        binding.cardMonthlyResult.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    Toast.makeText(ReportsActivity.this, "Gagal memproses data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ReportsActivity.this, message, Toast.LENGTH_SHORT).show();
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
                            String date = obj.optString("date", "");
                            double total = obj.optDouble("total", 0);

                            TextView tv = new TextView(ReportsActivity.this);
                            tv.setText(date + "  " + FormatUtils.rupiah(total));
                            tv.setPadding(0, 8, 0, 8);
                            binding.layoutStats.addView(tv);
                        }

                        binding.cardStatsResult.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    Toast.makeText(ReportsActivity.this, "Gagal memproses data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ReportsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
