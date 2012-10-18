/*
 * @author Haokun Luo
 * @Date   10/08/2012
 * 
 *  This is a utility functions for MobiBand
 *  
 */

package com.mobiband;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class Util {
    // access current time
    public static String getCurrentTimeWithFormat() {
    	SimpleDateFormat sdfDate = new SimpleDateFormat("MMMMM.dd.yyyy hh:mm:ss aaa");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate.trim();
    }
    
    // access current data to create a folder
    public static String getCurrenTimeForFile() {
    	SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy_MM_dd-HH");
    	Date today = new Date();
        String strDate = sdfDate.format(today);
        return strDate.trim();
    }
    
    // write result to the scroll screen
    public static void writeResultToFile(String filename, String content) {
    	String dstFilePath = constant.outPath + '/' + getCurrenTimeForFile() + ".txt";
    	File d = new File(constant.outPath);
    	File f = new File(dstFilePath);
    	
    	// check if directory exist
    	if (!d.exists()) {
    		if (!d.mkdirs()) {
    			Log.e(constant.logTagMSG, "ERROR: fail to create directory " + constant.outPath);
    		}
    	}
    	
    	// check file existence
    	if (!f.exists()) {
    		try {
    			f.createNewFile();
    			// set file to be readable
    		} catch (IOException e) {
    			e.printStackTrace();
    			Log.e(constant.logTagMSG, "ERROR: fail to create file " + dstFilePath);
    		}
    	}
    	
    	// append to file 
		try {
	        FileOutputStream out = new FileOutputStream(f, true);
	        out.write(content.getBytes(), 0, content.length());
	        out.close();
	        
	        //bufferWritter.write(tempResult);
	        //bufferWritter.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(constant.logTagMSG, "ERROR: cannot write to file.\n" + e.toString());
		}
    }
}
