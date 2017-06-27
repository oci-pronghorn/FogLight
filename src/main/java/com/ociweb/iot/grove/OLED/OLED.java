package com.ociweb.iot.grove.OLED;

public interface OLED {
	public boolean init();
	public boolean clear();
	public boolean cleanClear();
	public boolean displayOn();
	public boolean displayOff();
	public boolean setContrast(int contrast);
	public boolean setTextRowCol(int row, int col);
	public boolean printCharSequence(CharSequence s);
	public boolean printStringAt(int row, int col);
	public boolean drawBitmap(int[] bitmap);
	public boolean activateScroll();
	public boolean deactivateScroll();
	public boolean setUpScroll();
	
}
