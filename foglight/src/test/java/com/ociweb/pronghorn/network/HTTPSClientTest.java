package com.ociweb.pronghorn.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.pronghorn.stage.test.PipeCleanerStage;

public class HTTPSClientTest {

	Logger log = LoggerFactory.getLogger(HTTPSClientTest.class);
	
	
	@Ignore
	public void baselinetTest() {
		
		byte[] buffer = new byte[1024];
		try {
			long runStart = System.currentTimeMillis();
			
			StringBuilder builder = new StringBuilder();
			
			int testSize = 100;
			int iterations = testSize;
			
			while (--iterations>=0) {
				//long start = System.currentTimeMillis();
				URL u = new URL("https://encrypted.google.com");
				
				InputStream in = u.openStream();
				
				int temp = 0;
				do {
				  temp = in.read(buffer);
				
				  if (temp>0) {
					  builder.append(new String(buffer,0,temp));
				  }
				  
				} while (temp!=-1);
				in.close();
				//long duration = System.currentTimeMillis()-start;
				//System.out.println("blocking duration: "+duration);
			}
			
			long avgDuration = (System.currentTimeMillis()-runStart)/testSize;
			
			System.out.println("total bytes "+builder.length()+"   per msg   "+avgDuration+" ms"); //57  54 50
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
		

	private int doneCount(PipeCleanerStage[] cleaners) {
		int count;
		{
		int k = cleaners.length;
		int c=0;
		while (--k>=0) {
			c+=cleaners[k].getTotalSlabCount();
			
		}
		
		count = c;
		}
		return count;
	}


	
	
}
