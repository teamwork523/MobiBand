/*
 * @author Haokun Luo, 10/03/2012
 * 
 * Server side TCP receiver
 * 
 */

public class pktTrainServer{
	
    public static void main(String[] args) {
    	double gap_size = 0.0;
    	int pkt_size = 0;
    	int train_length = 0;
    	int port_num = 0;
    	
    	// input handling
    	// > java probingNet gap_size pkt_size train_length port_num
    	if (args.length > 0 && args.length < 5) {
    		try {
	    		if (args.length >= 1) {
	    			gap_size = Double.parseDouble(args[0]);
	    		}
	    		if (args.length >= 2) {
	    			pkt_size = Integer.parseInt(args[1]);
	    		}
	    		if (args.length >= 3) {
	    			train_length = Integer.parseInt(args[2]);
	    		}
	    		if (args.length >= 4) {
	    			port_num = Integer.parseInt(args[3]);
	    		}
    		} catch (NumberFormatException e) {
    			e.getStackTrace();
    		}
    	}
    	
    	// create a TCP thread
    	tcpThread srvThread = new tcpThread(gap_size, pkt_size, train_length, port_num);
    	
    	// create a UDP thread
    	// udpThread srvThread = new udpThread();
    	
    	// run the thread
    	srvThread.createServerSocket();
    	
    	// continuously listen on port
    	srvThread.start();
    }
}