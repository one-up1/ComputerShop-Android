package com.oneup.computershop;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.oneup.computershop.db.DbHelper;
import com.oneup.computershop.db.Repair;

import org.json.JSONException;
import org.json.JSONObject;

public class NetworkService extends GcmTaskService {
    private static final String TAG = "ComputerShop";

    public static final String EXTRA_REPAIR_ID = "com.oneup.extra.REPAIR_ID";
    public static final String EXTRA_INSERT = "com.oneup.extra.INSERT";

    public NetworkService() {
        Log.d(TAG, "construct");
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        Log.d(TAG, "NetworkService.onRunTask()");
        try {
            addRequest(this, new DbHelper(this).queryRepair(
                    taskParams.getExtras().getLong(EXTRA_REPAIR_ID)),
                    taskParams.getExtras().getBoolean(EXTRA_INSERT, false));
            return GcmNetworkManager.RESULT_SUCCESS;
        } catch (Exception ex) {
            Log.e(TAG, "Error", ex);
            return GcmNetworkManager.RESULT_RESCHEDULE;
        }
    }

    public static void addRequest(Context context, Repair repair, boolean insert) throws JSONException {
        Log.d(TAG, "NetworkService.addRequest(" + repair.getId() + ", " + insert + ")");
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("ID", repair.getId());
        jsonRequest.put("StartDate", repair.getStartDate());
        jsonRequest.put("EndDate", repair.getEndDate());
        jsonRequest.put("Status", repair.getStatus());
        jsonRequest.put("Description", repair.getDescription());

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        if (insert) {
            Log.d(TAG, "Inserting");
            requestQueue.add(new JsonObjectRequest(Request.Method.POST,
                    "http://192.168.2.202:8080/api/repairs/", jsonRequest,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "response: " + response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error", error);
                        }
                    }));
        } else {
            Log.d(TAG, "Updating");
            requestQueue.add(new JsonObjectRequest(Request.Method.PUT,
                    "http://192.168.2.202:8080/api/repairs/" + repair.getId(), jsonRequest,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "response: " + response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error", error);
                        }
                    }));
        }
    }
}
