package de.rwth_aachen.phyphox;

public class GpsInput {
    public boolean forceGNSS = false;
    public DataBuffer dataLat = null;
    public DataBuffer dataLon = null;
    public DataBuffer dataZ = null;
    public DataBuffer dataZWGS84 = null;
    public DataBuffer dataV = null;
    public DataBuffer dataDir = null;
    public DataBuffer dataT = null;
    public DataBuffer dataAccuracy = null;
    public DataBuffer dataZAccuracy = null;
    public DataBuffer dataStatus = null;
    public DataBuffer dataSatellites = null;

    public void prepare(Object resources) {
        // Empty implementation
    }
}
