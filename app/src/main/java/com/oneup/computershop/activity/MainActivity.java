package com.oneup.computershop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.oneup.computershop.R;
import com.oneup.computershop.Util;
import com.oneup.computershop.db.DbHelper;
import com.oneup.computershop.db.Repair;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener {
    private static final int REQUEST_EDIT_REPAIR = 1;

    private DbHelper db;
    private ArrayList<Repair> repairs;

    private ListView lvRepairs;
    private RepairAdapter repairAdapter;

    private FloatingActionButton fabAddRepair;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DbHelper(this);

        lvRepairs = findViewById(R.id.lvRepairs);
        lvRepairs.setOnItemClickListener(this);
        registerForContextMenu(lvRepairs);

        fabAddRepair = findViewById(R.id.fabAddRepair);
        fabAddRepair.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        repairs = db.queryRepairs();
        if (repairAdapter == null) {
            repairAdapter = new RepairAdapter();
            lvRepairs.setAdapter(repairAdapter);
        } else {
            repairAdapter.notifyDataSetChanged();
        }
        sync();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v == lvRepairs) {
            getMenuInflater().inflate(R.menu.list_item_repair, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        int index = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
        Repair repair = repairs.get(index);

        if (id == R.id.delete) {
            db.deleteRepair(repair);
            repairs.remove(index);
            repairAdapter.notifyDataSetChanged();
            sync();
        } else {
            return super.onContextItemSelected(item);
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        editRepair(repairs.get(position));
    }

    @Override
    public void onClick(View v) {
        if (v == fabAddRepair) {
            editRepair(null);
        }
    }

    private void editRepair(Repair repair) {
        Intent intent = new Intent(this, RepairActivity.class);
        if (repair != null) {
            intent.putExtra(Repair.EXTRA_REPAIR, repair);
        }
        startActivityForResult(intent, REQUEST_EDIT_REPAIR);
    }

    private void sync() {
        db.sync(new Runnable() {

            @Override
            public void run() {
                repairs = db.queryRepairs();
                repairAdapter.notifyDataSetChanged();
            }
        });
    }

    private class RepairAdapter extends BaseAdapter {
        public RepairAdapter() {
        }

        @Override
        public int getCount() {
            return repairs.size();
        }

        @Override
        public Object getItem(int position) {
            return repairs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            Repair repair = repairs.get(position);
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.list_item_repair, parent, false);
            }

            TextView tvStartDate = view.findViewById(R.id.tvStartDate);
            if (repair.getStartDate() == 0) {
                tvStartDate.setVisibility(View.GONE);
            } else {
                tvStartDate.setText(Util.formatDate(repair.getStartDate()));
                tvStartDate.setVisibility(View.VISIBLE);
            }

            TextView tvEndDate = view.findViewById(R.id.tvEndDate);
            if (repair.getEndDate() == 0) {
                tvEndDate.setVisibility(View.GONE);
            } else {
                tvEndDate.setText(Util.formatDate(repair.getEndDate()));
                tvEndDate.setVisibility(View.VISIBLE);
            }

            TextView tvStatus = view.findViewById(R.id.tvStatus);
            switch (repair.getStatus()) {
                case Repair.STATUS_BUSY:
                    tvStatus.setText(R.string.busy);
                    tvStatus.setVisibility(View.VISIBLE);
                    break;
                case Repair.STATUS_DONE:
                    tvStatus.setText(R.string.done);
                    tvStatus.setVisibility(View.VISIBLE);
                    break;
                default:
                    tvStatus.setVisibility(View.GONE);
                    break;
            }

            TextView tvDescription = view.findViewById(R.id.tvDescription);
            if (repair.getDescription() == null) {
                tvDescription.setVisibility(View.GONE);
            } else {
                tvDescription.setText(repair.getDescription());
                tvDescription.setVisibility(View.VISIBLE);
            }

            return view;
        }
    }
}