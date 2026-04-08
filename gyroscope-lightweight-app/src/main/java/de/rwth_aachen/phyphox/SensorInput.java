package de.rwth_aachen.phyphox;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import java.io.Serializable;
import java.util.Vector;
import java.util.concurrent.locks.Lock;

public class SensorInput implements SensorEventListener, Serializable {

    public int type;
    public SensorName sensorName;
    public String sensorNameFilter = null;
    public int sensorTypeFilter = -1;
    public boolean calibrated = true;
    public long period;
    public int stride;
    int strideCount;
    public SensorRateStrategy rateStrategy;
    private boolean lastOneTooFast = false;
    private final ExperimentTimeReference experimentTimeReference;
    public double fixDeviceTimeOffset = 0.0;

    public boolean ignoreUnavailable = false;

    public DataBuffer dataX;
    public DataBuffer dataY;
    public DataBuffer dataZ;
    public DataBuffer dataT;
    public DataBuffer dataAbs;
    public DataBuffer dataAccuracy;
    transient private SensorManager sensorManager;

    private long lastReading;
    private double avgX, avgY, avgZ, avgAccuracy;
    private double genX, genY, genZ, genAccuracy;
    private boolean average = false;
    private int aquisitions;

    private Lock dataLock;

    public boolean vendorSensor = false;
    public Sensor sensor;

    public enum SensorName {
        accelerometer, linear_acceleration, gravity, gyroscope, magnetic_field, pressure, light, proximity, temperature, humidity, attitude, custom
    }

    public enum SensorRateStrategy {
        auto, request, generate, limit
    }

    public static class SensorException extends Exception {
        public SensorException(String message) {
            super(message);
        }
    }

    public static int resolveSensorName(SensorName type) {
        switch (type) {
            case accelerometer: return Sensor.TYPE_ACCELEROMETER;
            case linear_acceleration: return Sensor.TYPE_LINEAR_ACCELERATION;
            case gravity: return Sensor.TYPE_GRAVITY;
            case gyroscope: return Sensor.TYPE_GYROSCOPE;
            case magnetic_field: return Sensor.TYPE_MAGNETIC_FIELD;
            case pressure: return Sensor.TYPE_PRESSURE;
            case light: return Sensor.TYPE_LIGHT;
            case proximity: return Sensor.TYPE_PROXIMITY;
            case temperature: return Sensor.TYPE_AMBIENT_TEMPERATURE;
            case humidity: return Sensor.TYPE_RELATIVE_HUMIDITY;
            case attitude: return Sensor.TYPE_ROTATION_VECTOR;
            case custom: return -1;
            default: return -2;
        }
    }

    private SensorInput(boolean ignoreUnavailable, double rate, SensorRateStrategy rateStrategy, int stride, boolean average, Vector<DataOutput> buffers, Lock lock, ExperimentTimeReference experimentTimeReference) throws SensorException {
        this.dataLock = lock;
        this.experimentTimeReference = experimentTimeReference;

        if (rate <= 0)
            this.period = 0;
        else
            this.period = (long) ((1 / rate) * 1e9);
        this.stride = stride;
        this.rateStrategy = rateStrategy;
        this.average = average;
        this.ignoreUnavailable = ignoreUnavailable;

        if (buffers == null)
            return;

        // Create default data buffers for gyroscope
        dataX = new DataBuffer("gyroscope_x", 1000);
        dataY = new DataBuffer("gyroscope_y", 1000);
        dataZ = new DataBuffer("gyroscope_z", 1000);
        dataT = new DataBuffer("time", 1000);
    }

    public SensorInput(SensorName type, String nameFilter, Integer typeFilter, boolean ignoreUnavailable, double rate, SensorRateStrategy rateStrategy, int stride, boolean average, Vector<DataOutput> buffers, Lock lock, ExperimentTimeReference experimentTimeReference) throws SensorException {
        this(ignoreUnavailable, rate, rateStrategy, stride, average, buffers, lock, experimentTimeReference);
        this.type = resolveSensorName(type);
        this.sensorName = type;
        this.sensorNameFilter = nameFilter;
        this.sensorTypeFilter = typeFilter;
        if (this.type < -1)
            throw new SensorException("Unknown sensor.");
    }

