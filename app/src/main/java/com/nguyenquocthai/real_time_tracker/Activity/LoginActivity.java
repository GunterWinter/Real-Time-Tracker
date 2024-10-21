package com.nguyenquocthai.real_time_tracker.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseNetworkException;
import com.nguyenquocthai.real_time_tracker.Utils.ProgressbarLoader;
import com.nguyenquocthai.real_time_tracker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeViews();
        setupLoginButtonListener();
        setupCreateAccountTextViewListener();
        SharedPreferences preferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        loginAttempts = preferences.getInt("loginAttempts", 0);
        lastLoginAttemptTime = preferences.getLong("lastLoginAttemptTime", 0);
    }

    private void setupLoginButtonListener() {
        loginButton.setOnClickListener(v -> {
            mediaPlayer.start();
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            loginUser(email, password);
        });
    }

    private void setupCreateAccountTextViewListener() {
        createAccountTextView.setOnClickListener(v -> {
            mediaPlayer.start();
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }


    private void loginUser(String email, String password) {
        long currentTime = System.currentTimeMillis();
        if (loginAttempts >= 3 && (currentTime - lastLoginAttemptTime) < 60000) {
            showToast("Please wait for a minute before trying again.");
            return;
        }
        /*if((currentTime - lastLoginAttemptTime) >= 60000){
            loginAttempts=0;
        }*/
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showToast("Email and password are required");
            return;
        }

        loader.showloader();
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    proceedToMainActivity();
                    resetLoginAttempts();
                })
                .addOnFailureListener(e -> {
                    handleLoginFailure(e);
                    updateLoginAttempts();
                });
        loader.dismissloader();
    }

    private void proceedToMainActivity() {
        loader.dismissloader();
        //showToast("Login successfully!");
        if(getIntent().getExtras()!=null){
            Intent mainActivity = new Intent(this, MainActivity.class);
            mainActivity.putExtra("userID", getIntent().getExtras().getString("userID"));
            mainActivity.putExtra("name", getIntent().getExtras().getString("name"));
            startActivity(mainActivity);
        }
        else{
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }
    private void handleLoginFailure(Exception e) {
        loader.dismissloader();
        String errorMessage = "";

        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "Authentication failed: Invalid email or password.";
        } else if (e instanceof FirebaseNetworkException) {
            errorMessage = "Login failed: No internet connection.";
        } else {
            errorMessage = "Login failed: " + e.getLocalizedMessage();
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }
    private void updateLoginAttempts() {
        loginAttempts++;
        lastLoginAttemptTime = System.currentTimeMillis();

        SharedPreferences preferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("loginAttempts", loginAttempts);
        editor.putLong("lastLoginAttemptTime", lastLoginAttemptTime);
        editor.apply();
    }

    private void resetLoginAttempts() {
        loginAttempts = 0;
        lastLoginAttemptTime = 0;

        SharedPreferences preferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("loginAttempts", 0);
        editor.putLong("lastLoginAttemptTime", 0);
        editor.apply();
    }
    private void showToast(String message){
        if (currentToast != null) {
            currentToast.cancel();
        }
        currentToast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        currentToast.show();
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.edittext_email);
        passwordEditText = findViewById(R.id.edittext_password);
        loginButton = findViewById(R.id.login_button);
        createAccountTextView = findViewById(R.id.logtosign);
        loader = new ProgressbarLoader(this);
        auth = FirebaseAuth.getInstance();
        mediaPlayer = MediaPlayer.create(this,R.raw.click);
    }
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView createAccountTextView;
    private FirebaseAuth auth;
    private ProgressbarLoader loader;
    private Toast currentToast = null;
    private int loginAttempts = 0;
    private long lastLoginAttemptTime = 0;
    private MediaPlayer mediaPlayer;

}