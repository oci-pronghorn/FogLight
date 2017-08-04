package com.ociweb.iot.maker.image;

/**
 * FogColorSpace defines all supported colorspaces
 */
public enum FogColorSpace {
    gray {
        @Override
        public byte getComponentCount() {
            return (byte)1;
        }
    },
    rgb {
        @Override
        public byte getComponentCount() {
            return (byte)3;
        }
    };
    /*
    rgba {
        @Override
        public FogBitmapLayout createDefaultLayout() {
            FogBitmapLayout layout = new FogBitmapLayout();
            layout.setComponentCount((byte)4);
            return layout;
        }
    },
    hls {
        @Override
        public FogBitmapLayout createDefaultLayout() {
            FogBitmapLayout layout = new FogBitmapLayout();
            layout.setComponentCount((byte)3);
            return layout;
        }
    },

    hlsa {
        @Override
        public FogBitmapLayout createDefaultLayout() {
            FogBitmapLayout layout = new FogBitmapLayout();
            layout.setComponentCount((byte)4);
            return layout;
        }
    };
    */

    public abstract byte getComponentCount();
}
