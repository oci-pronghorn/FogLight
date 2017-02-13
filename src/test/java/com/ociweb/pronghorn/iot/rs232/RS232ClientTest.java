package com.ociweb.pronghorn.iot.rs232;

import org.junit.*;

import java.io.*;

/**
 * These tests will be skipped if Socat is missing or if Make can not successfully execute.
 */
public class RS232ClientTest {

    private static final String SHELL = "";
    private static final String MKDIR_COMMAND = "mkdir -p ${HOME}/dev".replace("${HOME}", System.getProperty("user.home"));
    private static final String SOCAT_COMMAND = "/usr/local/bin/socat -d -d pty,raw,echo=0,link=${HOME}/dev/ttyVS1 pty,raw,echo=0,link=${HOME}/dev/ttyVS2".replace("${HOME}", System.getProperty("user.home"));

    private static final String SERIAL_1 = System.getProperty("user.home") + "/dev/ttyVS1";
    private static final String SERIAL_2 = System.getProperty("user.home") + "/dev/ttyVS2";

    private static Process socat;

    private static RS232Client client1;
    private static RS232Client client2;

    @BeforeClass
    public static void setup() {
        // Generate artifacts.
        try {
            // Discover Java Home.
            String jHome = System.getProperty("java.home");
            if (jHome.endsWith("/jre")) {
                jHome = jHome.substring(0, jHome.length() - 4);
            }

            // Generate make artifacts.
            Process p = Runtime.getRuntime().exec("/usr/bin/make", new String[]{"JAVA_HOME=" + jHome});
            p.waitFor();
        } catch (Exception e) {
            Assume.assumeNoException("Failed to start Socat. Do you have it installed on this system? Error: " + e.getMessage(), e);
        }

        // Start Socat.
        try {
            Runtime.getRuntime().exec(MKDIR_COMMAND).waitFor();
            socat = Runtime.getRuntime().exec(SOCAT_COMMAND);
        } catch (Exception e) {
            // Clean any make artifacts we generated.
            try {
                Runtime.getRuntime().exec("make clean").waitFor();
            } catch (Exception e2) { /* Quietly fail. */ }

            Assume.assumeNoException("Failed to start Socat. Do you have it installed on this system? Error: " + e.getMessage(), e);
        }

        // TODO: Test at different bauds?
        client1 = new RS232Client(SERIAL_1, RS232Client.B9600);
        client2 = new RS232Client(SERIAL_2, RS232Client.B9600);
    }

    @AfterClass
    public static void teardown() {
        client1.close();
        client2.close();

        // Clean make artifacts.
        try {
            Runtime.getRuntime().exec("make clean").waitFor();
        } catch (IOException | InterruptedException e) {
            Assert.fail("Failed to clean development binaries.");
        }

        // Stop socat.
        socat.destroy();
    }

    @Test
    public void shouldWriteAndReadBytes() throws Exception {
        for (int i = 0; i < 10; i++) {
            String str = "bazinga #" + i;
            client1.write((str).getBytes("UTF-8"));

            // TODO: 10 MS seems to be a good space for general testing...the ports aren't that fast.
            Thread.sleep(10);

            try {
                String read = new String(client2.read(50), "UTF-8");
                Assert.assertTrue(read.contains(str));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void shouldReadBytesIntoArrays() throws Exception {
        for (int i = 0; i < 10; i++) {
            String str = "bazinga #" + i;
            client1.write((str).getBytes("UTF-8"));

            // TODO: 10 MS seems to be a good space for general testing...the ports aren't that fast.
            Thread.sleep(10);

            try {
                byte[] bytes = new byte[50];
                int readSize = client2.readInto(bytes, 0, 50);
                String read = new String(bytes, 0, readSize,"UTF-8");
                Assert.assertTrue(read.contains(str));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}