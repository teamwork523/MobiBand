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
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MobiBand extends Activity {
	// user input && accessible view
	private EditText hostText;
	private EditText portText;
	private EditText pktSizeText;
	private EditText gapText;
	private EditText totalNumPktText;
	private Button startButton;
	private TextView bandwidthReasult;
	
	// Experiment related variables
	private int counter = 0;
	private String hostnameValue = "";
	private int portNumberValue = 0;
	private double pktSizeValue = 0.0;
	private double gapValue = 0.0;
	private int trainLengthValue = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobi_band);
        
        // connect with interface
        this.findAllViewsById();
        
        // setup listener
        startButton.setOnClickListener(OnClickStartListener);
    }
    
    // bind all the activities
    private void findAllViewsById() {
    	hostText = (EditText) findViewById(R.id.hostText);
    	portText = (EditText) findViewById(R.id.portText);
    	pktSizeText = (EditText) findViewById(R.id.pktSizeText);
    	gapText = (EditText) findViewById(R.id.gapText);
    	totalNumPktText = (EditText) findViewById(R.id.totalNumPktText);
    	startButton = (Button) findViewById(R.id.startButton);
    	bandwidthReasult = (TextView) findViewById(R.id.bandwidthReasult);
    }
    
    // enable/disable all Views
    private void viewControl(boolean enable) {
    	startButton.setEnabled(enable);
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
			pktSizeValue = Double.parseDouble(pktSizeText.getText().toString().trim());
			gapValue = Double.parseDouble(gapText.getText().toString().trim());
			trainLengthValue = Integer.parseInt(totalNumPktText.getText().toString().trim());
			
			// setup a task
			tcpSender bandwidthTask = new tcpSender(gapValue, pktSizeValue, trainLengthValue, hostnameValue, portNumberValue, bandwidthReasult);
			
			// start a task
			// Open/close socket has message only when exception happens
			// runSocket always has a message
			// TODO: refactor this part
			bandwidthTask.start();
			// currentTaskResult = "Please see results in /sdcard/tmp/";
			
			// display the result
			previousText = "******************\nTask #" + (++counter) + " started, see detail in log\n" + previousText;
			bandwidthReasult.setText(previousText);
			
			// re-enable the button
			viewControl(true);
		}
	};
}
