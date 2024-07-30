package com.example.munch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.Objects;

import pl.droidsonroids.gif.GifImageView;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText usernameEdit;
    private EditText passwordEdit;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(this.getSupportActionBar()).hide();
        mAuth = FirebaseAuth.getInstance();

        Button loginBtn = findViewById(R.id.loginBtn);
        usernameEdit = findViewById(R.id.usernameEdit);
        passwordEdit = findViewById(R.id.passwordEdit);
        loginBtn.setOnClickListener(this);

        if (Holiday.isChristmasPeriod()) {
            ImageView holly = findViewById(R.id.hollyImage);
            holly.setVisibility(View.VISIBLE);
            GifImageView santaGif = findViewById(R.id.santaGif);
            santaGif.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.loginBtn) {
            verifyCredentials();
        }
    }

    private void verifyCredentials() {
        String email = usernameEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            usernameEdit.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEdit.setError("Password is required");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
            task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    return;
                }
                Exception exception = task.getException();
                String errorMessage;
                if (exception == null) {
                    errorMessage = "Could not login";
                } else {
                    errorMessage = exception.getMessage();
                }
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                passwordEdit.setText("");
            }
        );
    }



}