/*
 * @author Haokun Luo
 * @Date   10/11/2012
 * 
 * Main Activity for MobiBand
 * 
 */

package com.mobiband;

import java.net.InetAddress;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Bundle;
import android.os.PowerManager;
import android.app.Activity;
import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
//import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.TextView;

public class MobiBand extends Activity {
  private MobiBand myActivity = null;
	// user input && accessible view
	private EditText hostText;
	private EditText portText;
	private EditText pktSizeText;
	private EditText gapText;
	private EditText totalNumPktText;
	private Button startButton;
	//private Button autoButton;
	private Button stopButton;
	private RadioGroup directionButtonGroup;
	private RadioButton upButton;
	private RadioButton downButton;
	private RadioGroup networkTypeButtonGroup;
	private RadioButton tcpButton;
	private RadioButton icmpButton;
	private TextView displayedResults;
	
	// Experiment related variables
	private int counterUp = 0;
	private int counterDown = 0;
	private String hostnameValue = "";
	private int portNumberValue = 0;
	private int pktSizeValue = 0;
	private double gapValue = 0.0;
	private int trainLengthValue = 0;
	public static String direction = "Up";
	public static String networkType = "TCP";
	private String TAG = "PktTrainService";
	TelephonyManager telephonyManager;
	PhoneStateListener listener;
	PowerManager pm;
	PowerManager.WakeLock wl;
	
