package com.ociweb.iot.grove;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.Hardware;

/**
 * Holds information for all standard Analog and Digital I/O twigs in the Grove starter kit.
 *
 * Methods are necessary for interpreting new connections declared in
 * {@link com.ociweb.iot.maker.IoTSetup#declareConnections(Hardware)} in the maker app.
 *
 * @see com.ociweb.iot.hardware.IODevice
 */
public enum GroveTwig implements IODevice {

    UVSensor() {
        @Override
        public boolean isInput() {
            return true;
        }

        @Override
        public int response() {
            return 30;
        }
    },
    LightSensor() {
        @Override
        public boolean isInput() {
            return true;
        }

        @Override
        public int response() {
            return 100;
        }
    },
    SoundSensor() {
        @Override
        public boolean isInput() {
            return true;
        }

        @Override
        public int response() {
            return 2;
        }
    },
    AngleSensor() {
        @Override
        public boolean isInput() {
            return true;
        }

        @Override
        public int response() {
            return 40;
        }

        @Override
        public int range() {
            return 1024;
        }
    },
    MoistureSensor() {
        @Override
        public boolean isInput() {
            return true;
        }
    },
    Button() {
        @Override
        public boolean isInput() {
            return true;
        }

        public int response() {
            return 40;
        }

        @Override
        public int range() {
            return 1;
        }

    },
    MotionSensor() {
        @Override
        public boolean isInput() {
            return true;
        }

        @Override
        public int range() {
            return 1;
        }
    },
    RotaryEncoder() {
        @Override
        public boolean isInput() {
            return true;
        }

        @Override
        public int pinsUsed() {
            return 2;
        }

    },
    Buzzer() {
        @Override
        public boolean isOutput() {
            return true;
        }

    },
    LED() {
        @Override
        public boolean isOutput() {
            return true;
        }

        @Override
        public boolean isPWM() {
            return true;
        }
    },
    Relay() {
        @Override
        public boolean isOutput() {
            return true;
        }
    },
    Servo() {
        @Override
        public boolean isOutput() {
            return true;
        }
    },
    I2C() {
        @Override
        public boolean isInput() {
            return true;
        }

        @Override
        public boolean isOutput() {
            return true;
        }
    },
    UltrasonicRanger() {
        @Override
        public boolean isInput() {
            return true;
        }

        @Override
        public int range() {
            return 1024;
        }

        @Override
        public int response() {
            return 200;
        }

        @Override
        public int scanDelay() {
            return 1_420_000;
        }
    };

    /**
     * @return True if this twig is an input device, and false otherwise.
     */
    public boolean isInput() {
        return false;
    }

    /**
     * @return True if this twig is an output device, and false otherwise.
     */
    public boolean isOutput() {
        return false;
    }

    /**
     * @return Response time, in milliseconds, for this twig.
     */
    public int response() {
        return 20;
    }

    /**
     * @return Delay, in milliseconds, for scan. TODO: What's scan?
     */
    public int scanDelay() {
        return 0;
    }

    /**
     * @return True if this twig is Pulse Width Modulated (PWM) device, and
     *         false otherwise.
     */
    public boolean isPWM() {
        return false;
    }

    /**
     * @return True if this twig is an I2C device, and false otherwise.
     */
    public boolean isI2C() {
        return false;
    }

    /**
     * @return The {@link I2CConnection} for this twig if it is an I2C
     *         device, as indicated by {@link #isI2C()}.
     */
    public I2CConnection getI2CConnection() {
        return null;
    }

    /**
     * @return The possible value range for reads from this device (from zero).
     */
    public int range() {
        return 256;
    }

    /**
     * TODO: What does this do?
     *
     * @return
     */
    public byte[] I2COutSetup() {
        return null;
    }

    /**
     * TODO: This does something with ring buffers, but what?
     *
     * @param backing
     * @param position
     * @param length
     * @param mask
     *
     * @return
     */
    public boolean isValid(byte[] backing, int position, int length, int mask) {
        return true;
    }

    /**
     * @return The number of hardware pins that this twig uses.
     */
    public int pinsUsed() {
        return 1;
    }
}
