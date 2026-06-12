package com.yudi.asmara.expensereport.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yudi.asmara.expensereport.databinding.ActivityLoginBinding;
import com.yudi.asmara.expensereport.network.NetworkResult;
import com.yudi.asmara.expensereport.network.services.AuthService;
import com.yudi.asmara.expensereport.sessions.AppSession;
import com.yudi.asmara.expensereport.utils.LottieDialog;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthService authService;
    private AppSession session;
    private LottieDialog loadingDialog;
    private long loadingStartTime;
    private static final long MIN_LOADING_MS = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authService = new AuthService(this);
        session = new AppSession(this);

        if (session.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        binding.btnLogin.setOnClickListener(v -> login());
        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void dismissLoading(Runnable after) {
        long elapsed = System.currentTimeMillis() - loadingStartTime;
        long remaining = MIN_LOADING_MS - elapsed;
        if (remaining > 0) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (loadingDialog != null) loadingDialog.dismiss();
                if (after != null) after.run();
            }, remaining);
        } else {
            if (loadingDialog != null) loadingDialog.dismiss();
            if (after != null) after.run();
        }
    }

    private void login() {
        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            binding.etUsername.setError("Username harus diisi");
            return;
        }
        if (password.isEmpty()) {
            binding.etPassword.setError("Password harus diisi");
            return;
        }

        loadingDialog = LottieDialog.showLoading(this);
        loadingStartTime = System.currentTimeMillis();

        authService.login(username, password, new NetworkResult() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getBoolean("status")) {
                        JSONObject data = json.getJSONObject("data");
                        String token = data.getString("token");
                        JSONObject user = data.getJSONObject("user");
                        String uname = user.getString("username");

                        session.setLoggedIn(true);
                        session.user().setUsername(uname);
                        session.setToken(token);

                        dismissLoading(() -> {
                            LottieDialog.showSuccess(LoginActivity.this, () -> {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            });
                        });
                    } else {
                        dismissLoading(() -> {
                            LottieDialog.showError(LoginActivity.this);
                            Toast.makeText(LoginActivity.this, json.getString("message"), Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (JSONException e) {
                    dismissLoading(() -> {
                        LottieDialog.showError(LoginActivity.this);
                        Toast.makeText(LoginActivity.this, "Gagal memproses data", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onError(String message) {
                dismissLoading(() -> {
                    LottieDialog.showError(LoginActivity.this);
                });
                binding.btnLogin.setEnabled(true);
                binding.btnLogin.setText("Masuk");
            }
        });
    }
}
