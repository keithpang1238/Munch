package com.example.munch;

import com.android.volley.VolleyError;

public interface HTTPListener {
    void onResponseReceived(String response);
    void onError(VolleyError error);
}
