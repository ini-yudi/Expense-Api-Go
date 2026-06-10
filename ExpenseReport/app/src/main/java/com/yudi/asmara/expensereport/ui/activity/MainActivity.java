package com.yudi.asmara.expensereport.ui.activity;
import com.yudi.asmara.expensereport.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yudi.asmara.expensereport.databinding.ActivityMainBinding;
import com.yudi.asmara.expensereport.network.NetworkResult;
import com.yudi.asmara.expensereport.network.services.DashboardService;
import com.yudi.asmara.expensereport.sessions.AppSession;
import com.yudi.asmara.expensereport.utils.FormatUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DashboardService dashboardService;
    private AppSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new AppSession(this);

        if (!session.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        setSupportActionBar(binding.toolbar);

        dashboardService = new DashboardService(this);

        binding.btnTransactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), TransactionsActivity.class));
            }
        });
        binding.btnCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CategoriesActivity.class));
            }
        });
        binding.btnReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ReportsActivity.class));
            }
        });
        binding.btnBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), BudgetActivity.class));
            }
        });
        binding.btnChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ChartActivity.class));
            }
        });

        loadDashboard();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        session.clearSession();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (session.isLoggedIn()) {
            loadDashboard();
        }
    }

    private void loadDashboard() {
        dashboardService.getDashboard(new NetworkResult() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        JSONObject data = json.getJSONObject("data");
                        binding.tvToday.setText(FormatUtils.rupiah(data.getDouble("today")));
                        binding.tvWeek.setText(FormatUtils.rupiah(data.getDouble("week")));
                        binding.tvMonth.setText(FormatUtils.rupiah(data.getDouble("month")));
                        binding.tvYear.setText(FormatUtils.rupiah(data.getDouble("year")));
                        binding.tvIncome.setText(FormatUtils.rupiah(data.optDouble("income", 0)));
                        binding.tvExpense.setText(FormatUtils.rupiah(data.optDouble("expense", 0)));
                        binding.tvNet.setText(FormatUtils.rupiah(data.optDouble("net", 0)));
                    }
                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, "Gagal memproses data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
