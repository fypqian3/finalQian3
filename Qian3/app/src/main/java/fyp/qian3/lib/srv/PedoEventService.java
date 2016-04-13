package fyp.qian3.lib.srv;

// Static parameters for PedoEventService

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import fyp.qian3.R;


public class PedoEventService extends Service implements PedoEvent.onPedoEventListener {

    private static int TARGET = 10000;

    private static final String TAG = "Qian3_Service";
    // Binder given to clients
    private final IBinder mBinder = new PedoSrvBinder();

    // Database to store values
    //private Database db;

    // Sensor event
    private PedoEvent mPedoEventForSrv;
    private SensorManager mSensorManager;
    private PedoEventDetector mPedoEventDetector;

    // Notification
    final int notifyID = 1; // 通知的識別號碼
    private Notification mForeGroundNotification;
    NotificationManager mNotificationManager;
    Notification.Builder mBuilder;

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
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Srv onBind()");

        setNotifBar(intent);
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // Set mPedoEvent to null
        mPedoEventDetector.setPedoEvent(null);
        return false;
    }

    @Override
    public void onPedoDetected() {
        if (mBuilder!=null) {
            int progressMax = TARGET; // 進度條的最大值，通常都是設為100。若是設為0，且indeterminate為false的話，表示不使用進度條
            int progress = mPedoEventDetector.getCurrentStep(); // 進度值
            mBuilder.setContentText(String.valueOf(progress) + "/" + String.valueOf(progressMax) + "  " + String.valueOf(progress * 100 / progressMax) + "% has finished!");
            mForeGroundNotification = mBuilder.build(); // 建立通知
            mNotificationManager.notify(notifyID, mForeGroundNotification); // 發送通知
        }
    }


    /***** Initialization *****/
    private void init() {
        SrvFlag = true;

        mPedoEventDetector = new PedoEventDetector(this);
        mPedoEventForSrv = new PedoEvent(this);
        mPedoEventDetector.setPedoEventForSrv(mPedoEventForSrv);
        // Sensor monitor
        registerListener();
    }

    private void setNotifBar(Intent intent) {

        // Progress bar setting
        int progressMax = TARGET; // 進度條的最大值，通常都是設為100。若是設為0，且indeterminate為false的話，表示不使用進度條
        int progress = mPedoEventDetector.getCurrentStep(); // 進度值
        final boolean indeterminate = false; // 是否為不確定的進度，如果不確定的話，進度條將不會明確顯示目前的進度。若是設為false，且progressMax為0的話，表示不使用進度條

        final int requestCode = notifyID; // PendingIntent的Request Code
        final int flags = PendingIntent.FLAG_CANCEL_CURRENT;
        final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), requestCode, intent, flags); // 取得PendingIntent

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務
        mBuilder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_srv_notif)
                .setContentTitle("Step Progress")
                .setContentText(String.valueOf(progress) + "/" + String.valueOf(progressMax) + "  " + String.valueOf(progress * 100 / progressMax) + "% has finished!")
                .setProgress(progressMax, progress, indeterminate)
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), requestCode, intent, flags))
                .setAutoCancel(true)
                .setOngoing(true);
        mForeGroundNotification = mBuilder.build(); // 建立通知
        mNotificationManager.notify(notifyID, mForeGroundNotification); // 發送通知
    }

    /***** To enable accelerometer listener *****/
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
        public void reloadServiceSetting() {
            mPedoEventDetector.reloadDetectorSetting();
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
