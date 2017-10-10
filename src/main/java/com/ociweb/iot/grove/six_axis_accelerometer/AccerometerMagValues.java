package com.ociweb.iot.grove.six_axis_accelerometer;

public abstract class AccerometerMagValues implements MagValsListener {
    private final AccelerometerMagDataRate rate;
    private final AccelerometerMagScale scale;
    private final AccelerometerMagRes res;

    private int x = 0;
    private int y = 0;
    private int z = 0;

    public AccerometerMagValues(AccelerometerMagDataRate rate, AccelerometerMagScale scale, AccelerometerMagRes res) {
        this.rate = rate;
        this.scale = scale;
        this.res = res;
    }

    @Override
    public AccelerometerMagDataRate getMagneticDataRate() {
        return rate;
    }

    @Override
    public AccelerometerMagScale getMagneticScale() {
        return scale;
    }

    @Override
    public AccelerometerMagRes getMagneticRes() {
        return res;
    }

    @Override
    public void magneticValues(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        onChange();
    }

    public abstract void onChange();

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public double getHeading() {
        double heading = 180.0 * Math.atan2(y, x) / Math.PI;
        if (heading < 0) {
            heading += 360.0;
        }
        return heading;
    }
}
