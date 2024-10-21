package com.nguyenquocthai.real_time_tracker.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nguyenquocthai.real_time_tracker.R;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initializeViews();
        setRegisterButtonListener();
        setLoginTextViewListener();
    }


    private void setRegisterButtonListener() {
        registerButton.setOnClickListener(v -> {
            mediaPlayer.start();
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String firstName = firstNameEditText.getText().toString();
            String lastName = lastNameEditText.getText().toString();
            if (validateInput(email, password, firstName, lastName)) {
                registerUser(email, password, firstName, lastName);
            }
        });
    }

    private boolean validateInput(String email, String password, String firstName, String lastName) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Empty credentials!", Toast.LENGTH_LONG).show();
            return false;
        } else if (password.length() < 6) {
            Toast.makeText(this, "Password too short!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void setLoginTextViewListener() {
        loginTextView.setOnClickListener(v -> {
            mediaPlayer.start();
            startActivity(new Intent(this, LoginActivity.class));
        });
    }

    private void registerUser(String email, String password, String firstName, String lastName) {
        if (!isRegisterButtonClicked) {
            isRegisterButtonClicked = true;

            auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    boolean isExistingUser = task.getResult().getSignInMethods() != null &&
                            !task.getResult().getSignInMethods().isEmpty();
                    if (isExistingUser) {
                        Toast.makeText(this, "Account already exists!", Toast.LENGTH_LONG).show();
                    } else {
                        startConfirmActivity(email, password, firstName, lastName);
                    }
                } else {
                    handleRegistrationError(task.getException());
                }
                isRegisterButtonClicked = false;
            });
        }
    }

    private void startConfirmActivity(String email, String password, String firstName, String lastName) {
        Intent confirmIntent = new Intent(this, ConfirmActivity.class);
        confirmIntent.putExtra("email", email);
        confirmIntent.putExtra("password", password);
        confirmIntent.putExtra("firstname", firstName);
        confirmIntent.putExtra("lastname", lastName);
        startActivity(confirmIntent);
    }

    private void handleRegistrationError(Exception exception) {
        String errorMessage = (exception != null) ? exception.getMessage() : "Unknown error occurred";
        Toast.makeText(this, "Network error: " + errorMessage, Toast.LENGTH_LONG).show();
    }
    private void initializeViews() {
        emailEditText = findViewById(R.id.edittext_signemail);
        passwordEditText = findViewById(R.id.edittext_signpassword);
        firstNameEditText = findViewById(R.id.edittext_firstname);
        lastNameEditText = findViewById(R.id.edittext_lastname);
        registerButton = findViewById(R.id.signup_button);
        loginTextView = findViewById(R.id.signtolog_txt);
        auth = FirebaseAuth.getInstance();
        mediaPlayer = MediaPlayer.create(this,R.raw.click);
    }

    private EditText emailEditText, passwordEditText, firstNameEditText, lastNameEditText;
    private Button registerButton;
    private TextView loginTextView;
    private FirebaseAuth auth;
    private boolean isRegisterButtonClicked = false;
    private MediaPlayer mediaPlayer;

}
