package com.ociweb.iot.grove.mp3_v2;

import com.ociweb.iot.maker.SerialWritable;
import com.ociweb.pronghorn.pipe.BlobWriter;

public class SerialWritableWithArgs implements SerialWritable {
		private String[] args;
		
		public void setArgs(String[] args){
			this.args = args;
		}
		public String[] getArgs(){
			return args;
		}
		
		@Override
		public void write(BlobWriter writer) {
			// TODO Auto-generated method stub		
		}
}
