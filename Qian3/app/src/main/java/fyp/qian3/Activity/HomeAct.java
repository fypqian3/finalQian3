package fyp.qian3.Activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import fyp.qian3.R;
import fyp.qian3.Service.PedoSrv;

public class HomeAct extends Activity {
    boolean mPedoSrvBound = false;

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
        Intent intent = new Intent(this, PedoSrv.class);
        // Start Service first
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // While clearing recent app (swipe out), service will restart (why?) and step counter will reset
        //  To resume from unexpected clearing, save current steps while quiting UI
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt("temp_currSteps", PedoSrv.getStepNumber());
        editor.commit();

        // Unbind from the service
        if (mPedoSrvBound) {
            unbindService(mConnection);
            mPedoSrvBound = false;
        }

        // Shut down service if background running is not allowed
        if (!sharedPrefs.getBoolean("pref_genPedoSrv", false)) {
            stopService(new Intent(this, PedoSrv.class));
        }
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
                Intent i = new Intent(HomeAct.this, SettingAct.class);
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
                    textView.setText(String.valueOf(PedoSrv.getStepNumber()));
                } else {
                    textView.setText("Service Not Bound");
                }
            }
        });
    }

    private void initSetting() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        //
    }

    // Defines callbacks for service binding, passed to bindService()
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            PedoSrv.PedoSrvBinder binder = (PedoSrv.PedoSrvBinder) service;
            mPedoSrvBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mPedoSrvBound = false;
        }
    };
}
