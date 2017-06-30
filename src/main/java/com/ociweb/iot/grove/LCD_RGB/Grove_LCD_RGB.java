package com.ociweb.iot.grove.LCD_RGB;

import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.IODeviceFacade;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.pronghorn.iot.i2c.I2CStage;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;

/**
 * Utility class for interacting with a Grove LCD RGB connected to a
 * {@link FogCommandChannel}.
 *
 * @author Nathan Tippy
 * @author Brandon Sanders [brandon@alicorn.io]
 * @author Alex Herriott
 */
public class Grove_LCD_RGB implements IODevice {

    public static boolean isStarted = false;

    // Current LCD_DISPLAYCONTROL states
    private static int LCD_DISPLAY = Grove_LCD_RGB_Constants.LCD_DISPLAYON;
    private static int LCD_CURSOR = Grove_LCD_RGB_Constants.LCD_CURSOROFF;
    private static int LCD_BLINK = Grove_LCD_RGB_Constants.LCD_BLINKOFF;

    /**
     * Sets up a Grove LCD RGB display on a given {@link FogCommandChannel}.
     * 
     * TODO: Move this so that it is automatically initialized on startup() if connected
     *
     * @param target {@link FogCommandChannel} of the I2C device for the Grove LCD RGB display.
     *
     * @return True if the device was successfully connected, and false otherwise.
     */
    public static boolean begin(FogCommandChannel target) {
        if (!target.i2cIsReady()) {
            return false;
        }
        isStarted = true;

        writeSingleByteToRegister(target, Grove_LCD_RGB_Constants.LCD_ADDRESS, Grove_LCD_RGB_Constants.LCD_SETDDRAMADDR,
                                  Grove_LCD_RGB_Constants.LCD_TWO_LINES);
        target.i2cDelay(Grove_LCD_RGB_Constants.LCD_ADDRESS,
                        5 * Grove_LCD_RGB_Constants.MS_TO_NS);  // wait more than 4.1ms

        // second try
        writeSingleByteToRegister(target, Grove_LCD_RGB_Constants.LCD_ADDRESS, Grove_LCD_RGB_Constants.LCD_SETDDRAMADDR,
                                  Grove_LCD_RGB_Constants.LCD_TWO_LINES);
        target.i2cDelay(Grove_LCD_RGB_Constants.LCD_ADDRESS, 1 * Grove_LCD_RGB_Constants.MS_TO_NS);

        // third go
        writeSingleByteToRegister(target, Grove_LCD_RGB_Constants.LCD_ADDRESS, Grove_LCD_RGB_Constants.LCD_SETDDRAMADDR,
                                  Grove_LCD_RGB_Constants.LCD_TWO_LINES);
        target.i2cDelay(Grove_LCD_RGB_Constants.LCD_ADDRESS, 1 * Grove_LCD_RGB_Constants.MS_TO_NS);


        // turn the display on with no cursor or blinking default
        setDisplayControl(target);

        // clear it off
        displayClear(target);

        // set the entry mode
        //writeSingleByteToRegister(target, Grove_LCD_RGB_Constants.LCD_ADDRESS, LCD_SETDDRAMADDR, LCD_ENTRYMODESET | LCD_ENTRYLEFT | LCD_ENTRYSHIFTDECREMENT);
        target.i2cDelay(Grove_LCD_RGB_Constants.LCD_ADDRESS, 1 * Grove_LCD_RGB_Constants.MS_TO_NS);

        setCursor(target, 0, 0);
        target.i2cDelay(Grove_LCD_RGB_Constants.LCD_ADDRESS, 1 * Grove_LCD_RGB_Constants.MS_TO_NS);
        target.i2cFlushBatch();
        return true;
    }

    /**
     * Sends a command to a given Grove LCD RGB device that will set its currently displayed text and color.
     *
     * @param target {@link FogCommandChannel} initialized via {@link #begin(FogCommandChannel)}.
     * @param text String to display on the Grove RGB LCD.
     * @param r 0-255 value for the Red color.
     * @param g 0-255 value for the Green color.
     * @param b 0-255 value for the Blue color.
     *
     * @return True if the command was successfully sent, and false otherwise. False will be immediately returned if the
     * target is not {@link FogCommandChannel#i2cIsReady()}.
     */
    public static boolean commandForTextAndColor(FogCommandChannel target, String text, int r, int g, int b) {
        if (!target.i2cIsReady()) {
            return false;
        }

        showRGBColor(target, r, g, b);
        showTwoLineText(target, text);
        target.i2cFlushBatch();
        return true;
    }

