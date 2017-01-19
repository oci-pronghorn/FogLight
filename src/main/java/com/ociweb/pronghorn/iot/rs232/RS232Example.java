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
            RS232Client client1 = new RS232Client("/dev/ttys004", RS232NativeLinuxBacking.B19200);
            RS232Client client2 = new RS232Client("/dev/ttys005", RS232NativeLinuxBacking.B19200);
            client1.write("bazinga");
            try {
                String read = client2.read(7);
                System.out.println("Read: " + read.length());
                System.out.println(read);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Exception encountered when doing serial things: " + e.getMessage());
            System.err.println("Did you remember to run the SH build script?");
        }
    }
}
