package com.mobiband;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobi_band);
        
        // connect with interface
        this.findAllViewsById();
        
        // TODO: setup listener
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
}
