/*
 * @author Haokun Luo, 09/27/2012
 * 
 * Open TCP package for transfer
 * 
 */

package com.mobiband;

//import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
// for accuracy of nanoseconds
// import java.util.concurrent.locks.LockSupport;

import android.util.Log;
//import android.telephony.SignalStrength;

public class tcpSender extends Thread {
	// Store the current experiment result
	private String probingResult = "";
	
	// define all the variables
	private Socket pkgTrainSocket = null;
	private DataOutputStream out = null;
	//private DataInputStream in = null;
	private PrintWriter outCtrl = null;
	private BufferedReader inCtrl = null;
	private long myGapSize = 0;
	private int myPktSize = 0;
	private int myTrainLength = 0;
	private String myHostname = "";
	private int myPortNumber = 0;
	private double estUplinkBWResult = 0.0;
	private double estDownlinkBWReult = 0.0;
	private String myDir = "Up";
	// thread stop
	private boolean stop = false;
	// private SignalStrength rssi = null;
	
	private MobiBand UIActivity = null;
	
	// thread execution part
	public void run() {
		sendPktTrain();
		
		// write to a sample file
		//writeSampleData();
	}
	
	// enforce a thread to stop
	public boolean isStopped() {
		return stop;
	}
	
	// stop a thread
	public void setStop(boolean stopResult) {
		stop = stopResult;
	}
	
	// process the packet train
	public boolean sendPktTrain() {
		// run the experiment once
		if (this.openSocket() && !stop) {
			// synchronize the client and server parameters
			this.sendConfigToSrv();
			// packet train in progress
			if (!this.runSocket())
				return false;
			// must close the socket
			if (!this.closeSocket())
				return false;
			return true;			
		}
		else {
			return false;
		}
	}
	
	// write sample results to file
	public void writeSampleData() {
		String filename = Util.getCurrenTimeForFile() + ".txt";
		String dstResult = "***************************\nTask @ " + 
                Util.getCurrentTimeWithFormat("MMMMM.dd.yyyy hh:mm:ss aaa") + '\n' + 
                this.paraInformation() + '\n' +
                probingResult + '\n';
		Util.writeResultToFile(filename, constant.outSamplePath, dstResult);
	}
	
	/** write measurement data to file
	 * In format of:
	 * 1. TIME 
	 * 2. UP_CAP 
	 * 3. DOWN_CAP 
	 * 4. GAP_SIZE 
	 * 5. PKT_SIZE 
	 * 6. TRAIN_LEN 
	 * 7. (...)
	 */
	public void writeMeasureData (String filename, String folder, boolean isErr) {
		String result = System.currentTimeMillis() + constant.DEL +
						String.format("%.4f", estUplinkBWResult) + constant.DEL +
						String.format("%.4f", estDownlinkBWReult) + constant.DEL +
						myGapSize + constant.DEL +
						myPktSize + constant.DEL +
						myTrainLength + "\n";
		if (isErr) {
			result = probingResult+"\n";
		}
		Util.writeResultToFile(filename, folder, result);
	}
	
	// update the variables
	public void updateParameters (double gap, int pkt, int train, String dir) {
		myGapSize = (long)(gap);
		myPktSize = pkt;
		myTrainLength = train;
		myDir = dir;
	}
	
