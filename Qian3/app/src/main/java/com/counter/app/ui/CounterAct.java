package com.counter.app.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.counter.app.R;
import com.counter.app.lib.srv.PedoEvent;
import com.counter.app.lib.srv.PedoEventService;



public class CounterAct extends Activity implements PedoEvent.onPedoEventListener {

    // For service
    boolean mPedoSrvBound;
    PedoEvent mPedoEvent;
    ServiceConnection mConnection;
    // Call service's method wherever you want
    PedoEventService.PedoSrvBinder mPedoSrvBinder;

    // For self counting
    private int stepCount = 0;
    private boolean countFlag = false;
    /*
    private final Runnable timeCount = new Runnable() {
        @Override
        public void run() {
            long time = SystemClock.currentThreadTimeMillis();
            long timeMS = time%1000;
        }
    };
    */

    Button btnStartPause;
    Button btnStop;
    TextView tvStepCount;
    TextView tvTimeCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Bind with the service
        bindService(new Intent(this, PedoEventService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Set mPedoEvent to null, no activity is using onPedoDetected() method
        mPedoSrvBinder.setPedoEvent(null);
        // Unbind from the service
        if (mPedoSrvBound) {
            unbindService(mConnection);
            mPedoSrvBound = false;
        }
    }

    @Override
    public void onPedoDetected() {
        if (mPedoSrvBound) {
            if (countFlag) {
                stepCount++;
                tvStepCount.setText(String.valueOf(stepCount));
            }
        } else {
            Log.e("CounterAct", "Error: Service not bound!");
        }
    }

    private void init() {
        /***** Link View Resources *****/
        btnStartPause = (Button) findViewById(R.id.btnCounterStartPause);
        btnStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (countFlag) {
                    btnStartPause.setText(getResources().getString(R.string.counter_btnStartPause_start));
                    countFlag = false;
                } else {
                    btnStartPause.setText(getResources().getString(R.string.counter_btnStartPause_pause));
                    countFlag = true;
                }
            }
        });
        btnStop = (Button) findViewById(R.id.btnCounterStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnStartPause.setText(getResources().getString(R.string.counter_btnStartPause_start));
                countFlag = false;
                stepCount = 0;
                tvStepCount.setText(String.valueOf(stepCount));
            }
        });

        tvStepCount = (TextView) findViewById(R.id.tvCounterStepCount);
        tvTimeCount = (TextView) findViewById(R.id.tvCounterTimeCount);

        /***** Set Parameters *****/
        // For determine whether current activity is connecting to service or not
        mPedoSrvBound = false;
        // Define PedoEvent for PedoEventListener, so that onPedoDetected() could work correctly
        mPedoEvent = new PedoEvent(this);
        // Defines callbacks for service binding, passed to bindService()
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                mPedoSrvBound = true;
                mPedoSrvBinder = (PedoEventService.PedoSrvBinder) service;
                // Pass current ui's PedoEvent to the service so that onPedoDetected() could be triggered.
                mPedoSrvBinder.setPedoEvent(mPedoEvent);
                // TODO: What you want to do when you connect to service (one time)


            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mPedoSrvBound = false;
                // TODO: What you want to do when you disconnect (one time)


            }
        };

    }

}