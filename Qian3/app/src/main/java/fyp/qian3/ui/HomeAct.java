package fyp.qian3.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.SoundPool;
import android.os.Build;
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

import android.util.AttributeSet;
import android.media.AudioAttributes;
import android.media.AudioManager;
import java.util.HashMap;

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
    TextView calories;
    TextView distance;
    ImageButton stat;
    ImageView ivMonster;
    //Ryan
    PieModel sliceGoal, sliceCurrent;
    PieChart pcStep;
    //sam
    SoundPool mPool;
    private SoundPool mSoundPool;
    //private HashMap<Integer, Integer> mSoundPoolMap;
    private AudioManager mAudioManager;
    private int id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // for soundpool
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            mSoundPool = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }
        // mSoundPool.load(this,R.raw.monsterlaugh);
        // int music = mSoundPool.load(this, R.raw.monsterlaugh, 1);
        //mPool.play(music, 1, 1, 1, 0, 1.0f);
        //load the sound , 1 mean priority
        id =  mSoundPool.load(getApplicationContext(), R.raw.monsterlaugh, 1);


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

            Animation am = new TranslateAnimation(0.0f, 0f, 0.0f, -40.0f);
            //setDuration (long durationMillis)
            am.setDuration(100);
            //setRepeatCount (int repeatCount)
            am.setRepeatCount(0);
            //start jumping
            ivMonster.startAnimation(am);

            double weight = sharedPrefs.getInt("pref_pinfoPersWeight", 0);
            double distances = sharedPrefs.getInt("pref_pinfoPersStepLength", 0)* mPedoSrvBinder.getCurrStep() * 0.00001;
            double caloriesBuried = 47 * distances * weight / 60;
            calories.setText(String.format("%.2f", caloriesBuried) + " cal");
            distance.setText(String.format("%.2f", distances) + " km");
            updatePie(mPedoSrvBinder.getCurrStep(),10000);
        } else {
            Log.e("HomeAct", "Error: Service not bound!");
        }
    }
    //new add to show today date
    private void setDate() {
        //calendar for getting today date and weekdad
        Calendar mCalendar = Calendar.getInstance();
        int weekDay = mCalendar.get(Calendar.DAY_OF_WEEK);
        int month = mCalendar.get(Calendar.MONTH) + 1;
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
        int year= mCalendar.get(Calendar.YEAR);


        tvDate.setText(day + " / "+ month + " / " + year);
        String week_day_str = new String();
        switch (weekDay) {
            case Calendar.SUNDAY:
                week_day_str = getString(R.string.home_sunday);
                break;

            case Calendar.MONDAY:
                week_day_str = getString(R.string.home_monday);
                break;

            case Calendar.TUESDAY:
                week_day_str = getString(R.string.home_tuesday);
                break;

            case Calendar.WEDNESDAY:
                week_day_str = getString(R.string.home_wednesday);
                break;

            case Calendar.THURSDAY:
                week_day_str = getString(R.string.home_thursday);
                break;

            case Calendar.FRIDAY:
                week_day_str = getString(R.string.home_friday);
                break;

            case Calendar.SATURDAY:
                week_day_str = getString(R.string.home_saturday);
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

        tvWeekDay = (TextView) findViewById(R.id.tvHomeWeekDay);
        tvDate = (TextView) findViewById(R.id.tvHomeDate);
        calories = (TextView) findViewById(R.id.tvCalories);
        distance = (TextView) findViewById(R.id.tvDistanceWalked);
        pcStep = (PieChart) findViewById(R.id.piechart);

        // slice for the steps taken today
        sliceCurrent = new PieModel("Current Steps", 0, Color.parseColor("#99CC00"));
        pcStep.addPieSlice(sliceCurrent);

        // slice for the "missing" steps until reaching the goal
        //assume is 10
        sliceGoal = new PieModel("Steps remained", 10 , Color.parseColor("#CC0000"));
        pcStep.addPieSlice(sliceGoal);

        pcStep.setUsePieRotation(true);
        pcStep.startAnimation();

        tvWeekDay = (TextView) findViewById(R.id.tvHomeWeekDay);
        tvDate = (TextView) findViewById(R.id.tvHomeDate);


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
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                //calories.setText(String.valueOf(mPedoSrvBinder.getCurrStep()));
               // updatePie(mPedoSrvBinder.getCurrStep(), 10);
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
        ivMonster = (ImageView) findViewById(R.id.homeMonster);
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
                //laugh when click using soundpool (id of music, leftVol , RightVol, priority, loop(0 or 1 ) , rate 0.5 -2)
                mSoundPool.play(id, 1f, 1f, 1, 0, 2);
            }
        });
    }
    //udatePie function
    private void updatePie(int currStep, int targetStep){
        sliceCurrent.setValue(currStep);

        if(targetStep - currStep > 0)
        {
            if (pcStep.getData().size() == 1){
                pcStep.addPieSlice(sliceGoal);
            }
            sliceGoal.setValue(targetStep - currStep);
        }
        else{
            pcStep.clearChart();
            pcStep.addPieSlice(sliceCurrent);
        }
        pcStep.update();
    }
}
