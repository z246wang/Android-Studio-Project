package com.example.eecs4443_project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class Results extends Activity {
    private final static String MYDEBUG = "MYDEBUG"; // for Log.i messages

    private TextView trailTimeText, lapAvgText, suceedLapText, failLapText;
    private String trialTime, lapAvgTime, succeededLaps, failedLaps;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results);

        trailTimeText = (TextView) findViewById(R.id.trialTime);
        lapAvgText = (TextView) findViewById(R.id.lapAvgTime);
        suceedLapText = (TextView) findViewById(R.id.successedLaps);
        failLapText = (TextView) findViewById(R.id.failedLaps);

        // get results calculated in each activity
        Bundle b = getIntent().getExtras();
        trialTime = b.getString("trialTime");
        lapAvgTime = b.getString("lapAvgTime");
        succeededLaps = b.getString("succeededLap");
        failedLaps = b.getString("failedLap");

        //set text for text view
        trailTimeText.setText("Trial Time: "+ trialTime + " s");
        lapAvgText.setText("Average Lap Time: " + lapAvgTime + " s");
        suceedLapText.setText("Succeeded Laps: " + succeededLaps);
        failLapText.setText("Failed Laps: " + failedLaps);
    }

    public void clickSetup(View view){
        // start setup activity
        Intent i = new Intent(getApplicationContext(), InputActivity.class);
        startActivity(i);
    }
}
