package com.counter.app.lib.srv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class BootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_genPedoSrv", false)) {
                context.startService(new Intent(context, PedoEventService.class));
            }
        }
    }
}
