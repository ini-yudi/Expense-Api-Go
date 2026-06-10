package com.yudi.asmara.expensereport.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yudi.asmara.expensereport.databinding.ActivityProfileBinding;
import com.yudi.asmara.expensereport.sessions.AppSession;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private AppSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        session = new AppSession(this);
        binding.tvUsername.setText(session.user().getUsername());

        binding.btnLogout.setOnClickListener(v -> logout());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void logout() {
        session.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
