package com.mobiband;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class MobiBand extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobi_band);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_mobi_band, menu);
        return true;
    }
}
