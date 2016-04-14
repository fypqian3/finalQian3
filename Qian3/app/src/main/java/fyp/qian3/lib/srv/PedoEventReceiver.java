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

import java.util.Calendar;

import fyp.qian3.R;
import fyp.qian3.ui.HomeAct;

public class PedoEventReceiver extends BroadcastReceiver {
    private int PINumber = 2;
    AlarmManager mAlarmManager;
    PendingIntent mPendingIntent[] = new PendingIntent[PINumber];

    private final int mBGNotifyID = 0;
    private NotificationManager mBGNotificationMgr;
    private Notification.Builder mBGNotificationBuilder;

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

    public void registerAlarm(Context context) {
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar mCal = Calendar.getInstance();
        Intent intentAlarm;

        /***** First Remind *****/
        // Set tomorrow's alarm
        //mCal.add(Calendar.DATE, 1);
        mCal.set(Calendar.HOUR_OF_DAY, 13);
        mCal.set(Calendar.MINUTE, 0);
        mCal.set(Calendar.SECOND, 0);
        intentAlarm = new Intent(context, PedoEventReceiver.class);
        intentAlarm.putExtra("alarm_msg", "first_remind");

        mPendingIntent[0] = PendingIntent.getBroadcast(context, 0, intentAlarm, PendingIntent.FLAG_ONE_SHOT);

        mAlarmManager.set(AlarmManager.RTC_WAKEUP, mCal.getTimeInMillis(), mPendingIntent[0]);

        /***** Second Remind *****/
        mCal.set(Calendar.HOUR_OF_DAY, 20);
        intentAlarm.putExtra("alarm_msg", "first_remind");

        mPendingIntent[1] = PendingIntent.getBroadcast(context, 0, intentAlarm, PendingIntent.FLAG_ONE_SHOT);

        mAlarmManager.set(AlarmManager.RTC_WAKEUP, mCal.getTimeInMillis(), mPendingIntent[1]);
    }

    public void unRegisterAlarm() {
        for (int i=0; i<PINumber; i++) {
            mAlarmManager.cancel(mPendingIntent[i]);
        }
    }

}
