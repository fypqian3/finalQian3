package fyp.qian3.lib.srv;


import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class PedoEventDetector implements SensorEventListener {

    private static int CURRENT_SETP;
    private static float SENSITIVITY;

    private float mLastValues[] = new float[3 * 2];
    private float mScale[] = new float[2];
    private float mYOffset;
    private static long end = 0;
    private static long start = 0;

    private float mLastDirections[] = new float[3 * 2];
    private float mLastExtremes[][] = { new float[3 * 2], new float[3 * 2] };
    private float mLastDiff[] = new float[3 * 2];
    private int mLastMatch = -1;

    private PedoEvent mPedoEvent;

    private Context mContext;
    SharedPreferences mSharedPreferences;


    public PedoEventDetector(Context context) {
        super();
        int h = 480;
        mYOffset = h * 0.5f;
        mScale[0] = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        mScale[1] = -(h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        setSensitivity(mSharedPreferences.getInt("pref_genPedoSens", 69));
        // Load from temp data since service is shut down when recent active app list is cleared
        CURRENT_SETP = mSharedPreferences.getInt("temp_curr_steps", 0);
    }

    public void setPedoEvent(PedoEvent pe) {
        mPedoEvent = pe;
    }

    public void setSensitivity(int sens) {
        SENSITIVITY = 10 - (float) (sens+1)/10;
    }

    public void setCurrentStep(int step) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("temp_curr_steps", step);
        editor.commit();
        CURRENT_SETP = step;
    }

    public int getCurrentStep() {
        return CURRENT_SETP;
    }

    public void reloadDetectorSetting() {
        setSensitivity(mSharedPreferences.getInt("pref_genPedoSens", 69));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Log.i(Constant.STEP_SERVER, "StepDetector");
        Sensor sensor = event.sensor;
        // Log.i(Constant.STEP_DETECTOR, "onSensorChanged");
        synchronized (this) {
            if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
            } else {
                int j = (sensor.getType() == Sensor.TYPE_ACCELEROMETER) ? 1 : 0;
                if (j == 1) {
                    float vSum = 0;
                    for (int i = 0; i < 3; i++) {
                        final float v = mYOffset + event.values[i] * mScale[j];
                        vSum += v;
                    }
                    int k = 0;
                    float v = vSum / 3;

                    float direction = (v > mLastValues[k] ? 1: (v < mLastValues[k] ? -1 : 0));
                    if (direction == -mLastDirections[k]) {
                        // Direction changed
                        int extType = (direction > 0 ? 0 : 1); // minumum or
                        // maximum?
                        mLastExtremes[extType][k] = mLastValues[k];
                        float diff = Math.abs(mLastExtremes[extType][k]- mLastExtremes[1 - extType][k]);

                        if (diff > SENSITIVITY) {
                            boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k] * 2 / 3);
                            boolean isPreviousLargeEnough = mLastDiff[k] > (diff / 3);
                            boolean isNotContra = (mLastMatch != 1 - extType);

                            if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
                                end = System.currentTimeMillis();
                                if (end - start > 500) {
                                    Log.i("StepDetector", "CURRENT_SETP:"
                                            + CURRENT_SETP);
                                    CURRENT_SETP++;
                                    mLastMatch = extType;
                                    start = end;
                                }
                            } else {
                                mLastMatch = -1;
                            }
                        }
                        mLastDiff[k] = diff;
                    }
                    mLastDirections[k] = direction;
                    mLastValues[k] = v;

                    if (mPedoEvent != null) {
                        mPedoEvent.callChangeListener();
                    }

                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putInt("temp_curr_steps", CURRENT_SETP);
                    editor.commit();

                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}