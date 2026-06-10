package com.yudi.asmara.expensereport.ui.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yudi.asmara.expensereport.databinding.ActivityChartBinding;
import com.yudi.asmara.expensereport.network.NetworkResult;
import com.yudi.asmara.expensereport.network.services.ChartService;
import com.yudi.asmara.expensereport.utils.FormatUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class ChartActivity extends AppCompatActivity {

    private ActivityChartBinding binding;
    private ChartService chartService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        chartService = new ChartService(this);

        Calendar cal = Calendar.getInstance();
        binding.etMonth.setText(String.valueOf(cal.get(Calendar.MONTH) + 1));
        binding.etYear.setText(String.valueOf(cal.get(Calendar.YEAR)));

        binding.btnLoad.setOnClickListener(v -> loadChart());

        binding.rbExpense.setChecked(true);

        loadChart();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadChart() {
        int month = Integer.parseInt(binding.etMonth.getText().toString().trim());
        int year = Integer.parseInt(binding.etYear.getText().toString().trim());
        String tipe = binding.rbExpense.isChecked() ? "expense" : "income";

        chartService.getChartData(tipe, month, year, new NetworkResult() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        JSONArray data = json.getJSONArray("data");
                        binding.layoutChart.removeAllViews();
                        double grandTotal = 0;

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            String nama = obj.optString("nama_kategori", "");
                            double total = obj.optDouble("total", 0);
                            grandTotal += total;

                            androidx.appcompat.widget.AppCompatTextView tv = new androidx.appcompat.widget.AppCompatTextView(ChartActivity.this);
                            tv.setText(nama + " : " + FormatUtils.rupiah(total));
                            tv.setPadding(0, 8, 0, 8);
                            tv.setTextSize(14);
                            binding.layoutChart.addView(tv);
                        }

                        binding.tvGrandTotal.setText("Total: " + FormatUtils.rupiah(grandTotal));
                        binding.cardChartResult.setVisibility(data.length() > 0 ? android.view.View.VISIBLE : android.view.View.GONE);
                    }
                } catch (JSONException e) {
                    Toast.makeText(ChartActivity.this, "Gagal memproses data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ChartActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
