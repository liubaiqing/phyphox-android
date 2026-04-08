package de.rwth_aachen.phyphox;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

public class PhyphoxExperiment {

    public List<SensorInput> inputSensors = new ArrayList<>();
    public List<DataBuffer> dataBuffers = new ArrayList<>();
    public Lock dataLock;
    public ExperimentTimeReference experimentTimeReference;
    public boolean loaded = true;
    public String title = "Gyroscope Lightweight";
    public String message = "";
    public String baseTitle = "Gyroscope Lightweight";
    public String category = "Gyroscope";
    public String baseCategory = "Gyroscope";
    public boolean isLocal = true;
    public String source = null;
    public long crc32 = 0;
    public boolean timedRun = false;
    public double timedRunStartDelay = 3.0;
    public double timedRunStopDelay = 10.0;
    public DataExport exporter = new DataExport(this);
    public List<BluetoothInput> bluetoothInputs = new ArrayList<>();
    public List<BluetoothOutput> bluetoothOutputs = new ArrayList<>();
    public List<NetworkConnection> networkConnections = new ArrayList<>();
    public Object audioRecord = null;
    public GpsInput gpsIn = null;
    public Object depthInput = null;
    public Object cameraInput = null;
    public String micOutput = "";
    public String micRateOutput = "";
    public List<ExpView> experimentViews = new ArrayList<>();
    public boolean newData = false;
    public double analysisTime = 0.0;
    public Object resources = null;
    public String resourceFolder = "";
    public java.util.Map<String, String> highlightedLinks = new java.util.HashMap<>();

    public void init(Object sensorManager, Object locationManager) {
        // Initialize sensors
        for (SensorInput sensor : inputSensors) {
            sensor.attachSensorManager((android.hardware.SensorManager) sensorManager);
        }
    }

    public DataBuffer getBuffer(String name) {
        for (DataBuffer buffer : dataBuffers) {
            if (buffer.name.equals(name)) {
                return buffer;
            }
        }
        return null;
    }
}
