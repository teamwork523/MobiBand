/*
 * @author Haokun Luo, 10/03/2012
 * 
 * Server side TCP thread
 * 
 */

//import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.locks.LockSupport;

public class tcpThread extends Thread {
	// server socket listener
    private ServerSocket serverSocket = null;
    // target client socket
    private Socket clientSocket = null;
    // socket stream
    private DataOutputStream out = null;
    // private DataInputStream in = null;
    private PrintWriter outCtrl = null;
    private BufferedReader inCtrl = null;
    // downlink bandwidth test variable
    private long myGapSize = 0;
	private int myPktSize = 0;
	private int myTrainLength = 0;
	private int myPortNum = 0;
	private String myDir = "Up";
    
    // class constructor
    tcpThread (double gap, int pkt, int train, int port_number) {
		if (gap != 0)
			// convert from ms to ns
			myGapSize = (long)(gap);
		else
			myGapSize = constantSrv.pktGapNS;
		if (pkt != 0)
			myPktSize = pkt;
		else
			myPktSize = constantSrv.pktSize;
		if (train != 0)
			myTrainLength = train;
		else
			myTrainLength = constantSrv.pktTrainLength;
		if (port_number != 0)
			myPortNum = port_number;
		else
			myPortNum = constantSrv.portNumber;
	}
    
	// setup a server Socket
	public void createServerSocket() {
		try {
            serverSocket = new ServerSocket(myPortNum);
            
            // receive buffer size
            // System.out.println("Before setting, receive buffer is " + serverSocket.getReceiveBufferSize());
            // serverSocket.setReceiveBufferSize(724);
            // System.out.println("After setting, receive buffer is " + serverSocket.getReceiveBufferSize());
            
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + myPortNum + ".");
            System.exit(1);
        }
        
