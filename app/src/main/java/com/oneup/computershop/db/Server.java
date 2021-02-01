package com.oneup.computershop.db;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class Server {
    private static final String TAG = "ComputerShop";
    private static final String URL = "http://192.168.2.202:8080/api/repairs/";

    private static Server server;

    private Context context;
    private RequestQueue requestQueue;
    private DbHelper db;

    private Server(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
        this.db = new DbHelper(context);
    }

    public void getRepairs(Response.Listener<JSONArray> listener) {
        Log.d(TAG, "Server.getRepairs()");
        requestQueue.add(new JsonArrayRequest(URL, listener,
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse", error);
                    }
                }));
    }

    public void insertRepair(final Repair repair) {
        Log.d(TAG, "Server.insertRepair(" + repair.getId() + ")");
        try {
            requestQueue.add(new JsonObjectRequest(Request.Method.POST, URL,
                    repair.getJsonObject(),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "response: " + response);
                            db.resetRepairPending(repair);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error", error);
                        }
                    }));
        } catch (Exception ex) {
            Log.e(TAG, "Error posting insert", ex);
        }
    }

    public void updateRepair(final Repair repair) {
        Log.d(TAG, "Server.updateRepair(" + repair.getId() + ")");
        try {
            requestQueue.add(new JsonObjectRequest(Request.Method.PUT,
                    URL + repair.getId(), repair.getJsonObject(),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "response: " + response);
                            db.resetRepairPending(repair);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error", error);
                        }
                    }));
        } catch (Exception ex) {
            Log.e(TAG, "Error posting update", ex);
        }
    }

    public void deleteRepair(final Repair repair) {
        Log.d(TAG, "Server.deleteRepair(" + repair.getId() + ")");
        try {
            requestQueue.add(new JsonObjectRequest(Request.Method.DELETE,
                    URL + repair.getId(), new JSONObject(),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "response: " + response);
                            db.deleteRepair(repair);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error", error);
                        }
                    }));
        } catch (Exception ex) {
            Log.e(TAG, "Error posting delete", ex);
        }
    }

    public static Server get(Context context) {
        if (server == null) {
            server = new Server(context);
        }
        return server;
    }
}
