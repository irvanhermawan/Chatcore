package com.example.user.okhttp;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * Created by fabio on 30/01/2016.
 */
public class SensorService extends Service {
    public int counter=0;
    private MessageRetrievalThread retrievalThread  = null;
    public SensorService(Context applicationContext) {
        super();
        Log.i("HERE", "here I am!");
    }

    public SensorService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        retrievalThread = new MessageRetrievalThread();
        retrievalThread.start();

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
       // startTimer();
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (retrievalThread != null) {
            retrievalThread.stopThread();
        }
        Log.i("EXIT", "ondestroy!");
        Intent broadcastIntent = new Intent("uk.ac.shef.oak.ActivityRecognition.RestartSensor");
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;
    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {

                Log.i("in timer", "in timer ++++  "+ (counter++));
            }
        };
    }

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    private class MessageRetrievalThread extends Thread implements Thread.UncaughtExceptionHandler {

        private AtomicBoolean stopThread = new AtomicBoolean(false);

        MessageRetrievalThread() {
            super("MessageRetrievalService");
            setUncaughtExceptionHandler(this);
        }

        @Override
        public void run() {
            List<String> data = Arrays.asList("", "0");
            while (!stopThread.get()) {

                data = NetworkService.INSTANCE.search3(data.get(0));
                Log.i("",data.get(0));
                Log.i("",data.get(1));
               // Log.w(TAG, "Waiting for websocket state change....");
               // waitForConnectionNecessary();

               // Log.w(TAG, "Making websocket connection....");
              //  pipe = receiver.createMessagePipe();

               // SignalServiceMessagePipe localPipe = pipe;

              /*   try {
                   while (isConnectionNecessary() && !stopThread.get()) {
                        try {
                            Log.w(TAG, "Reading message...");
                            localPipe.read(REQUEST_TIMEOUT_MINUTES, TimeUnit.MINUTES,
                                    envelope -> {
                                        Log.w(TAG, "Retrieved envelope! " + envelope.getSource());

                                        PushContentReceiveJob receiveJob = new PushContentReceiveJob(MessageRetrievalService.this);
                                        receiveJob.handle(envelope);

                                        decrementPushReceived();
                                    });
                        } catch (TimeoutException e) {
                            Log.w(TAG, "Application level read timeout...");
                        } catch (InvalidVersionException e) {
                            Log.w(TAG, e);
                        }
                    }
                } catch (Throwable e) {
                    Log.w(TAG, e);
                } finally {
                    Log.w(TAG, "Shutting down pipe...");
                   // shutdown(localPipe);
                }   */

                Log.w("TAG", "Looping...");
            }

            Log.w("TAG", "Exiting...");
        }

        private void stopThread() {
            stopThread.set(true);
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
           // Log.w("TAG"", "*** Uncaught exception!");
            Log.w("TAG", e);
        }
    }

}
