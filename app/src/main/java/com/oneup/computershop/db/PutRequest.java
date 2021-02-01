package com.oneup.computershop.db;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONObject;

public class PutRequest extends JsonRequest<Object> {
    private static final String TAG = "ComputerShop";

    public PutRequest(String url, JSONObject jsonRequest,
                      Response.Listener<Object> listener, Response.ErrorListener errorListener) {
        super(Request.Method.PUT, url, jsonRequest.toString(), listener, errorListener);
    }

    @Override
    protected Response<Object> parseNetworkResponse(NetworkResponse response) {
        Log.d(TAG, "parseNetworkResponse()");
        return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
    }
}
