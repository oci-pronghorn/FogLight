package com.ociweb.iot.grove.six_axis_accelerometer;

public abstract class AccerometerValues implements MagValsListener, AccelValsListener, TempValsListener {
    private int t = 0;
    private int mX = 0;
    private int mY = 0;
    private int mZ = 0;
    private int aX = 0;
    private int aY = 0;
    private int aZ = 0;

    public enum Changed {
        mag,
        accel,
        temp
    }

    public AccerometerValues() {
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
        this.mX = x;
        this.mY = y;
        this.mZ = z;
        onChange(Changed.mag);
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
        this.aX = x;
        this.aY = y;
        this.aZ = z;
        onChange(Changed.accel);
    }

    public abstract void onChange(Changed changed);

    public int getMagX() {
        return mX;
    }

    public int getMagY() {
        return mY;
    }

    public int getMagZ() {
        return mZ;
    }

    public int getAccelX() {
        return aX;
    }

    public int getAccelY() {
        return aY;
    }

    public int getAccelZ() {
        return aZ;
    }

    public double getPitch() {
        return Math.asin(-aX);
    }

    public double getRoll() {
        return Math.asin(aY/Math.cos(getPitch()));
    }

    public double getHeading() {
        double heading = 180.0 * Math.atan2(mY, mX) / Math.PI;
        if (heading < 0) {
            heading += 360.0;
        }
        return heading;
    }

    public double getTiltHeading()  {
        double pitch = getPitch();
        double roll = getRoll();
        double xh = mX * Math.cos(pitch) + mZ * Math.sin(pitch);
        double yh = mX * Math.sin(roll) * Math.sin(pitch) + mY * Math.cos(roll) - mZ * Math.sin(roll) * Math.cos(pitch);
        double zh = -mX * Math.cos(roll) * Math.sin(pitch) + mY * Math.sin(roll) + mZ * Math.cos(roll) * Math.cos(pitch);
        double heading = 180 * Math.atan2(yh, xh)/Math.PI;
        if (yh < 0) {
            heading += 360.0;
        }
        return heading;
    }
}
