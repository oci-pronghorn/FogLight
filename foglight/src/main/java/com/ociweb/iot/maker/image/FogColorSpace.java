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
    },
    rgba {
        @Override
        public byte getComponentCount() {
            return (byte)4;
        }
    };

    public abstract byte getComponentCount();
}