	// private access to class member
	private SenderWrapper completeTask;
	private tcpSender tcpTask;
	private PingSender pingTask;
	
	
	// auto probing arraies
	private int[] pktSizeList = {1, 2, 4, 8, 16, 32};
	private double[] gapSizeList = {0.1, 0.3, 0.5, 0.7, 0.9};
	private int[]    trainSizeList = {10, 25, 50, 100, 250};
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume function called");
		wl.acquire();
	}
	
	@Override
	public void onPause() {
		super.onDestroy();
		Log.d(TAG, "onDestroy function called");
		wl.release();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myActivity = this;
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Screen lock");
        wl.acquire();
        
        setContentView(R.layout.activity_mobi_band);
        
        // connect with interface
        this.findAllViewsById();
        
        // setup listener
        startButton.setOnClickListener(OnClickStartListener);
        // auto button listener
        // autoButton.setOnClickListener(OnClickAutoListener);
        // stop button listener
        stopButton.setOnClickListener(OnClickStopListener);
        // set direction
        upButton.setOnClickListener(OnClickDirectionButtonListener);   
        downButton.setOnClickListener(OnClickDirectionButtonListener);
        directionButtonGroup.check(upButton.getId());
        tcpButton.setOnClickListener(OnClickNetworkTypeButtonListener);
        icmpButton.setOnClickListener(OnClickNetworkTypeButtonListener);
        networkTypeButtonGroup.check(tcpButton.getId());
        
        /*// start the service
        Intent intent = new Intent(this, backgroundService.class);
        // store the hostname and port number into the intent
        intent.putExtra("hostname", hostText.getText().toString().trim());
        intent.putExtra("portNumber", Integer.parseInt(portText.getText().toString().trim()));
        startService(intent);*/
        
        /*
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        listener = myPhoneStateListener;
        // connect telephony manager with phone state listener
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);*/
    }
    
    // bind all the activities
    private void findAllViewsById() {
    	hostText = (EditText) findViewById(R.id.hostText);
    	portText = (EditText) findViewById(R.id.portText);
    	pktSizeText = (EditText) findViewById(R.id.pktSizeText);
    	gapText = (EditText) findViewById(R.id.gapText);
    	totalNumPktText = (EditText) findViewById(R.id.totalNumPktText);
    	startButton = (Button) findViewById(R.id.startButton);
    	//autoButton = (Button) findViewById(R.id.autoButton);
    	stopButton = (Button) findViewById(R.id.stopButton);
    	displayedResults = (TextView) findViewById(R.id.displayedResults);
    	directionButtonGroup = (RadioGroup) findViewById(R.id.DirectionRadioButtonGroup);
    	upButton = (RadioButton) findViewById(R.id.MobibandUpButton);
    	downButton = (RadioButton) findViewById(R.id.MobibandDownButton);
    	networkTypeButtonGroup = (RadioGroup) findViewById(R.id.NetworkTypeRadioButtonGroup);
    	tcpButton = (RadioButton) findViewById(R.id.TCPButton);
    	icmpButton = (RadioButton) findViewById(R.id.ICMPButton);
    }
    
    // enable/disable all Views
    private void viewControl(boolean enable) {
    	startButton.setEnabled(enable);
    	//autoButton.setEnabled(enable);
    }
    
    // define the direction radio button
    private OnClickListener OnClickDirectionButtonListener = new OnClickListener() {
			
			public void onClick(View v) {
				 RadioButton rb = (RadioButton) v;
				 MobiBand.direction = (String) rb.getText();	
				 // displayedResults.append(MobiBand.direction + "\n");
			}
		};
		
		// define the network type radio button
    private OnClickListener OnClickNetworkTypeButtonListener = new OnClickListener() {
      
      public void onClick(View v) {
         RadioButton rb = (RadioButton) v;
         MobiBand.networkType = (String) rb.getText();
         // displayedResults.append(MobiBand.networkType + "\n");
      }
    };
    
    // define start button listener
    private OnClickListener OnClickStartListener = new OnClickListener() {
		
		public void onClick(View v) {
			// output definition
			//String previousText = displayedResults.getText().toString().trim();
			// String currentTaskResult = "";
			
			// disable all related view
			viewControl(false);
			
			// fetch the current user input value
			hostnameValue = hostText.getText().toString().trim();
			portNumberValue = Integer.parseInt(portText.getText().toString().trim());
			// Unit: Bytes
			pktSizeValue = Integer.parseInt(pktSizeText.getText().toString().trim());
			// Unit: ms
			gapValue = Double.parseDouble(gapText.getText().toString().trim());
			trainLengthValue = Integer.parseInt(totalNumPktText.getText().toString().trim());
			
			// Run the TCP measurement or the ICMP (Ping) measurement
			if (MobiBand.networkType.equals("TCP")) { 
			  tcpTask = new tcpSender(gapValue, pktSizeValue, trainLengthValue, 
  					                       hostnameValue, portNumberValue, MobiBand.direction, myActivity);
  			
  			// start a task
  			// Open/close socket has message only when exception happens
  			// runSocket always has a message
  			// TODO: refactor this part
			  tcpTask.start();
  			// currentTaskResult = "Please see results in /sdcard/tmp/";
  			
  			// display the result
  			//previousText = "******************\nSingle Task "+ MobiBand.direction +"#" + ((MobiBand.direction.equals("Up")) ? (++counterUp) : (++counterDown))
  			//		           + " started.\n" + previousText;
			} else {
			  pingTask = new PingSender(gapValue, pktSizeValue, hostnameValue, myActivity);
			  pingTask.start();
			}
			//displayedResults.setText(Util.accessResults());
			
			// re-enable the button
			viewControl(true);
		}
	};

	// define auto button listener
	private OnClickListener OnClickAutoListener = new OnClickListener() {

		public void onClick(View v) {
			// disable all related view
			viewControl(false);
			
			Log.i("PktTrainService", "Total number of cases in background is " + pktSizeList.length * gapSizeList.length * trainSizeList.length);
			
			// output definition
			String previousText = displayedResults.getText().toString().trim();
			
			// fetch the hostname and port number
			String srvHostname = hostText.getText().toString().trim();
			int srvPortNumber = Integer.parseInt(portText.getText().toString().trim());
			// Unit: Bytes
			pktSizeValue = Integer.parseInt(pktSizeText.getText().toString().trim());
			// Unit: ms
			gapValue = Double.parseDouble(gapText.getText().toString().trim());
			trainLengthValue = Integer.parseInt(totalNumPktText.getText().toString().trim());
			
			// loop through all the test cases
			// create a thread for test
			completeTask = new SenderWrapper(gapValue, pktSizeValue, trainLengthValue, srvHostname, srvPortNumber, MobiBand.direction, myActivity);
			completeTask.start();
			
			// display the result
			previousText = "******************\nComplete Task " + MobiBand.direction + " #" + ((MobiBand.direction.equals("Up")) ? (++counterUp) : (++counterDown))
					+ " started. \n" + previousText;
			displayedResults.setText(previousText);
						
			Log.i("PktTrainService", "Auto test start!");
			// re-enable the button
			viewControl(true);
		}
	};
	
	//define stop button listener
	private OnClickListener OnClickStopListener = new OnClickListener() {

		public void onClick(View v) {
			if (!completeTask.isStopped()) {
				Log.d(TAG,"Complete Task got interrupt");
				completeTask.setStop(true);
			}
			if (!tcpTask.isStopped()) {
				Log.d(TAG,"Single Task got interrupt");
				tcpTask.setStop(true);
			}
		}
	};
	
	private PhoneStateListener myPhoneStateListener = new PhoneStateListener() {
  	@Override
  	public void onSignalStrengthsChanged(SignalStrength signalStrength) {
  		// check for check state
  		/*
  		Log.d(TAG, "Security positive cache value: " + Security.getProperty("networkaddress.cache.ttl"));
  		Log.d(TAG, "System positive cache value: " + System.getProperty("networkaddress.cache.ttl"));
  		Log.d(TAG, "Security negative cache value: " + Security.getProperty("networkaddress.cache.negative.ttl"));
  		Log.d(TAG, "System negative cache value: " + System.getProperty("networkaddress.cache.negative.ttl"));
  		*/
  		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
  		String nowStr = dateFormat.format(new Date()); 
  		Log.d(TAG, "Time in human readable format is " + nowStr);
  		Log.d(TAG, "Current time is " + System.currentTimeMillis() + "\n" + signalStrength.toString());
			Definition.NETWORK_TYPE = telephonyManager.getNetworkType();
  		Log.d(TAG, "Network type is " + Definition.NETWORK_TYPE);
  		String newMessage = String.valueOf(System.currentTimeMillis());
  		if (Definition.NETWORK_TYPE == Definition.LTE_TYPE) {
  			// LTE network
  			Definition.setLTESIG(Integer.parseInt(signalStrength.toString().split(" ")[Definition.LTE_SIG_INDEX]));
  			Definition.setLTERSRP(Integer.parseInt(signalStrength.toString().split(" ")[Definition.LTE_RSRP_INDEX]));
  			Definition.setLTERSRQ(Integer.parseInt(signalStrength.toString().split(" ")[Definition.LTE_RSRQ_INDEX]));
  			Definition.setLTESNR(Integer.parseInt(signalStrength.toString().split(" ")[Definition.LTE_SNR_INDEX]));
  			Log.d(TAG, "Signal Strength is " + Definition.getLTESIG());
  			Log.d(TAG, "LTE RSRP is " + Definition.getLTERSRP());
  			Log.d(TAG, "LTE RSRQ is " + Definition.getLTERSRQ());
  			Log.d(TAG, "LTE SNR is " + Definition.getLTESNR());
  			Log.d(TAG, "Network type is " + Definition.NETWORK_TYPE);
  		}
  		else {
  			// 3G network assign RSSI
  			newMessage += "\t" + signalStrength.toString().split(" ")[Definition.GSM_SS_INDEX];
  			Definition.setGSMSS(Integer.parseInt(signalStrength.toString().split(" ")[Definition.GSM_SS_INDEX]));
  			Log.d(TAG, "Signal level is " + Definition.getGSMSS());
  			Log.d(TAG, "Network type is " + Definition.NETWORK_TYPE);
  		}
  		displayedResults.append(newMessage+"\n");
  	}
  };
  
  public void updateTextView(final String data){
    runOnUiThread(new Runnable(){
      @Override
      public void run() {
        String previousText = displayedResults.getText().toString().trim();
        displayedResults.setText(data + "\n" + previousText);
      } 
    });
  }
}
