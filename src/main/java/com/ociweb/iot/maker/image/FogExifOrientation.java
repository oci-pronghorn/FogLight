package com.ociweb.iot.maker.image;

public enum FogExifOrientation {
    topLeft(1),
    topRight(2),
    bottomRight(3),
    bottomLeft(4),
    leftTop(5),
    rightTop(6),
    rightBottom(7),
    leftBottom(8);

    private int specification;

    FogExifOrientation(int specification) {
        this.specification = specification;
    }

    public int getSpecification() {
        return specification;
    }
}
