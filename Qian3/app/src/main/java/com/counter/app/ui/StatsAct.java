package com.counter.app.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


import com.counter.app.R;
import com.counter.app.lib.db.Database;
import com.counter.app.lib.db.DatabaseItem;
import com.counter.app.lib.srv.PedoEvent;
import com.counter.app.lib.srv.PedoEventService;

public class StatsAct extends Activity implements PedoEvent.onPedoEventListener {

    boolean mPedoSrvBound;
    PedoEvent mPedoEvent;
    ServiceConnection mConnection;
    // Call service's method wherever you want
    PedoEventService.PedoSrvBinder mPedoSrvBinder;

    private TextView record, totalThisWeek, totalThisMonth, averageThisWeek, averageThisMonth;
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);


        test();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Bind with the service
        bindService(new Intent(this, PedoEventService.class), mConnection, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onStart() {
        super.onStart();


        Database db = Database.getInstance(this);
        Calendar fromDay = Calendar.getInstance();
        fromDay.add(Calendar.DATE, -8);
        Calendar endDay = Calendar.getInstance();
        endDay.add(Calendar.DATE, -1);
        int daysThisMonth = endDay.get(Calendar.DAY_OF_MONTH);
        //currStep = mSharedPreferences.getInt("data_currSteps", 0);
        DatabaseItem[] maxSteps;
        maxSteps = db.getMaxSteps(1);
        record.setText(String.valueOf(maxSteps[0].steps) + " steps at " + String.valueOf(maxSteps[0].getYear()) + " / " +
                String.valueOf(maxSteps[0].getMonth()) + " / " + String.valueOf(maxSteps[0].getDay()));

        totalThisWeek.setText(String.valueOf(db.getStepSum(fromDay, endDay)) + " steps");
        fromDay.set(Calendar.DATE, 1);
        totalThisMonth.setText(String.valueOf(db.getStepSum(fromDay, endDay)) + " steps");
        averageThisWeek.setText(String.valueOf(db.getStepSum(fromDay, endDay) / 7) + " steps");
        averageThisMonth.setText(String.valueOf(db.getStepSum(fromDay, endDay)/ daysThisMonth) + " steps");

        db.close();
        barChart();





    }


    @Override
    protected void onPause() {
        super.onPause();
        // Set mPedoEvent to null, no activity is using onPedoDetected() method
        mPedoSrvBinder.setPedoEvent(null);
        // Unbind from the service
        if (mPedoSrvBound) {
            mPedoSrvBound = false;
            unbindService(mConnection);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mPedoSrvBound) {
            unbindService(mConnection);
            mPedoSrvBound = false;
        }

    }

    @Override
    public void onPedoDetected() {
        if (mPedoSrvBound) {
            // TODO: What you want to do when step is detected.
            // Call service's method by mPedoSrvBinder
            // e.g. mPedoSrvBinder.getCurrStep();




        } else {
            Log.e("SomeAct", "Error: Service not bound!");
        }
    }

    private void barChart(){
        Database db = Database.getInstance(this);
        Calendar sevenDayBefore = Calendar.getInstance();
        sevenDayBefore.add(Calendar.DATE, -6);

        int step;
        BarModel bm;
        BarChart barChart = (BarChart) findViewById(R.id.bargraph);
        if (barChart.getData().size() > 0)
            barChart.clearChart();
        SimpleDateFormat df = new SimpleDateFormat("dd/M");
        for(int i = 0; i < 7; i++){
            step = db.getSteps(sevenDayBefore);
            if (step > 0) {
                Date day = sevenDayBefore.getTime();
                bm = new BarModel(df.format(day), 0,  Color.parseColor("#99CC00"));
                bm.setValue(step);
                barChart.addBar(bm);
            }
            sevenDayBefore.add(Calendar.DATE, 1);
        }


        if(barChart.getData().size() > 0){
            barChart.startAnimation();
        }
        else
        {
            barChart.setVisibility(View.GONE);
        }

        db.close();


    }
    ////test
    private void test(){
        Database db = Database.getInstance(this);
        Calendar one = Calendar.getInstance();
        one.add(Calendar.DATE, -1);
        Calendar two = Calendar.getInstance();
        two.add(Calendar.DATE, -2);
        Calendar three = Calendar.getInstance();
        three.add(Calendar.DATE, -3);
        Calendar four = Calendar.getInstance();
        four.add(Calendar.DATE, -4);
        Calendar five = Calendar.getInstance();
        five.add(Calendar.DATE, -5);
        Calendar six = Calendar.getInstance();
        six.add(Calendar.DATE, -6);
        Calendar seven = Calendar.getInstance();
        seven.add(Calendar.DATE, -7);
        db.setSteps(one, 10);
        db.setSteps(two, 3124);
        db.setSteps(three, 1232);
        db.setSteps(four, 123);
        db.setSteps(five, 1564);
        db.setSteps(six, 564);
        db.setSteps(seven, 546);
    }

    private void init() {
        /***** Link View Resources *****/

        record = (TextView) findViewById(R.id.record);
        totalThisWeek = (TextView) findViewById(R.id.totalthisweek);
        totalThisMonth = (TextView) findViewById(R.id.totalthismonth);
        averageThisWeek = (TextView) findViewById(R.id.averagethisweek);
        averageThisMonth = (TextView) findViewById(R.id.averagethismonth);
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