package com.ociweb.iot.maker.image;

/**
 * FogPixelScanner provides a strategy to iterate over pixels of a bmp
 */
@FunctionalInterface
public interface FogPixelScanner {
    boolean next(FogPixelConsumer consumer);
}
