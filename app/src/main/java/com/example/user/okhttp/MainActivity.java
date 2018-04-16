package com.example.user.okhttp;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;



public class MainActivity extends AppCompatActivity {
    TextView tvDisplay;
    Handler mHandler = new Handler();
    Intent mServiceIntent;
    private SensorService mSensorService;
    Context ctx;
    public Context getCtx() {
        return ctx;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvDisplay =  findViewById(R.id.tv_display);


    }

    public void apiCall(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                searchCall();
            }
        }).start();
    }

    public void apiCall1(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                searchCall1();
            }
        }).start();
    }
    public void apiCall2(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                searchCall2();
            }
        }).start();
    }

    public void apiCall3(View v) {
        ctx = this;
        mSensorService = new SensorService(getCtx());
        mServiceIntent = new Intent(getCtx(), mSensorService.getClass());
        if (!isMyServiceRunning(mSensorService.getClass())) {
            startService(mServiceIntent);
        }

    }


    private void searchCall() {
        final String data = NetworkService.INSTANCE.search("hello");


        mHandler.post(new Runnable() {
            @Override
            public void run() {
                tvDisplay.setText(data);
            }
        });
    }
    private void searchCall1() {
        final String data = NetworkService.INSTANCE.search1("hello");


        mHandler.post(new Runnable() {
            @Override
            public void run() {
                tvDisplay.setText(data);
            }
        });
    }
    private void searchCall2() {
        final String data = NetworkService.INSTANCE.search2("hello");


        mHandler.post(new Runnable() {
            @Override
            public void run() {
                tvDisplay.setText(data);
            }
        });

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }


    @Override
    protected void onDestroy() {
        stopService(mServiceIntent);
        Log.i("MAINACT", "onDestroy!");
        super.onDestroy();

    }
}
