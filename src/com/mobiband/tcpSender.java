/*
 * @author Haokun Luo, 09/27/2012
 * 
 * Open TCP package for transfer
 * 
 */

package com.mobiband;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
// for accuracy of nanoseconds
import java.util.concurrent.locks.LockSupport;

import android.util.Log;
import android.widget.TextView;

public class tcpSender extends Thread {
	// Store the current experiment result
	private String probingResult = "";
	private TextView bandwidthDisResult = null;
	
	// define all the variables
	private Socket pkgTrainSocket = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private long myGapSize = 0;
	private int myPktSize = 0;
	private int myTrainLength = 0;
	private String myHostname = "";
	private int myPortNumber = 0;
	private double estUplinkBWResult = 0.0;
	private double estDownlinkBWReult = 0.0;
	
	// thread execution part
	public void run() {
		if (this.openSocket()) {
			this.runSocket();
			// must close the socket
			this.closeSocket();
		}
		
		Log.i(constant.logTagMSG, "Start to write to file");
		this.writeResultToFile();
		Log.i(constant.logTagMSG, "Successful write to file");
	}
	
	// class constructor
	tcpSender(double gap, double pkt, int train, String hostname, int portNumber, TextView textField) {
		if (gap != 0)
			// convert from ms to ns
			myGapSize = (long) (gap*java.lang.Math.pow(10.0, 6.0));
		else
			myGapSize = constant.pktGapNS;
		if (pkt != 0)
			// convert kB to Byte
			myPktSize = (int)(pkt*java.lang.Math.pow(2.0, 10.0));
		else
			myPktSize = constant.pktSize;
		if (train != 0)
			myTrainLength = train;
		else
			myTrainLength = constant.pktTrainLength;
		if (!hostname.equals("")) 
			myHostname = hostname;
		else 
			myHostname = constant.hostName;
		if (portNumber != 0) 
			myPortNumber = portNumber;
		else
			myPortNumber = constant.tcpPortNumber;
		
		// bind the scroll view
		bandwidthDisResult = textField;
	}
	
	// fetch the experiment result
	public String fetchExperiementResult() {
		return probingResult;
	}
	
	// test if the result is ready
	public boolean testExpResultReady() {
		return (probingResult != "");
	}
	
	// set up all the package parameters
    public boolean openSocket() {
    	try {
    		pkgTrainSocket = new Socket(myHostname, myPortNumber);
            out = new PrintWriter(pkgTrainSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(
            		pkgTrainSocket.getInputStream()));
            
            // set the time out
            pkgTrainSocket.setSoTimeout(constant.tcpTimeOut);

            Log.d(constant.logTagMSG, "Current receive buffer size is " + pkgTrainSocket.getReceiveBufferSize());
            
            // set TCP no delay for packet transfer
            pkgTrainSocket.setTcpNoDelay(true);
            Log.d(constant.logTagMSG, "Current nodelay is " + pkgTrainSocket.getTcpNoDelay());
            
            /*
            // set TCP sending buffer size
            pkgTrainSocket.setSendBufferSize(myPktSize/10);
            Log.d(constant.logTagMSG, "Current send buffer size is " + pkgTrainSocket.getSendBufferSize());
            
            // set TCP receiving buffer size
            pkgTrainSocket.setReceiveBufferSize(myPktSize/10);
            Log.d(constant.logTagMSG, "Current send buffer size is " + pkgTrainSocket.getReceiveBufferSize());*/
            
        } catch (UnknownHostException e) {
            // System.err.println("Don't know about host: " + myHostname);
            // System.exit(1);
        	probingResult += constant.errMSG + ": Don't know about host: " + myHostname + " with port " + myPortNumber;
        	// Log.d(constant.logTagMSG, e.getStackTrace().toString());
        	e.printStackTrace();
        	return false;
        	
        } catch (IOException e) {
            // System.err.println("Couldn't get I/O for " + "the connection to: " + myHostname );
            // System.exit(1);
        	probingResult += constant.errMSG + ": Couldn't get I/O for " + "the connection to: " + myHostname + " with port " + myPortNumber;
        	//Log.d(constant.logTagMSG, e.getStackTrace().toString());
        	e.printStackTrace();
        	return false;
        }
    	
    	Log.d(constant.logTagMSG, "Open Socket for package train.");
    	return true;
    }
        
