/*
 * @author Haokun Luo, 09/27/2012
 * 
 * Define all the constant for the package train project
 * 
 */

package com.mobiband;

import android.os.Environment;

public class constant {
	// define the host name for the destination server
	public static final String hostName = "owl.eecs.umich.edu";
	
	// define the IP address for the destination server
	//public static final String ipAddress = "74.125.225.210"; 	// Google IP
	
	// define the port number that set up the connection (tcp)
	public static final int tcpPortNumber = 10100;
	
	// define the port number that set up the connection (udp)
	public static final int udpPortNumber = 10009;
	
	// timeout for TCP connections (in ms)
	public static final int tcpTimeOut = 100000;
	
	// the number of package on the train
	public static final int pktTrainLength = 50;
	
	// the size of each packet on the train (in Byte)
	public static final int pktSize = 500;
	
	// the gap of the size (in ms = 10^(-3)s)
	public static final long pktGapMS = 1;
	
	// the gap of the size (in ns = 10^(-9)s)
	public static final double msParameter = 0.5;
	public static final long pktGapNS = (long)(msParameter*java.lang.Math.pow(10.0, 6.0));
	
	// special tag for message
	public static final String finalMSG = "END";
	public static final String ackMSG = "ACK";
	public static final String resultMSG = "RT";
	public static final String errMSG = "ERROR";
	public static final String successMSG = "SUCCESS";
	public static final String configMSG = "CONFIG";
	public static final String logTagMSG = "MobilePktTrain";
	
	// limit the control message size;
	public static final int lastMSGLEN = 15;
	
	// output file path
	public static final String outSamplePath = Environment.getExternalStorageDirectory().getPath() + "/mobiband/sample";
	public static final String outDataPath = Environment.getExternalStorageDirectory().getPath() + "/mobiband/data";
	//public static final String outPath = "/sdcard/tmp";
	
	// output measurement data delimiter
	public static final String DEL = "\t";
}