    private Sensor findSensor() {
        Sensor sensor = null;
        vendorSensor = false;
        if (type >= 0)
            sensor = sensorManager.getDefaultSensor(type);
        return sensor;
    }

    public void attachSensorManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        sensor = findSensor();
    }

    public boolean isAvailable() {
        return (sensor != null);
    }

    public static int getDescriptionRes(int type) {
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER:
                return R.string.sensorAccelerometer;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                return R.string.sensorLinearAcceleration;
            case Sensor.TYPE_GRAVITY:
                return R.string.sensorGravity;
            case Sensor.TYPE_GYROSCOPE:
                return R.string.sensorGyroscope;
            case Sensor.TYPE_MAGNETIC_FIELD:
                return R.string.sensorMagneticField;
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                return R.string.sensorMagneticField;
            case Sensor.TYPE_PRESSURE:
                return R.string.sensorPressure;
            case Sensor.TYPE_LIGHT:
                return R.string.sensorLight;
            case Sensor.TYPE_PROXIMITY:
                return R.string.sensorProximity;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                return R.string.sensorTemperature;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return R.string.sensorHumidity;
            case Sensor.TYPE_ROTATION_VECTOR:
                return R.string.sensorAttitude;
        }
        return R.string.unknown;
    }

    public int getDescriptionRes() {
        return SensorInput.getDescriptionRes(type);
    }

    public static String getUnit(int type) {
        switch (type) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                return "m/s²";
            case Sensor.TYPE_LIGHT:
                return "lx";
            case Sensor.TYPE_GYROSCOPE:
                return "rad/s";
            case Sensor.TYPE_ACCELEROMETER:
                return "m/s²";
            case Sensor.TYPE_MAGNETIC_FIELD:
                return "µT";
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                return "µT";
            case Sensor.TYPE_PRESSURE:
                return "hPa";
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                return "°C";
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "%";
            case Sensor.TYPE_PROXIMITY:
                return "cm";
        }
        return "";
    }

    public void start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && (type == Sensor.TYPE_MAGNETIC_FIELD || type == Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED)) {
            if (calibrated)
                this.type = Sensor.TYPE_MAGNETIC_FIELD;
            else
                this.type = Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED;
        }

        sensor = findSensor();

        if (sensor == null)
            return;

        lastReading = 0;
        avgX = 0.;
        avgY = 0.;
        avgZ = 0.;
        avgAccuracy = 0.;
        aquisitions = 0;
        strideCount = 0;
        lastOneTooFast = false;

        if (rateStrategy == SensorRateStrategy.request || rateStrategy == SensorRateStrategy.auto)
            this.sensorManager.registerListener(this, sensor, (int)(period / 1000));
        else
            this.sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void stop() {
        if (sensor == null)
            return;
        this.sensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void appendToBuffers(long timestamp, double x, double y, double z, double accuracy) {
        strideCount++;
        if (strideCount < stride) {
            return;
        } else {
            strideCount = 0;
        }
        dataLock.lock();
        try {
            if (dataX != null)
                dataX.append(x);
            if (dataY != null)
                dataY.append(y);
            if (dataZ != null)
                dataZ.append(z);
            if (dataT != null) {
                double t;
                if (timestamp == 0) {
                    t = experimentTimeReference.getExperimentTime();
                } else {
                    t = experimentTimeReference.getExperimentTimeFromEvent(timestamp);
                    if (fixDeviceTimeOffset == 0.0) {
                        double now = experimentTimeReference.getExperimentTime();
                        if ((t < -300 || (t > now + 0.1)) && fixDeviceTimeOffset == 0.0) {
                            Log.w("SensorInput", "Unrealistic time offset detected at " + now + ". Applying adjustment of " + -t + "s.");
                            fixDeviceTimeOffset = now-t;
                        }
                    }
                    t += fixDeviceTimeOffset;
                    if (t < 0.0) {
                        Log.w("SensorInput", this.sensorName + ": Adjusted one timestamp from t = " + t + "s to t = 0s.");
                        t = 0.0;
                    }
                }
                dataT.append(t);
            }
        } finally {
            dataLock.unlock();
        }
    }

    private void resetAveraging(long t) {
        avgX = 0.;
        avgY = 0.;
        avgZ = 0.;
        avgAccuracy = 0.;
        lastReading = t;
        aquisitions = 0;
    }

    public void updateGeneratedRate() {
        long now;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            now = SystemClock.elapsedRealtimeNanos();
        } else {
            now = SystemClock.elapsedRealtime() * 1000000L;
        }
        if (rateStrategy == SensorRateStrategy.generate && lastReading > 0) {
            while (lastReading + 2*period <= now) {
                appendToBuffers(lastReading + period, genX, genY, genZ, genAccuracy);
                lastReading += period;
            }
        }
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == sensor.getType()) {
            Double accuracy = Double.NaN;

            if (rateStrategy == SensorRateStrategy.generate) {
                if (lastReading == 0) {
                    genX = event.values[0];
                    genY = event.values.length > 1 ? event.values[1] : event.values[0];
                    genZ = event.values.length > 2 ? event.values[2] : event.values[0];
                    genAccuracy = accuracy;
                } else if (lastReading + period <= event.timestamp) {
                    while (lastReading + 2*period <= event.timestamp) {
                        appendToBuffers(lastReading + period, genX, genY, genZ, genAccuracy);
                        lastReading += period;
                    }
                    if (aquisitions > 0) {
                        genX = avgX / aquisitions;
                        genY = avgY / aquisitions;
                        genZ = avgZ / aquisitions;
                        genAccuracy = avgAccuracy;
                    }
                    appendToBuffers(lastReading + period, genX, genY, genZ, genAccuracy);
                    resetAveraging(lastReading + period);
                }
            }

            if (rateStrategy != SensorRateStrategy.request) {
                if (average) {
                    avgX += event.values[0];
                    if (event.values.length > 1) {
                        avgY += event.values[1];
                        if (event.values.length > 2)
                            avgZ += event.values[2];
                    }
                    avgAccuracy = Math.min(accuracy, avgAccuracy);
                    aquisitions++;
                } else {
                    avgX = event.values[0];
                    if (event.values.length > 1) {
                        avgY = event.values[1];
                        if (event.values.length > 2)
                            avgZ = event.values[2];
                    }
                    avgAccuracy = accuracy;
                    aquisitions = 1;
                }
                if (lastReading == 0)
                    lastReading = event.timestamp;
            }

            switch (rateStrategy) {
                case auto:
                    if (event.timestamp - lastReading < period * 0.9) {
                        if (lastOneTooFast)
                            rateStrategy = SensorRateStrategy.generate;
                        lastOneTooFast = true;
                    } else {
                        lastOneTooFast = false;
                    }
                    appendToBuffers(event.timestamp, event.values[0], event.values.length > 1 ? event.values[1] : event.values[0], event.values.length > 2 ? event.values[2] : event.values[0], accuracy);
                    resetAveraging(event.timestamp);
                    break;
                case request:
                    appendToBuffers(event.timestamp, event.values[0], event.values.length > 1 ? event.values[1] : event.values[0], event.values.length > 2 ? event.values[2] : event.values[0], accuracy);
                    break;
                case generate:
                    break;
                case limit:
                    if (lastReading + period <= event.timestamp) {
                        appendToBuffers(event.timestamp, avgX / aquisitions, avgY / aquisitions, avgZ / aquisitions, avgAccuracy);
                        resetAveraging(event.timestamp);
                    }
                    break;
            }
        }
    }
}
