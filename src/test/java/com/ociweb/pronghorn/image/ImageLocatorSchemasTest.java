
package com.ociweb.pronghorn.image;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ociweb.pronghorn.pipe.util.build.FROMValidation;

public class ImageLocatorSchemasTest {

  @Test
  public void testLocatonModeSchema() {
	  assertTrue(FROMValidation.checkSchema("/LocationModeSchema.xml", LocationModeSchema.class));
  }
  
  @Test
  public void testCalibrationStatusSchema() {
	  assertTrue(FROMValidation.checkSchema("/CalibrationStatusSchema.xml", CalibrationStatusSchema.class));
  }
  

}







