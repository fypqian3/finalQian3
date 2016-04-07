package fyp.qian3.lib.srv;

// Static parameters for PedoEventService

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;


public class PedoEventService extends Service {

    private static int CURR_STEP;
    private static final String TAG = "Qian3_Service";
    private PedoEvent mPedoEvent;

    // Database to store values
    //private Database db;

    // Binder given to clients
    private final IBinder mBinder = new PedoSrvBinder();
    // Sensor event listener
    private XPedoEventListener mPedoEventListener;
    private SensorManager mSensorManager;

    // Indicates if service is on or off
    private boolean SrvFlag = false;

    SharedPreferences sharedPrefs;
    private int threshold;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Srv onCreate()");

        SrvFlag = true;

        mPedoEvent = new PedoEvent(new onPedoEventListener() {
        });

        // Load setting & data
        srvLoadData();
        srvLoadSetting();

        // Sensor monitor
        mPedoEventListener = new XPedoEventListener(this);
        enableAcceMeterListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Srv onDestroy()");

        SrvFlag = false;
        if (mPedoEventListener != null) {
            mSensorManager.unregisterListener(mPedoEventListener);
        }
        // TODO: Save result to local storage
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Srv onBind()");

        return mBinder;
    }

    /**
     * Description:     Load settings and data  from shared preference
     */

    private void srvLoadData() {
        // Load from temp data since service is shut down when recent active app list is cleared
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        CURR_STEP = sharedPrefs.getInt("temp_currSteps", 0);
    }

    private void srvLoadSetting() {
        setSensitive(sharedPrefs.getInt("pref_genPedoSens", 10));
    }

    /**
     * Description:     To enable accelerometer listener
     */
    private void enableAcceMeterListener() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mPedoEventListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                mSensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Description:     An inner class for override event listener.
     */
    private class XPedoEventListener implements SensorEventListener {
        // Temporal Data
        private float prevY;
        private float currY;

        private XPedoEventListener(Context context) {
            super();
        }

        /**
         * Description:     Sensor event listener. Change algorithm of detecting steps here
         */
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            currY = y;

            if (Math.abs(currY - prevY) > threshold) {
                CURR_STEP++;

                // While clearing recent app (swipe out), service will restart (why?) and step counter will reset
                //  To resume from unexpected clearing, save current steps while current step is changed
                SharedPreferences.Editor sharedPrefsEditor = sharedPrefs.edit();
                sharedPrefsEditor.putInt("temp_currSteps", getCurrStep());
                sharedPrefsEditor.commit();

                callChangeListener();
            }

            prevY = y;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    private void setSensitive(int newValue) {
        // Smallest value of newValue = 0, but smallest value of threshold = 1, so +1
        threshold = newValue + 1;
    }


    /**
     * Description:     Used for client Binder.
     */
    public class PedoSrvBinder extends Binder {
        public PedoEventService getService() {
            return PedoEventService.this;
        }
    }

    public boolean getSrvState() {
        return SrvFlag;
    }

    public void reloadSrvSetting(Context context) {
        setSensitive(PreferenceManager.getDefaultSharedPreferences(context).getInt("pref_genPedoSens", 10));
    }

    public static int getCurrStep() {
        return CURR_STEP;
    }

}
