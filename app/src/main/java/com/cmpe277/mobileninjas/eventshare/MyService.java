package com.cmpe277.mobileninjas.eventshare;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.cmpe277.mobileninjas.eventshare.Images.Upload.ServiceAlarmSetter;
import com.cmpe277.mobileninjas.eventshare.model.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

/**
 * Created by BhuvanTeja on 4/30/2016.
 */
public class MyService extends Service {

    String tag = "MyService";
    private LatLng currentLocation;
    private boolean islocationSet = false;
    String url;
    private Handler handler = new Handler();
    Database db;
    int count = 0;
    private String userID;
    private String emailID;
    ServiceAlarmSetter alarm;
    String eventID;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        System.out.println("Here in MyService!!!");

        alarm = new ServiceAlarmSetter();

        emailID = MainActivity.getUserID();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        userID=sharedPrefs.getString("USER_ID", emailID);

        //userID = "1"; //for aimulTION

        url = Constants.url;

        db = new Database(MyService.this);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 5000, 0,
                networkLocationListener);

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 10, networkLocationListener);

        //stopSelf();
        updateServerWithCurrentLocation.run();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private Runnable updateServerWithCurrentLocation = new Runnable() {
        public void run() {
            if (isLocationServiceOn()) {

                if (islocationSet) {

                    eventID = db.isEventCreated(userID);

                    //String  eventID ="2";

                    if (eventID == null || eventID.equals(""))
                        count = 0;

                    //String eventID ="2"; for simulation purpose

                    if (eventID != null && !eventID.equals("")) {
                        //Inform server about current location
                        AsyncHttpClient client = new AsyncHttpClient();

                        if (count == 0) {

                            System.out.println("Here creating Notification Manager");
                            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                            Context context = MyService.this;

                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                                    .setSmallIcon(android.R.drawable.stat_notify_more)
                                    .setContentTitle("A new event of yours has started!!")
                                    .setContentText("Participate in the event!!");

                            Intent intent = new Intent(context, MyService.class);
                            PendingIntent pending = PendingIntent.getActivity(context, 0, intent, 0);

                            mBuilder.setContentIntent(pending);
                            nm.notify(0, mBuilder.build());

                            count++;
                        }


                        try {

                            HashMap<String, String> paramMap = new HashMap<String, String>();
                            RequestParams params = new RequestParams(paramMap);
                            params.put("event_id", eventID);
                            params.put("user_id", userID);
                            params.put("latitude", currentLocation.latitude);
                            params.put("longitude", currentLocation.longitude);

                            System.out.println("Going to update server with current location");

                            client.post(getApplicationContext(), url + "/updateCurrentLocation/", params,
                                    new JsonHttpResponseHandler() {
                                        @Override
                                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                                            System.out.println("Server Update status" + response.toString());

                                            Log.d("Response", "" + response);
                                        }
                                    }
                            );

                        } catch (Exception e) {

                        }

                        setImagesUploadAlarm();

                    }

                }
            }
            Log.d(tag, "Updating current location thread executed");
            handler.postDelayed(this, 15000); // Thread running at the interval of 15 sec
        }
    };

    public void setImagesUploadAlarm() {
        Cursor cursor = db.getStartedEventDetails(userID);
        if(cursor.getCount() > 0) {
            cursor.moveToNext();
            Log.d("PM", "No. of running events: " +cursor.getCount());
            String startedEventId = cursor.getString(0);
            String startedEventEndTime = cursor.getString(1);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(sdf.parse(startedEventEndTime));
            }
            catch (ParseException e) {
                e.printStackTrace();
            }
            alarm.setAlarm(MyService.this, startedEventId, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        }
    }

    public boolean isLocationServiceOn() {

        LocationManager lm = (LocationManager) this.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            Toast.makeText(getApplicationContext(), "Please enable the Location Services!! ", Toast.LENGTH_LONG)
                    .show();
            return false;

        }
        return true;
    }


    public void setCurrentLocation(LatLng location) {
        islocationSet = true;
        currentLocation = location;
    }

    private final LocationListener networkLocationListener =
            new LocationListener() {

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }

                @Override
                public void onLocationChanged(Location location) {
                    setCurrentLocation(new LatLng(location.getLatitude(), location.getLongitude()));

                }

            };


}
