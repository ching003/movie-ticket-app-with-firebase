package com.example.firebase;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.firebase.data.FirebaseRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String PREF_NAME = "MovieTicketPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_UID = "userUid";
    private static final String KEY_USERNAME = "username";

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ImageView ivTogglePassword;
    private TextView tvRecoverAccess;
    private SharedPreferences sharedPreferences;
    private FirebaseRepository repository;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        repository = FirebaseRepository.getInstance();
        repository.seedDataIfNeeded();
        repository.seedDemoUsernameRegistry(getString(R.string.demo_account_username));

        if (isLoggedIn()) {
            String uid = sharedPreferences.getString(KEY_USER_UID, "");
            repository.ensureSampleTicketsForUser(uid);
            navigateToHome();
            return;
        }

        repository.ensureDemoAccountOnLaunch(
                getString(R.string.demo_account_username),
                getString(R.string.demo_account_password));

        setContentView(R.layout.activity_login);
        
        initViews();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        tvRecoverAccess = findViewById(R.id.tvRecoverAccess);
        applyDemoAccountDefaults();
    }

    /** Dien san tai khoan demo; lan dau dang nhap Firebase se tu dang ky neu chua co. */
    private void applyDemoAccountDefaults() {
        if (etUsername.getText().toString().trim().isEmpty()) {
            etUsername.setText(getString(R.string.demo_account_username));
        }
        if (etPassword.getText().toString().trim().isEmpty()) {
            etPassword.setText(getString(R.string.demo_account_password));
        }
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
        
        ivTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        
        tvRecoverAccess.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng đang được phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("Vui lòng nhập tên đăng nhập");
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }

        repository.login(username, password, new FirebaseRepository.SingleCallback<FirebaseUser>() {
            @Override
            public void onResult(FirebaseUser data) {
                saveLoginState(data.getUid(), username);
                repository.seedDataIfNeeded(null);
                repository.ensureSampleTicketsForUser(data.getUid());
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                navigateToHome();
            }

            @Override
            public void onError(String message) {
                repository.seedDemoUsernameRegistry(username);
                repository.ensureDemoAccountOnLaunch(username, password);
                Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + message, Toast.LENGTH_SHORT).show();
            }
        });
   }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivTogglePassword.setImageResource(R.drawable.ic_visibility);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivTogglePassword.setImageResource(R.drawable.ic_visibility_off);
        }
        isPasswordVisible = !isPasswordVisible;
        
        etPassword.setSelection(etPassword.getText().length());
    }

    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    private void saveLoginState(String userUid, String username) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_UID, userUid);
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public static void logout(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static int getCurrentUserId(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_USER_UID, "").hashCode();
    }

    public static String getCurrentUserUid(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_USER_UID, "");
    }

    public static String getCurrentUsername(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_USERNAME, "");
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
