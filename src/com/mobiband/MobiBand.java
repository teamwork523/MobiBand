/*
 * @author Haokun Luo
 * @Date   10/11/2012
 * 
 * Main Activity for MobiBand
 * 
 */

package com.mobiband;

import android.os.Bundle;
import android.app.Activity;
//import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import android.widget.Button;
import android.widget.TextView;

public class MobiBand extends Activity {
	// user input && accessible view
	private EditText hostText;
	private EditText portText;
	private EditText pktSizeText;
	private EditText gapText;
	private EditText totalNumPktText;
	private Button startButton;
	private Button autoButton;
	private TextView bandwidthReasult;
	
	// Experiment related variables
	private int counter = 0;
	private String hostnameValue = "";
	private int portNumberValue = 0;
	private int pktSizeValue = 0;
	private double gapValue = 0.0;
	private int trainLengthValue = 0;
	
	// auto probing arraies
	private int[] pktSizeList = {1, 2, 4, 8, 16, 32};
	private double[] gapSizeList = {0.1, 0.3, 0.5, 0.7, 0.9};
	private int[]    trainSizeList = {10, 25, 50, 100, 250};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobi_band);
        
        // connect with interface
        this.findAllViewsById();
        
        // setup listener
        startButton.setOnClickListener(OnClickStartListener);
        // TODO: redesign this so that our app will not crash
        autoButton.setOnClickListener(OnClickAutoListener);
        
        /*// start the service
        Intent intent = new Intent(this, backgroundService.class);
        // store the hostname and port number into the intent
        intent.putExtra("hostname", hostText.getText().toString().trim());
        intent.putExtra("portNumber", Integer.parseInt(portText.getText().toString().trim()));
        startService(intent);*/
    }
    
    // bind all the activities
    private void findAllViewsById() {
    	hostText = (EditText) findViewById(R.id.hostText);
    	portText = (EditText) findViewById(R.id.portText);
    	pktSizeText = (EditText) findViewById(R.id.pktSizeText);
    	gapText = (EditText) findViewById(R.id.gapText);
    	totalNumPktText = (EditText) findViewById(R.id.totalNumPktText);
    	startButton = (Button) findViewById(R.id.startButton);
    	autoButton = (Button) findViewById(R.id.autoButton);
    	bandwidthReasult = (TextView) findViewById(R.id.bandwidthReasult);
    }
    
    // enable/disable all Views
    private void viewControl(boolean enable) {
    	startButton.setEnabled(enable);
    	autoButton.setEnabled(enable);
    }
    
    // define start button listener
    private OnClickListener OnClickStartListener = new OnClickListener() {
		
		public void onClick(View v) {
			// output definition
			String previousText = bandwidthReasult.getText().toString().trim();
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
			
			// setup a task
			tcpSender bandwidthTask = new tcpSender(gapValue, pktSizeValue, trainLengthValue, hostnameValue, portNumberValue);
			
			// start a task
			// Open/close socket has message only when exception happens
			// runSocket always has a message
			// TODO: refactor this part
			bandwidthTask.start();
			// currentTaskResult = "Please see results in /sdcard/tmp/";
			
			// display the result
			previousText = "******************\nTask #" + (++counter) + " started. See files in sdcard or log for detail\n" + previousText;
			bandwidthReasult.setText(previousText);
			
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
			String previousText = bandwidthReasult.getText().toString().trim();
			
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
			tcpSenderWrapper bandwidthTask = new tcpSenderWrapper(gapValue, pktSizeValue, trainLengthValue, srvHostname, srvPortNumber);
			bandwidthTask.start();
			
			// display the result
			previousText = "******************\nTask (Auto) #" + (++counter) + " started. See files in sdcard or log for detail\n" + previousText;
			bandwidthReasult.setText(previousText);
						
			Log.i("PktTrainService", "Auto test start!");
			// re-enable the button
			viewControl(true);
		}
	};
}