    /**
     * Sends a command to a given Grove LCD RGB device that will set its currently displayed color.
     *
     * @param target {@link FogCommandChannel} initialized via {@link #begin(FogCommandChannel)}.
     * @param r 0-255 value for the Red color.
     * @param g 0-255 value for the Green color.
     * @param b 0-255 value for the Blue color.
     *
     * @return True if the command was successfully sent, and false otherwise. False will be immediately returned if the
     * target is not {@link FogCommandChannel#i2cIsReady()}.
     */
    public static boolean commandForColor(FogCommandChannel target, int r, int g, int b) {

        if (!target.i2cIsReady()) {
            return false;
        }

        showRGBColor(target, r, g, b);

        target.i2cFlushBatch();
        return true;
    }

    @Override
    public int scanDelay() {
        return 0;
    }

    /**
     * TODO: The JavaDoc is not right. Need to figure out what this what this command does
     * Sends a command to a given Grove LCD RGB device that will set its currently displayed color.
     *
     * @param target {@link FogCommandChannel} initialized via {@link #begin(FogCommandChannel)}.
     * @param text String to display on the Grove LCD RGB device.
     *
     * @return True if the command was successfully sent, and false otherwise. False will be immediately returned if the
     * target is not {@link FogCommandChannel#i2cIsReady()}.
     */
    public static boolean commandForText(FogCommandChannel target, CharSequence text) {

        if (!target.i2cIsReady()) {
            return false;
        }

        showTwoLineText(target, text);
        target.i2cFlushBatch();

        return true;
    }

    /**
     * Sends a command to a given Grove LCD RGB device that will turn it on or off. Defaults to on.
     *
     * @param target {@link FogCommandChannel} initialized via {@link #begin(FogCommandChannel)}.
     * @param on True if the display should be turned on, and false if it should be turned off.
     *
     * @return True if the command was successfully sent, and false otherwise. False will be immediately returned if the
     * target is not {@link FogCommandChannel#i2cIsReady()}.
     */
    public static boolean commandForDisplay(FogCommandChannel target, boolean on) {
        if (!target.i2cIsReady()) {
            return false;
        }

        if (on) {
            LCD_DISPLAY = Grove_LCD_RGB_Constants.LCD_DISPLAYON;
        } else {
            LCD_DISPLAY = Grove_LCD_RGB_Constants.LCD_DISPLAYOFF;
        }

        setDisplayControl(target);
        target.i2cFlushBatch();
        return true;
    }

    /**
     * Sends a command to a given Grove LCD RGB device that will turn it on or off. Defaults to off.
     *
     * @param target {@link FogCommandChannel} initialized via {@link #begin(FogCommandChannel)}.
     * @param on True if the cursor should be turned on, and false if it should be turned off.
     *
     * @return True if the command was successfully sent, and false otherwise. False will be immediately returned if the
     *         target is not {@link FogCommandChannel#i2cIsReady()}.
     */
    public static boolean commandForCursor(FogCommandChannel target, boolean on) {
        if (!target.i2cIsReady()) {
            return false;
        }

        if (on) {
            LCD_CURSOR = Grove_LCD_RGB_Constants.LCD_CURSORON;
        } else {
            LCD_CURSOR = Grove_LCD_RGB_Constants.LCD_CURSOROFF;
        }

        setDisplayControl(target);
        target.i2cFlushBatch();

        return true;
    }

    /**
     * Sends a command to a given Grove LCD RGB device that will enable or disable
     * cursor blinking. Defaults to off.
     *
     * @param target {@link FogCommandChannel} initialized via {@link #begin(FogCommandChannel)}.
     * @param on True if blinking should be turned on, and false if it should be turned off.
     *
     * @return True if the command was successfully sent, and false otherwise. False will be immediately returned if the
     *         target is not {@link FogCommandChannel#i2cIsReady()}.
     */
    public static boolean commandForBlink(FogCommandChannel target, boolean on) {
        if (!target.i2cIsReady()) {
            return false;
        }

        if (on) {
            LCD_BLINK = Grove_LCD_RGB_Constants.LCD_BLINKON;
        } else {
            LCD_BLINK = Grove_LCD_RGB_Constants.LCD_BLINKOFF;
        }

        setDisplayControl(target);
        target.i2cFlushBatch();

        return true;
    }

