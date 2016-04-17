package fyp.qian3.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.List;

import fyp.qian3.R;
import fyp.qian3.lib.pref.NumberPickerPreference;
import fyp.qian3.lib.srv.PedoEventService;

public class SettingAct extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

        // For service connection
        private boolean mPedoSrvBound;
        private PedoEventService.PedoSrvBinder mPedoSrvBinder;
        private ServiceConnection mConnection;

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_gen);
            // Set Default Values
            PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_gen, false);

            // For service connection
            mPedoSrvBound = false;
            mConnection = new ServiceConnection() {
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

            findPreference("pref_genPedoSrv").setOnPreferenceChangeListener(this);
            findPreference("pref_genPedoSrvNotif").setOnPreferenceChangeListener(this);
            findPreference("pref_genPedoSens").setOnPreferenceChangeListener(this);
            findPreference("pref_genOtherMusic").setOnPreferenceChangeListener(this);
        }

        @Override
        public void onResume() {
            super.onResume();
            getActivity().bindService(new Intent(getActivity(), PedoEventService.class), mConnection, Context.BIND_AUTO_CREATE);
        }

        @Override
        public void onPause() {
            super.onPause();
            // Reload service setting when SettingAct is destroyed
            mPedoSrvBinder.reloadSrvSensitive();
            // Unbind from the service
            if (mPedoSrvBound) {
                getActivity().unbindService(mConnection);
                mPedoSrvBound = false;
            }
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
                case "pref_genPedoSrvNotif":
                    if ((boolean) newValue) {
                        mPedoSrvBinder.showFGNotification();
                    } else {
                        mPedoSrvBinder.hideFGNotification();
                    }
                    break;
                case "pref_genPedoSens":
                    Log.v("Key_genPedoSensPref", Integer.toString((Integer) newValue));
                    break;
                case "pref_genOtherMusic":
                    if ((boolean) newValue) {
                        //getActivity().startService(new Intent(getActivity(), BackgroundMusicService.class));
                    } else {
                        //getActivity().stopService(new Intent(getActivity(), BackgroundMusicService.class));
                    }
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
            // Set Default Values
            PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_pinfo, false);

            ListPreference pref_pinfoPersGender = (ListPreference) findPreference("pref_pinfoPersGender");
            int index = pref_pinfoPersGender.findIndexOfValue(sharedPrefs.getString("pref_pinfoPersGender", ""));
            pref_pinfoPersGender.setSummary(
                    index >= 0 ? pref_pinfoPersGender.getEntries()[index] : null
            );
            pref_pinfoPersGender.setOnPreferenceChangeListener(this);
            Preference pref_pinfoPersBirth = findPreference("pref_pinfoPersBirth");
            int birthday = sharedPrefs.getInt("pref_pinfoPersBirth", 0);
            pref_pinfoPersBirth.setSummary(birthday/10000 + "/" + ((birthday/100)%100+1) + "/" + birthday%100);
            pref_pinfoPersBirth.setOnPreferenceChangeListener(this);
            Preference pref_pinfoPersHeight = findPreference("pref_pinfoPersHeight");
            pref_pinfoPersHeight.setSummary(Integer.toString(sharedPrefs.getInt("pref_pinfoPersHeight", 0)) + " cm");
            pref_pinfoPersHeight.setOnPreferenceChangeListener(this);
            Preference pref_pinfoPersWeight = findPreference("pref_pinfoPersWeight");
            pref_pinfoPersWeight.setSummary(Integer.toString(sharedPrefs.getInt("pref_pinfoPersWeight", 0)) + " kg");
            pref_pinfoPersWeight.setOnPreferenceChangeListener(this);
            Preference pref_pinfoPersStepLength = findPreference("pref_pinfoPersStepLength");
            pref_pinfoPersStepLength.setSummary(Integer.toString(sharedPrefs.getInt("pref_pinfoPersStepLength", 0)) + " cm");
            pref_pinfoPersStepLength.setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            Log.v("PersonalInfoPref", "PersonalInfoPref Changed");
            Log.v("Key_PersonalInfoPref", pref.getKey());
            switch (pref.getKey()) {
                case "pref_pinfoPersGender":
                    pref.setSummary(((ListPreference) pref).getEntries()[((ListPreference) pref).findIndexOfValue(newValue.toString())]);
                    break;
                case "pref_pinfoPersBirth":
                    pref.setSummary((Integer) newValue/10000 + "/" + (((Integer) newValue/100)%100+1) + "/" + (Integer) newValue%100);
                    break;
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
