package com.cmpe277.mobileninjas.eventshare.Images.Upload;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.cmpe277.mobileninjas.eventshare.Database;

import java.util.Calendar;

public class ServiceAlarmSetter extends WakefulBroadcastReceiver {
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, ImageUploadService.class);
        Log.d("PM", "Alarm started");
        startWakefulService(context, service);
    }

    public void setAlarm(Context context, String eventId, int hours, int mins) {
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ServiceAlarmSetter.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, mins);

        Database db = new Database(context);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
        Log.d("PM", "Image upload alarm set");
        db.updateAlarmFlag(eventId, "Current");

        //enables DeviceBootReceiver which is set to false in the manifest
        ComponentName receiver = new ComponentName(context, DeviceBootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    //call when the app is getting uninstalled
    public void cancelAlarm(Context context) {
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }

        ComponentName receiver = new ComponentName(context, DeviceBootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }
}