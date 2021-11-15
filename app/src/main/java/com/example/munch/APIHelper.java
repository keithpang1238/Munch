package com.example.munch;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class APIHelper extends AppCompatActivity {

    private RequestQueue queue;
    private final String TAG_SEARCH_SHOW = "tagShow";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public StringRequest movieTVAPICall(String url, Activity activity, HTTPListener httpListener) {

        // first StringRequest: getting items searched
        return new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
                // 3rd param - method onResponse lays the code procedure of success return
                // SUCCESS
                @Override
                public void onResponse(String response) {
                    if (httpListener != null) {
                        httpListener.onResponseReceived(response);
                    }
                } // public void onResponse(String response)
            }, // Response.Listener<String>()
            new Response.ErrorListener() {
                // 4th param - method onErrorResponse lays the code procedure of error return
                // ERROR
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (httpListener != null) {
                        httpListener.onError(error);
                    }
                }
            });
    }
}