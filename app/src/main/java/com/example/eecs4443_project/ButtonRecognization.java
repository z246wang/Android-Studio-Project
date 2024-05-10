package com.example.eecs4443_project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class ButtonRecognization extends Activity {
    private final static String MYDEBUG = "MYDEBUG";

    private Button startButton, nextButton, naxtButton;
    private TextView countDownText;

    private CountDownTimer countDownTimer;
    private final long TIMER_SEC = 3000;
    private long countDownTimeLeft = TIMER_SEC;
    private final int LAP_NUM = 5;
    private int lapCount = 0;
    private int failedLaps = 0;

    // Trial timer
    private Timer trialTimer;
    private TimerTask trialTimerTask;
    private Double trialTotalTime = 0.0;

    // Lap timer
    private ArrayList<Double> lapTimes;
    private Timer lapTimer;
    private TimerTask lapTimerTask;
    private Double lapTotalTime = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.button_recognization);
        initialize();
    }

    private void initialize() {
        startButton = (Button) findViewById(R.id.startButton);
        nextButton = (Button) findViewById(R.id.nextButton);
        countDownText = (TextView) findViewById(R.id.countDownText);

        nextButton.setVisibility(View.INVISIBLE);

        naxtButton = (Button) findViewById(R.id.naxtButton);
        naxtButton.setVisibility(View.INVISIBLE);

        lapTimes = new ArrayList<>();
        trialTimer = new Timer();
        lapTimer = new Timer();

        naxtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementFailedLaps();
            }
        });
    }

    public void startClick(View v) {
        Log.i(MYDEBUG, "Start button clicked!");
        startButton.setVisibility(View.INVISIBLE);
        startTrialTimer();
        startCountDown();
    }

    public void nextClick(View v) {
        lapCount++;
        lapTimerReset();

        naxtButton.setVisibility(View.INVISIBLE);

        if (lapCount == LAP_NUM) {
            // All laps completed, stop trial timer and calculate total time
            trialTimerTask.cancel();
            double lapTimeSum = 0.0;
            for (Double time : lapTimes) {
                lapTimeSum += time;
            }
            double finalTrialTime = lapTimeSum + 15.0; // Add 15 seconds to the sum of lap times

            // Transition to results page
            Intent intent = new Intent(this, Results.class);
            intent.putExtra("trialTime", "" + Math.round((finalTrialTime * 100)) / 100.0);
            intent.putExtra("lapAvgTime", "" + calculateAverageLapTime(lapTimes));
            intent.putExtra("succeededLap", "" + lapCount);
            intent.putExtra("failedLap", "" + failedLaps);
            startActivity(intent);
        } else {
            // Reset countdown for next lap
            countDownTimeLeft = TIMER_SEC;
            startCountDown();
        }
    }


    public void startCountDown() {
        nextButton.setVisibility(View.INVISIBLE);
        countDownTimer = new CountDownTimer(countDownTimeLeft, 1000) {
            public void onTick(long millisUntilFinished) {
                countDownTimeLeft = millisUntilFinished;
                updateTimer();
            }

            public void onFinish() {
                nextButton.setVisibility(View.VISIBLE);
                startLapTimer();

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screenWidth = displayMetrics.widthPixels;

                int newWidth = new Random().nextInt(screenWidth / 6) + 200;
                int newHeight = new Random().nextInt(50) + 100;

                int spaceForNaxt = 200;
                int minMargin = spaceForNaxt;
                int maxMargin = screenWidth - newWidth - spaceForNaxt;
                int nextButtonMargin = new Random().nextInt(maxMargin - minMargin) + minMargin;

                RelativeLayout.LayoutParams nextLayoutParams = new RelativeLayout.LayoutParams(
                        newWidth, newHeight);
                nextLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                nextLayoutParams.setMargins(nextButtonMargin, 0, 0, 200);
                nextButton.setLayoutParams(nextLayoutParams);

                boolean isLeft = new Random().nextBoolean();
                int naxtButtonMargin;
                if (isLeft) {
                    naxtButtonMargin = nextButtonMargin - spaceForNaxt;
                } else {
                    naxtButtonMargin = nextButtonMargin + newWidth;
                }

                RelativeLayout.LayoutParams naxtLayoutParams = new RelativeLayout.LayoutParams(
                        newWidth, newHeight);
                naxtLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                naxtLayoutParams.setMargins(naxtButtonMargin, 0, 0, 200);
                naxtButton.setLayoutParams(naxtLayoutParams);
                naxtButton.setVisibility(View.VISIBLE);
                naxtButton.requestLayout();
            }

        }.start();
    }

    public void updateTimer() {
        int seconds = (int) (countDownTimeLeft / 1000);
        countDownText.setText(String.valueOf(seconds));
    }

    public void startTrialTimer() {
        Log.i(MYDEBUG, "Start Trial Timer");
        trialTotalTime = 0.0;
        trialTimerTask = new TimerTask() {
            @Override
            public void run() {
                trialTotalTime += 0.001;
            }
        };
        trialTimer.scheduleAtFixedRate(trialTimerTask, 0, 1);
    }


    public void startLapTimer() {
        Log.i(MYDEBUG, "Start Lap Timer");
        lapTotalTime = 0.0;
        lapTimerTask = new TimerTask() {
            @Override
            public void run() {
                lapTotalTime += 0.001;
            }
        };
        lapTimer.scheduleAtFixedRate(lapTimerTask, 0, 1);
    }

    public void lapTimerReset() {
        Log.i(MYDEBUG, "Lap Timer Reset");
        if (lapTimerTask != null) {
            lapTimerTask.cancel();
        }

        double roundedLapTime = Math.round(lapTotalTime * 1000) / 1000.0;
        lapTimes.add(roundedLapTime);
        Log.i(MYDEBUG, "Lap Time: " + roundedLapTime + " seconds");
        lapTotalTime = 0.0;
    }
    private double calculateAverageLapTime(ArrayList<Double> times) {
        double sum = 0.0;
        if (!times.isEmpty()) {
            for (Double time : times) {
                sum += time;
                Log.i(MYDEBUG, "sum: " + sum + " seconds");
            }
            return Math.round((sum / times.size() * 100)) / 100.0;
        }
        return 0.0;
    }

    private void incrementFailedLaps() {
        failedLaps++;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            incrementFailedLaps();
            return true;
        }
        return super.onTouchEvent(event);
    }
}
