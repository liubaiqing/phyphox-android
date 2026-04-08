package de.rwth_aachen.phyphox;

public interface Experiment {
    boolean measuring = false;
    long millisUntilFinished = 0;

    void remoteStartMeasurement();
    void remoteStopMeasurement();
    void clearData();
    void requestDefocus();
}