        System.out.println("Start listen on port: " + myPortNum + ".");
	}
	
	// close all the socket and stream
	public void closeServerSocket() {
		try {
	        serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// main thread function
	public void run() {  
		// continuously listening on the port
		// add global counter to count the current run
		int counter = 0;
		while (true) {
	        try {
	            clientSocket = serverSocket.accept();
	            
	            // set no delay on the server side
	            clientSocket.setTcpNoDelay(true);
	            
	        } catch (IOException e) {
	            System.err.println("No client socket binded");
	            System.exit(1);
	        }
	        
	        System.out.println("*****************************************************************");
	        System.out.println("********* Find client and set up a socket. **********************");
	        System.out.println("*****************************************************************");
	        System.out.println("Current running " + (++counter) + " times");
	        try {
		        /*out = new DataOutputStream(clientSocket.getOutputStream());
		        in = new DataInputStream(clientSocket.getInputStream());*/
		        // control stream
		        outCtrl = new PrintWriter(clientSocket.getOutputStream(), true);
	            inCtrl = new BufferedReader(new InputStreamReader(
	            		clientSocket.getInputStream()));
		        // Synchronize the client configuration
		        synClientConfig();
		        
		        if (myDir.equals("Up")) {
			        System.out.println("*****************************************************************");
			        System.out.println("************************ Uplink BW Test *************************");
			        System.out.println("*****************************************************************");
			        
			        // uplink test
			        upLinkBandwidthtest();
		        } else {
		        
			        System.out.println("*****************************************************************");
			        System.out.println("********************** Downlink BW Test *************************");
			        System.out.println("*****************************************************************");
			        
			        // downlink test
			        downLinkBandwidthtest();
		        }
		        
		        // close the current client socket
		        clientSocket.close();
		        /*out.close();
		        in.close();*/
		        inCtrl.close();
		        outCtrl.close();
	        
	        } catch (IOException e) {
	        	e.printStackTrace();
	        	// close the server socket
	        	closeServerSocket();
	        }
			}
    }
	
	// Fetch client side parameters and reset those to server side
	private void synClientConfig() throws IOException {
		// Format: "CONFIG: gap_size,pkt_size,train_len"
		String configParaStr = "";
		/*int size;
		byte[] buffer = new byte[200];
		size = in.read(buffer);*/
		while ((configParaStr = inCtrl.readLine()) != null) {
			/*configParaStr = new String(buffer).trim();
			buffer = new byte[200];*/
			
			if (configParaStr.substring(0, constantSrv.configMSG.length()).equals(constantSrv.configMSG)) {
				// extract the useful information
				configParaStr = configParaStr.substring(constantSrv.configMSG.length()+1);
				String[] configParaArray = configParaStr.split(",");
				
				myGapSize = (long)(Integer.parseInt(configParaArray[0]));
				myPktSize = Integer.parseInt(configParaArray[1]);
				myTrainLength = Integer.parseInt(configParaArray[2]);
				myDir = configParaArray[3];
				System.out.println("Success sync parameters with client side");
				System.out.println("gap size is " + myGapSize + "; packet size is " + myPktSize + 
													 "; Train length is " + myTrainLength);
				System.out.println("Direction is " + myDir);
				
				// reset server side buffer size
				serverSocket.setReceiveBufferSize(myPktSize);
				
				// send back ACK message
				/*out.write(constantSrv.ackMSG.getBytes());
				out.flush();
				outCtrl.println(constantSrv.ackMSG);
				outCtrl.flush();*/
				
				// finish synchronize
				break;
			} else {
				System.out.println("Waiting for paramter sync ...");
			}
			// size = in.read(buffer);
		}
		//inCtrl.close();
		//outCtrl.close();
	}
	
	// client side upload link test
	private void upLinkBandwidthtest() throws IOException {
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
        double estTotalUpBandWidth = 0.0;
        double estAvailiableUpBandWidth = 0.0;
        double availableBWFraction = 1.0;
        
        // output from what received
        /*byte[] buffer = new byte[myPktSize];
        int size;
        size = in.read(buffer);*/
        while ((inputLine = inCtrl.readLine()) != null) {
        	/*System.out.println("Received "+ counter +" buffer: ");
        	for (int i = 0; i < buffer.length; i++) {
        		System.out.print((char)(buffer[i]));
        	}
        	System.out.print("\n");
        	inputLine = new String(buffer).trim();
        	buffer = new byte[myPktSize];*/
        	
        	// check if the start time recorded for first received packet
        	if (startTime == 0) {
        		startTime = System.currentTimeMillis();
        		//startTime = System.nanoTime();
        		singlePktSize = inputLine.length();
        	}

        	// out.flush();
        	byteCounter += inputLine.length();
        	
        	// check for last message
        	if (inputLine.substring(0, constantSrv.finalMSG.length()).equals(constantSrv.finalMSG)) {
        		System.out.println("Detect last upload link message");
        		gapTimeClt = Double.parseDouble(inputLine.substring(constantSrv.finalMSG.length()+1));
        		break;
        	}
        	
        	System.out.println("Received the "+ (counter + 1) +" message with size: " + inputLine.length());
        	// increase the counter
        	counter++;

        	//size = in.read(buffer);
        }
        // inCtrl.close();
        
        endTime = System.currentTimeMillis();
        //endTime = System.nanoTime();
        gapTimeSrv = endTime - startTime;
        
        // Bandwidth calculation
        // 1 Mbit/s = 125 Byte/ms 
        estTotalUpBandWidth = byteCounter/gapTimeSrv/125.0;
        availableBWFraction = Math.min(gapTimeClt/gapTimeSrv,1.0);
        estAvailiableUpBandWidth = estTotalUpBandWidth / availableBWFraction;
        
        // Display information at the server side
        System.out.println("Receive single Pkt size is " + singlePktSize + " Bytes.");
        System.out.println("Total receiving " + counter + " packets.");
        System.out.println("Client gap time is " + gapTimeClt + " ms.");
        System.out.println("Total package received " + byteCounter + " Bytes with " + gapTimeSrv + " ms total GAP.");
        System.out.println("Estimated Total upload bandwidth is " + estTotalUpBandWidth + " Mbits/sec.");
        System.out.println("Availabe fraction is " + availableBWFraction);
        System.out.println("Estimated Available upload bandwidth is " + estAvailiableUpBandWidth + " Mbits/sec.");
        
        // sending back the bandwidth result until receiving ACK message
        // String ackMessage;
        // byte[] ackBuffer = new byte[200];
		
        //do {
        	//byte[] ackBuffer = new byte[200];
        	// flush back the bandwidth result
        	outCtrl.println(constantSrv.resultMSG + ':' + estAvailiableUpBandWidth);
        	outCtrl.flush();
        	/*size = in.read(ackBuffer);
        	ackMessage = new String(ackBuffer).trim();*/
        //} while((ackMessage = inCtrl.readLine()) != null && !ackMessage.equals(constantSrv.ackMSG));
        //inCtrl.close();
        //outCtrl.close();
	}
	
	// client side download link test
	private void downLinkBandwidthtest() throws IOException {
		// create payload for the packet train
    	// StringBuilder payload = new StringBuilder();
		byte[] payload = new byte[myPktSize];
		Random rand = new Random();
	  	// Randomize the string
    		//rand.nextBytes(payload);
		for (int i = 0; i < payload.length; i++) {
		        payload[i] = (byte)('A' + rand.nextInt(52));
      		}
	
    	// assign special characters
    	payload[0] = '0';
    	// inject "e\n" into payload
    	String endStr = "1";
    	byte[] endStrbyte = endStr.getBytes();
    	// System.out.println("End byte is " + endStrbyte.toString() + "; With length " + endStrbyte.length);
    	for (int i = 0; i < endStrbyte.length; i++) {
    		payload[myPktSize - endStrbyte.length + i] = endStrbyte[i];
    	}
    	
	    // create a counter for packet train
    	int counter = 0;
    	long beforeTime = 0;
    	long afterTime = 0;
    	double diffTime = 0;
    	
    String realPayload = new String(payload); 
		while (counter < myTrainLength) {
			// start recording the first packet send time
			if (beforeTime == 0) {
				// beforeTime = System.nanoTime();
				beforeTime = System.currentTimeMillis();
			}
			
			// send packet with constant gap
			outCtrl.println(realPayload);
			outCtrl.flush();
			System.out.println("Send " + (counter + 1) + "th message with size: " + realPayload.length());
			// create train gap in nanoseconds
			try {
				Thread.sleep(myGapSize);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//LockSupport.parkNanos(myGapSize);		
			counter++;
		}
		
		
		// record finish transmission time
		// afterTime = System.nanoTime();
		afterTime = System.currentTimeMillis();
				
		System.out.println("Single Packet size is " + payload.length + " Bytes.");
		System.out.println("Single GAP is " + myGapSize + " ms.");
		System.out.println("Total number of packet is " + counter);
		
		String lastMSG;
		// Total GAP calculation
		diffTime = afterTime - beforeTime;
		// diffTime = myTrainLength*myGapSize/Math.pow(10.0, 6.0);
		// diffTime = myTrainLength*constant.pktGapMS;
		lastMSG = "END:" + diffTime;
		
		// send the last message
		outCtrl.println(lastMSG);
		outCtrl.flush();
		// outCtrl.close();
		
		System.out.println("Server side takes " + diffTime + " ms.");
	}
}
