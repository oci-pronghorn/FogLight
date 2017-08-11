package com.ociweb.iot.maker.image;

/**
 * Scans the pixels linearly from (0,0) to (width, height)
 * but does even lines, then odd lines
 */
public class FogPixelInterleaveScanner implements FogPixelScanner {
    private final FogBitmap bmp;
    private final int width;
    private final int height;
    private int x = 0;
    private int y = 0;
    private int i = 0;
    private int stage = 0;

    public FogPixelInterleaveScanner(FogBitmap bmp) {
        this.bmp = bmp;
        this.width = bmp.getWidth();
        this.height = bmp.getHeight();
    }

    public void reset() {
        x = y = i = stage = 0;
    }

    @Override
    public boolean next(FogPixelConsumer consumer) {
        consumer.consume(bmp, i, x, y);
        x++;
        if (x == width) {
            x = 0;
            y+=2;
        }
        if (y >= height) {
            if (stage == 0) {
                y = 1;
                stage++;
            }
            else {
                return false;
            }
        }
        i++;
        return true;
    }
}
