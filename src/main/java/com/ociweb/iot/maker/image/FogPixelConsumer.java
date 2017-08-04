package com.ociweb.iot.maker.image;

@FunctionalInterface
public interface FogPixelConsumer {
    void consume(FogBitmap bmp, int i, int x, int y);
}
