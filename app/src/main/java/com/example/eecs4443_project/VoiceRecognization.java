package com.example.eecs4443_project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class VoiceRecognization extends Activity {
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

    //random words for user to say
    private String[] lowercaseWords = {"next", "hello", "right", "left"};
    private String[] uppercaseWords = {"Next", "Hello", "Right", "Left"};
    Random randWord;
    int wordIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voice_recognization);
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

        randWord = new Random();

    }

    //when startButton is pressed
    public void startClick(View v) {
        Log.i(MYDEBUG, "Start button clicked!");
        startButton.setVisibility(View.INVISIBLE);
        startTrialTimer();
        startCountDown();
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
                //countdown ends
                wordIndex = randWord.nextInt(4); //get random word from the word list
                countDownText.setText(lowercaseWords[wordIndex]);
                startLapTimer(); //start timer for lap
                getSpeechInput(); //start speech input
            }
        }.start();
    }

    public void updateTimer(){
        int seconds = (int) countDownTimeLeft % 60000 / 1000;
        countDownText.setText(""+seconds);
    }

    //uses speech recognizer to get user's input
    public void getSpeechInput(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, 10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case 10:
                if(resultCode == RESULT_OK && data != null){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if(result.get(0).equals(lowercaseWords[wordIndex]) || result.get(0).equals(uppercaseWords[wordIndex])){
                        //succeeded lap
                        lapTimerReset(); //stop lap timer, add result time to the arraylist, reset timer
                        succeededLaps++;

                        if(succeededLaps == LAP_NUM){
                            //All laps successfully completed
                            Log.i(MYDEBUG, "Cleared!");
                            trialTimerTask.cancel();

                            trailTotalTime = Math.round((trailTotalTime / 1000) * 100) / 100.0;

                            //Bundle all data for Results
                            Bundle b = new Bundle();
                            b.putString("trialTime", ""+trailTotalTime);
                            b.putString("lapAvgTime", ""+lapTimeAvg(timeOfLaps));
                            b.putString("succeededLap", ""+succeededLaps);
                            b.putString("failedLap", ""+failedLaps);

                            // start results activity
                            Intent i = new Intent(getApplicationContext(), Results.class);
                            i.putExtras(b);
                            startActivity(i);

                            finish();
                        }else{
                            //start 5 second timer
                            countDownTimeLeft = TIMER_SEC;
                            startCountDown();
                        }
                    }
                    else{
                        //failed lap. redo speech input
                        failedLaps++;
                        getSpeechInput();
                    }
                }

                break;
        }
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
