package fyp.qian3.lib.srv;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;

import fyp.qian3.R;
import fyp.qian3.lib.db.Database;
import fyp.qian3.ui.HomeAct;

public class PedoEventReceiver extends BroadcastReceiver {
    private static int ALARM_NUM = 3;
    private static final int ALARMRC_DBUPDATE = 0;
    private static final int ALARMRC_1 = 1;
    private static final int ALARMRC_2 = 2;
    AlarmManager mAlarmManager;
    PendingIntent mPendingIntent[] = new PendingIntent[ALARM_NUM];

    private Database mDatabase;
    private static int CURR_DATE;

    private PedoEventDetector mPedoEventDetector;

    private final int mBGNotifyID = 1;
    private NotificationManager mBGNotificationMgr;
    private Notification.Builder mBGNotificationBuilder;

    //public PedoEventReceiver () {}

    public PedoEventReceiver (Context context, PedoEventDetector ped) {
        mDatabase = Database.getInstance(context);
        mPedoEventDetector = ped;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int percentFinished = mSharedPreferences.getInt("data_currSteps", 0) * 100
                / mSharedPreferences.getInt("data_goalSteps", 8000);

        Bundle bData = intent.getExtras();
        switch ((String) bData.get("alarm_msg")) {
            case "first_remind":
                if (percentFinished < 30) {
                    String ctTitle = context.getResources().getString(R.string.notif_BGctTitle_general);
                    String ctText = String.valueOf(percentFinished) +
                            context.getResources().getString(R.string.notif_BGctText_general);
                    setBGNotification(ctTitle, ctText, context, intent);
                    mBGNotificationMgr.notify(mBGNotifyID, mBGNotificationBuilder.build());
                }
                break;
            case "second_remind":
                if (percentFinished < 70) {
                    String ctTitle = context.getResources().getString(R.string.notif_BGctTitle_general);
                    String ctText = String.valueOf(percentFinished) +
                            context.getResources().getString(R.string.notif_BGctText_general);
                    setBGNotification(ctTitle, ctText, context, intent);
                    mBGNotificationMgr.notify(mBGNotifyID, mBGNotificationBuilder.build());
                }
                break;
            case "database_update":
                updateDB(context);
                // register alarms for next day
                registerGeneralAlarm(context, 1);
                registerDBUpdateAlarm(context, 1);
                break;
        }
    }

    public void updateDB(Context context) {
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        CURR_DATE = mSharedPreferences.getInt("data_currDate", 0);
        int sysDate = mDatabase.cvtCalendarToID(Calendar.getInstance());

        if (CURR_DATE != sysDate) {
            registerGeneralAlarm(context, 0);
            if (CURR_DATE == 0) {
                // First time to open app
                CURR_DATE = sysDate;
                mSharedPreferences.edit().putInt("data_currDate", CURR_DATE).commit();
            } else {
                // It's a new day (maybe 2 or more days later)
                mDatabase.setSteps(CURR_DATE, mSharedPreferences.getInt("data_currSteps", 0));
                CURR_DATE = sysDate;
                mSharedPreferences.edit().putInt("data_currDate", CURR_DATE).commit();
                // Reset counter
                mPedoEventDetector.setCurrentStep(0);
            }
        } else {
            // Regular store
            mDatabase.setSteps(CURR_DATE, mSharedPreferences.getInt("data_currSteps", 0));
        }
    }

    private void setBGNotification(String contentTitle, String contentText, Context context, Intent intent) {
        final int requestCode = mBGNotifyID;
        final int flags = PendingIntent.FLAG_CANCEL_CURRENT;
        // When foreground notification clicked, go to HomeAct
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, requestCode, new Intent(context, HomeAct.class), flags);

        mBGNotificationMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBGNotificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_srv_notif)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setContentTitle(contentTitle)
                .setContentText(contentText);
    }

    // Cannot place to onCreate() of service, or it will appear every time when you shut down the app
    public void registerGeneralAlarm(Context context, int addDay) {
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar mCal = Calendar.getInstance();
        Intent intentAlarm;

        /***** First Remind *****/
        // Set tomorrow's alarm
        mCal.add(Calendar.DATE, addDay);
        mCal.set(Calendar.HOUR_OF_DAY, 13);
        mCal.set(Calendar.MINUTE, 0);
        mCal.set(Calendar.SECOND, 0);
        intentAlarm = new Intent(context, PedoEventReceiver.class);
        intentAlarm.putExtra("alarm_msg", "first_remind");

        mPendingIntent[ALARMRC_1] = PendingIntent.getBroadcast(context, ALARMRC_1, intentAlarm, PendingIntent.FLAG_ONE_SHOT);

        mAlarmManager.set(AlarmManager.RTC_WAKEUP, mCal.getTimeInMillis(), mPendingIntent[ALARMRC_1]);
        // 1 day = 86400000 ms, repeat everyday
        //mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 0, 5000, mPendingIntent[0]); (NOT WORK)

        /***** Second Remind *****/
        mCal.set(Calendar.HOUR_OF_DAY, 20);
        //mCal.set(Calendar.MINUTE, 28);
        //mCal.set(Calendar.SECOND, 5);
        //intentAlarm = new Intent(context, PedoEventReceiver.class);
        intentAlarm.putExtra("alarm_msg", "second_remind");

        mPendingIntent[ALARMRC_2] = PendingIntent.getBroadcast(context, ALARMRC_2, intentAlarm, PendingIntent.FLAG_ONE_SHOT);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, mCal.getTimeInMillis(), mPendingIntent[ALARMRC_2]);
        // 1 day = 86400000 ms, repeat everyday
        //mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 0, 5000, mPendingIntent[1]); (NOT WORK)
    }

    public void registerDBUpdateAlarm(Context context, int addDay) {
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intentAlarm;
        Calendar mCal = Calendar.getInstance();

        mCal.add(Calendar.DATE, addDay);
        mCal.set(Calendar.HOUR_OF_DAY, 23);
        mCal.set(Calendar.MINUTE, 59);
        mCal.set(Calendar.SECOND, 59);
        intentAlarm = new Intent(context, PedoEventReceiver.class);
        intentAlarm.putExtra("alarm_msg", "database_update");
        mPendingIntent[ALARMRC_DBUPDATE] = PendingIntent.getBroadcast(context, ALARMRC_DBUPDATE, intentAlarm, PendingIntent.FLAG_ONE_SHOT);
        mAlarmManager.set(AlarmManager.RTC, mCal.getTimeInMillis(), mPendingIntent[ALARMRC_DBUPDATE]);
    }


    //private void registerFirstAlarm(Context context, int addDay) {}

    //private void registerSecondAlarm(Context context, int addDay) {}

    public void unRegisterAlarm() {
        for (int i=0; i< ALARM_NUM; i++) {
            mAlarmManager.cancel(mPendingIntent[i]);
            Log.i("PedoEventReceiver", "unregAlarm" + String.valueOf(i));
        }
    }

}
