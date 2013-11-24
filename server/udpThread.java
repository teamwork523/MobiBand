/*
 * @author Haokun Luo, 10/03/2012
 * 
 * Server side UDP thread
 * 
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class udpThread extends Thread {
	// constant variables
	public static final int udpPortNumber = 10009;
	public static final int numOfByte = 2000;
	
	public void run() {
		try {
			byte[] buffer = new byte[numOfByte];
			
			// open a socket
			DatagramPacket  incoming = new DatagramPacket(buffer, buffer.length);
			DatagramSocket  ds = new DatagramSocket(udpPortNumber);
			
			System.out.println("Build a UDP server socket on port: " + udpPortNumber + ".");
			
			ds.receive(incoming);
			byte[] data = incoming.getData();
			String s = new String(data, 0, data.length);
			System.out.println("Port" + incoming.getPort() + " on " + incoming.getAddress() + " sent this message:");
			System.out.println(s);
		}
		catch (IOException e) {
			System.err.println(e); 
		}
	}
	
}
