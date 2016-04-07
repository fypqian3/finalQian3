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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import fyp.qian3.R;
//import fyp.qian3.lib.srv.PedoEvent;
import fyp.qian3.lib.srv.PedoEventService;

public class HomeAct extends Activity {
    boolean mPedoSrvBound = false;
    SharedPreferences sharedPrefs;

    Button btnSetting;
    // Test
    Button btnUpdate;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initItem();
        initSetting();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, PedoEventService.class), mConnection, Context.BIND_AUTO_CREATE);

        // Abandon as it's implemented in SettingAct
        //startService(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unbind from the service
        if (mPedoSrvBound) {
            unbindService(mConnection);
            mPedoSrvBound = false;
        }

        // Shut down service if background running is not allowed
        /** Abandon as it's implemented in SettingAct
         if (!sharedPrefs.getBoolean("pref_genPedoSrv", false)) {
         stopService(new Intent(this, StepEventService.class));
         }
         */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initItem() {
        btnSetting = (Button) findViewById(R.id.btnHomeSetting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeAct.this, fyp.qian3.ui.SettingAct.class);
                startActivity(i);
            }
        });

        // Test
        textView = (TextView) findViewById(R.id.textView);
        btnUpdate = (Button) findViewById(R.id.btnHomeUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPedoSrvBound) {
                    textView.setText(String.valueOf(PedoEventService.getCurrStep()));
                } else {
                    textView.setText("Service Not Bound");
                }
            }
        });
    }



    private void initSetting() {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    // Defines callbacks for service binding, passed to bindService()
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            PedoEventService.PedoSrvBinder binder = (PedoEventService.PedoSrvBinder) service;
            mPedoSrvBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mPedoSrvBound = false;
        }
    };
}
