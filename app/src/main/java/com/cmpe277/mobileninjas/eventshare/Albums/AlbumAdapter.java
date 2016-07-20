package com.cmpe277.mobileninjas.eventshare.Albums;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.cmpe277.mobileninjas.eventshare.MainActivity;
import com.cmpe277.mobileninjas.eventshare.R;

import java.util.List;

/**
 * Created by prashanth.mudhelli on 4/30/16.
 */
public class AlbumAdapter extends BaseAdapter {
    private Context mContext;
    private List<AlbumDS> eventNames;
    private List<AlbumDS> eventNamesMain;
    Drawable albumIcon;

    public AlbumAdapter(Context context, List<AlbumDS> eventNames, Drawable albumIcon){
        this.mContext = context;
        this.eventNames = eventNames;
        this.albumIcon = albumIcon;
    }

    @Override
    public int getCount() {
        return eventNames.size();
    }

    @Override
    public Object getItem(int position) {
        return eventNames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.grid_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.folderIcon = (ImageView) convertView.findViewById(R.id.folderIcon);
            viewHolder.eventName = (TextView) convertView.findViewById(R.id.eventName);
            viewHolder.eventId = (TextView) convertView.findViewById(R.id.eventId);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        AlbumDS eachEvent = eventNames.get(position);

        //if(!MainActivity.imageUrls.get(eachEvent.getEventId()).isEmpty()) {
            viewHolder.folderIcon.setImageDrawable(albumIcon);
            viewHolder.eventName.setText(eachEvent.getEventName());
            viewHolder.eventId.setText(eachEvent.getEventId());
        //}
        return convertView;
    }

    private static class ViewHolder {
        ImageView folderIcon;
        TextView eventName;
        TextView eventId;
    }
}