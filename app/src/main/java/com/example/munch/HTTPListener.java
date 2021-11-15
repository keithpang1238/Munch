package com.example.munch;

import com.android.volley.VolleyError;

import org.json.JSONObject;

public interface HTTPListener {
    void onResponseReceived(String response);
    void onError(VolleyError error);
}