    // close all the socket and IO streams
    public boolean closeSocket() {
    	try {
	    	in.close();
	    	out.close();
	    	pkgTrainSocket.close();
    	} catch (IOException e) {
    		//Log.d(constant.logTagMSG, e.getStackTrace().toString());
    		e.printStackTrace();
    		probingResult += constant.errMSG + ": Fail to close the socket.";
    		return false;
    	}
    	
    	Log.i(constant.logTagMSG, "Close Socket for package train.");
    	return true;
    }
    
    // send TCP packet train
    public boolean runSocket() {
    	try {
    		 Log.d(constant.logTagMSG, "*****************************************************************");
		     Log.d(constant.logTagMSG, "************************ Uplink BW Test *************************");
		     Log.d(constant.logTagMSG, "*****************************************************************");
		     
	    	// upload link bandwidth test
	    	runUpLinkTask();
	    	
	    	 Log.d(constant.logTagMSG, "*****************************************************************");
		     Log.d(constant.logTagMSG, "********************** Downlink BW Test *************************");
		     Log.d(constant.logTagMSG, "*****************************************************************");
	    	
	    	// download link bandwidth test
	    	runDownLinkTask();
    	} catch (NumberFormatException n) {
    		//Log.d(constant.logTagMSG, n.getStackTrace().toString());
    		n.printStackTrace();
    		probingResult += constant.errMSG + ": Cannot convert string into proper format.";
    		return false;
    	} catch (IOException e) {
    		// Log.d(constant.logTagMSG, e.getStackTrace().toString());
    		e.printStackTrace();
    		probingResult += constant.errMSG + ": IO Error.";
    		return false;
    	}
    	
    	// TODO: format the double precision
    	probingResult += "Uplink Available Bandwidth is " + estUplinkBWResult + " Mbps\n" +
    			         "Downlink Available Bandwidth is " + estDownlinkBWReult + " Mbps";
    	Log.i(constant.logTagMSG, probingResult);
    	return true;
    }

