package fyp.qian3.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import fyp.qian3.R;
import fyp.qian3.lib.srv.PedoSrv;

public class HomeAct extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    boolean mPedoSrvBound = false;
    SharedPreferences sharedPrefs;

    Button btnSetting;
    // Test
    Button btnUpdate;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        initItem();
        initSetting();
        initDrawer();
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
        navigationView.setNavigationItemSelectedListener(this);
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
    /*@Override !---Setting----!
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }*/
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_about) {
            Intent i=new Intent();
            i.setClass(this,StatsAct.class);
            startActivity(i);
            // Handle the camera action
        } else if (id == R.id.nav_achievement) {
            Intent i=new Intent();
            startActivity(this,);
        } else if (id == R.id.) {
            Intent i=new Intent();
            i.setClass(this,.class);
            startActivity(i);
        } else if (id == R.id.nav_goal) {
            Intent i=new Intent();
            i.setClass(this,GoalAct.class);
            startActivity(i);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_home) {
            Intent i=new Intent();
            i.setClass(this,HomeAct.class);
            startActivity(i);
        }else if (id == R.id.nav_history) {
            Intent i=new Intent();
            i.setClass(this,HistoryAct.class);
            startActivity(i);
        }else if (id == R.id.nav_chart) {
            Intent i=new Intent();
            i.setClass(this,StatsAct.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, PedoSrv.class), mConnection, Context.BIND_AUTO_CREATE);

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
         stopService(new Intent(this, PedoSrv.class));
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
                    textView.setText(String.valueOf(PedoSrv.getCurrStep()));
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
            PedoSrv.PedoSrvBinder binder = (PedoSrv.PedoSrvBinder) service;
            mPedoSrvBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mPedoSrvBound = false;
        }
    };
}
