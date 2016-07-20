package com.cmpe277.mobileninjas.eventshare.Albums;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.cmpe277.mobileninjas.eventshare.MainActivity;
import com.cmpe277.mobileninjas.eventshare.R;
import com.cmpe277.mobileninjas.eventshare.Images.ImageGridActivity;
import com.cmpe277.mobileninjas.eventshare.Database;

import java.util.ArrayList;

public class AlbumsActivity extends AppCompatActivity {

    public ArrayList<AlbumDS> eventNames = new ArrayList<AlbumDS>();
    private AlbumAdapter adapter;
    public static String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);

        Database db = new Database(AlbumsActivity.this);
        eventNames = db.registeredEvents(MainActivity.getUserID());
        db.close();

        if(!eventNames.isEmpty()) {
            adapter = new AlbumAdapter(getBaseContext(), eventNames, getDrawable(R.drawable.album_image));
            GridView gridView = (GridView) findViewById(R.id.albumsGridView);
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d("PM", "Clicked at event: " + position + " " + id + " " + eventNames.get(position).getEventId());
                    //Call the activity which handles displaying image grid view
                    eventId = eventNames.get(position).getEventId();
                    Intent intent = new Intent(getBaseContext(), ImageGridActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}