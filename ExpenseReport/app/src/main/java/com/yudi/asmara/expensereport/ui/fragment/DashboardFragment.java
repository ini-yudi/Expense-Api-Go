package com.yudi.asmara.expensereport.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yudi.asmara.expensereport.databinding.FragmentDashboardBinding;
import com.yudi.asmara.expensereport.network.NetworkResult;
import com.yudi.asmara.expensereport.network.services.DashboardService;
import com.yudi.asmara.expensereport.sessions.AppSession;
import com.yudi.asmara.expensereport.utils.FormatUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardService dashboardService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppSession session = new AppSession(requireContext());
        dashboardService = new DashboardService(requireContext());

        loadDashboard();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboard();
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
