package com.ociweb.iot.maker.image;

public enum FogColorSpace {
    gray {
        @Override
        public FogBitmapLayout createDefaultLayout() {
            FogBitmapLayout layout = new FogBitmapLayout();
            layout.setComponentCount((byte)1);
            return layout;
        }
    },
    rgb {
        @Override
        public FogBitmapLayout createDefaultLayout() {
            FogBitmapLayout layout = new FogBitmapLayout();
            layout.setComponentCount((byte)3);
            return layout;
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

    public abstract FogBitmapLayout createDefaultLayout();
}
