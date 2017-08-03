package com.ociweb.iot.maker.image;

import com.ociweb.iot.maker.FogExternalizable;
import com.ociweb.pronghorn.pipe.BlobReader;
import com.ociweb.pronghorn.pipe.BlobWriter;

public class FogBitmapLayout implements FogExternalizable {
    private int width = 0;
    private int height = 0;
    private byte componentCount = 1;
    private byte componentDepth = 1;
    private byte minComponentWidth = 1;

    private byte componentWidth;
    private double magnitude;
    private int valueMask;
    private int rowWidth;
    private int pixelWidth;

    // Construction

    public FogBitmapLayout() {
        cacheCalculatedValues();
    }

    // Accessors

    @Override
    public void writeExternal(BlobWriter writer) {
        writer.writeInt(width);
        writer.writeInt(height);
        writer.writeByte(componentCount);
        writer.writeByte(componentDepth);
        writer.writeByte(minComponentWidth);
    }

    // Pixels wide
    public int getWidth() {
        return width;
    }

    // Pixels high
    public int getHeight() {
        return height;
    }

    // 3 for RGB, 4 for RGBA, 1 for Grayscale, etc
    public byte getComponentCount() {
        return componentCount;
    }

    // For each component how many bits are used
    public byte getComponentDepth() {
        return componentDepth;
    }

    // How many total bytes needed for bitmap
    public int bmpSize() {
        return height * width * pixelWidth;
    }

    // Mutators

    @Override
    public void readExternal(BlobReader reader) {
        width = reader.readInt();
        height = reader.readInt();
        componentCount = reader.readByte();
        componentDepth = reader.readByte();
        minComponentWidth = reader.readByte();
        cacheCalculatedValues();
    }

    public void setWidth(int width) {
        this.width = width;
        cacheCalculatedValues();
    }

    public void setHeight(int height) {
        this.height = height;
        cacheCalculatedValues();
    }

    public void setComponentCount(byte componentCount) {
        assert(componentCount > 1) : "componentCount must be greater than 1";
        this.componentCount = componentCount;
        cacheCalculatedValues();
    }

    public void setComponentDepth(byte componentDepth) {
        assert(componentDepth > 1 && componentDepth <= 32) : "componentDepth must be between 1 and 32";
        this.componentDepth = componentDepth;
        cacheCalculatedValues();
    }

    public void setMinComponentDepth(byte minComponentWidth) {
        assert(componentDepth > 1 && componentDepth <= 4) : "minComponentWidth must be between 1 and 4";
        this.minComponentWidth = minComponentWidth;
        cacheCalculatedValues();
    }

    private void cacheCalculatedValues() {
        magnitude = Math.pow(componentDepth, 2.0);
        valueMask = 0xFFFFFFFF >>> (32 - componentDepth);
        componentWidth = (byte)Math.max( Math.ceil(componentDepth / 8d), minComponentWidth);
        pixelWidth = componentCount * componentWidth;
        rowWidth = width * pixelWidth;
    }

    // Bitmap Manipulations

    public byte[] allocateBitmap() {
        return new byte[bmpSize()];
    }

    public int address(int x, int y, int z) {
        assert(x >= 0 && x < width) : "x must be in 0 indexed range of width";
        assert(y >= 0 && y < height) : "y must be in 0 indexed range of height";
        assert(z >= 0 && z < componentCount) : "z must be in 0 indexed range of componentCount";
        return (x * rowWidth) + (y * pixelWidth) + (z * componentWidth);
    }

    public double getValue(byte[] bmp, int x, int y, int z) {
        double value = getComponent(bmp, x, y, z);
        return (value / magnitude);
    }

    public void setValue(byte[] bmp, int x, int y, int z, double value) {
        int component = (int) (value * magnitude);
        setComponent(bmp, x, y, z, component);
    }

    public int getComponent(byte[] bmp, int x, int y, int z) {
        int i = address(x, y, z);
        switch (componentWidth) {
            case 1:
                return bmp[i];
            case 2:
                return ((bmp[i] & 0xFF) << 8) | (bmp[i+1] & 0xFF);
            case 3:
                return ((bmp[i] & 0xFF) << 16) | ((bmp[i+1] & 0xFF) << 8) | (bmp[i+2] & 0xFF);
            case 4:
                return ((bmp[i] & 0xFF) << 24) | ((bmp[i+1] & 0xFF) << 16) | ((bmp[i+2] & 0xFF) << 8) | (bmp[i+3] & 0xFF);
        }
        return 0;
    }

    public void setComponent(byte[] bmp, int x, int y, int z, int value) {
        value = value & valueMask;
        int i = address(x, y, z);
        switch (componentWidth) {
            case 1:
                bmp[i] = (byte) value;
                break;
            case 2:
                bmp[i] = (byte) ((value >> 8) & 0xFF);
                bmp[i+1] = (byte) (value & 0xFF);
                break;
            case 3:
                bmp[i] = (byte) ((value >> 16) & 0xFF);
                bmp[i+1] = (byte) ((value >> 8) & 0xFF);
                bmp[i+2] = (byte) (value & 0xFF);
                break;
            case 4:
                bmp[i] = (byte) ((value >> 24) & 0xFF);
                bmp[i+1] = (byte) ((value >> 16) & 0xFF);
                bmp[i+2] = (byte) ((value >> 8) & 0xFF);
                bmp[i+3] = (byte) (value & 0xFF);
                break;
        }
    }
}
