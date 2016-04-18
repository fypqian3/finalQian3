package com.counter.app.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.TextView;

import com.counter.app.R;
import com.counter.app.lib.srv.PedoEvent;
import com.counter.app.lib.srv.PedoEventService;

import java.util.Calendar;

/**
 * Created by kimpochu on 7/4/16.
 */
public class MeAct extends Activity implements PedoEvent.onPedoEventListener {



    boolean mPedoSrvBound;
    PedoEvent mPedoEvent;
    ServiceConnection mConnection;
    // Call service's method wherever you want
    PedoEventService.PedoSrvBinder mPedoSrvBinder;

    private TextView height, weight, gender, age, bmi, suggestion;
    SharedPreferences sharedPrefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();

        ////////////////////////////////////////*******temp******///////////////////////////////////////////////////
       // boolean gender = true;
        //int age = 10;
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        double weightOfUser = sharedPrefs.getInt("pref_pinfoPersWeight", 0);
        double heightOfUser = sharedPrefs.getInt("pref_pinfoPersHeight", 0) / 100.0;
        int birthday = sharedPrefs.getInt("pref_pinfoPersBirth", 0);
        String genderOfUser = sharedPrefs.getString("pref_pinfoPersGender", "");
        int year = birthday / 10000;
        int month = ((birthday / 100) % 100) + 1;
        int day = birthday % 100;

        int ageOfUser = getAge(year , month , day);
        double bmiOfUser =  weightOfUser / (heightOfUser * heightOfUser);
        weight.setText(String.format("%.2f", weightOfUser) + " kg");
        height.setText(String.format("%.2f", heightOfUser) + " m");
        bmi.setText(String.format("%.1f", bmiOfUser));

        age.setText(String.valueOf(ageOfUser));

        //gender
        if (genderOfUser == "0")
            gender.setText("M");
        else
            gender.setText("F");

        if (weightOfUser == 0) {
            suggestion.setText("Please insert your personal information first");
        }
        else if(ageOfUser < 12) {
            String suggestionOfUser = bmiCalculator(bmiOfUser, ageOfUser, genderOfUser);

            suggestion.setText(suggestionOfUser);
        }
        else
        {
            suggestion.setText("Please check your own BMI reference: https://www3.ha.org.hk/bmi/b5_standard.aspx");
            Linkify.addLinks(suggestion, Linkify.WEB_URLS);
        }
    }

    private int getAge(int year, int month, int day){
        Calendar c = Calendar.getInstance();
        int currYear = c.get(Calendar.YEAR);
        int currMonth = c.get(Calendar.MONTH) + 1;
        int currDay = c.get(Calendar.DATE);
        int age;
        age = currYear - year;
        if(currMonth < month)
            return age;
        else {
            if (currMonth == month)
            {
                if(currDay < day)
                    return age;
                else
                    return age++;
            }
            return age++;
        }

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
            mPedoSrvBound = false;
            unbindService(mConnection);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
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


    private void init() {
        /***** Link View Resources *****/
        height = (TextView) findViewById(R.id.height);
        weight = (TextView) findViewById(R.id.weight);
        gender = (TextView) findViewById(R.id.gender);
        age = (TextView) findViewById(R.id.age);
        bmi = (TextView) findViewById(R.id.bmi);
        suggestion = (TextView) findViewById(R.id.suggestion);
        /***** Set Parameters *****/
        // For determine whether current activity is connecting to service or not
        mPedoSrvBound = false;
        // Get shared preference
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
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

    private String bmiCalculator(double bmi, int age, String gender){
        //  user is a male
        if (gender == "0") {
            if (age == 6 || age == 7) {
                if (bmi < 13.5)
                    return "Underweight, you need to consume more calories";
                else if (bmi < 17)
                    return "Healthy weight, keep it";
                else if (bmi < 18.5)
                    return "Overweight, keep using 'counter and monster'";
                else
                    return "Obesity, please control your weight!!! SET A DAILY TARGET";
            } else if (age == 8) {
                if (bmi < 13.5)
                    return "Underweight, you need to consume more calories";
                else if (bmi < 18)
                    return "Healthy weight, keep it";
                else if (bmi < 20)
                    return "Overweight, keep using 'counter and monster'";
                else
                    return "Obesity, please control your weight!!! SET A DAILY TARGET";

            } else if (age == 9) {
                if (bmi < 14)
                    return "Underweight, you need to consume more calories";
                else if (bmi < 18.5)
                    return "Healthy weight, keep it";
                else if (bmi < 21)
                    return "Overweight, keep using 'counter and monster'";
                else
                    return "Obesity, please control your weight!!! SET A DAILY TARGET";
            } else if (age == 10) {
                if (bmi < 14.5)
                    return "Underweight, you need to consume more calories";
                else if (bmi < 19)
                    return "Healthy weight, keep it";
                else if (bmi < 22)
                    return "Overweight, keep using 'counter and monster'";
                else
                    return "Obesity, please control your weight!!! SET A DAILY TARGET";
            } else if (age == 11) {
                if (bmi < 14.5)
                    return "Underweight, you need to consume more calories";
                else if (bmi < 20)
                    return "Healthy weight, keep it";
                else if (bmi < 23)
                    return "Overweight, keep using 'counter and monster'";
                else
                    return "Obesity, please control your weight!!! SET A DAILY TARGET";
            } else
                return "";
        }else{
            //User is female
            if(age ==6)
            {
                if(bmi <13.5)
                    return "Underweight, you need to consume more calories";
                else if (bmi < 17)
                    return "Healthy weight, keep it";
                else if (bmi < 18.8)
                    return "Overweight, keep using 'counter and monster'";
                else
                    return "Obesity, please control your weight!!! SET A DAILY TARGET";
            }else if(age ==7)
            {
                if(bmi <13.5)
                    return "Underweight, you need to consume more calories";
                else if (bmi < 17.5)
                    return "Healthy weight, keep it";
                else if (bmi < 19.5)
                    return "Overweight, keep using 'counter and monster'";
                else
                    return "Obesity, please control your weight!!! SET A DAILY TARGET";

            }else if(age ==8)
            {
                if(bmi <13.5)
                    return "Underweight, you need to consume more calories";
                else if (bmi < 18.4)
                    return "Healthy weight, keep it";
                else if (bmi < 20.7)
                    return "Overweight, keep using 'counter and monster'";
                else
                    return "Obesity, please control your weight!!! SET A DAILY TARGET";

            }else if(age ==9)
            {
                if(bmi <13.7)
                    return "Underweight, you need to consume more calories";
                else if (bmi < 19)
                    return "Healthy weight, keep it";
                else if (bmi < 21.7)
                    return "Overweight, keep using 'counter and monster'";
                else
                    return "Obesity, please control your weight!!! SET A DAILY TARGET";
            }else if(age ==10)
            {
                if(bmi <14)
                    return "Underweight, you need to consume more calories";
                else if (bmi < 20)
                    return "Healthy weight, keep it";
                else if (bmi < 23)
                    return "Overweight, keep using 'counter and monster'";
                else
                    return "Obesity, please control your weight!!! SET A DAILY TARGET";
            }else if(age ==11)
            {
                if(bmi <14.4)
                    return "Underweight, you need to consume more calories";
                else if (bmi < 21)
                    return "Healthy weight, keep it";
                else if (bmi < 24)
                    return "Overweight, keep using 'counter and monster'";
                else
                    return "Obesity, please control your weight!!! SET A DAILY TARGET";
            }
        }
        return "";
    }

}
