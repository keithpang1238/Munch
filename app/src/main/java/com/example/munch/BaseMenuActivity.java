package com.example.munch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;

public class BaseMenuActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_menu);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.header_menu, menu);

        /*
        MenuItem menuLogoutItem = menu.findItem(R.id.menu_logout);
        menuLogoutItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_logout) {
                    mAuth.signOut();
                    Intent intent = new Intent(BaseMenuActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                return true;
            }
        });

        MenuItem menuProfileItem = menu.findItem(R.id.menu_profile);
        menuProfileItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_profile) {
                    Intent intent = new Intent(BaseMenuActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    finish();
                }
                return true;
            }
        });
        */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();
        if (itemID == R.id.menu_profile) {
            Intent intent = new Intent(BaseMenuActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (itemID == R.id.menu_logout) {
            mAuth.signOut();
            Intent intent = new Intent(BaseMenuActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else if (itemID == android.R.id.home) {
            Intent intent = new Intent(BaseMenuActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            super.onOptionsItemSelected(item);
        }
        return true;
    }

}