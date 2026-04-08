package de.rwth_aachen.phyphox;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends AppCompatActivity implements Experiment {

    private SensorManager sensorManager;
    private SensorInput gyroscopeInput;
    private RemoteServer remoteServer;
    public boolean measuring = false;
    public long millisUntilFinished = 0;
    private Handler updateHandler;
    private ReentrantLock dataLock;
    private ExperimentTimeReference timeReference;

    private TextView xValue, yValue, zValue;
    private Button startButton, stopButton, remoteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xValue = findViewById(R.id.x_value);
        yValue = findViewById(R.id.y_value);
        zValue = findViewById(R.id.z_value);
        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);
        remoteButton = findViewById(R.id.remote_button);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        dataLock = new ReentrantLock();
        timeReference = new ExperimentTimeReference();

        try {
            // Create gyroscope sensor input
            gyroscopeInput = new SensorInput(SensorInput.SensorName.gyroscope, null, null, false, 100, SensorInput.SensorRateStrategy.auto, 1, false, null, dataLock, timeReference);
            gyroscopeInput.attachSensorManager(sensorManager);
        } catch (SensorInput.SensorException e) {
            Log.e("MainActivity", "Error creating gyroscope input: " + e.getMessage());
            Toast.makeText(this, "Gyroscope not available", Toast.LENGTH_LONG).show();
            finish();
        }

        updateHandler = new Handler();

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMeasurement();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopMeasurement();
            }
        });

        remoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRemoteServer();
            }
        });

        // Create a simple experiment for remote server
        PhyphoxExperiment experiment = new PhyphoxExperiment();
        experiment.inputSensors.add(gyroscopeInput);
        experiment.dataLock = dataLock;
        experiment.experimentTimeReference = timeReference;

        // Create remote server
        remoteServer = new RemoteServer(experiment, this);
    }

    private void startMeasurement() {
        if (!measuring) {
            gyroscopeInput.start();
            measuring = true;
            updateHandler.postDelayed(updateUI, 100);
            Toast.makeText(this, "Measurement started", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopMeasurement() {
        if (measuring) {
            gyroscopeInput.stop();
            measuring = false;
            updateHandler.removeCallbacks(updateUI);
            Toast.makeText(this, "Measurement stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleRemoteServer() {
        if (remoteServer != null) {
            if (remoteServer.httpServer != null) {
                remoteServer.stop();
                Toast.makeText(this, "Remote server stopped", Toast.LENGTH_SHORT).show();
            } else {
                remoteServer.start();
                String addresses = RemoteServer.getAddresses(this);
                Toast.makeText(this, "Remote server started. Access at:\n" + addresses, Toast.LENGTH_LONG).show();
            }
        }
    }

    private Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            if (measuring && gyroscopeInput != null) {
                dataLock.lock();
                try {
                    if (gyroscopeInput.dataX != null && gyroscopeInput.dataY != null && gyroscopeInput.dataZ != null) {
                        xValue.setText(String.format("X: %.3f rad/s", gyroscopeInput.dataX.value));
                        yValue.setText(String.format("Y: %.3f rad/s", gyroscopeInput.dataY.value));
                        zValue.setText(String.format("Z: %.3f rad/s", gyroscopeInput.dataZ.value));
                    }
                } finally {
                    dataLock.unlock();
                }
                updateHandler.postDelayed(this, 100);
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        stopMeasurement();
        if (remoteServer != null) {
            remoteServer.stop();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateHandler.postDelayed(updateUI, 100);
    }

    // Methods required by RemoteServer
    public void remoteStartMeasurement() {
        startMeasurement();
    }

    public void remoteStopMeasurement() {
        stopMeasurement();
    }

    public void clearData() {
        // Clear data buffers
        if (gyroscopeInput != null) {
            dataLock.lock();
            try {
                if (gyroscopeInput.dataX != null) gyroscopeInput.dataX.clear();
                if (gyroscopeInput.dataY != null) gyroscopeInput.dataY.clear();
                if (gyroscopeInput.dataZ != null) gyroscopeInput.dataZ.clear();
                if (gyroscopeInput.dataT != null) gyroscopeInput.dataT.clear();
            } finally {
                dataLock.unlock();
            }
        }
    }

    public void requestDefocus() {
        // Not needed for this simple implementation
    }
}
