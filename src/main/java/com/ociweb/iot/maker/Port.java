package com.ociweb.iot.maker;

/**
 * Enumeration for analog and digital ports on a device with GPIO.
 *
 * @author Nathan Tippy
 */
public enum Port {
    A0(Port.IS_ANALOG, 0),
    A1(Port.IS_ANALOG, 1),
    A2(Port.IS_ANALOG, 2),

    D0(Port.IS_DIGITAL, 0),
    D1(Port.IS_DIGITAL, 1),
    D2(Port.IS_DIGITAL, 2),
    D3(Port.IS_DIGITAL, 3),
    D4(Port.IS_DIGITAL, 4),
    D5(Port.IS_DIGITAL, 5),
    D6(Port.IS_DIGITAL, 6),
    D7(Port.IS_DIGITAL, 7),
    D8(Port.IS_DIGITAL, 8);

    public final byte port;
    public final byte mask;

    private Port(byte mask, int number) {
        this.port = (byte) number;
        this.mask = mask;
    }

    public static final byte IS_ANALOG = 1;
    public static final byte IS_DIGITAL = 2;

    /**
     * @return True if this port is analog, and false otherwise.
     */
    public boolean isAnalog() {
        return 0 != (IS_ANALOG & mask);
    }

    public static Port nextPort(Port p) {    	
    	return Port.values()[p.ordinal()+1];
    }
    
    /**
     * Array of all standard analog ports.
     */
    public static Port[] ANALOGS = {A0, A1, A2};

    /**
     * Array of all standard digital ports.
     */
    public static Port[] DIGITALS = {D0, D1, D2, D3, D4, D5, D6, D7, D8};
}

