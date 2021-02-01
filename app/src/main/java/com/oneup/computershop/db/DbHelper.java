package com.oneup.computershop.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = "ComputerShop";

    private static final String DATABASE_NAME = "UPlayer.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_REPAIRS = "repairs";

    private static final String SERVER_URL = "http://192.168.2.202:8080/api/repairs/";
    private static final String PENDING = "pending";
    private static final int PENDING_INSERT = 1;
    private static final int PENDING_UPDATE = 2;
    private static final int PENDING_DELETE = 3;

    private static final String SQL_CREATE_REPAIRS =
            "CREATE TABLE " + TABLE_REPAIRS + "(" +
                    Repair.ID + " INTEGER PRIMARY KEY," +
                    Repair.START_DATE + " INTEGER," +
                    Repair.END_DATE + " INTEGER," +
                    Repair.STATUS + " INTEGER," +
                    Repair.DESCRIPTION + " TEXT," +
                    PENDING + " INTEGER)";

    private final RequestQueue requestQueue;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        requestQueue = Volley.newRequestQueue(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "DbHelper.onCreate()");
        db.execSQL(SQL_CREATE_REPAIRS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public ArrayList<Repair> queryRepairs() {
        Log.d(TAG, "DbHelper.queryRepairs()");
        try (SQLiteDatabase db = getReadableDatabase()) {
            try (Cursor c = db.query(TABLE_REPAIRS, null,
                    PENDING + "!=" + PENDING_DELETE, null,
                    null, null, Repair.START_DATE + " DESC")) {
                ArrayList<Repair> repairs = new ArrayList<>();
                while (c.moveToNext()) {
                    repairs.add(new Repair(c));
                }
                Log.d(TAG, repairs.size() + " repairs queried");
                return repairs;
            }
        }
    }

    public void insertOrUpdateRepair(Repair repair) {
        Log.d(TAG, "DbHelper.insertOrUpdateRepair(" + repair.getId() + ")");
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = repair.getValues();

            if (repair.getId() == 0) {
                values.put(PENDING, PENDING_INSERT);
                repair.setId(db.insert(TABLE_REPAIRS, null, values));
                Log.d(TAG, "Repair inserted: " + repair.getId());
                requestInsert(repair);
            } else {
                values.put(PENDING, PENDING_UPDATE);
                if (db.update(TABLE_REPAIRS, values, repair.getWhereClause(), null) == 1) {
                    Log.d(TAG, "Repair updated: " + repair.getId());
                } else {
                    throw new SQLiteException("Repair not found: " + repair.getId());
                }
                requestUpdate(repair);
            }
        }
    }

    public void deleteRepair(Repair repair) {
        Log.d(TAG, "DbHelper.deleteRepair(" + repair.getId() + ")");
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(PENDING, PENDING_DELETE);
            if (db.update(TABLE_REPAIRS, values, repair.getWhereClause(), null) == 1) {
                Log.d(TAG, "Repair deleted: " + repair.getId());
            } else {
                throw new SQLiteException("Repair not found: " + repair.getId());
            }
            requestDelete(repair);
        }
    }

    public void sync(Runnable callback) {
        Log.d(TAG, "DbHelper.sync()");
        try (SQLiteDatabase db = getReadableDatabase()) {
            try (Cursor c = db.query(TABLE_REPAIRS, null, PENDING + " IS NOT NULL", null,
                    null, null, null)) {
                while (c.moveToNext()) {
                    Repair repair = new Repair(c);
                    switch (c.getInt(5)) {
                        case PENDING_INSERT:
                            requestInsert(repair);
                            break;
                        case PENDING_UPDATE:
                            requestUpdate(repair);
                            break;
                        case PENDING_DELETE:
                            requestDelete(repair);
                            break;
                    }
                }
            }

            requestQueue.add(new JsonArrayRequest(SERVER_URL,
                    new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray response) {
                            Log.d(TAG, "onResponse: " + response);
                            try {
                                setRepairs(response);
                                callback.run();
                            } catch (Exception ex) {
                                Log.e(TAG, "Error", ex);
                            }
                        }
                    },
                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "onErrorResponse", error);
                        }
                    }));
        }
    }

    private void requestInsert(Repair repair) {
        Log.d(TAG, "DbHelper.requestInsert(" + repair.getId() + ")");
        requestQueue.add(new JsonObjectRequest(Request.Method.POST, SERVER_URL,
                repair.getJsonObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Insert response: " + response);
                        resetRepairPending(repair);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Insert error", error);
                    }
                }));
        Log.d(TAG, "Insert request posted");
    }

    private void requestUpdate(Repair repair) {
        Log.d(TAG, "DbHelper.requestUpdate(" + repair.getId() + ")");
        requestQueue.add(new JsonObjectRequest(Request.Method.PUT,
                SERVER_URL + repair.getId(), repair.getJsonObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Update response: " + response);
                        resetRepairPending(repair);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Update error", error);
                    }
                }));
        Log.d(TAG, "Update request posted");
    }

    private void requestDelete(Repair repair) {
        Log.d(TAG, "DbHelper.requestDelete(" + repair.getId() + ")");
        requestQueue.add(new JsonObjectRequest(Request.Method.DELETE,
                SERVER_URL + repair.getId(), new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Delete response: " + response);
                        deleteRepair(repair);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Delete error", error);
                    }
                }));
        Log.d(TAG, "Delete request posted");
    }

    private void resetRepairPending(Repair repair) {
        Log.d(TAG, "DbHelper.setRepairPending(" + repair.getId() + ")");
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.putNull(PENDING);

            if (db.update(TABLE_REPAIRS, values, repair.getWhereClause(), null) == 1) {
                Log.d(TAG, "Repair updated: " + repair.getId());
            } else {
                throw new SQLiteException("Repair not found: " + repair.getId());
            }
        }
    }

    public void setRepairs(JSONArray repairs) throws JSONException {
        Log.d(TAG, "DbHelper.setRepairs()");
        try (SQLiteDatabase db = getWritableDatabase()) {
            Log.d(TAG, db.delete(TABLE_REPAIRS, PENDING + " IS NULL", null) + " repairs deleted");

            for (int i = 0; i < repairs.length(); i++) {
                JSONObject repair = repairs.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put(Repair.ID, repair.getLong("ID"));
                values.put(Repair.START_DATE, repair.getLong("StartDate"));
                values.put(Repair.END_DATE, repair.getLong("EndDate"));
                values.put(Repair.STATUS, repair.getInt("Status"));
                values.put(Repair.DESCRIPTION, repair.getString("Description"));

                db.insert(TABLE_REPAIRS, null, values);
                Log.d(TAG, "Repair inserted: " + repair);
            }
        }
    }
}
