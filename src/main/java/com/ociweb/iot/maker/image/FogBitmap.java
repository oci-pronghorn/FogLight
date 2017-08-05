package com.ociweb.iot.maker.image;

import com.ociweb.iot.maker.FogExternalizable;
import com.ociweb.pronghorn.pipe.BlobReader;
import com.ociweb.pronghorn.pipe.BlobWriter;

/**
 * FogBitmap combines a FogBitmapLayout with a backstore
 */
public class FogBitmap implements FogExternalizable {
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

    public FogBitmap(BlobReader in) {
        readExternal(in);
    }

    @Override
    public void readExternal(BlobReader in) {
        layout.readExternal(in);
        bmp = this.layout.allocateBitmap();
        in.read(bmp);
    }

    @Override
    public void writeExternal(BlobWriter out) {
        layout.writeExternal(out);
        out.write(bmp);
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
