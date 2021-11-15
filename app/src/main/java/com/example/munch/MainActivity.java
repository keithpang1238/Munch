package com.example.munch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button loginBtn;
    private EditText usernameEdit;
    private EditText passwordEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginBtn = findViewById(R.id.loginBtn);
        usernameEdit = findViewById(R.id.usernameEdit);
        passwordEdit = findViewById(R.id.passwordEdit);

        loginBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.loginBtn:
                if (verifyCredentials(usernameEdit.getText().toString(), passwordEdit.getText().toString())) {
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                }
                else {
                    Toast.makeText(this, "Incorrect credentials", Toast.LENGTH_SHORT).show();
                }
            default:
                break;
        }
    }

    private Boolean verifyCredentials(String username, String password) {
        return username.equals("username") && password.equals("password");
    }


}