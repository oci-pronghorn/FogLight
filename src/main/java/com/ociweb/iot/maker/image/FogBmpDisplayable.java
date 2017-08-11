package com.ociweb.iot.maker.image;

public interface FogBmpDisplayable {

    FogBitmapLayout newBmpLayout();

    FogBitmap newEmptyBmp();

    FogPixelScanner newPreferredBmpScanner(FogBitmap bmp);

    boolean display(FogPixelScanner scanner);
}
