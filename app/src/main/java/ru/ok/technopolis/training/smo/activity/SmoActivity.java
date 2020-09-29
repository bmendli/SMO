package ru.ok.technopolis.training.smo.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import ru.ok.technopolis.training.smo.R;
import ru.ok.technopolis.training.smo.adapter.DeviceAdapter;
import ru.ok.technopolis.training.smo.adapter.DeviceKoefAdapter;
import ru.ok.technopolis.training.smo.adapter.SourceAdapter;
import ru.ok.technopolis.training.smo.controller.Controller;
import ru.ok.technopolis.training.smo.device.Device;

public class SmoActivity extends AppCompatActivity {

    public static final String COUNT_SOURCES_EXTRA = "count_sources";
    public static final String COUNT_DEVICES_EXTRA = "count_devices";
    public static final String COUNT_REQUESTS_EXTRA = "count_requests";
    public static final String BUFFER_CAPACITY_EXTRA = "buffer_capacity";
    public static final String LAMBDA_EXTRA = "lambda";
    public static final String ALPHA_EXTRA = "alpha_sources";
    public static final String BETA_EXTRA = "beta_sources";
    public static final String STEP_EXTRA = "step";

    private DeviceKoefAdapter deviceKoefAdapter;

    private RecyclerView sourceRecyclerView;
    private RecyclerView deviceRecyclerView;
    private RecyclerView deviceUsableRecyclerView;

    private TextView countSourcesTextView;
    private TextView countDevicesTextView;
    private TextView bufferCapacityTextView;
    private TextView lambdaTextView;
    private TextView alphaTextView;
    private TextView betaTextView;
    private TextView probabilityRefusalTextView;
    private TextView loadFactorTextView;
    private TextView timeInSystemTextView;

    private View dividerSecond;
    private View dividerThird;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smo);

        sourceRecyclerView = findViewById(R.id.source_recycler);
        deviceRecyclerView = findViewById(R.id.device_recycler);
        deviceUsableRecyclerView = findViewById(R.id.device_usable_recycler);

        countSourcesTextView = findViewById(R.id.count_sources);
        countDevicesTextView = findViewById(R.id.count_devices);
        bufferCapacityTextView = findViewById(R.id.buffer_capacity);
        lambdaTextView = findViewById(R.id.lambda);
        alphaTextView = findViewById(R.id.alpha);
        betaTextView = findViewById(R.id.beta);
        probabilityRefusalTextView = findViewById(R.id.probability_refusal);
        loadFactorTextView = findViewById(R.id.load_factor);
        timeInSystemTextView = findViewById(R.id.time_in_system);

        dividerSecond = findViewById(R.id.divider_second);
        dividerThird = findViewById(R.id.divider_third);

        SourceAdapter sourceAdapter = new SourceAdapter();
        sourceRecyclerView.setAdapter(sourceAdapter);
        sourceRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        DeviceAdapter deviceAdapter = new DeviceAdapter();
        deviceRecyclerView.setAdapter(deviceAdapter);
        deviceRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        deviceKoefAdapter = new DeviceKoefAdapter();
        deviceUsableRecyclerView.setAdapter(deviceKoefAdapter);
        deviceUsableRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        final Intent intent = getIntent();
        final int countSources = intent.getIntExtra(COUNT_SOURCES_EXTRA, 10);
        final int countDevices = intent.getIntExtra(COUNT_DEVICES_EXTRA, 2);
        final int countRequests = intent.getIntExtra(COUNT_REQUESTS_EXTRA, 1000);
        final int bufferCapacity = intent.getIntExtra(BUFFER_CAPACITY_EXTRA, 8);
        final float lambda = intent.getFloatExtra(LAMBDA_EXTRA, 2f);
        final float alpha = intent.getFloatExtra(ALPHA_EXTRA, 0.05f);
        final float beta = intent.getFloatExtra(BETA_EXTRA, 0.15f);
        final int step = intent.getIntExtra(STEP_EXTRA, 10);
        Controller controller = new Controller(sourceAdapter, deviceAdapter, this);
        controller.startSystem(countSources, countDevices, countRequests, bufferCapacity, step, lambda, alpha, beta);
    }

    public void showResult(int countSources, int countDevices, int bufferCapacity,
                           float lambda, float alpha, float beta, double probabilityRefusal,
                           double loadFactor, double timeInSystem, List<Device> devices, long tomeWorkingSystem) {
        countSourcesTextView.setVisibility(View.VISIBLE);
        countSourcesTextView.setText(getString(R.string.count_sources_result, countSources));
        countDevicesTextView.setVisibility(View.VISIBLE);
        countDevicesTextView.setText(getString(R.string.count_devices_result, countDevices));
        bufferCapacityTextView.setVisibility(View.VISIBLE);
        bufferCapacityTextView.setText(getString(R.string.buffer_capacity_result, bufferCapacity));
        lambdaTextView.setVisibility(View.VISIBLE);
        lambdaTextView.setText(getString(R.string.lambda_result, lambda));
        alphaTextView.setVisibility(View.VISIBLE);
        alphaTextView.setText(getString(R.string.alpha_result, alpha));
        betaTextView.setVisibility(View.VISIBLE);
        betaTextView.setText(getString(R.string.beta_result, beta));
        probabilityRefusalTextView.setVisibility(View.VISIBLE);
        probabilityRefusalTextView.setText(getString(R.string.probability_refusal_result, (float) 100 * probabilityRefusal));
        loadFactorTextView.setVisibility(View.VISIBLE);
        loadFactorTextView.setText(getString(R.string.load_factor_result, (float) 100 * loadFactor));
        timeInSystemTextView.setVisibility(View.VISIBLE);
        timeInSystemTextView.setText(getString(R.string.time_in_system_result, (float) timeInSystem));
        deviceUsableRecyclerView.setVisibility(View.VISIBLE);
        deviceKoefAdapter.setNewData(devices, tomeWorkingSystem);
        dividerSecond.setVisibility(View.VISIBLE);
        dividerThird.setVisibility(View.VISIBLE);
    }
}