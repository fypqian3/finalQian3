package fyp.qian3.Service;

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

public class PedoSrv extends Service {

    private static final String TAG = "Qian3_Service";

    // Binder given to clients
    private final IBinder mBinder = new PedoSrvBinder();
    // Sensor event listener
    private PedoEventListener mPedoEventListener;
    private SensorManager mSensorManager;

    // Indicates if service is on or off
    private static boolean SrvFlag = false;

    private static int threshold;
    private static int CurrStep;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Srv onCreate()");

        SrvFlag = true;

        // Load settings and data
        load();

        // Sensor monitor
        mPedoEventListener = new PedoEventListener(this);
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
    private void load() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        threshold = Integer.parseInt(sharedPrefs.getString("pref_genPedoSens", "10"));
        // Load from temp data since service is shut down when recent active app list is cleared
        CurrStep = sharedPrefs.getInt("temp_currSteps", 0);
    }

    /**
     *Description:     To enable accelerometer listener
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
    private class PedoEventListener implements SensorEventListener {
        // Temporal Data
        private float prevY;
        private float currY;

        private PedoEventListener(Context context) {
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

            if (Math.abs(currY-prevY)>threshold) {
                CurrStep++;
            }

            prevY = y;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    /***** Methods For Clients *****/

    /**
     * Description:     Used for client Binder.
     */
    public class PedoSrvBinder extends Binder {
        public PedoSrv getService() {
            return PedoSrv.this;
        }
    }

    public static boolean getSrvState() {
        return SrvFlag;
    }

    public static int getStepNumber() {
        return CurrStep;
    }
}
