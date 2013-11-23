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
	private boolean stop = false;
	
	//enforce a thread to stop
	public boolean isStopped() {
		return stop;
	}
	
	// stop a thread
	public void setStop(boolean stopResult) {
		stop = stopResult;
	}
	
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
		// RRCTrafficPatternExp();
		LongDelayFACHExp();
	}
	
	// Increasing second round size
	private void LongDelayFACHExp() {
		try {
			// use the input gap as fixed gap period between the two rounds
			int initWaitSec = 15;
			int firstRepeat = 1;
			int secondRepeat = 1;
			int totalRepeat = 30;
			// each round we increase the number packets that we send
			int incrSendBytes = 500;
			for (int j = 0; j < totalRepeat; j++) {
				// check stop flag
				if (stop) {
					Log.w(constant.logTagMSG, "Thread Interrupted!!!");
					return;
				}
				Log.w(constant.logTagMSG, "First Test: " +(j+1)+ "th run started");
				Log.w(constant.logTagMSG, "First test: Wait for " + initWaitSec + " seconds ...");
				Thread.sleep(initWaitSec*1000);
				retxSender.updateParameters(0, fixMaxSize*10, firstRepeat, myDir);
				retxSender.sendPktTrain();
				Log.w(constant.logTagMSG, "First test: Second wait for "+ fixGap / 1000 + " seconds ...");
				Thread.sleep((long)(fixGap));
				// each time we increase a few amount of bytes
				retxSender.updateParameters(0, fixMinSize + j*incrSendBytes, secondRepeat, myDir);
				retxSender.sendPktTrain();
			}
		} catch (InterruptedException e) {
			Log.e(constant.logTagMSG, e.getMessage());
		}
	}
	
	// Follow the MAX/MIN pattern from the RRC inference experiment
	private void RRCTrafficPatternExp() {
		try {
			// first several task
			int initWaitSec = 15;
			int firstRepeat = 1;
			int secondRepeat = 1;
			// Each state has 3 diff gaps
			double[] gapOfInterest = {0,4.5,10};
			int totalRepeat = 50;
			for (int j = 0; j < totalRepeat; j++) {
				for (double i:gapOfInterest) {
					// check stop flag
					if (stop) {
						Log.w(constant.logTagMSG, "Thread Interrupted!!!");
						return;
					}
					Log.w(constant.logTagMSG, "First Test: " + i + " secs gap round;" +(j+1)+ "th run started");
					Log.w(constant.logTagMSG, "First test: Wait for " + initWaitSec + " seconds ...");
					Thread.sleep(initWaitSec * 1000);
					retxSender.updateParameters(0, fixMaxSize, firstRepeat, myDir);
					retxSender.sendPktTrain();
					/*retxSender.updateParameters(0, fixMaxSize, 1, myDir);
					for (int j = 0; j < firstRepeat; j++) {
						retxSender.sendPktTrain();
					}*/
					Log.w(constant.logTagMSG, "First test: Second wait for "+ i + " seconds ...");
					Thread.sleep((int)(i*1000));
					retxSender.updateParameters(0, fixMinSize, secondRepeat, myDir);
					retxSender.sendPktTrain();
					/*retxSender.updateParameters(0, fixMinSize, 1, myDir);
					for (int j = 0; j < secondRepeat; j++) {
						retxSender.sendPktTrain();
					}*/
				}
				Log.w(constant.logTagMSG, "WOW... " + (j+1) + "th run done!!!");
			}
			// second several task
			/*
			for (int i = 1; i <= TOTALROUND; i++) {
				Log.w(constant.logTagMSG, "Second Test: " + i + "th round started");
				Log.w(constant.logTagMSG, "Second test: Wait for "+ initWaitSec + " seconds ...");
				Thread.sleep(initWaitSec * 1000);
				retxSender.updateParameters(0, fixMaxSize, firstRepeat, myDir);
				retxSender.sendPktTrain();
				//retxSender.updateParameters(0, fixMaxSize, 1, myDir);
				//for (int j = 0; j < firstRepeat; j++) {
					//retxSender.sendPktTrain();
				//}
				Log.w(constant.logTagMSG, "Second test: Second wait for "+ i + " seconds ...");
				Thread.sleep(i*1000);
				retxSender.updateParameters(0, fixMaxSize, secondRepeat, myDir);
				retxSender.sendPktTrain();
				//retxSender.updateParameters(0, fixMaxSize, 1, myDir);
				//for (int j = 0; j < secondRepeat; j++) {
					//retxSender.sendPktTrain();
				//}
			}*/
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
