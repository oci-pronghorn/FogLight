package com.ociweb.iot.grove.display;

public interface LCDable extends Seven_Segmentable {
	public void switchPixel(boolean on, int row, int col);
	public boolean switchCursor(boolean on);
	public boolean switchBlinking(boolean on);
	public boolean switchDisplay(boolean on);
	public boolean displayText(String txt);
	public boolean setDisplayColor(int r, int g, int b);
	public boolean displayTextAndSetColor(String txt, int r, int g, int b);
	public boolean writeChar(char c, int col, int row);
	public boolean customChar(int location, char charMap);
	
}
