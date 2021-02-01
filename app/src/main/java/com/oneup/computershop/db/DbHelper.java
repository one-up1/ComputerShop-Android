package com.oneup.computershop.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLInput;
import java.util.ArrayList;

public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = "ComputerShop";

    public static final String PENDING = "pending";
    public static final int PENDING_INSERT = 1;
    public static final int PENDING_UPDATE = 2;
    public static final int PENDING_DELETE = 3;

    private static final String DATABASE_NAME = "UPlayer.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_REPAIRS = "repairs";

    private static final String SQL_CREATE_REPAIRS =
            "CREATE TABLE " + TABLE_REPAIRS + "(" +
                    Repair.ID + " INTEGER PRIMARY KEY," +
                    Repair.START_DATE + " INTEGER," +
                    Repair.END_DATE + " INTEGER," +
                    Repair.STATUS + " INTEGER," +
                    Repair.DESCRIPTION + " TEXT," +
                    PENDING + " INTEGER)";


    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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

    public Repair queryRepair(long id) {
        Log.d(TAG, "DbHelper.queryRepair(" + id + ")");
        try (SQLiteDatabase db = getReadableDatabase()) {
            try (Cursor c = db.query(TABLE_REPAIRS, null,
                    Repair.ID + "=" + id, null, null, null, null)) {
                if (c.moveToFirst()) {
                    return new Repair(c);
                } else {
                    throw new SQLiteException("Repair not found: " + id);
                }
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
            } else {
                values.put(PENDING, PENDING_UPDATE);
                if (db.update(TABLE_REPAIRS, values, repair.getWhereClause(), null) == 1) {
                    Log.d(TAG, "Repair updated: " + repair.getId());
                } else {
                    throw new SQLiteException("Repair not found: " + repair.getId());
                }
            }
        }
    }

    public void deleteRepair(Repair repair) {
        Log.d(TAG, "DbHelper.deleteRepair(" + repair.getId() + ")");
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(PENDING, PENDING_DELETE);

            if (db.update(TABLE_REPAIRS, values, repair.getWhereClause(), null) == 1) {
                Log.d(TAG, "Repair scheduled for deletion: " + repair.getId());
            } else {
                throw new SQLiteException("Repair not found: " + repair.getId());
            }
        }
    }

    public void resetRepairPending(Repair repair) {
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