    // upload link test
    private void runUpLinkTask() throws IOException {
	    // create payload for the packet train
    	StringBuilder payload = new StringBuilder();
    		
    	// Create a zero string
    	for (int i = 0; i < myPktSize; i++) {
    		payload.append('0');
    	}
    		
    	// assign special characters
    	payload.setCharAt(0, 's');
    	payload.setCharAt(payload.length()-1, 'e');
			
	    // create a counter for packet train
    	int counter = 0;
    	long beforeTime = 0;
    	long afterTime = 0;
    	double diffTime = 0;
    	
		while (counter < myTrainLength) {
			// start recording the first packet send time
			if (beforeTime == 0) {
				beforeTime = System.nanoTime();
				//beforeTime = System.currentTimeMillis();
			}
			
			// send packet with constant gap
			out.println(payload);
			out.flush();
			
			// create train gap in nanoseconds
			/*try {
				Thread.sleep(constant.pktGapMS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
			LockSupport.parkNanos(myGapSize);		
			counter++;
		}
		
		// record finish transmission time
		afterTime = System.nanoTime();
		//afterTime = System.currentTimeMillis();
				
		Log.d(constant.logTagMSG, "Single Packet size is " + payload.length() + " Bytes.");
		Log.d(constant.logTagMSG, "Single GAP is " + myGapSize/java.lang.Math.pow(10.0, 6.0) + " ms.");
		Log.d(constant.logTagMSG, "Total number of packet is " + counter);
		
		String lastMSG;
		// Total GAP calculation
		diffTime = (afterTime - beforeTime)/java.lang.Math.pow(10.0, 6.0);
		// diffTime = myTrainLength*myGapSize/java.lang.Math.pow(10.0, 6.0);
		// diffTime = myTrainLength*constant.pktGapMS;
		lastMSG = constant.finalMSG + ':' + diffTime;
		// send the last message
		out.println(lastMSG);
		out.flush();
		
		double test = Double.parseDouble(lastMSG.substring(constant.finalMSG.length()+1));
		
		Log.d(constant.logTagMSG, "Client side takes " + test + " ms.");
		
		// waiting for the upload link result
		String uplinkBWResult;
		while ((uplinkBWResult = in.readLine()) != null) {
			if (uplinkBWResult.substring(0, constant.resultMSG.length()).equals(constant.resultMSG)) {
				estUplinkBWResult = Double.parseDouble(uplinkBWResult.substring(constant.resultMSG.length()+1));
				// extra colon added
				Log.d(constant.logTagMSG, "Uplink Bandwidth result is " + estUplinkBWResult + " Mbps");
				// send back the ACK result
				out.println(constant.ackMSG);
				out.flush();
				break;
			}
		}
    }
    
    // download link test
    private void runDownLinkTask() throws NumberFormatException, IOException {
    	String inputLine = "";
        int counter = 0;
        int singlePktSize = 0;
        
        // timer to record packet arrived
        // long startTime = System.endTimeMillis();
        long startTime = 0;
        long endTime = 0;
        // the gap time
        double gapTimeSrv = 0.0;
        double gapTimeClt = 0.0;
        double byteCounter = 0.0;
        double estTotalDownBandWidth = 0.0;
        double availableBWFraction = 1.0;
        
        // output from what received
        while ((inputLine = in.readLine()) != null) {
        	// check if the start time recorded for first received packet
        	if (startTime == 0) {
        		//startTime = System.currentTimeMillis();
        		startTime = System.nanoTime();
        		singlePktSize = inputLine.length();
        	}

        	// out.flush();
        	byteCounter += inputLine.length();
        	
        	// check for last message
        	if (inputLine.substring(0, constant.finalMSG.length()).equals(constant.finalMSG)) {
        		Log.d(constant.logTagMSG, "Detect last download link message");
        		gapTimeSrv = Double.parseDouble(inputLine.substring(constant.finalMSG.length()+1));
        		break;
        	}
        	
        	// increase the counter
        	counter++;
        }
        
        
        //endTime = System.currentTimeMillis();
        endTime = System.nanoTime();
        gapTimeClt = (endTime - startTime)/java.lang.Math.pow(10.0, 6.0);
        
        // Bandwidth calculation
        // 1 Mbit/s = 125 Byte/ms 
        estTotalDownBandWidth = byteCounter/gapTimeClt/125.0;
        availableBWFraction = gapTimeSrv/gapTimeClt;
        estDownlinkBWReult = estTotalDownBandWidth * availableBWFraction;
        
        
        // Display information at the server side
        Log.d(constant.logTagMSG, "Receive single Pkt size is " + singlePktSize + " Bytes.");
        Log.d(constant.logTagMSG, "Total receiving " + counter + " packets.");
        Log.d(constant.logTagMSG, "Server gap time is " + gapTimeSrv + " ms.");
        Log.d(constant.logTagMSG, "Total package received " + byteCounter + " Bytes with " + gapTimeClt + " ms total GAP.");
        Log.d(constant.logTagMSG, "Estimated Total download bandwidth is " + estTotalDownBandWidth + " Mbps.");
        Log.d(constant.logTagMSG, "Availabe fraction is " + availableBWFraction);
        Log.d(constant.logTagMSG, "Estimated Available download bandwidth is " + estDownlinkBWReult + " Mbps.");
    }

    // write result to the scroll screen
    private void writeResultToFile() {
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
			/*FileWriter fileWritter;
			fileWritter = new FileWriter(dstFilePath,true);
			BufferedWriter bufferWritter = new BufferedWriter(fileWritter);*/

	        String tempResult = "***************************\nTask @ " + getCurrentTimeWithFormat() + '\n' + probingResult + '\n';
	        FileOutputStream out = new FileOutputStream(f, true);
	        out.write(tempResult.getBytes(), 0, tempResult.length());
	        out.close();
	        
	        //bufferWritter.write(tempResult);
	        //bufferWritter.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(constant.logTagMSG, "ERROR: cannot write to file.\n" + e.toString());
		}
    }
    
    // access current time
    private String getCurrentTimeWithFormat() {
    	SimpleDateFormat sdfDate = new SimpleDateFormat("MMMMM.dd.yyyy hh:mm aaa");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate.trim();
    }
    
    // access current data to create a folder
    private String getCurrenTimeForFile() {
    	SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy_MM_dd-HH");
    	Date today = new Date();
        String strDate = sdfDate.format(today);
        return strDate.trim();
    }
}