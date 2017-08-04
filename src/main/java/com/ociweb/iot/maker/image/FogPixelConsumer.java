package com.ociweb.iot.maker.image;

/**
 * FogPixelConsumer is used by a FogPixelScanner to act on individual pixels of a bmp
 */
@FunctionalInterface
public interface FogPixelConsumer {
    void consume(FogBitmap bmp, int i, int x, int y);
}
