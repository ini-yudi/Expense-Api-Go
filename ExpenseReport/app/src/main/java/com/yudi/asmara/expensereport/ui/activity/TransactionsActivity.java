package com.yudi.asmara.expensereport.ui.activity;
import com.yudi.asmara.expensereport.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yudi.asmara.expensereport.adapters.TransactionAdapter;
import com.yudi.asmara.expensereport.databinding.ActivityTransactionsBinding;
import com.yudi.asmara.expensereport.models.Transaction;
import com.yudi.asmara.expensereport.network.NetworkResult;
import com.yudi.asmara.expensereport.network.services.ExportService;
import com.yudi.asmara.expensereport.network.services.TransactionService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TransactionsActivity extends AppCompatActivity {

    private ActivityTransactionsBinding binding;
    private TransactionAdapter adapter;
    private List<Transaction> allTransactions;
    private TransactionService transactionService;
    private ExportService exportService;
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        transactionService = new TransactionService(this);
        exportService = new ExportService(this);
        allTransactions = new ArrayList<>();
        adapter = new TransactionAdapter(new ArrayList<>(), transaction -> deleteTransaction(transaction.getId()));

        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTransactions.setAdapter(adapter);

        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransactionFormActivity.class);
            startActivity(intent);
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                filterTransactions(s.toString(), currentFilter);
            }
        });

        binding.btnFilterAll.setOnClickListener(v -> {
            currentFilter = "all";
            filterTransactions(binding.etSearch.getText().toString(), currentFilter);
        });
        binding.btnFilterIncome.setOnClickListener(v -> {
            currentFilter = "income";
            filterTransactions(binding.etSearch.getText().toString(), currentFilter);
        });
        binding.btnFilterExpense.setOnClickListener(v -> {
            currentFilter = "expense";
            filterTransactions(binding.etSearch.getText().toString(), currentFilter);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTransactions();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_transactions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_export_csv) {
            exportCSV();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exportCSV() {
        String tipeFilter = currentFilter.equals("all") ? "" : currentFilter;
        String search = binding.etSearch.getText().toString().trim();
        exportService.exportCSV(tipeFilter, search, new NetworkResult() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(TransactionsActivity.this, "Export berhasil, cek penyimpanan", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(TransactionsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTransactions() {
        transactionService.getAll(new NetworkResult() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        JSONArray data = json.getJSONArray("data");
                        allTransactions.clear();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            Transaction t = new Transaction();
                            t.setId(obj.getInt("id"));
                            t.setKategoriId(obj.getInt("kategori_id"));
                            t.setNamaKategori(obj.optString("nama_kategori", ""));
                            t.setIcon(obj.optString("icon", ""));
                            t.setTipe(obj.optString("tipe", "expense"));
                            t.setNominal(obj.getDouble("nominal"));
                            t.setKeterangan(obj.optString("keterangan", ""));
                            t.setTanggalTransaksi(obj.optString("tanggal_transaksi", ""));
                            allTransactions.add(t);
                        }
                        filterTransactions(binding.etSearch.getText().toString(), currentFilter);
                    }
                } catch (JSONException e) {
                    Toast.makeText(TransactionsActivity.this, "Gagal memproses data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(TransactionsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterTransactions(String query, String tipeFilter) {
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction t : allTransactions) {
            boolean matchTipe = true;
            if (tipeFilter.equals("income")) {
                matchTipe = "income".equals(t.getTipe());
            } else if (tipeFilter.equals("expense")) {
                matchTipe = "expense".equals(t.getTipe());
            }
            boolean matchQuery = true;
            if (query != null && !query.isEmpty()) {
                String q = query.toLowerCase();
                matchQuery = (t.getNamaKategori() != null && t.getNamaKategori().toLowerCase().contains(q))
                        || (t.getKeterangan() != null && t.getKeterangan().toLowerCase().contains(q));
            }
            if (matchTipe && matchQuery) {
                filtered.add(t);
            }
        }
        adapter.updateData(filtered);
    }

    private void deleteTransaction(int id) {
        transactionService.delete(id, new NetworkResult() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(TransactionsActivity.this, "Berhasil dihapus", Toast.LENGTH_SHORT).show();
                loadTransactions();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(TransactionsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
