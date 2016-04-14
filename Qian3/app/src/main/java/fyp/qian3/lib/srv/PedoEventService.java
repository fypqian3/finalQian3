package fyp.qian3.lib.srv;

// Static parameters for PedoEventService

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;

import fyp.qian3.R;
import fyp.qian3.lib.db.Database;
import fyp.qian3.ui.HomeAct;


public class PedoEventService extends Service implements PedoEvent.onPedoEventListener {

    private static final String TAG = "Qian3_Service";
    private static int CURR_DATE;

    // Binder given to clients
    private final IBinder mBinder = new PedoSrvBinder();

    // Database to store values
    private Database mDatabase;
    private SharedPreferences mSharedPreferences;

    // Broadcast receiver
    private PedoEventReceiver mPedoEventReceiver;

    // Sensor event
    private PedoEvent mPedoEventForSrv;
    private SensorManager mSensorManager;
    private PedoEventDetector mPedoEventDetector;

    // Notification
    // Foreground Notification
    private final int mFGNotifyID = 1;
    private NotificationManager mFGNotificationMgr;
    private Notification.Builder mFGNotificationBuilder;
    private int FGNProgressMax;
    // Background Notification


    // Indicates if service is on or off
    private boolean SrvFlag = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Srv onCreate()");

        init();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Srv onDestroy()");

        SrvFlag = false;
        if (mPedoEventDetector != null) {
            mSensorManager.unregisterListener(mPedoEventDetector);
        }
        updateDatabase();
        mPedoEventReceiver.unRegisterAlarm();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Srv onBind()");
        updateDatabase();
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // Set mPedoEvent to null, no activity is using onPedoDetected() method
        mPedoEventDetector.setPedoEvent(null);
        return false;
    }

    @Override
    public void onPedoDetected() {
        if (mFGNotificationBuilder !=null) {
            onFGNotificationChanged();
            mFGNotificationMgr.notify(mFGNotifyID, mFGNotificationBuilder.build());
        }
        //updateDatabase();
    }


    // Initialization
    private void init() {
        SrvFlag = true;
        mDatabase = Database.getInstance(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication());

        // Broadcast receiver
        mPedoEventReceiver = new PedoEventReceiver();

        mPedoEventDetector = new PedoEventDetector(this);
        mPedoEventForSrv = new PedoEvent(this);
        mPedoEventDetector.setPedoEventForSrv(mPedoEventForSrv);
        // Sensor monitor
        registerListener();

        if (mSharedPreferences.getBoolean("pref_genOtherNotif", false)) {
            setFGNotification();
            mFGNotificationMgr.notify(mFGNotifyID, mFGNotificationBuilder.build());
        }

        CURR_DATE = mSharedPreferences.getInt("data_currDate", 0);

        // If data_currDate does not exist ( == 0 ), load date from system
        if (CURR_DATE == 0) {
            CURR_DATE = mDatabase.cvtCalendarToID(Calendar.getInstance());
            mSharedPreferences.edit().putInt("data_currDate", CURR_DATE).commit();
        } else if (CURR_DATE != mDatabase.cvtCalendarToID(Calendar.getInstance())) {
            // Else check if CURR_DATE is same as system date, if not, save current steps into database with old date
            // and start with new record of steps
            onDateChanged();
        }

        mPedoEventReceiver.registerAlarm(this);

    }

    private void updateDatabase() {
        mDatabase.setSteps(CURR_DATE, mPedoEventDetector.getCurrentStep());
    }

    private void onDateChanged() {
        updateDatabase();
        CURR_DATE = mDatabase.cvtCalendarToID(Calendar.getInstance());
        mPedoEventDetector.setCurrentStep(0);
    }

    private void setFGNotification() {
        // Progress bar setting
        FGNProgressMax = mSharedPreferences.getInt("data_goalSteps", 8000);

        final int requestCode = mFGNotifyID;
        final int flags = PendingIntent.FLAG_CANCEL_CURRENT;
        // When foreground notification clicked, go to HomeAct
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(), requestCode, new Intent(this, HomeAct.class), flags);

        mFGNotificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mFGNotificationBuilder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_srv_notif)
                .setContentTitle(getResources().getString(R.string.notif_FGctTitle))
                .setContentIntent(pendingIntent)
                //.setAutoCancel(true)
                .setOngoing(true);

        // setContentText and setProgress
        onFGNotificationChanged();
    }

    private void onFGNotificationChanged() {
        mFGNotificationBuilder
                .setContentText(
                        String.valueOf(mPedoEventDetector.getCurrentStep() * 100 / FGNProgressMax)
                        + getResources().getString(R.string.notif_FGctText_HasFinished)
                        + "  ["
                        + String.valueOf(mPedoEventDetector.getCurrentStep())
                        + "/"
                        + String.valueOf(FGNProgressMax)
                        + "]")
                .setProgress(FGNProgressMax, mPedoEventDetector.getCurrentStep(), false);
    }

    // To enable accelerometer listener
    private void registerListener() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mPedoEventDetector,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    /***** Clients' methods *****/
    public class PedoSrvBinder extends Binder {
        public PedoEventService getService() {
            return PedoEventService.this;
        }
        public void reloadSrvSensitive() {
            mPedoEventDetector.reloadDetectorSensitive();
        }
        public void reloadSrvGoalSteps() {
            FGNProgressMax = mSharedPreferences.getInt("data_goalSteps", 8000);
            onFGNotificationChanged();
        }
        public void showFGNotification() {
            if (mFGNotificationBuilder == null) {
                setFGNotification();
            }
                mFGNotificationMgr.notify(mFGNotifyID, mFGNotificationBuilder.build());
        }
        public void hideFGNotification() {
            mFGNotificationMgr.cancel(mFGNotifyID);
            mFGNotificationBuilder = null;
        }

        public void setPedoEvent(PedoEvent pe) {
            mPedoEventDetector.setPedoEvent(pe);
        }
        public int getCurrStep() {
            return mPedoEventDetector.getCurrentStep();
        }
        public void reset() {
            mPedoEventDetector.setCurrentStep(0);
        }
    }

}
