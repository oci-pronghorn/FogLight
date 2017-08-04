package com.ociweb.iot.maker.image;

@FunctionalInterface
public interface FogPixelScanner {
    boolean next(FogPixelConsumer consumer);
}
