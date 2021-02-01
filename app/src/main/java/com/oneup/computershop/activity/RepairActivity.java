package com.oneup.computershop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.oneup.computershop.R;
import com.oneup.computershop.Util;
import com.oneup.computershop.db.DbHelper;
import com.oneup.computershop.db.Repair;
import com.oneup.computershop.db.Server;

public class RepairActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ComputerShop";

    private static final int REQUEST_SELECT_START_DATE = 1;
    private static final int REQUEST_SELECT_END_DATE = 2;

    private Repair repair;

    private Button bStartDate;
    private Button bEndDate;
    private RadioGroup rgStatus;
    private RadioButton rbStatusBusy;
    private RadioButton rbStatusDone;
    private EditText etDescription;
    private Button bOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repair);

        if (savedInstanceState == null) {
            repair = getIntent().getParcelableExtra(Repair.EXTRA_REPAIR);
        } else {
            repair = savedInstanceState.getParcelable(Repair.EXTRA_REPAIR);
        }

        if (repair == null) {
            repair = new Repair();
        }

        bStartDate = findViewById(R.id.bStartDate);
        bStartDate.setOnClickListener(this);

        bEndDate = findViewById(R.id.bEndDate);
        bEndDate.setOnClickListener(this);

        rgStatus = findViewById(R.id.rgStatus);
        rbStatusBusy = findViewById(R.id.rbStatusBusy);
        rbStatusDone = findViewById(R.id.rbStatusDone);

        etDescription = findViewById(R.id.etDescription);

        bOk = findViewById(R.id.bOk);
        bOk.setOnClickListener(this);

        if (repair == null) {
            rbStatusBusy.setChecked(true);
        } else {
            if (repair.getStartDate() != 0) {
                setDate(bStartDate, repair.getStartDate());
            }
            if (repair.getEndDate() != 0) {
                setDate(bEndDate, repair.getEndDate());
            }
            switch (repair.getStatus()) {
                case Repair.STATUS_BUSY:
                    rbStatusBusy.setChecked(true);
                    break;
                case Repair.STATUS_DONE:
                    rbStatusDone.setChecked(true);
                    break;
            }
            if (repair.getDescription() != null) {
                etDescription.setText(repair.getDescription());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AppCompatActivity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_SELECT_START_DATE:
                    repair.setStartDate(setDate(bStartDate,
                            data.getLongExtra(DateActivity.EXTRA_DATE, 0)));
                    break;
                case REQUEST_SELECT_END_DATE:
                    repair.setEndDate(setDate(bEndDate,
                            data.getLongExtra(DateActivity.EXTRA_DATE, 0)));
                    break;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putParcelable(Repair.EXTRA_REPAIR, repair);
    }

    @Override
    public void onClick(View v) {
        if (v == bStartDate) {
            selectDate(R.string.start_date, repair.getStartDate(), REQUEST_SELECT_START_DATE);
        } else if (v == bEndDate) {
            selectDate(R.string.end_date, repair.getEndDate(), REQUEST_SELECT_END_DATE);
        } else if (v == bOk) {
            ok();
        }
    }

    private void selectDate(int titleId, long date, int requestCode) {
        Intent intent = new Intent(this, DateActivity.class);
        intent.putExtra(DateActivity.EXTRA_TITLE_ID, titleId);
        if (date != 0) {
            intent.putExtra(DateActivity.EXTRA_DATE, date);
        }
        startActivityForResult(intent, requestCode);
    }

    private long setDate(Button button, long date) {
        button.setText(Util.formatDate(date));
        return date;
    }

    private void ok() {
        repair.setDescription(etDescription.getText().toString());
        int status = rgStatus.getCheckedRadioButtonId();
        if (status == R.id.rbStatusBusy) {
            repair.setStatus(Repair.STATUS_BUSY);
        } else if (status == R.id.rbStatusDone) {
            repair.setStatus(Repair.STATUS_DONE);
        }

        boolean insert = repair.getId() == 0;
        new DbHelper(this).insertOrUpdateRepair(repair);
        if (insert) {
            Server.get(this).insertRepair(repair);
        } else {
            Server.get(this).updateRepair(repair);
        }

        setResult(RESULT_OK);
        finish();
    }
}
