package com.counter.app.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.counter.app.R;
import com.counter.app.lib.srv.PedoEvent;
import com.counter.app.lib.srv.PedoEventService;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.games.Games;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.util.Calendar;


public class HomeAct extends AppCompatActivity implements PedoEvent.onPedoEventListener, NavigationView.OnNavigationItemSelectedListener,
        Animation.AnimationListener, GoogleApiClient.ConnectionCallbacks,
        OnConnectionFailedListener {

    boolean mPedoSrvBound;
    SharedPreferences sharedPrefs;
    PedoEvent mPedoEvent;
    ServiceConnection mConnection;
    // Binder is used to call service function
    PedoEventService.PedoSrvBinder mPedoSrvBinder;

    CallbackManager callbackManager;

    ShareDialog shareDialog;
    int enemyLevel = 1;
    ImageButton imageButton1;
    ImageButton imageButton2;
    ImageButton imageButton3;

    Animation animBlink;
    ImageView image;

    int diamond;
    private GoogleApiClient mGoogleApiClient;
    // Requestcode to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Uniquetag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool totrack whether the app is already resolving an error
    private boolean mResolvingError = false;
    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    private SoundPool mSoundPool;
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
    private int id;
   // RoundCornerProgressBar bar;
    int progressEnemy = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getApplicationContext());

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_drawer);
        initMusic();
        init();
        initDrawer();

       // enemyShow();
        // monsterEat();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Games.API)
                .addScope(Games.SCOPE_GAMES)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        //   mGoogleApiClient.connect();
        mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

       /* bar = (RoundCornerProgressBar) findViewById(R.id.enemyBar);
        if (enemyLevel == 1) {
            int max = 50000;

            bar.setMax(max);
            // the progress according to the onstepchange like progress=(onstepchange)

        }*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind with the service
        bindService(new Intent(this, PedoEventService.class), mConnection, Context.BIND_AUTO_CREATE);

        if (!mResolvingError) {  // more about this later
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();

        // Unbind from the service
        if (mPedoSrvBound) {
            unbindService(mConnection);
            mPedoSrvBound = false;
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
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
            double distances = sharedPrefs.getInt("pref_pinfoPersStepLength", 0) * mPedoSrvBinder.getCurrStep() * 0.00001;
            double caloriesBuried = 47 * distances * weight / 60;
            calories.setText(String.format("%.2f", caloriesBuried) + " cal");
            distance.setText(String.format("%.2f", distances) + " km");
            updatePie(mPedoSrvBinder.getCurrStep(), 10000);
            //bar.setProgress(++progressEnemy);

        } else

        {
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
        int year = mCalendar.get(Calendar.YEAR);


        tvDate.setText(day + " / " + month + " / " + year);
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

    public void initMusic() {
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
        id = mSoundPool.load(getApplicationContext(), R.raw.monsterlaugh, 1);
    }

    private void init() {


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
        sliceGoal = new PieModel("Steps remained", 10, Color.parseColor("#CC0000"));
        pcStep.addPieSlice(sliceGoal);

        pcStep.setUsePieRotation(true);
        pcStep.startAnimation();

        tvWeekDay = (TextView) findViewById(R.id.tvHomeWeekDay);
        tvDate = (TextView) findViewById(R.id.tvHomeDate);
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
            startService(new Intent(HomeAct.this, com.counter.app.lib.srv.PedoEventService.class));
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
    private void updatePie(int currStep, int targetStep) {
        sliceCurrent.setValue(currStep);

        if (targetStep - currStep > 0) {
            if (pcStep.getData().size() == 1) {
                pcStep.addPieSlice(sliceGoal);
            }
            sliceGoal.setValue(targetStep - currStep);
        } else {
            pcStep.clearChart();
            pcStep.addPieSlice(sliceCurrent);
        }
        pcStep.update();
    }


    private void initDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener) this);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_about)
            enableAboutUs();
        else if (id == R.id.nav_achievement) {
            if (mGoogleApiClient.isConnected())
                startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 5001);

        } else if (id == R.id.nav_share)


            enableShare();
        else if (id == R.id.nav_home)


            startActivity(new Intent(HomeAct.this, com.counter.app.ui.HomeAct.class));
        else if (id == R.id.nav_Timer)


            startActivity(new Intent(HomeAct.this, com.counter.app.ui.CounterAct.class));
        else if (id == R.id.nav_chart)

            startActivity(new Intent(HomeAct.this, com.counter.app.ui.StatsAct.class));
        else if (id == R.id.nav_leaderboard) {

            if (mGoogleApiClient.isConnected())
                startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(
                        mGoogleApiClient), 100);

        } else if (id == R.id.nav_me)
            startActivity(new Intent(HomeAct.this, com.counter.app.ui.MeAct.class));
        else if (id == R.id.nav_setting)
            startActivity(new Intent(HomeAct.this, com.counter.app.ui.SettingAct.class));
        else if (id == R.id.nav_faq) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.google.com.hk"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void enableShare() {
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse("https://developers.facebook.com"))
                .build();

        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        // this part is optional
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("Counter Monster")
                    .setContentDescription(
                            "Hello Every,I am using Counter Monster App")
                    .setContentUrl(Uri.parse("http://developers.facebook.com/android"))
                    .build();

            shareDialog.show(linkContent);
        }
    }

   /* @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }*/

    public void enableAboutUs() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About Us");
        TextView tv = new TextView(this);
        tv.setPadding(10, 10, 10, 10);
        tv.setText(R.string.about_text_links);

        tv.setMovementMethod(LinkMovementMethod.getInstance());
        builder.setView(tv);
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

/*
    public void enemyShow() {
        final Dialog settingsDialog = new Dialog(this);
        imageButton1 = (ImageButton) findViewById(R.id.home);

        imageButton1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                settingsDialog.setContentView(getLayoutInflater().inflate(R.layout.image_layout
                        , null));
                settingsDialog.show();
            }

        });

    }*/

   /* public void monsterEat() {
        imageButton2 = (ImageButton) findViewById(R.id.eat);
        animBlink = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.blink);
        image = (ImageView) findViewById(R.id.monster);

        // set animation listener
        animBlink.setAnimationListener(this);

        imageButton2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                v.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.image_click));

                // start the animation
                image.startAnimation(animBlink);

            }

        });

    }*/


    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }


    @Override
    public void onConnected(Bundle bundle) {

        com.counter.app.ui.AchievementAct.achievementsAndLeaderboard(mGoogleApiClient, getApplicationContext());

    }

    @Override
    public void onConnectionSuspended(int i) {
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
           showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }

    // The rest of this code is all about building the error dialog

    // Creates a dialog for an error message
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), "errordialog");
    }


    public void onDialogDismissed() {
        mResolvingError = false;
    }


    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((HomeAct) getActivity()).onDialogDismissed();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not alreadyconnected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }
}
