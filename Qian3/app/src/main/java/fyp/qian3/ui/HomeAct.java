package fyp.qian3.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;


import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import fyp.qian3.R;
import fyp.qian3.lib.srv.PedoEvent;
import fyp.qian3.lib.srv.PedoEventService;

import java.util.Calendar;

public class HomeAct extends Activity implements PedoEvent.onPedoEventListener {

    boolean mPedoSrvBound;
    SharedPreferences sharedPrefs;
    PedoEvent mPedoEvent;
    ServiceConnection mConnection;
    // Binder is used to call service function
    PedoEventService.PedoSrvBinder mPedoSrvBinder;

    Button btnSetting;
    Button btnCounter;
    TextView tvCurrStep;
    TextView tvWeekDay;
    TextView tvDate;
    ImageButton stat;
    ImageView ivMonster;
    //Ryan
    PieModel sliceGoal, sliceCurrent;
    PieChart pcStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind with the service
        bindService(new Intent(this, PedoEventService.class), mConnection, Context.BIND_AUTO_CREATE);


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
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPedoDetected() {
        if (mPedoSrvBound) {
            tvCurrStep.setText(String.valueOf(mPedoSrvBinder.getCurrStep()));
        } else {
            Log.e("HomeAct", "Error: Service not bound!");
        }
    }

    //new add to show today date
    private void setDate() {
        Calendar mCalendar = Calendar.getInstance();
        int weekDay = mCalendar.get(Calendar.DAY_OF_WEEK);
        int month = mCalendar.get(Calendar.MONTH) + 1;
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);

        tvDate.setText(month + getString(R.string.month) + day
                + getString(R.string.day));

        String week_day_str = new String();
        switch (weekDay) {
            case Calendar.SUNDAY:
                week_day_str = getString(R.string.sunday);
                break;

            case Calendar.MONDAY:
                week_day_str = getString(R.string.monday);
                break;

            case Calendar.TUESDAY:
                week_day_str = getString(R.string.tuesday);
                break;

            case Calendar.WEDNESDAY:
                week_day_str = getString(R.string.wednesday);
                break;

            case Calendar.THURSDAY:
                week_day_str = getString(R.string.thursday);
                break;

            case Calendar.FRIDAY:
                week_day_str = getString(R.string.friday);
                break;

            case Calendar.SATURDAY:
                week_day_str = getString(R.string.saturday);
                break;
        }
        tvWeekDay.setText(week_day_str);
    }

    private void init() {
        /***** Link View Resources *****/
        btnSetting = (Button) findViewById(R.id.btnHomeSetting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeAct.this, fyp.qian3.ui.SettingAct.class));

            }

        });

        btnCounter = (Button) findViewById(R.id.btnHomeCounter);
        btnCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeAct.this, fyp.qian3.ui.CounterAct.class));
            }
        });

        tvCurrStep = (TextView) findViewById(R.id.tvHomeCurrStep);
        tvWeekDay = (TextView) findViewById(R.id.tvWeekDay);
        tvDate = (TextView) findViewById(R.id.tvDate);
        pcStep = (PieChart) findViewById(R.id.piechart);

        // slice for the steps taken today
        sliceCurrent = new PieModel("Current Steps", 5, Color.parseColor("#99CC00"));
        pcStep.addPieSlice(sliceCurrent);

        // slice for the "missing" steps until reaching the goal
        //assume is 10
        sliceGoal = new PieModel("Steps remained", 10 , Color.parseColor("#CC0000"));
        pcStep.addPieSlice(sliceGoal);

        pcStep.setUsePieRotation(true);
        pcStep.startAnimation();

        //Ryan testing
        stat = (ImageButton) findViewById(R.id.statistic);
        stat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeAct.this, fyp.qian3.ui.StatsAct.class));
            }
        });

        /***** Set Parameters *****/
        // For determine whether current activity is connecting to service or not
        mPedoSrvBound = false;
        // Get shared preference
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        // Define  PedoEvent for PedoEventListener, so that onPedoDetected() could work correctly
        mPedoEvent = new PedoEvent(this);
        // Defines callbacks for service binding, passed to bindService()
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                mPedoSrvBound = true;
                mPedoSrvBinder = (PedoEventService.PedoSrvBinder) service;
                // Pass current ui  PedoEvent to the service so that  onPedoDetected() could be triggered.
                mPedoSrvBinder.setPedoEvent(mPedoEvent);
                tvCurrStep.setText(String.valueOf(mPedoSrvBinder.getCurrStep()));
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mPedoSrvBound = false;
            }
        };

        /***** Synchronize Settings *****/
        if (sharedPrefs.getBoolean("pref_genPedoSrv", false)) {
            startService(new Intent(HomeAct.this, fyp.qian3.lib.srv.PedoEventService.class));
        }

        //set the date
        setDate();
        ivMonster = (ImageView) findViewById(R.id.monster);
        ivMonster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TranslateAnimation(float x1, float x2, float y1, float y2)
                Animation am = new TranslateAnimation(0.0f, 0f, 0.0f, -120.0f);
                //setDuration (long durationMillis)
                am.setDuration(400);
                //setRepeatCount (int repeatCount)
                am.setRepeatCount(4);
                //start jumping
                ivMonster.startAnimation(am);
            }
        });
    }
}
