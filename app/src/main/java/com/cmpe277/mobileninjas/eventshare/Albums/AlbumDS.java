package com.cmpe277.mobileninjas.eventshare.Albums;

/**
 * Created by prashanth.mudhelli on 4/30/16.
 */
public class AlbumDS {
    public String eventName;
    public String eventId;

    public AlbumDS() {

    }

    public AlbumDS(String eventId, String eventName) {
        this.eventName = eventName;
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}