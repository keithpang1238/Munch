package com.example.munch;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;


public class APIHelper extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public StringRequest movieTVAPICall(String url, HTTPListener httpListener) {
        return new StringRequest(Request.Method.GET, url,
            response -> {
                if (httpListener != null) {
                    httpListener.onResponseReceived(response);
                }
            },
            error -> {
                if (httpListener != null) {
                    httpListener.onError(error);
                }
            }
        );
    }
}