package com.ociweb.iot.maker.image;

public enum FogFixedRotation {
    one(0.0),
    two(Math.PI * 0.5),
    three(Math.PI),
    four(Math.PI * 1.5);

    private double radians;

    FogFixedRotation(double radians) {
        this.radians = radians;
    }

    public double getRadians() {
        return radians;
    }
}
