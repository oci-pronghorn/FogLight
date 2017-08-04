package com.ociweb.iot.maker.image;

public class FogPixelProgressiveScanner implements FogPixelScanner {
    private final FogBitmap bmp;
    private final FogPixelConsumer consumer;
    private final int width;
    private final int height;
    private int x = 0;
    private int y = 0;
    private int i = 0;

    public FogPixelProgressiveScanner(FogBitmap bmp, FogPixelConsumer consumer) {
        this.bmp = bmp;
        this.width = bmp.getWidth();
        this.height = bmp.getHeight();
        this.consumer = consumer;
    }

    @Override
    public boolean next(FogPixelConsumer consumer) {
        this.consumer.consume(bmp, i, x, y);
        x++;
        if (x == width) {
            x = 0;
            y++;
        }
        if (y == height) {
           return false;
        }
        i++;
        return true;
    }
}



