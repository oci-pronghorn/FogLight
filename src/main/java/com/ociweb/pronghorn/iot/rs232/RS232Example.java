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
            int fd = RS232Native.open("/dev/ttys004", RS232Native.B19200);
            int fd2 = RS232Native.open("/dev/ttys005", RS232Native.B19200);
            RS232Native.write(fd, "Bazinga");
            try {
                String read = RS232Native.read(fd2, 7);
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
