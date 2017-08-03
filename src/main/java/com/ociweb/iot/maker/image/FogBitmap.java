package com.ociweb.iot.maker.image;

import com.ociweb.iot.maker.FogExternalizable;
import com.ociweb.pronghorn.pipe.BlobReader;
import com.ociweb.pronghorn.pipe.BlobWriter;

public class FogBitmap implements FogExternalizable {
    private final FogBitmapLayout layout;
    private byte[] bmp;

    FogBitmap() {
        this.layout = new FogBitmapLayout();
        this.bmp = this.layout.allocateBitmap();
    }

    FogBitmap(FogBitmapLayout layout) {
        this.layout = layout;
        this.bmp = this.layout.allocateBitmap();
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

    // Pixels wide
    public int getWidth() {
        return layout.getWidth();
    }

    // Pixels high
    public int getHeight() {
        return layout.getHeight();
    }

    // 3 for RGB, 4 for RGBA, 1 for Grayscale, etc
    public byte getComponentCount() {
        return layout.getComponentCount();
    }

    // For each component how many bits are used
    public byte getComponentDepth() {
        return layout.getComponentDepth();
    }

    // How many total bytes in bmp
    public int bmpSize() {
        return this.bmp.length;
    }

    public int address(int x, int y, int z) {
        return layout.address(x, y, z);
    }

    public double getValue(byte[] bmp, int x, int y, int z) {
        return layout.getValue(bmp, x, y, z);
    }

    public void setValue(byte[] bmp, int x, int y, int z, double value) {
        layout.setValue(bmp, x, y, z, value);
    }

    public int getComponent(byte[] bmp, int x, int y, int z) {
        return layout.getComponent(bmp, x, y, z);
    }

    public void setComponent(byte[] bmp, int x, int y, int z, int value) {
        layout.setComponent(bmp, x, y, z, value);
    }
}
