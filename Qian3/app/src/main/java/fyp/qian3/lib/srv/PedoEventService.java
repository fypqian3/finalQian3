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

    private static final String TAG = "Qian3_Service";
    // Binder given to clients
    private final IBinder mBinder = new PedoSrvBinder();

    // Database to store values
    //private Database db;

    // Sensor event
    private SensorManager mSensorManager;
    private PedoEventDetector mPedoEventDetector;

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

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // Set mPedoEvent to null
        mPedoEventDetector.setPedoEvent(null);
        return false;
    }

    /***** Initialization *****/
    private void init() {
        SrvFlag = true;

        mPedoEventDetector = new PedoEventDetector(this);

        // Sensor monitor
        registerListener();
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
