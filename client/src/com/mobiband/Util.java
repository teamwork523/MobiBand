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
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class Util {
  // displayed UI messages and frequency
  private static String measurementResults = "";
  // private static long UISleepPeriod = 1000;
  
  public static synchronized void updateResults (String updateContent) {
    measurementResults += updateContent + "\n";
  }
  public static synchronized String accessResults () {
    return measurementResults;
  }
  
  /*
  public static synchronized void updateSleepPeriod (long newPeriod) {
    UISleepPeriod = newPeriod;
  }
  public static synchronized long accessSleepPeriod () {
    return UISleepPeriod;
  }*/
  
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
  
  /**
   * Return a list of system environment path 
   */
  public static String[] fetchEnvPaths() {
    String path = "";
    Map<String, String> env = System.getenv();
    if (env.containsKey("PATH")) {
      path = env.get("PATH");
    }
    return (path.contains(":")) ? path.split(":") : (new String[]{path});
  }
  
  /**
   * Determine the ping executable based on ip address byte length
   */
  public static String getPingExecutablePath() {
    Process testPingProc = null;
    String[] progList = fetchEnvPaths();
    String pingExecutable = null, ping_executable = "ping";
    if (progList != null && progList.length != 0) {
      for (String pingLocation : progList) {
        try {
          pingExecutable = pingLocation + "/" + ping_executable;
          testPingProc = Runtime.getRuntime().exec(pingExecutable);
        } catch (IOException e) {
          // reset the executable
          pingExecutable = null;
          // The ping command doesn't exist in that path, try another one
          continue;
        } finally {
          if (testPingProc != null)
            testPingProc.destroy();
        }
        break;
      }
    }
    return pingExecutable;
  }
  
  public static String constructCommand(Object... strings) throws InvalidParameterException {
    String finalCommand = "";
    int len = strings.length;
    if (len < 0) {
      throw new InvalidParameterException("0 arguments passed in for constructing command");
    }
    
    for (int i = 0; i < len - 1; i++) {
      finalCommand += (strings[i] + " ");
    }
    finalCommand += strings[len - 1];
    return finalCommand;
  }
  
  /**
   * Returns a String array that contains the ICMP sequence number and the round
   * trip time extracted from a ping output. The first array element is the
   * sequence number and the second element is the round trip time.
   * 
   * Returns a null object if either element cannot be found.
   */
  public static String[] extractInfoFromPingOutput(String outputLine) {
    try {
      Pattern pattern = Pattern.compile("icmp_seq=([0-9]+)\\s.* time=([0-9]+(\\.[0-9]+)?)");
      Matcher matcher = pattern.matcher(outputLine);
      matcher.find();
      
      return new String[] {matcher.group(1), matcher.group(2)};
    } catch (IllegalStateException e) {
      return null;
    }
  }
}
