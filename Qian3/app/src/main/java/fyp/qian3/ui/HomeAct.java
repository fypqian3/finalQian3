package fyp.qian3.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import fyp.qian3.R;
import fyp.qian3.lib.srv.PedoEvent;
import fyp.qian3.lib.srv.PedoEventService;

public class HomeAct extends Activity implements PedoEvent.onPedoEventListener {

    boolean mPedoSrvBound;
    SharedPreferences sharedPrefs;
    PedoEvent mPedoEvent;
    ServiceConnection mConnection;
    // Binder is used to call service function
    PedoEventService.PedoSrvBinder mPedoSrvBinder;

    Button btnSetting;
    TextView tvCurrStep;

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

    private void init() {
        /***** Link View Resources *****/
        btnSetting = (Button) findViewById(R.id.btnHomeSetting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeAct.this, fyp.qian3.ui.SettingAct.class));
            }
        });

        tvCurrStep = (TextView) findViewById(R.id.tvHomeCurrStep);

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
    }
}