    /**
     * Sends a command to a given Grove LCD RGB device that will clear its display.
     *
     * @param target {@link FogCommandChannel} initialized via {@link #begin(FogCommandChannel)}.
     *
     * @return True if the command was successfully sent, and false otherwise. False will be immediately returned if the
     *         target is not {@link FogCommandChannel#i2cIsReady()}.
     */
    public static boolean clearDisplay(FogCommandChannel target) {
        if (!target.i2cIsReady()) {
            return false;
        }
        displayClear(target);

        target.i2cFlushBatch();
        return true;
    }

    /**
     * Sends a command to a given Grove LCD RGB device that will set its cursor position.
     *
     * @param target {@link FogCommandChannel} initialized via {@link #begin(FogCommandChannel)}.
     * @param col Column index to place the cursor at.
     * @param row Row index to place the cursor at.
     *
     * @return True if the command was successfully sent, and false otherwise. False will be immediately returned if the
     *         target is not {@link FogCommandChannel#i2cIsReady()}.
     */
    public static boolean setCursor(FogCommandChannel target, int col, int row) {
        if (!target.i2cIsReady()) {
            return false;
        }
        col = (row == 0 ? col | 0x80 : col | 0xc0);
        writeSingleByteToRegister(target, Grove_LCD_RGB_Constants.LCD_ADDRESS, Grove_LCD_RGB_Constants.LCD_SETDDRAMADDR,
                                  col);
        target.i2cFlushBatch();
        return true;
    }

    /////////////////////////////
    ////  Write text methods ////
    /////////////////////////////

    /**
     * Sends a command to a given Grove LCD RGB device that will save a new custom character to its
     * memory. Up to 8 custom characters can be saved on a single device.
     *
     * @param target {@link FogCommandChannel} initialized via {@link #begin(FogCommandChannel)}.
     * @param location location 0-7 to store the character map in the LCD.
     * @param charMap Array of 8 bytes. Each byte is a row. Least significant 5 bits determines values within row.
     *
     * @return True if the command was successfully sent, and false otherwise. False will be immediately returned if the
     *         target is not {@link FogCommandChannel#i2cIsReady()}.
     */
    public static boolean setCustomChar(FogCommandChannel target, int location, byte charMap[]) {
        if (!target.i2cIsReady()) {
            return false;
        }
        if (!isStarted) {
            begin(target);
        }
        assert (location < 8 && location >= 0) : "Only locations 0-7 are valid";
        assert (charMap.length == 8) : "charMap must contain an array of 8 bytes";
        location &= 0x7;
        for (int i = 0; i < charMap.length; i++) {
            charMap[i] &= 0x1F; //each element contains 5 bits
        }

        writeSingleByteToRegister(target, ((Grove_LCD_RGB_Constants.LCD_ADDRESS)),
                                  Grove_LCD_RGB_Constants.LCD_SETDDRAMADDR,
                                  Grove_LCD_RGB_Constants.LCD_SETCGRAMADDR | (location << 3));
        target.i2cDelay(Grove_LCD_RGB_Constants.LCD_ADDRESS, Grove_LCD_RGB_Constants.CGRAM_SET_DELAY);
        writeMultipleBytesToRegister(target, Grove_LCD_RGB_Constants.LCD_ADDRESS,
                                     Grove_LCD_RGB_Constants.LCD_SETCGRAMADDR, charMap, 0, charMap.length);
        target.i2cDelay(Grove_LCD_RGB_Constants.LCD_ADDRESS, Grove_LCD_RGB_Constants.DDRAM_WRITE_DELAY);
        target.i2cFlushBatch();

        //begin(target); //TODO: Seems to be necessary, but shouldn't be
        return true;
    }

