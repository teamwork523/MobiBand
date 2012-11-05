/**
 * @author Haokun Luo
 * @date   11/02/2012
 * 
 * This is an automatic test class for tcpSender using packet train
 * 
 */

package com.mobiband;

public class tcpSenderWrapper extends Thread {
	// private variable
	private tcpSender mySender = null;
	private double fixGap = 0.2;
	private int fixPktSize = 1300;
	private int fixTrainLength = 200;
	private String myFolderName = "";
	private int TOTALROUND = 200;
	private int TOTALRANDOM = 100000;
	
	// Test cases
	private double [] gapSizeList = {0.1, 0.3, 0.5, 0.7, 0.9, 1.1, 1.3, 1.5};
	private int[]     pktSizeList = {200, 300, 400, 500, 600, 700, 724};
	private int[]     trainSizeList = {20, 40, 80, 160, 320, 640};
		
	// class constructor
	public tcpSenderWrapper(double gap, int pktSize, int train, String hostname, int portNumber) {
		mySender = new tcpSender(gap, pktSize, train, hostname, portNumber);
		// reset the parameters
		if (gap != 0) {
			fixGap = gap;
		}
		if (pktSize != 0) {
			fixPktSize = pktSize;
		}
		if (train != 0) {
			fixTrainLength = train;
		}
		myFolderName = genFolderName();
	}
	
	// main thread function
	public void run() {
		// Part 1: gap size iteration
		String filename = "gapSizeResult.txt";
		String header = "Time" + constant.DEL +
						"Uplink_Cap(Mbps)" + constant.DEL +
						"Down_Cap(Mbps)" + constant.DEL +
						"Gap_Size(ms)" + constant.DEL +
						"Pkt_Size(B)" + constant.DEL +
						"Train_Len\n";
		
		// write header to file
		Util.writeResultToFile(filename, myFolderName, header);
		
		for (double gap : gapSizeList) { 
			for (int i = 0; i < TOTALROUND; i++) {
				
				mySender.updateParameters(gap, fixPktSize, fixTrainLength);
				if (mySender.sendPktTrain()) {
					// no err
					mySender.writeMeasureData(filename, myFolderName, false);
				}
				else {
					// err exist
					mySender.writeMeasureData(filename, myFolderName, true);
				}
			}
		}
		
		// Part 2: packet size iteration
		filename = "pktSizeResult.txt";
		// write header to file
		Util.writeResultToFile(filename, myFolderName, header);
		
		for (int pkt : pktSizeList) { 
			for (int i = 0; i < TOTALROUND; i++) {
				mySender.updateParameters(fixGap, pkt, fixTrainLength);
				if (mySender.sendPktTrain()) {
					// no err
					mySender.writeMeasureData(filename, myFolderName, false);
				}
				else {
					// err exist
					mySender.writeMeasureData(filename, myFolderName, true);
				}
			}
		}
		
		// Part 3: train length iteration
		filename = "trainLength.txt";
		// write header to file
		Util.writeResultToFile(filename, myFolderName, header);
		
		for (int train : trainSizeList) { 
			for (int i = 0; i < TOTALROUND; i++) {
				mySender.updateParameters(fixGap, fixPktSize, train);
				if (mySender.sendPktTrain()) {
					// no err
					mySender.writeMeasureData(filename, myFolderName, false);
				}
				else {
					// err exist
					mySender.writeMeasureData(filename, myFolderName, true);
				}
			}
		}
	}
	
	// create a folder name with current Date + random number
	private String genFolderName() {
		return constant.outDataPath + '/' + Util.getCurrentTimeWithFormat("yyyy_MM_dd_HH_mm") + '/' + (int)(Math.random()*TOTALRANDOM);
	}
}
