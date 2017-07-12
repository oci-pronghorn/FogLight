package com.ociweb.grove;

import java.awt.FileDialog;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class ImageGenerator {
	public static int[][] convertToGrayScale(int pixelDepth, int startX, int startY, int rowCount, int colCount) throws IOException{
		JFrame fdFrame = new JFrame();
		FileDialog fd = new FileDialog(fdFrame, "Pick an image file", FileDialog.LOAD);
		fd.setFile("*.jpg");
		fd.setVisible(true);
		File[] f = fd.getFiles();
		return convertToGrayScale(f[0], 0,0, pixelDepth, rowCount, colCount);
	}

	public static int[][] convertToGrayScale(File f, int startX, int startY, int pixelDepth, int rowCount, int colCount) throws IOException{
		BufferedImage img = ImageIO.read(f);
		final byte[] pix = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
		int[][] ret = new int[rowCount][colCount];

		int index = 0;
		int offset = 0;
		if (img.getAlphaRaster() != null){
			offset = 1;
		}
		for (int i = startY ; i < rowCount + startY; i ++){
			for (int j = startX; j < colCount + startX; j ++){
				int combo = img.getRGB(j, i) ;
				int R = combo >> 16 & 0xFF;
			int G = combo >> 8 & 0xFF;
			int B = combo &0xFF;
			int b = (R + G + B) / 3;
			ret[i][j] = b >> (8 - pixelDepth);
			}
		}
		/*
		for (int i = 0; i < ret.length; i ++){
			System.out.print("{");
			for (int j = 0; j < ret[0].length;j++){
				System.out.print(ret[i][j]);
				if (j != ret[0].length -1){
					System.out.print(",");
				}
			}
			System.out.println("},");
		}
		 */
		return ret;
		 
	}

}