    /**
     * Writes an ASCII char with the specified ID to a cell on a given Grove RGB LCD device.
     *
     * @param target {@link FogCommandChannel} initialized via {@link #begin(FogCommandChannel)}.
     * @param characterIdx Index/ID of the character to display. 0 - 7 are custom characters defined
     *                     via {@link #setCustomChar(FogCommandChannel, int, byte[])}.
     * @param col Column index to place the character on.
     * @param row Row index to place the character on.
     *
     * @return True if the command was successfully sent, and false otherwise. False will be immediately returned if the
     *         target is not {@link FogCommandChannel#i2cIsReady()}.
     */
    public static boolean writeChar(FogCommandChannel target, int characterIdx, int col, int row) {
        if (!target.i2cIsReady()) {
            return false;
        }
        setCursor(target, col, row);
        writeSingleByteToRegister(target, ((Grove_LCD_RGB_Constants.LCD_ADDRESS)),
                                  Grove_LCD_RGB_Constants.LCD_SETCGRAMADDR, characterIdx);
        target.i2cDelay(Grove_LCD_RGB_Constants.LCD_ADDRESS, Grove_LCD_RGB_Constants.DDRAM_WRITE_DELAY);
        target.i2cFlushBatch();

        return true;
    }

    /**
     * Writes multiple ASCII characters starting at a given cell and row on a given Grove RGB LCD device.
     *
     * TODO: What happens if the given column/row would cause the given array of characters to flow off-screen?
     *
     * @param target {@link FogCommandChannel} initialized via {@link #begin(FogCommandChannel)}.
     * @param characterIdx Ordered byte array of indexes/IDs of the characters to display. 0 - 7 are custom characters
     *                     defined via {@link #setCustomChar(FogCommandChannel, int, byte[])}.
     * @param col Column index to begin writing the characters from.
     * @param row Row index to begin writing the characters from.
     *
     * @return True if the command was successfully sent, and false otherwise. False will be immediately returned if the
     *         target is not {@link FogCommandChannel#i2cIsReady()}.
     */
    public static boolean writeMultipleChars(FogCommandChannel target, byte[] characterIdx, int col, int row) { //TODO: creates lots of garbage
        return writeMultipleChars(target, characterIdx, 0, characterIdx.length, col, row);
    }

    /**
     * Writes multiple ASCII characters starting at a given cell and row on a given Grove RGB LCD device.
     *
     * TODO: What happens if the given column/row would cause the given array of characters to flow off-screen?
     *
     * @param target {@link FogCommandChannel} initialized via {@link #begin(FogCommandChannel)}.
     * @param characterIdx Ordered byte array of indexes/IDs of the characters to display. 0 - 7 are custom characters
     *                     defined via {@link #setCustomChar(FogCommandChannel, int, byte[])}.
     * @param startIdx TODO:
     * @param length TODO:
     * @param col Column index to begin writing the characters from.
     * @param row Row index to begin writing the characters from.
     *
     * @return True if the command was successfully sent, and false otherwise. False will be immediately returned if the
     *         target is not {@link FogCommandChannel#i2cIsReady()}.
     */
    public static boolean writeMultipleChars(FogCommandChannel target, byte[] characterIdx, int startIdx, int length, int col, int row) {
        if (!target.i2cIsReady()) {
            return false;
        }
        int iterator = startIdx;
        int endOfLineIdx = 16 - col;
        int steps = 4;
        setCursor(target, col, row);

        while (iterator < length) {
            if (endOfLineIdx < Math.min(iterator + steps, length - 1)) {
                writeMultipleBytesToRegister(target, Grove_LCD_RGB_Constants.LCD_ADDRESS,
                                             Grove_LCD_RGB_Constants.LCD_SETCGRAMADDR,
                                             characterIdx, iterator, endOfLineIdx - iterator);
                iterator = endOfLineIdx;
                endOfLineIdx += 16;
                row = (row + 1) & 1;
                setCursor(target, col, row);
            } else {
                writeMultipleBytesToRegister(target, Grove_LCD_RGB_Constants.LCD_ADDRESS,
                                             Grove_LCD_RGB_Constants.LCD_SETCGRAMADDR,
                                             characterIdx, iterator, Math.min(steps, length - iterator));
                iterator += steps;
            }
        }
        target.i2cFlushBatch();
        return true;
    }

    /**
     * Writes a sequence of characters starting at a given cell and row on a given Grove RGB LCD device.
     *
     * TODO: What happens if the given column/row would cause the given array of characters to flow off-screen?
     *
     * @param target {@link FogCommandChannel} initialized via {@link #begin(FogCommandChannel)}.
     * @param text Text to write to the display.
     * @param col Column index to begin writing the characters from.
     * @param row Row index to begin writing the characters from.
     *
     * @return True if the command was successfully sent, and false otherwise. False will be immediately returned if the
     *         target is not {@link FogCommandChannel#i2cIsReady()}.
     */
    public static boolean writeCharSequence(FogCommandChannel target, CharSequence text, int col, int row) {
        return writeCharSequence(target, text, 0, text.length(), col, row);
    }

