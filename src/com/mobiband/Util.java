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
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class Util {
    // access current time
    public static String getCurrentTimeWithFormat(String format) {
    	SimpleDateFormat sdfDate = new SimpleDateFormat(format);
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
    // create lock to prevent multiple file writes at the same time
    public static void writeResultToFile(String filename, String foldername, String content) {
    	String dstFilePath = foldername + "/" + filename;
    	File d = new File(foldername);
    	File f = new File(dstFilePath);
    	
    	// check if directory exist
    	if (!d.exists()) {
    		if (!d.mkdirs()) {
    			Log.e(constant.logTagMSG, "ERROR: fail to create directory " + foldername);
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
			// prevent multiple threads write to the same file
			@SuppressWarnings("resource")
			FileChannel channel = new RandomAccessFile(f, "rw").getChannel(); // Use the file channel to create a lock on the file.
			FileLock lock = null;
			
			do {
				// try to acquire a lock
				lock = channel.tryLock();
			} while (lock == null);
			
	        FileOutputStream out = new FileOutputStream(f, true);
	        out.write(content.getBytes(), 0, content.length());
	        out.close();
	        
	        // release the lock
	        lock.release();
	        channel.close();
	        //bufferWritter.write(tempResult);
	        //bufferWritter.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(constant.logTagMSG, "ERROR: cannot write to file.\n" + e.toString());
		}
    }
}
