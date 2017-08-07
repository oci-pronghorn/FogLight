package com.ociweb.iot.maker.image;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * FogBitmap combines a FogBitmapLayout with a backstore
 */
public class FogBitmap implements Externalizable {
    private FogBitmapLayout layout;
    private byte[] bmp;

    // Represents a color value
    public FogBitmap(FogColorSpace colorSpace, byte componentDepth) {
        this(colorSpace, componentDepth, 1, 1);
    }

    public FogBitmap(FogColorSpace colorSpace, byte componentDepth, int width, int height) {
        this.layout = new FogBitmapLayout(colorSpace);
        this.layout.setWidth(width);
        this.layout.setHeight(height);
        this.layout.setComponentDepth(componentDepth);
        this.bmp = this.layout.allocateBitmap();
    }

    public FogBitmap(FogBitmapLayout layout) {
        this.layout = layout;
        this.bmp = this.layout.allocateBitmap();
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        layout.readExternal(in);
        bmp = this.layout.allocateBitmap();
        in.read(bmp);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        layout.writeExternal(out);
        out.write(bmp);
    }

    public int messageSize() {
        return layout.bmpSize() + layout.messageSize();
    }

    public int getWidth() {
        return layout.getWidth();
    }

    public int getHeight() {
        return layout.getHeight();
    }

    public double getValue(int x, int y, int z) {
        return layout.getValue(bmp, x, y, z);
    }

    public void setValue(int x, int y, int z, double value) {
        layout.setValue(bmp, x, y, z, value);
    }

    public int getComponent(int x, int y, int z) {
        return layout.getComponent(bmp, x, y, z);
    }

    public void setComponent(int x, int y, int z, int value) {
        layout.setComponent(bmp, x, y, z, value);
    }
}