    /**
     * Writes a sequence of characters starting at a given cell and row on a given Grove RGB LCD device.
     *
     * TODO: What happens if the given column/row would cause the given array of characters to flow off-screen?
     *
     * @param target {@link FogCommandChannel} initialized via {@link #begin(FogCommandChannel)}.
     * @param text Text to write to the display.
     * @param startIdx TODO:
     * @param length TODO:
     * @param col Column index to begin writing the characters from.
     * @param row Row index to begin writing the characters from.
     *
     * @return True if the command was successfully sent, and false otherwise. False will be immediately returned if the
     *         target is not {@link FogCommandChannel#i2cIsReady()}.
     */
    public static boolean writeCharSequence(FogCommandChannel target, CharSequence text, int startIdx, int length, int col, int row) {
        if (!target.i2cIsReady()) {
            return false;
        }
        int iterator = startIdx;
        int endOfLineIdx = 16 - col;
        int steps = 4;
        setCursor(target, col, row);

        while (iterator < length) {
            if (endOfLineIdx < Math.min(iterator + steps, length - 1)) {
                writeCharSequenceToRegister(target, Grove_LCD_RGB_Constants.LCD_ADDRESS,
                                            Grove_LCD_RGB_Constants.LCD_SETCGRAMADDR,
                                            text, iterator, endOfLineIdx - iterator);
                iterator = endOfLineIdx;
                endOfLineIdx += 16;
                row = (row + 1) & 1;
                setCursor(target, col, row);
            } else {
                writeCharSequenceToRegister(target, Grove_LCD_RGB_Constants.LCD_ADDRESS,
                                            Grove_LCD_RGB_Constants.LCD_SETCGRAMADDR,
                                            text, iterator, Math.min(steps, length - iterator));
                iterator += steps;
            }
        }

        target.i2cFlushBatch();
        return true;
    }

    /**
     * TODO: What exactly does write padded int do that makes it necessary?
     *
     * @param target {@link FogCommandChannel} initialized via {@link #begin(FogCommandChannel)}.
     * @param value Integer value to write.
     * @param length TODO: Is is this the "padding" indicated by the method signature?
     * @param col Column index to begin writing the characters from.
     * @param row Row index to begin writing the characters from.
     *
     * @return True if the command was successfully sent, and false otherwise. False will be immediately returned if the
     *         target is not {@link FogCommandChannel#i2cIsReady()}.
     */
    public static boolean writePaddedInt(FogCommandChannel target, int value, int length, int col, int row) {
        if (!target.i2cIsReady()) {
            return false;
        }
        writeCharSequence(target, String.format("%0" + length + "d", value), col, row);
        return true;
    }

    //////////////////////////////
    ///    Private Methods    ////
    //////////////////////////////
    private static void setDisplayControl(FogCommandChannel target) {
        writeSingleByteToRegister(target, Grove_LCD_RGB_Constants.LCD_ADDRESS, Grove_LCD_RGB_Constants.LCD_SETDDRAMADDR,
                                  Grove_LCD_RGB_Constants.LCD_DISPLAYCONTROL | LCD_DISPLAY | LCD_CURSOR | LCD_BLINK);
        target.i2cDelay((Grove_LCD_RGB_Constants.LCD_ADDRESS), Grove_LCD_RGB_Constants.DISPLAY_SWITCH_DELAY);
    }

    private static void showRGBColor(FogCommandChannel target, int r, int g, int b) {
        writeSingleByteToRegister(target, ((Grove_LCD_RGB_Constants.RGB_ADDRESS)), 0, 0);
        writeSingleByteToRegister(target, ((Grove_LCD_RGB_Constants.RGB_ADDRESS)), 1, 0);
        writeSingleByteToRegister(target, ((Grove_LCD_RGB_Constants.RGB_ADDRESS)), 0x08, 0xaa);
        writeSingleByteToRegister(target, ((Grove_LCD_RGB_Constants.RGB_ADDRESS)), 4, r);
        writeSingleByteToRegister(target, ((Grove_LCD_RGB_Constants.RGB_ADDRESS)), 3, g);
        writeSingleByteToRegister(target, ((Grove_LCD_RGB_Constants.RGB_ADDRESS)), 2, b);
    }

