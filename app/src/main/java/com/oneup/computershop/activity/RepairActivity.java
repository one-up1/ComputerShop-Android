package com.oneup.computershop.activity;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.oneup.computershop.NetworkService;
import com.oneup.computershop.R;
import com.oneup.computershop.Util;
import com.oneup.computershop.db.DbHelper;
import com.oneup.computershop.db.Repair;

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
        scheduleTask(insert);

        setResult(RESULT_OK);
        finish();
    }

    private void scheduleTask(boolean insert) {
        PersistableBundle extras = new PersistableBundle();
        extras.putLong(NetworkService.EXTRA_REPAIR_ID, repair.getId());
        extras.putBoolean(NetworkService.EXTRA_INSERT, insert);
        /*GcmNetworkManager.getInstance(this).schedule(
                new OneoffTask.Builder()
                        .setService(NetworkService.class)
                        .setTag(TAG)
                        .setExecutionWindow(1000, 2000)
                        .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                        .setRequiresCharging(false)
                        //.setPersisted(true)
                        .setUpdateCurrent(true)
                       // .setExtras(extras)
                        .build());*/

        ComponentName serviceComponent = new ComponentName(this, NetworkService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(1000); // wait at least
        builder.setOverrideDeadline(3000); // maximum delay
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        builder.setRequiresCharging(false); // we don't care if the device is charging or not
        builder.setExtras(extras);
        JobScheduler jobScheduler = getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());
        Log.d(TAG, "Scheduled");
    }
}
