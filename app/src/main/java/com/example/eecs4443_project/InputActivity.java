package com.example.eecs4443_project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class InputActivity extends Activity
{

    private final static String MYDEBUG = "MYDEBUG"; // for Log.i messages

    private Button tapButton, motionButton, voiceButton, exitButton;

    // called when the activity is first created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(MYDEBUG, "tapButton: " + R.id.tapInput);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        initialize();
        Log.i(MYDEBUG, "Initialization done. Application running.");
    }

    private void initialize() {
        Log.i(MYDEBUG, "tapButton: " + R.id.tapInput);
        // get references to buttons and text view from the layout manager (rather than instantiate them)
        tapButton = (Button) findViewById(R.id.tapInput);
        Log.i(MYDEBUG, "tapButton: " + tapButton);
        motionButton = (Button) findViewById(R.id.motionbutton);
        Log.i(MYDEBUG, "motionButton: " + motionButton);
        voiceButton = (Button) findViewById(R.id.voicebutton);
        Log.i(MYDEBUG, "voiceButton: " + voiceButton);
        exitButton = (Button) findViewById(R.id.exitbutton);
        Log.i(MYDEBUG, "exitButton: " + exitButton);
    }

    // this code executes when a button is clicked (i.e., tapped with user's finger)
    public void buttonClick(View v) {
        Log.i(MYDEBUG, "clicked!");
        if (v == tapButton) {
            Log.i(MYDEBUG, "Tap button clicked!");

            // start Button activity
            Intent i = new Intent(getApplicationContext(), ButtonRecognization.class);
            startActivity(i);

        } else if (v == motionButton) {
            Log.i(MYDEBUG, "Motion button clicked!");

            // start Motion activity
            Intent i = new Intent(getApplicationContext(), MotionRecognization.class);
            startActivity(i);

        } else if(v == voiceButton){
            Log.i(MYDEBUG, "Voice button clicked!");

            // start Voice activity
            Intent i = new Intent(getApplicationContext(), VoiceRecognization.class);
            startActivity(i);

        }else if (v == exitButton) {
            Log.i(MYDEBUG, "Good bye!");
            this.finish();

        } else
            Log.i(MYDEBUG, "Oops: Invalid Click Event!");

    }
}