    private static void displayClear(FogCommandChannel target) {
        //clear display
        writeSingleByteToRegister(target, ((Grove_LCD_RGB_Constants.LCD_ADDRESS)),
                                  Grove_LCD_RGB_Constants.LCD_SETDDRAMADDR, Grove_LCD_RGB_Constants.LCD_CLEARDISPLAY);
        target.i2cDelay((Grove_LCD_RGB_Constants.LCD_ADDRESS), Grove_LCD_RGB_Constants.SCREEN_CLEAR_DELAY);
    }

    private static void showTwoLineText(FogCommandChannel target, CharSequence text) {
        if (!isStarted) {
            begin(target);
        }
        displayClear(target);

        target.i2cDelay((Grove_LCD_RGB_Constants.LCD_ADDRESS), Grove_LCD_RGB_Constants.INPUT_SET_DELAY);

        int p = 0;
        int start = 0;
        while (p < text.length()) {
            if (text.charAt(p++) == '\n') {
                writeSingleLine(target, text, start, p - 1);// -1 to skip the \n
                start = p;
            }
        }
        assert (p == text.length());
        writeSingleLine(target, text, start, p);
    }

    private static void writeSingleLine(FogCommandChannel target, CharSequence line, int p, int limit) {
        int steps = 4;

        while (p < limit) {
            writeUTF8ToRegister(target, ((Grove_LCD_RGB_Constants.LCD_ADDRESS)),
                                Grove_LCD_RGB_Constants.LCD_SETCGRAMADDR, line, p, Math.min(steps, limit - p));
            p += steps;

        }

        //new line
        writeSingleByteToRegister(target, ((Grove_LCD_RGB_Constants.LCD_ADDRESS)),
                                  Grove_LCD_RGB_Constants.LCD_SETDDRAMADDR, 0xc0);
    }

    private static void writeSingleByteToRegister(FogCommandChannel target, int address, int register, int value) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(address);

        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeByte(value);

        target.i2cCommandClose();
    }

    private static void writeMultipleBytesToRegister(FogCommandChannel target, int address, int register, byte[] values, int startIdx, int length) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(address);

        i2cPayloadWriter.writeByte(register);
        for (int i = startIdx; i < startIdx + length; i++) {
            i2cPayloadWriter.writeByte(values[i]);
        }

        target.i2cCommandClose();
    }

    private static void writeCharSequenceToRegister(FogCommandChannel target, int address, int register, CharSequence values, int startIdx, int length) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(address);

        i2cPayloadWriter.writeByte(register);
        i2cPayloadWriter.writeASCII(values.subSequence(startIdx, startIdx + length));

        target.i2cCommandClose();
    }

    private static void writeUTF8ToRegister(FogCommandChannel target, int address, int register, CharSequence text, int pos, int len) {
        DataOutputBlobWriter<I2CCommandSchema> i2cPayloadWriter = target.i2cCommandOpen(address);

        i2cPayloadWriter.writeByte(register);
        DataOutputBlobWriter.encodeAsUTF8(i2cPayloadWriter, text, pos, len);

        target.i2cCommandClose();
    }

    @Override
    public boolean isInput() {
        return false;
    }

    @Override
    public boolean isOutput() {
        return true;
    }

    @Override
    public boolean isPWM() {
        return false;
    }

    @Override
    public int range() {
        return 0;
    }

    @Override
    public I2CConnection getI2CConnection() { //putting getI2CConnection in i2cOutput twigs allows setup commands to be sent
        byte[] LCD_READCMD = {};
        byte[] LCD_SETUP = {};
        byte LCD_ADDR = 0x04;
        byte LCD_BYTESTOREAD = 0;
        byte LCD_REGISTER = 0;
        return new I2CConnection(this, LCD_ADDR, LCD_READCMD, LCD_BYTESTOREAD, LCD_REGISTER, LCD_SETUP);
    }

    @Override
    public int response() {
        return 20;
    }

    @Override
    public boolean isValid(byte[] backing, int position, int length, int mask) {
        return true;
    }

    @Override
    public int pinsUsed() {
        return 1;
    }

	@Override
	public <F extends IODeviceFacade> F newFacade(FogCommandChannel... ch) {
		// TODO Auto-generated method stub
		return null;
	}
}
