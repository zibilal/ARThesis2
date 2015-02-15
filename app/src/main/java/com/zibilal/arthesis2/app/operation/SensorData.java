package com.zibilal.arthesis2.app.operation;

/**
 * Created by bilalmuhammad on 1/3/15.
 */
public class SensorData implements Data {

    private long timestamp;
    private double x;
    private double y;
    private double z;

    public SensorData(long timestamp, double x, double y, double z){
        this.timestamp=timestamp;
        this.x=x;
        this.y=y;
        this.z=z;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public String getData() {
        return String.format("%4.4f,%4.4f,%4.4f\n", getX(), getY(), getZ());
    }
}
