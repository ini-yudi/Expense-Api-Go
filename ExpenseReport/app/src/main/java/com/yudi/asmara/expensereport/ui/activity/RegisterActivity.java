package com.yudi.asmara.expensereport.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yudi.asmara.expensereport.databinding.ActivityRegisterBinding;
import com.yudi.asmara.expensereport.network.NetworkResult;
import com.yudi.asmara.expensereport.network.services.AuthService;
import com.yudi.asmara.expensereport.sessions.AppSession;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthService authService;
    private AppSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authService = new AuthService(this);
        session = new AppSession(this);

        binding.btnRegister.setOnClickListener(v -> register());
        binding.tvLogin.setOnClickListener(v -> finish());
    }

    private void register() {
        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        if (username.isEmpty()) {
            binding.etUsername.setError("Username harus diisi");
            return;
        }
        if (password.isEmpty()) {
            binding.etPassword.setError("Password harus diisi");
            return;
        }
        if (password.length() < 6) {
            binding.etPassword.setError("Password minimal 6 karakter");
            return;
        }
        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("Password tidak cocok");
            return;
        }

        binding.btnRegister.setEnabled(false);
        binding.btnRegister.setText("Memuat...");

        authService.register(username, password, new NetworkResult() {
            @Override
            public void onSuccess(String response) {
                binding.btnRegister.setEnabled(true);
                binding.btnRegister.setText("Daftar");
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        JSONObject data = json.getJSONObject("data");
                        String token = data.getString("token");
                        JSONObject user = data.getJSONObject("user");
                        String username = user.getString("username");

                        session.setLoggedIn(true);
                        session.user().setUsername(username);
                        session.setToken(token);

                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, json.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(RegisterActivity.this, "Gagal memproses data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                binding.btnRegister.setEnabled(true);
                binding.btnRegister.setText("Daftar");
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
