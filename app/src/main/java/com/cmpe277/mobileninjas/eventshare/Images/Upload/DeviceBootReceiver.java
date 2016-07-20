package com.cmpe277.mobileninjas.eventshare.Images.Upload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.cmpe277.mobileninjas.eventshare.Database;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DeviceBootReceiver extends BroadcastReceiver {
    ServiceAlarmSetter alarm = new ServiceAlarmSetter();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            Database db = new Database(context);
            Cursor cursor = db.getCurrentEvent();
            if(cursor.getCount() > 0) {
                cursor.moveToNext();
                String eventId = cursor.getString(cursor.getColumnIndex(db.event_id));
                String endTime = cursor.getString(cursor.getColumnIndex(db.end_date));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                Calendar cal = Calendar.getInstance();
                try {
                    cal.setTime(sdf.parse(endTime));
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
                alarm.setAlarm(context, eventId, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
                Log.d("PM", "Restarted service after device restart");
            }
        }
    }
}