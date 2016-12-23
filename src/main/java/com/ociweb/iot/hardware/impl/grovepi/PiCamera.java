/*
 * Project: PronghornIoT
 * Since: Nov 02, 2016
 *
 * Copyright (c) Brandon Sanders [brandon@alicorn.io]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ociweb.iot.hardware.impl.grovepi;

import java.io.File;
import java.io.IOException;

/**
 * Test class for the Pi Camera.
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public class PiCamera {

    /**
     * Takes a picture from the currently connected RPi camera and
     * saves it to a file.
     *
     * @param fileName Name of the file (without extensions) to save
     *                 the image to.
     *
     * @return A reference to the created image file.
     */
    public static File takePicture(String fileName) {
        try {
            Runtime.getRuntime().exec("raspistill -o " + fileName + ".jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new File(fileName + ".jpg");
    }

    public static void main(String[] args) {
        System.out.println(takePicture("test"));
    }
}
