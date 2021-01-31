package com.oneup.computershop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;

import androidx.appcompat.app.AppCompatActivity;

import com.oneup.computershop.R;

import java.util.Calendar;

public class DateActivity extends AppCompatActivity implements
        CalendarView.OnDateChangeListener, View.OnClickListener {
    public static final String EXTRA_TITLE_ID = "com.oneup.extra.TITLE_ID";
    public static final String EXTRA_DATE = "com.oneup.extra.DATE";

    private Calendar calendar;

    private CalendarView calendarView;
    private Button bOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date);
        setTitle(getIntent().getIntExtra(EXTRA_TITLE_ID, 0));

        calendar = Calendar.getInstance();

        calendarView = findViewById(R.id.calendarView);
        if (getIntent().hasExtra(EXTRA_DATE)) {
            calendarView.setDate(getIntent().getLongExtra(EXTRA_DATE, 0));
        }
        calendarView.setOnDateChangeListener(this);

        bOk = findViewById(R.id.bOk);
        bOk.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_date, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.ok) {
            ok();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
        if (view == calendarView) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == bOk) {
            ok();
        }
    }

    private void ok() {
        setResult(RESULT_OK, new Intent().putExtra(EXTRA_DATE, calendar.getTimeInMillis()));
        finish();
    }
}
