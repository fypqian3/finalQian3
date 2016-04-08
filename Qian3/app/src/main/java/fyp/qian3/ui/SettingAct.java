package fyp.qian3.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.List;

import fyp.qian3.R;
import fyp.qian3.lib.srv.PedoEventService;

public class SettingAct extends PreferenceActivity {

    boolean mPedoSrvBound = false;
    PedoEventService.PedoSrvBinder mPedoSrvBinder;
    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mPedoSrvBinder = (PedoEventService.PedoSrvBinder) service;
            mPedoSrvBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mPedoSrvBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(new Intent(this, PedoEventService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Reload service setting when SettingAct is destroyed
        mPedoSrvBinder.reloadSrvSetting(getApplication());
        // Unbind from the service
        if (mPedoSrvBound) {
            unbindService(mConnection);
            mPedoSrvBound = false;
        }
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || PersonalInfoPrefFrag.class.getName().equals(fragmentName)
                || GeneralPrefFrag.class.getName().equals(fragmentName);
    }

    public static class GeneralPrefFrag extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_gen);

            Preference pref_genPedoSrv = findPreference("pref_genPedoSrv");
            pref_genPedoSrv.setOnPreferenceChangeListener(this);
            Preference pref_genPedoSens = findPreference("pref_genPedoSens");
            pref_genPedoSens.setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            Log.v("GeneralPref", "GeneralPref Changed");
            Log.v("Key_GeneralPref", pref.getKey());
            switch (pref.getKey()) {
                case "pref_genPedoSrv":
                    if ((boolean) newValue) {
                        getActivity().startService(new Intent(getActivity(),  PedoEventService.class));
                    } else {
                        getActivity().stopService(new Intent(getActivity(),  PedoEventService.class));
                    }
                    break;
                case "pref_genPedoSens":
                    Log.v("Key_genPedoSensPref", Integer.toString((Integer) newValue));
                    /*
                                        if ((Integer) newValue < 7) {
                                            pref.setSummary("High");
                                        } else if ((Integer) newValue < 14) {
                                            pref.setSummary("Middle");
                                        } else {
                                            pref.setSummary("Low");
                                        }
                                        */
                    break;
            }
            return true;
        }
    }

    public static class PersonalInfoPrefFrag extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        SharedPreferences sharedPrefs;

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_pinfo);

            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            Preference pref_pinfoPersHeight = findPreference("pref_pinfoPersHeight");
            if (sharedPrefs.getInt("pref_pinfoPersHeight", 0) != 0) {
                pref_pinfoPersHeight.setSummary(Integer.toString(sharedPrefs.getInt("pref_pinfoPersHeight", 0)) + " cm");
            }
            pref_pinfoPersHeight.setOnPreferenceChangeListener(this);
            Preference pref_pinfoPersWeight = findPreference("pref_pinfoPersWeight");
            if (sharedPrefs.getInt("pref_pinfoPersWeight", 0) != 0) {
                pref_pinfoPersWeight.setSummary(Integer.toString(sharedPrefs.getInt("pref_pinfoPersWeight", 0)) + " kg");
            }
            pref_pinfoPersWeight.setOnPreferenceChangeListener(this);
            Preference pref_pinfoPersStepLength = findPreference("pref_pinfoPersStepLength");
            if (sharedPrefs.getInt("pref_pinfoPersStepLength", 0) != 0) {
                pref_pinfoPersStepLength.setSummary(Integer.toString(sharedPrefs.getInt("pref_pinfoPersStepLength", 0)) + " cm");
            }
            pref_pinfoPersStepLength.setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            Log.v("PersonalInfoPref", "PersonalInfoPref Changed");
            Log.v("Key_PersonalInfoPref", pref.getKey());
            switch (pref.getKey()) {
                case "pref_pinfoPersHeight":
                    pref.setSummary(Integer.toString((Integer) newValue) + " cm");
                    break;
                case "pref_pinfoPersWeight":
                    pref.setSummary(Integer.toString((Integer) newValue) + " kg");
                    break;
                case "pref_pinfoPersStepLength":
                    pref.setSummary(Integer.toString((Integer) newValue) + " cm");
                    break;
            }
            return true;
        }
    }
}
