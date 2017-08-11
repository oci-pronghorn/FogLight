package com.ociweb.iot.maker.image;

/**
 * FogBmpDisplayable allows a transducer to fully describe
 * and use bmps
 */
public interface FogBmpDisplayable {

    FogBitmapLayout newBmpLayout();

    FogBitmap newEmptyBmp();

    FogPixelScanner newPreferredBmpScanner(FogBitmap bmp);

    boolean display(FogPixelScanner scanner);
}
