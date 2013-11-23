package com.mobiband;

public class Definition {
	// signal strength value
	public static final int INVALID = -100;
	public static int NETWORK_TYPE = INVALID;
	public static final int LTE_TYPE = 13;
	public static int GSM_SS = INVALID;
	public static int LTE_SIG = INVALID;
	public static int LTE_RSRP = INVALID;
	public static int LTE_RSRQ = INVALID;
	public static int LTE_SNR = INVALID;
	public static final int LTE_SIG_INDEX = 8;
	public static final int LTE_RSRP_INDEX = 9; // in W
	public static final int LTE_RSRQ_INDEX = 10;
	public static final int LTE_SNR_INDEX = 11; // in dB
	public static final int GSM_SS_INDEX = 1;
	
	// synchronized method to set all the static variable
	public static synchronized void setGSMSS (int newvalue) { GSM_SS = newvalue; }
	public static synchronized void setLTESIG (int newvalue) { LTE_SIG = newvalue; }
	public static synchronized void setLTERSRP (int newvalue) { LTE_RSRP = newvalue; }
	public static synchronized void setLTERSRQ (int newvalue) { LTE_RSRQ = newvalue;}
	public static synchronized void setLTESNR (int newvalue) { LTE_SNR = newvalue; }
	
	public static synchronized int getGSMSS () { return GSM_SS; }
	public static synchronized int getLTESIG () { return LTE_SIG; }
	public static synchronized int getLTERSRP () { return LTE_RSRP;	}
	public static synchronized int getLTERSRQ () { return LTE_RSRQ;	}
	public static synchronized int getLTESNR () { return LTE_SNR; }
}
