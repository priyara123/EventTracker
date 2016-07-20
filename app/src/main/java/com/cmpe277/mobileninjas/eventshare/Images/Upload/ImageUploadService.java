package com.cmpe277.mobileninjas.eventshare.Images.Upload;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.cmpe277.mobileninjas.eventshare.Database;
import com.raweng.built.Built;
import com.raweng.built.BuiltApplication;
import com.raweng.built.BuiltError;
import com.raweng.built.BuiltObject;
import com.raweng.built.BuiltResultCallBack;
import com.raweng.built.BuiltUpload;
import com.raweng.built.BuiltUploadCallback;
import com.raweng.built.utilities.BuiltConstant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by prashanth.mudhelli on 5/6/16.
 */
public class ImageUploadService extends Service {
    public BuiltApplication builtApplication;
    public static final String TAG = "PM";
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    Database db;
    String eventId;
    String startTime;
    String endTime;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "service started");
        db = new Database(ImageUploadService.this);
        try {
            builtApplication = Built.application(ImageUploadService.this, "bltc86a080623f49dda");
            if(getEventDetails()) {
                getEventImages();
                db.updateAlarmFlag(eventId, "Past");
                stopSelf();
            }
        } catch (Exception e) {
            Log.d(TAG, "Built.io connection error. " +e.getMessage());
        }
    }

    public boolean getEventDetails() {
        Cursor cursor = db.getCurrentEvent();
        if(cursor.getCount() > 0) {
            cursor.moveToNext();
            eventId = cursor.getString(cursor.getColumnIndex(db.event_id));
            startTime = cursor.getString(cursor.getColumnIndex(db.start_date));
            endTime = cursor.getString(cursor.getColumnIndex(db.end_date));
            return true;
        }
        return false;
    }

    public void getEventImages() {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] columns = {MediaStore.Images.Media.DATA};
        String selection = MediaStore.Images.Media.DATE_ADDED + " >= ? and " +MediaStore.Images.Media.DATE_ADDED +" <= ?";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        Cursor cursor = null;

        try {
            cursor = getContentResolver().query(uri, columns, selection, new String[]{String.valueOf(sdf.parse(startTime).getTime() / 1000), String.valueOf(sdf.parse(endTime).getTime()/1000)}, null);
        }
        catch (ParseException e) {
            Log.d(TAG, e.getMessage());
        }

        if (cursor.getCount() > 0) {
            Log.d(TAG, "Images to upload: " + cursor.getCount());
            String imageUri;
            while (cursor.moveToNext()) {
                imageUri = cursor.getString(0);
                uploadCurrentImage(imageUri);
            }
        }
        else {
            Log.d(TAG, "No images to upload" + cursor.getCount());
        }
    }

    public void uploadCurrentImage(String imageUri) {

        final BuiltUpload uploadObject = builtApplication.upload();
        uploadObject.setFile(imageUri);

        uploadObject.saveInBackground(new BuiltUploadCallback() {
            @Override
            public void onCompletion(BuiltConstant.ResponseType responseType, BuiltError builtError) {
                if(builtError == null){
                    Log.d(TAG, "UID: " +uploadObject.getUploadUid());
                    setUploadedImage(uploadObject.getUploadUid());
                }
                else{
                    Log.d(TAG, "Upload error: ");
                }
            }

            @Override
            public void onProgress(int progress) {
                Log.d(TAG, "On progress" +progress);
            }
        });
    }

    public void setUploadedImage(String imageUid) {
        BuiltObject projectObject = builtApplication.classWithUid("images").object();
        projectObject.set("event_id", eventId);
        projectObject.set("image", imageUid);

        projectObject.saveInBackground(new BuiltResultCallBack() {
            @Override
            public void onCompletion(BuiltConstant.ResponseType responseType, BuiltError error) {
                if (error == null) {
                    Log.d(TAG, "Image set to event");
                }
                else {
                    Log.d(TAG, "Image is not set to the event. " +error.getErrorMessage());
                }

            }
        });
    }
}