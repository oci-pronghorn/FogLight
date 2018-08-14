package com.ociweb.iot.grove.six_axis_accelerometer;

public abstract class AccelerometerListener implements
        MagValsListener,
        AccelValsListener,
        TempValsListener {

    private AccelerometerValues values = new AccelerometerValues();

    public enum Changed {
        mag,
        accel,
        temp
    }

    @Override
    public AccelerometerMagDataRate getMagneticDataRate() {
        return AccelerometerMagDataRate.hz50;
    }

    @Override
    public AccelerometerMagScale getMagneticScale() {
        return AccelerometerMagScale.gauss2;
    }

    @Override
    public AccelerometerMagRes getMagneticRes() {
        return AccelerometerMagRes.high;
    }

    @Override
    public void magneticValues(int x, int y, int z) {
        values.magneticValues(x, y, z);
        onChange(values, Changed.mag);
    }

    @Override
    public AccelerometerAccelDataRate getAccerometerDataRate() {
        return AccelerometerAccelDataRate.hz50;
    }

    @Override
    public AccelerometerAccelScale getAccerometerScale() {
        return AccelerometerAccelScale.gauss2;
    }

    @Override
    public int getAccerometerAxes() {
        return  AccelerometerAccelAxes.all;
    }

    @Override
    public void accelerationValues(int x,int y,int z) {
        values.accelerationValues(x, y, z);
        onChange(values, Changed.accel);
    }

    public abstract void onChange(AccelerometerValues values, Changed changed);
}
