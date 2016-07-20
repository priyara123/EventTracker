package com.cmpe277.mobileninjas.eventshare;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cmpe277.mobileninjas.eventshare.Albums.AlbumDS;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by BhuvanTeja on 4/28/2016.
 */
public class Database extends SQLiteOpenHelper {

    private static final String events_table = "EVENTS_TABLE";
    public static final String event_name = "EVENT_NAME";
    public static final String user_id = "USER_ID";
    public static final String start_date = "START_DATE";
    public static final String end_date = "END_DATE";
    public static final String event_id = "EVENT_ID";
    public static final String _id = "_ID";
    public static final String alarm_flag = "ALARM_FLAG";
    SQLiteDatabase db;

    public Database(Context context) {
        super(context, "EventShare.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE " + events_table + "(" +
                _id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                event_id + " TEXT, " +
                event_name + " TEXT, " +
                user_id + " TEXT, " +
                start_date + " TEXT, " +
                end_date + " TEXT, " +
                alarm_flag + " TEXT" +
                ");";

        db.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long createEvent(String evnt_id, String name, String userID, String start_dt, String end_dt) {

        db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(event_id, evnt_id);
        values.put(event_name, name);
        values.put(user_id, userID);
        values.put(start_date, start_dt);
        values.put(end_date, end_dt);
        values.put(alarm_flag, "Future");
        return db.insertOrThrow(events_table, null, values);
    }

    public String isEventCreated(String loggedInUserID) {

       // createSampleEvents();
        String eventID = null;
        //String loggedInUserID = loggedInUserID;
        db = getReadableDatabase();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String today = dateFormat.format(date);

        //String sql = "select event_id from events_table where datetime(start_date) <= datetime('" + today + "')" + " and datetime(end_date) >= datetime('" + today + "') and user";

        String sql = "select " +event_id +" from " +events_table +" where datetime(" +start_date +") <= datetime(?) and datetime(" +end_date +") >= datetime(?) and " +user_id +" = ?";

        Cursor cursor = db.rawQuery(sql, new String[] {today, today, loggedInUserID});

//        String sql = "select " +event_id +" from " +events_table +" where datetime(" +start_date +") <= datetime(?) and datetime(" +end_date +") >= datetime(?) " ;
//        Cursor cursor = db.rawQuery(sql, new String[] {today, today});

        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            eventID = cursor.getString(cursor.getColumnIndex("EVENT_ID"));
            return eventID;
        }

        return eventID;
    }

    public Cursor getStartedEventDetails(String loggedInUserID) {
        Cursor cursor = null;
        db = getReadableDatabase();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String today = dateFormat.format(date);

        String sql = "select " +event_id +", " +end_date +" from " +events_table +" where datetime(" +start_date +") <= datetime(?) and datetime(" +end_date +") >= datetime(?) and " +user_id +" = ? and " +alarm_flag +" = ?";
        cursor = db.rawQuery(sql, new String[] {today, today, loggedInUserID, "Future"});

        return cursor;
    }

    public void createSampleEvents() {
        db = getReadableDatabase();
        String sql = "select * from " +events_table;
        Cursor cursor = db.rawQuery(sql, null);
        if(cursor.getCount() == 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String today = dateFormat.format(date);
            createEvent("111", "Event One", "prashanth0205@gmail.com",  today, today);
            createEvent("222", "Second Event", "prashanth0205@gmail.com",  today, today);
            createEvent("333", "Third Event", "prashanth0205@gmail.com", today, today);
            createEvent("444", "Event Four", "prashanth0205@gmail.com",  today, today);
        }
    }

    public ArrayList<AlbumDS> registeredEvents(String loggedInUserID) {
        //createSampleEvents();
        db = getReadableDatabase();
        String sql = "select " +event_id +"," +event_name +" from " +events_table +" where "+user_id+"= ?";
        Cursor cursor = db.rawQuery(sql,new String[] {loggedInUserID});
        ArrayList<AlbumDS> eventNames = new ArrayList<AlbumDS>();
        int i = 0;

        while (cursor.moveToNext()){
            AlbumDS eachEvent = new AlbumDS(cursor.getString(cursor.getColumnIndex(event_id)), cursor.getString(cursor.getColumnIndex(event_name)));
            eventNames.add(i++, eachEvent);
        }

        return eventNames;
    }

    public String[] registeredEventIds(String loggedInUserID) {
        db = getReadableDatabase();
        String sql = "select " +event_id +" from " +events_table +" where "+user_id+"= ?";
        Cursor cursor = db.rawQuery(sql, new String[] {loggedInUserID});
        if(cursor.getCount() <= 0) {
            return null;
        }
        String[] eventIds = new String[cursor.getCount()];
        int i = 0;
        while (cursor.moveToNext()){
            eventIds[i++] = cursor.getString(cursor.getColumnIndex(event_id));
        }
        return eventIds;
    }

    //current is the alarmFlag value, which means alarm is set for that event
    public Cursor getCurrentEvent() {
        Cursor cursor = null;
        db = getReadableDatabase();
        String sql = "select " +event_id +", " +start_date +", " +end_date +" from " +events_table +" where " +alarm_flag +" = ?";
        cursor = db.rawQuery(sql, new String[] {"Current"});
        return cursor;
    }

    public void updateAlarmFlag(String currentEventId, String flag) {
        db = getWritableDatabase();
        ContentValues updateValues = new ContentValues();
        updateValues.put(alarm_flag, flag);
        db.update(events_table, updateValues, event_id +" = ?", new String[]{currentEventId});
//        String sql = "update " +events_table +" set " +alarm_flag +" = ? where " +event_id +" = ?";
//        db.execSQL(sql, new String[] {flag, currentEventId});
    }

    public void close() {
        db.close();
    }
}