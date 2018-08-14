package com.ociweb.iot.maker.image;

public class FogImageOrientation {
    private FogFixedRotation rotation;
    private boolean mirrored;
    private final boolean mirroredX;

    FogImageOrientation(FogFixedRotation rotation, boolean mirrored, boolean mirroredX) {
        this.rotation = rotation;
        this.mirrored = mirrored;
        this.mirroredX = mirroredX;
    }

    FogImageOrientation(FogFixedRotation rotation, boolean mirrored) {
        this.rotation = rotation;
        this.mirrored = mirrored;
        this.mirroredX = true;
    }

    public void cycleRotation() {
        int idx = rotation.ordinal();
        if (idx == FogFixedRotation.values().length) {
            idx = 0;
        }
        rotation = FogFixedRotation.values()[idx];
    }

    public void cycleMirrored() {
        mirrored = !mirrored;
    }

    public FogExifOrientation getExifOrientation() {
        if (!mirrored) {
            switch (rotation) {
                case one:
                    return FogExifOrientation.topLeft;
                case two:
                    return FogExifOrientation.rightTop;
                case three:
                    return FogExifOrientation.bottomRight;
                case four:
                    return FogExifOrientation.leftBottom;
            }
        }
        switch (rotation) {
            case one:
                return mirroredX ? FogExifOrientation.topRight : FogExifOrientation.bottomLeft;
            case two:
                return mirroredX ? FogExifOrientation.rightBottom : FogExifOrientation.leftTop;
            case three:
                return mirroredX ? FogExifOrientation.bottomLeft : FogExifOrientation.topRight;
            case four:
                return mirroredX ? FogExifOrientation.leftTop : FogExifOrientation.rightBottom;
        }
        return null;
    }
}