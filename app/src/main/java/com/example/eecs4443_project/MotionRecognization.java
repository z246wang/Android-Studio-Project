package com.example.eecs4443_project;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MotionRecognization extends Activity implements SensorEventListener {

    private final static String MYDEBUG = "MYDEBUG"; // for Log.i messages

    private Button startButton; //start button
    private TextView countDownText; //text for the count down

    private CountDownTimer countDownTimer; //timer to count down
    private long countDownTimeLeft; //time left in milliseconds for Count Down
    private final int LAP_NUM = 5; //number of total laps for the test
    private final long TIMER_SEC = 3000; //3 seconds
    private int succeededLaps, failedLaps; //number of succeeded and failed speech input. If all laps succeeded: succeededLaps == LAP_NUM


    //variables for the Trial Timer
    private Timer trialTimer; //count up timer for the whole trial. start when startButton is pressed
    private TimerTask trialTimerTask; //timer task for the trialTimer
    private Double trailTotalTime = 0.0; //total time of trial


    //variables for the Lap Timer
    private ArrayList<Double> timeOfLaps; //list of times for each attempt. length == ATTEMPT_NUM
    private Timer lapTimer; //count up timer for each lap. start when count down finishes for all laps
    private TimerTask lapTimerTask; //timer task for the lapTimer
    private Double lapTotalTime = 0.0; //lap total time

    //Sensor for tilting
    SensorManager sensorManager;
    Sensor sA;
    float roll, alpha, x, y, z;
    int samplingRate;
    float[] accValues = new float[3]; // smoothed values from accelerometer
    final static float RADIANS_TO_DEGREES = 57.2957795f;

    boolean failedFlag = false; //if user tilted in wrong direction
    boolean succeedLap = false; //if lap has succeeded
    boolean gotDir = false; //if user has already been given a direction
    private boolean startLap; //if lap has started

    //random value to pick direction
    Random randDir;
    int direction = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.motion_recognization);
        initialize();
    }

    private void initialize() {
        startButton = (Button) findViewById(R.id.startbutton);
        countDownText = (TextView) findViewById(R.id.countDown_text);

        succeededLaps = 0;
        failedLaps = 0;
        countDownTimeLeft = TIMER_SEC;

        trialTimer = new Timer();

        timeOfLaps = new ArrayList<Double>();
        lapTimer = new Timer();

        startLap = false;

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        sA = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // null on HTC Desire C, Asus MeMO Pad
        alpha = 1.0f;

        randDir = new Random();
    }

    //when startButton is pressed
    public void startClick(View v) {
        Log.i(MYDEBUG, "Start button clicked!");
        startButton.setVisibility(View.INVISIBLE);
        startTrialTimer();
        startCountDown();
    }

    @Override
    public void onSensorChanged(SensorEvent se)
    {
        // =======================
        // DETERMINE DEVICE ROLL
        // =======================

        // smooth the sensor values using a low-pass filter
        accValues = lowPass(se.values.clone(), accValues, alpha);

        x = accValues[0];
        y = accValues[1];
        z = accValues[2];
        roll = (float)Math.atan(x / Math.sqrt(y * y + z * z)) * RADIANS_TO_DEGREES;

        if(startLap){
            if(!gotDir) {
                direction = randDir.nextInt(2); //0 = left, 1 = right
                if(direction == 0)
                    countDownText.setText("Left");
                else
                    countDownText.setText("Right");

                gotDir = true;
            }else {
                if (!failedFlag && direction == 0) {
                    if (roll > 10f) {
                        Log.i(MYDEBUG, ""+roll);
                        succeedLap = true;
                        startLap = false;
                    } else if (roll < -10f) {
                        //try to go the opposite direction
                        Log.i(MYDEBUG, ""+roll);
                        failedLaps++;
                        failedFlag = true;
                    }
                } else if (!failedFlag && direction == 1) {
                    if (roll < -10f) {
                        Log.i(MYDEBUG, ""+roll);
                        succeedLap = true;
                        startLap = false;
                    } else if (roll > 10f) {
                        //try to go the opposite direction
                        Log.i(MYDEBUG, ""+roll);
                        failedLaps++;
                        failedFlag = true;
                    }
                } else if (failedFlag && (roll < 0.5f && roll > -0.5f)) {
                    //user reset phone tilt to try again
                    Log.i(MYDEBUG, "reset tilt: "+roll);
                    failedFlag = false;
                }
            }
        }

        if(succeedLap){
            Log.i(MYDEBUG, "cleared!!!");
            lapTimerReset();
            succeededLaps++;
            if(succeededLaps == LAP_NUM){
                trialTimerTask.cancel();
                trailTotalTime = Math.round((trailTotalTime / 1000) * 100) / 100.0;
                //Bundle all data for Results
                Bundle b = new Bundle();
                b.putString("trialTime", ""+trailTotalTime);
                b.putString("lapAvgTime", ""+lapTimeAvg(timeOfLaps));
                b.putString("succeededLap", ""+succeededLaps);
                b.putString("failedLap", ""+failedLaps);

                //call results activity
                Intent i = new Intent(getApplicationContext(), Results.class);
                i.putExtras(b);
                startActivity(i);

                finish();
            }
            else{
                //start 3 second timer
                countDownTimeLeft = TIMER_SEC;
                succeedLap = false;
                gotDir = false;
                startCountDown();
            }
        }
    } // end onSensorChanged

    /*
     * Low pass filter. The algorithm requires tracking only two numbers - the prior number and the
     * new number. There is a time constant "alpha" which determines the amount of smoothing. Alpha
     * is like a "weight" or "momentum". It determines the effect of the new value on the current
     * smoothed value.
     *
     * A lower alpha means more smoothing. NOTE: 0 <= alpha <= 1.
     *
     * See...
     *
     * http://blog.thomnichols.org/2011/08/smoothing-sensor-data-with-a-low-pass-filter
     */
    protected float[] lowPass(float[] input, float[] output, float alpha)
    {
        for (int i = 0; i < input.length; i++)
            output[i] = output[i] + alpha * (input[i] - output[i]);
        return output;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        sensorManager.registerListener(this, sA, samplingRate); // sM might be null (that's OK)
        Log.i(MYDEBUG, "onResume 2!");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void startCountDown(){
        startButton.setVisibility(View.INVISIBLE);
        countDownTimer = new CountDownTimer(countDownTimeLeft, 1000) {
            @Override
            public void onTick(long l) {
                countDownTimeLeft = l;
                updateTimer();
            }

            @Override
            public void onFinish() {
                startLapTimer();
                startLap = true;
            }
        }.start();
    }

    public void updateTimer(){
        int seconds = (int) countDownTimeLeft % 60000 / 1000;
        countDownText.setText(""+seconds);
    }

    //Timer for Trial. starts when start button is pressed.
    public void startTrialTimer(){
        trialTimerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        trailTotalTime++;
                    }
                });
            }
        };
        trialTimer.scheduleAtFixedRate(trialTimerTask, 0, 1);
    }

    //Timer for each Lap. starts when count down finishes
    public void startLapTimer(){
        lapTimerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lapTotalTime++;
                    }
                });
            }
        };
        lapTimer.scheduleAtFixedRate(lapTimerTask, 0, 1);
    }

    //reset lap timer. add result time to arraylist
    public void lapTimerReset(){
        Log.i(MYDEBUG, "Lap Total Time: "+lapTotalTime);
        lapTimerTask.cancel();
        timeOfLaps.add(lapTotalTime);
        lapTotalTime = 0.0;
    }

    //calculate average of lap times
    private double lapTimeAvg(ArrayList <Double> time) {
        double sum = 0;
        if(!time.isEmpty()) {
            for (Double i : time) {
                sum += i;
            }
            return Math.round(((sum / time.size()) / 1000) * 100) / 100.0;
        }
        return sum;
    }
}
