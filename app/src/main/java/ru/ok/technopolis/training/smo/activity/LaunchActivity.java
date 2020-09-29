package ru.ok.technopolis.training.smo.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import ru.ok.technopolis.training.smo.R;

public class LaunchActivity extends AppCompatActivity {

    private EditText countSourceEditText;
    private EditText countDevicesEditText;
    private EditText countRequestsEditText;
    private EditText bufferCapacityEditText;
    private EditText lambdaEditText;
    private EditText alphaEditText;
    private EditText betaEditText;
    private EditText stepEditText;
    private TextView startTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        countSourceEditText = findViewById(R.id.count_source);
        countDevicesEditText = findViewById(R.id.count_devices);
        countRequestsEditText = findViewById(R.id.count_requests);
        bufferCapacityEditText = findViewById(R.id.buffer_capacity);
        lambdaEditText = findViewById(R.id.lambda);
        alphaEditText = findViewById(R.id.alpha);
        betaEditText = findViewById(R.id.beta);
        stepEditText = findViewById(R.id.step);
        startTextView = findViewById(R.id.start_tv);
        startTextView.setOnClickListener((v) -> {
            Intent intent = new Intent(this, SmoActivity.class);
            final String countSources = countSourceEditText.getText().toString();
            if (!TextUtils.isEmpty(countSources)) {
                intent.putExtra(SmoActivity.COUNT_SOURCES_EXTRA, Integer.parseInt(countSources));
            }
            final String countDevices = countDevicesEditText.getText().toString();
            if (!TextUtils.isEmpty(countDevices)) {
                intent.putExtra(SmoActivity.COUNT_DEVICES_EXTRA, Integer.parseInt(countDevices));
            }
            final String countRequests = countRequestsEditText.getText().toString();
            if (!TextUtils.isEmpty(countRequests)) {
                intent.putExtra(SmoActivity.COUNT_REQUESTS_EXTRA, Integer.parseInt(countRequests));
            }
            final String bufferCapacity = bufferCapacityEditText.getText().toString();
            if (!TextUtils.isEmpty(bufferCapacity)) {
                intent.putExtra(SmoActivity.BUFFER_CAPACITY_EXTRA, Integer.parseInt(bufferCapacity));
            }
            final String lambda = lambdaEditText.getText().toString();
            if (!TextUtils.isEmpty(lambda)) {
                intent.putExtra(SmoActivity.LAMBDA_EXTRA, Float.parseFloat(lambda));
            }
            final String alpha = alphaEditText.getText().toString();
            if (!TextUtils.isEmpty(alpha)) {
                intent.putExtra(SmoActivity.ALPHA_EXTRA, Float.parseFloat(alpha));
            }
            final String beta = betaEditText.getText().toString();
            if (!TextUtils.isEmpty(beta)) {
                intent.putExtra(SmoActivity.BETA_EXTRA, Float.parseFloat(beta));
            }
            final String step = stepEditText.getText().toString();
            if (!TextUtils.isEmpty(step)) {
                intent.putExtra(SmoActivity.STEP_EXTRA, Integer.parseInt(step));
            }
            startActivity(intent);
        });
    }
}