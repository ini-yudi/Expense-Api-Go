package com.yudi.asmara.expensereport.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yudi.asmara.expensereport.databinding.FragmentProfileBinding;
import com.yudi.asmara.expensereport.sessions.AppSession;
import com.yudi.asmara.expensereport.ui.activity.LoginActivity;
import com.yudi.asmara.expensereport.utils.LottieDialog;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private AppSession session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new AppSession(requireContext());
        binding.tvUsername.setText(session.user().getUsername());

        binding.btnLogout.setOnClickListener(v -> showLogoutWarning());
    }

    private void showLogoutWarning() {
        LottieDialog.showWarning(requireContext(), () -> {
            session.clearSession();
            Toast.makeText(requireContext(), "Berhasil logout", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }
}
