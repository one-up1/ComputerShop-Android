package com.oneup.computershop.db;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class Repair implements Parcelable {
    public static final String EXTRA_REPAIR = "com.oneup.extra.REPAIR";

    public static final int STATUS_BUSY = 1;
    public static final int STATUS_DONE = 2;

    static final String ID = "id";
    static final String START_DATE = "start_date";
    static final String END_DATE = "end_date";
    static final String STATUS = "status";
    static final String DESCRIPTION = "description";

    long id;
    private long startDate;
    private long endDate;
    private int status;
    private String description;

    public Repair() {
    }

    Repair(Cursor c) {
        this.id = c.getLong(0);
        this.startDate = c.getLong(1);
        this.endDate = c.getLong(2);
        this.status = c.getInt(3);
        this.description = c.getString(4);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(id);
        out.writeLong(startDate);
        out.writeLong(endDate);
        out.writeInt(status);
        out.writeString(description);
    }

    public long getId() {
        return id;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static final Parcelable.Creator<?> CREATOR = new Parcelable.Creator<Repair>() {

        @Override
        public Repair createFromParcel(Parcel source) {
            Repair repair = new Repair();
            repair.id = source.readLong();
            repair.startDate = source.readLong();
            repair.endDate = source.readLong();
            repair.status = source.readInt();
            repair.description = source.readString();
            return repair;
        }

        @Override
        public Repair[] newArray(int size) {
            return new Repair[size];
        }

    };
}
