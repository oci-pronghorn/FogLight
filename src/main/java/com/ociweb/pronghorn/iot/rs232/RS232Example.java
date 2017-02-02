package com.ociweb.pronghorn.iot.rs232;

/**
 * TODO:
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class RS232Example {
    // TODO : Simple test only.
    public static void main(String[] args) {
        try {
            RS232Client client1 = new RS232Client("/dev/ttys004", RS232Client.B9600);
            RS232Client client2 = new RS232Client("/dev/ttys005", RS232Client.B9600);
            for (int i = 0; i < 100; i++) {
                String str = "bazinga #" + i;
                client1.write((str).getBytes("UTF-8"));

                Thread.sleep(250);

                try {
                    System.out.println("Available Bytes: " + client2.getAvailableBytes());
                    byte[] bytes = client2.read(50);
                    String read = new String(bytes, "UTF-8");
                    System.out.println("Bytes Read: " + read.length());
                    System.out.println("Value Read: " + read);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Thread.sleep(250);
            }
        } catch (Exception e) {
            System.err.println("Exception encountered when doing serial things: " + e.getMessage());
            System.err.println("Did you remember to run the SH build script?");
        }
    }
}