	// class constructor
	public tcpSender(double gap, int pkt, int train, String hostname, int portNumber, String dir, MobiBand uiActivity) {
	  if (gap >= 0)
	    myGapSize = (long) (gap);
	  else
	    myGapSize = constant.pktGapNS;
		if (pkt != 0)
			myPktSize = pkt;
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
		if (!dir.equals("Up")) {
			myDir = dir;
		}
		// fetch current signal strength
		// rssi = new 
		UIActivity = uiActivity;
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
    		 Log.w(constant.logTagMSG, "Hostname is " + myHostname + "; port number is " + myPortNumber);
    		pkgTrainSocket = new Socket(myHostname, myPortNumber);
            /*out = new DataOutputStream(pkgTrainSocket.getOutputStream());
            in = new DataInputStream(pkgTrainSocket.getInputStream());
            // control stream
	        outCtrl = new PrintWriter(pkgTrainSocket.getOutputStream(), true);
            inCtrl = new BufferedReader(new InputStreamReader(
            		pkgTrainSocket.getInputStream()));*/
            // set the time out
            pkgTrainSocket.setSoTimeout(constant.tcpTimeOut);

            Log.d(constant.logTagMSG, "Current receive buffer size is " + pkgTrainSocket.getReceiveBufferSize());
            
            // set TCP no delay for packet transfer
            pkgTrainSocket.setTcpNoDelay(true);
            Log.d(constant.logTagMSG, "Current nodelay is " + pkgTrainSocket.getTcpNoDelay());
            

            // set TCP sending buffer size
            pkgTrainSocket.setSendBufferSize(myPktSize);
            //Log.d(constant.logTagMSG, "Current send buffer size is " + pkgTrainSocket.getSendBufferSize());
            
            // set TCP receiving buffer size
            //pkgTrainSocket.setReceiveBufferSize(myPktSize);
            //Log.d(constant.logTagMSG, "Current send buffer size is " + pkgTrainSocket.getReceiveBufferSize());
            outCtrl = new PrintWriter(pkgTrainSocket.getOutputStream(), true);
        		inCtrl = new BufferedReader(new InputStreamReader(
                		pkgTrainSocket.getInputStream()));
        		
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
	    	/*in.close();
	    	out.close();
	    	inCtrl.close();
	    	outCtrl.close();*/
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
    
    // send client parameters to the server
    // Format: "CONFIG: gap_size,pkt_size,train_len"
    private void sendConfigToSrv() {
    	String ackForConfigMessage;
    	/*byte[] buffer = new byte[200];
		int size;*/
    	//try {
    		/*outCtrl = new PrintWriter(pkgTrainSocket.getOutputStream(), true);
    		inCtrl = new BufferedReader(new InputStreamReader(
            		pkgTrainSocket.getInputStream()));*/
	        //do {
	        	// flush back the bandwidth result
	        	outCtrl.println(constant.configMSG + ':' + myGapSize + ',' + myPktSize + ','
	        								  + myTrainLength + ',' + myDir);
	        	outCtrl.flush();
	        	
	        	// receive from sender
	        	// size = in.read(buffer);
	        	// ackForConfigMessage = new String(buffer).trim();
	        //} while( (ackForConfigMessage = inCtrl.readLine()) != null && !ackForConfigMessage.equals(constant.ackMSG));
	        Log.d(constant.logTagMSG, "Send configuration message");
	        //inCtrl.close();
    	//} catch (IOException e) {
    		//Log.d(constant.logTagMSG, e.toString());
    	//} finally {
    		// outCtrl.close();
    	//}
    }
    
    // send TCP packet train
    public boolean runSocket() {
    	try {
    		
    		if (myDir == "Up") {
	  		  Log.d(constant.logTagMSG, "*****************************************************************");
		      Log.d(constant.logTagMSG, "************************ Uplink BW Test *************************");
		      Log.d(constant.logTagMSG, "*****************************************************************");
			     
		    	// upload link bandwidth test
		    	runUpLinkTask();
    		} else {
		    	Log.d(constant.logTagMSG, "*****************************************************************");
			    Log.d(constant.logTagMSG, "********************** Downlink BW Test *************************");
			    Log.d(constant.logTagMSG, "*****************************************************************");
		    	
		    	// download link bandwidth test
		    	runDownLinkTask();
    		}
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
    	} finally {
    		try {
	        inCtrl.close();
        } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
    		outCtrl.close();
    	}
    	
    	probingResult += "Uplink Capacity is " + String.format("%.4f", estUplinkBWResult) + " Mbps\n" +
    			         "Downlink Capacity is " + String.format("%.4f", estDownlinkBWReult) + " Mbps";
    	Log.i(constant.logTagMSG, probingResult);
    	return true;
    }

    // upload link test
    private void runUpLinkTask() throws IOException {
	    // create payload for the packet train
    	/*StringBuilder payload = new StringBuilder();
    		
    	// Create a zero string
    	// include the newline at the end of packet
    	for (int i = 0; i < myPktSize; i++) {
    		payload.append('0');
    	}
    		
    	// assign special characters
    	payload.setCharAt(0, 's');
    	payload.setCharAt(payload.length()-1, 'e');*/
    	// StringBuilder payload = new StringBuilder();
      byte[] payload = new byte[myPktSize];
      Random rand = new Random();
    	// Randomize the payload
      //rand.nextBytes(payload);
      for (int i = 0; i < payload.length; i++) {
        payload[i] = (byte)('A' + rand.nextInt(52));
      }
    	// assign special characters
    	payload[0] = '0';
    	// inject "e\n" into payload
    	String endStr = "1";
    	byte[] endStrbyte = endStr.getBytes();
    	System.out.println("End byte is " + String.valueOf(endStrbyte) + "; With length " + endStrbyte.length);
    	for (int i = 0; i < endStrbyte.length; i++) {
    		payload[myPktSize - endStrbyte.length + i] = endStrbyte[i];
    	}
    	
    	System.out.println("Current payload is " + new String(payload));
    	
	    // create a counter for packet train
    	int counter = 0;
    	long beforeTime = 0;
    	long afterTime = 0;
    	double diffTime = 0;
    	if (pkgTrainSocket == null) {
    		System.out.println("Invalid socket!!!");
    	}
    		 
    	// out = new DataOutputStream(pkgTrainSocket.getOutputStream());
    	
		while (counter < myTrainLength) {
			// start recording the first packet send time
			if (beforeTime == 0) {
				// beforeTime = System.nanoTime();
				beforeTime = System.currentTimeMillis();
			}
			
			// send packet with constant gap
			// out.write(payload);
			// out.flush();
			outCtrl.println(new String(payload));
			outCtrl.flush();
			
			// stop the process
			if (stop) {
    		throw new IOException("Thread interrupted");
    	}
			
			// create train gap in nanoseconds
			try {
			  if (myGapSize > 0) {
			    Thread.sleep(myGapSize);
			  }
			} catch (InterruptedException e) {
				e.printStackTrace();
			}		
			counter++;
		}
		
		// out.close();
		
		// record finish transmission time
		// afterTime = System.nanoTime();
		afterTime = System.currentTimeMillis();
				
		Log.d(constant.logTagMSG, "Single Packet size is " + payload.length + " Bytes.");
		Log.d(constant.logTagMSG, "Single GAP is " + myGapSize + " ms.");
		Log.d(constant.logTagMSG, "Total number of packet is " + counter);
		
		String lastMSG;
		// Total GAP calculation
		diffTime = afterTime - beforeTime;
		// diffTime = myTrainLength*myGapSize/java.lang.Math.pow(10.0, 6.0);
		// diffTime = myTrainLength*constant.pktGapMS;
		lastMSG = constant.finalMSG + ':' + diffTime;
		// byte[] lastBuffer = new byte[lastMSG.length()];
		
		// outCtrl = new PrintWriter(pkgTrainSocket.getOutputStream(), true);
		
		// send the last message
		outCtrl.println(lastMSG);
		outCtrl.flush();
		
		// outCtrl.close();
		
		//Log.d(constant.logTagMSG, "The last message in bytes: " + new String(lastMSG.getBytes()));
		
		double test = Double.parseDouble(lastMSG.substring(constant.finalMSG.length()+1));
		
		Log.d(constant.logTagMSG, "Client side takes " + test + " ms.");
		
		// waiting for the upload link result
		String uplinkBWResult;
		/*byte[] buffer = new byte[200];
		int size;
		size = in.read(buffer);*/
		
		/*outCtrl = new PrintWriter(pkgTrainSocket.getOutputStream(), true);
        inCtrl = new BufferedReader(new InputStreamReader(
        		pkgTrainSocket.getInputStream()));*/
		
		if (stop) {
  		throw new IOException("Thread interrupted");
  	}
		
		Log.d(constant.logTagMSG, "Before waiting for the input");
		while ((uplinkBWResult = inCtrl.readLine()) != null) {
			Log.d(constant.logTagMSG, "Received bandwidth is " + uplinkBWResult);
			// uplinkBWResult = new String(buffer).trim();
			if (uplinkBWResult.substring(0, constant.resultMSG.length()).equals(constant.resultMSG)) {
				estUplinkBWResult = Double.parseDouble(uplinkBWResult.substring(constant.resultMSG.length()+1));
				String uplinkResult = "TCP Uplink Bandwidth result is " + Math.floor(estUplinkBWResult * 1000)/1000 + " Mbps";
				// extra colon added
				Log.d(constant.logTagMSG, uplinkResult);
				UIActivity.updateTextView(uplinkResult);
				// send back the ACK result
				// outCtrl.println(constant.ackMSG);
				// outCtrl.flush();
				break;
			}
		}
		// outCtrl.close();
		// inCtrl.close();
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
        
        /*byte[] buffer = new byte[myPktSize];
        int size;
        size = in.read(buffer);*/
        
        /*inCtrl = new BufferedReader(new InputStreamReader(
        		pkgTrainSocket.getInputStream()));*/
        
        // output from what received
        while ((inputLine = inCtrl.readLine()) != null) {
        	/*System.out.println("Received "+ counter +" buffer: ");
        	for (int i = 0; i < buffer.length; i++) {
        		System.out.print((char)(buffer[i]));
        	}
        	System.out.print("\n");
        	inputLine = new String(buffer).trim();*/
        	System.out.println("Received the "+ counter +" message: " + inputLine);
  
        	// check if the start time recorded for first received packet
        	if (startTime == 0) {
        		startTime = System.currentTimeMillis();
        		// startTime = System.nanoTime();
        		singlePktSize = inputLine.length();
        	}
        	
        	if (stop) {
        		throw new IOException("Thread interrupted");
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

        	//size = in.read(buffer);
        }
        
        //inCtrl.close();
        
        endTime = System.currentTimeMillis();
        //endTime = System.nanoTime();
        gapTimeClt = endTime - startTime;
        
        // Bandwidth calculation
        // 1 Mbit/s = 125 Byte/ms 
        estTotalDownBandWidth = byteCounter/gapTimeClt/125.0;
        availableBWFraction = Math.min(gapTimeSrv/gapTimeClt,1.0);
        // estimate the IP layer capacity
        estDownlinkBWReult = estTotalDownBandWidth / availableBWFraction;
      
        // Display information at the server side
        Log.d(constant.logTagMSG, "Receive single Pkt size is " + singlePktSize + " Bytes.");
        Log.d(constant.logTagMSG, "Total receiving " + counter + " packets.");
        Log.d(constant.logTagMSG, "Server gap time is " + gapTimeSrv + " ms.");
        Log.d(constant.logTagMSG, "Total package received " + byteCounter + " Bytes with " + gapTimeClt + " ms total GAP.");
        Log.d(constant.logTagMSG, "Estimated Total download bandwidth is " + estTotalDownBandWidth + " Mbps.");
        Log.d(constant.logTagMSG, "Availabe fraction is " + availableBWFraction);
        Log.d(constant.logTagMSG, "Estimated Available download bandwidth is " + estDownlinkBWReult + " Mbps.");
        String downlinkResult = "TCP Downlink bandwidth is " + Math.floor(estTotalDownBandWidth * 1000) / 1000 + " Mbps";
        UIActivity.updateTextView(downlinkResult);
    }    
    
    // form the parameters information
    private String paraInformation() {
    	return "Hostname: " + myHostname + '\n' +
    		   "Port: " + myPortNumber + '\n' +
    		   "Packet Size: " + myPktSize + " Bytes\n" +
    		   "Gap Size: " + (double)(myGapSize)/java.lang.Math.pow(10, 6) + " ms\n" +
    		   "Train Length: " + myTrainLength;
    }
}