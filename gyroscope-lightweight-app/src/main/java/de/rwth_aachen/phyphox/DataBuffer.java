package de.rwth_aachen.phyphox;

public class DataBuffer {

    public String name;
    public int size;
    private double[] data;
    private int fillLevel;
    public double value;

    public DataBuffer(String name, int size) {
        this.name = name;
        this.size = size;
        this.data = new double[size];
        this.fillLevel = 0;
        this.value = Double.NaN;
    }

    public void append(double value) {
        this.value = value;
        if (fillLevel < size) {
            data[fillLevel] = value;
            fillLevel++;
        } else {
            // Shift data left and add new value at the end
            System.arraycopy(data, 1, data, 0, size - 1);
            data[size - 1] = value;
        }
    }

    public void clear() {
        fillLevel = 0;
        value = Double.NaN;
    }

    public int getFilledSize() {
        return fillLevel;
    }

    public Double[] getArray() {
        Double[] result = new Double[fillLevel];
        for (int i = 0; i < fillLevel; i++) {
            result[i] = data[i];
        }
        return result;
    }
}
