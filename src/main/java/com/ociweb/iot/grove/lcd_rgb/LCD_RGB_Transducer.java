package com.ociweb.iot.grove.lcd_rgb;

import com.ociweb.gl.api.transducer.StartupListenerTransducer;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceTransducer;
import com.ociweb.iot.maker.image.*;

public class LCD_RGB_Transducer implements IODeviceTransducer, FogBmpDisplayable, StartupListenerTransducer {
	private final FogCommandChannel ch;

	public LCD_RGB_Transducer(FogCommandChannel ch) {
		this.ch = ch;
		ch.ensureI2CWriting();
	}

	public FogBitmapLayout newBmpLayout() {
		FogBitmapLayout bmpLayout = new FogBitmapLayout(FogColorSpace.rgb);
		bmpLayout.setComponentDepth((byte) 6);
		bmpLayout.setWidth(8);
		bmpLayout.setHeight(8);
		return bmpLayout;
	}

	public FogBitmap newEmptyBmp() {
		return new FogBitmap(newBmpLayout());
	}

	public boolean display(FogBitmap bmp) {
		return false;
	}

	public boolean begin(){
		return Grove_LCD_RGB.begin(ch);
	}
	public boolean commandForTextAndColor(String text, int r, int g, int b) {
		return Grove_LCD_RGB.commandForTextAndColor(ch, text,r,g,b);
	}
	public  boolean commandForColor(int r, int g, int b){
		return Grove_LCD_RGB.commandForColor(ch, r, g, b);
	}
	public boolean commandForText(CharSequence text) {
		return Grove_LCD_RGB.commandForText(ch,text);
	}
	public boolean commandForDisplay(boolean on){
		return Grove_LCD_RGB.commandForDisplay(ch, on);
	}
	public boolean commandForCursor(boolean on){
		return Grove_LCD_RGB.commandForCursor(ch,on);
	}
	public boolean commandForBlink(boolean on){
		return Grove_LCD_RGB.commandForBlink(ch, on);
	}
	public boolean setCursor(int col, int row) {
		return Grove_LCD_RGB.setCursor(ch, col,row);
	}
	public boolean setCustomChar(int location, byte[] charMap){
		return Grove_LCD_RGB.setCustomChar(ch, location, charMap);
	}
	public boolean writeChar(int characterIdx, int col, int row){
		return Grove_LCD_RGB.writeChar(ch, characterIdx, col, row);
	}
	public boolean writeMultipleChars(byte[] characterIdx, int col, int row){
		return Grove_LCD_RGB.writeMultipleChars(ch, characterIdx, col, row);
	}
	public boolean writeMultipleChars(byte[] characterIdx, int startIdx, int length, int col, int row) {
		return Grove_LCD_RGB.writeMultipleChars(ch, characterIdx, startIdx, length, col, row);
	}
	public boolean writeCharSequence(CharSequence text, int col, int row) {
		return Grove_LCD_RGB.writeCharSequence(ch, text, col, row);
	}
	public boolean writeCharSequence( CharSequence text, int startIdx, int length, int col, int row){
		return Grove_LCD_RGB.writeCharSequence(ch, text, startIdx, length, col, row);
	}

	@Override
	public void startup() {
		begin();
	}
}
