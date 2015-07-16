package io.wyrmise.jumpmanga.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

public class BootReceiver extends BroadcastReceiver {
    private SharedPreferences prefs;
    public void onReceive(Context context, Intent intent) {
        prefs = context.getSharedPreferences("Alarm", context.MODE_PRIVATE);
        if(!prefs.getBoolean("Alarm", false)) {
            Intent i = new Intent(context, FetchLatestService.class);
            PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.cancel(pi); // cancel any existing alarms
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_DAY,
                    AlarmManager.INTERVAL_DAY, pi);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("Alarm", true);
            editor.commit();
        }
    }
}
