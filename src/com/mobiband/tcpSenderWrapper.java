/**
 * @author Haokun Luo
 * @date   11/02/2012
 * 
 * This is an automatic test class for tcpSender using packet train
 * 
 */

package com.mobiband;

import android.util.Log;

public class tcpSenderWrapper extends Thread {
	// private variable
	private tcpSender retxSender = null;
	private double fixGap = 0.2;
	private int preferSize = 1024;
	private int fixMaxSize = 1024;
	private int fixMinSize = 30;
	private int fixTrainLength = 200;
	private String myFolderName = "";
	private int TOTALROUND = 15;
	private int TOTALRANDOM = 100000;
	private String myDir = "Up";
	
	// class constructor
	public tcpSenderWrapper(double gap, int pktSize, int train, String hostname, int portNumber, String dir) {
		// only send one packet per round
		retxSender = new tcpSender(0, fixMaxSize, 1, hostname, portNumber, dir);
		// reset the parameters
		if (gap != 0) {
			fixGap = gap;
		}
		if (pktSize != 0) {
			preferSize = pktSize;
		}
		if (train != 0) {
			fixTrainLength = train;
		}
		if (!dir.equals("Up")) {
			myDir = dir;
		}
		myFolderName = genFolderName();
	}
	
	// main thread function
	public void run() {
		try {
			// first several task
			int initWaitSec = 15;
			int firstRepeat = 30;
			int secondRepeat = 10;
			for (int i = 1; i <= TOTALROUND; i++) {
				Log.w(constant.logTagMSG, "First Test: " + i + "th round started");
				Log.w(constant.logTagMSG, "First test: Wait for "+ initWaitSec + " seconds ...");
				Thread.sleep(initWaitSec * 1000);
				// retxSender.updateParameters(0, fixMaxSize, 30);
				// retxSender.sendPktTrain();
				retxSender.updateParameters(0, fixMaxSize, 1, myDir);
				for (int j = 0; j < firstRepeat; j++) {
					retxSender.sendPktTrain();
				}
				Log.w(constant.logTagMSG, "First test: Second wait for "+ i + " seconds ...");
				Thread.sleep(i*1000);
				// retxSender.updateParameters(0, fixMinSize, 10);
				// retxSender.sendPktTrain();
				retxSender.updateParameters(0, fixMinSize, 1, myDir);
				for (int j = 0; j < secondRepeat; j++) {
					retxSender.sendPktTrain();
				}
			}
			// second several task
			for (int i = 1; i <= TOTALROUND; i++) {
				Log.w(constant.logTagMSG, "Second Test: " + i + "th round started");
				Log.w(constant.logTagMSG, "Second test: Wait for "+ initWaitSec + " seconds ...");
				Thread.sleep(initWaitSec * 1000);
				// retxSender.updateParameters(0, fixMaxSize, 30);
				// retxSender.sendPktTrain();
				retxSender.updateParameters(0, fixMaxSize, 1, myDir);
				for (int j = 0; j < firstRepeat; j++) {
					retxSender.sendPktTrain();
				}
				Log.w(constant.logTagMSG, "Second test: Second wait for "+ i + " seconds ...");
				Thread.sleep(i*1000);
				// retxSender.updateParameters(0, fixMaxSize, 10);
				// retxSender.sendPktTrain();
				retxSender.updateParameters(0, fixMaxSize, 1, myDir);
				for (int j = 0; j < secondRepeat; j++) {
					retxSender.sendPktTrain();
				}
			}
			Log.w(constant.logTagMSG, "!!!!Finished!!!!");
		} catch (InterruptedException e) {
			Log.e(constant.logTagMSG, e.getMessage());
		}
			
		
	}
	
	// create a folder name with current Date + random number
	private String genFolderName() {
		return constant.outDataPath + '/' + Util.getCurrentTimeWithFormat("yyyy_MM_dd_HH_mm") + '/' + (int)(Math.random()*TOTALRANDOM);
	}
}
