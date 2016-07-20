package com.cmpe277.mobileninjas.eventshare.model;

/**
 * Created by Administrator on 5/2/2016.
 */
public class Event {
    private String eventName;
    private String startDateTime;
    private String endDateTime;
    private String location;
    private String users;
    private String eventId;
    private String approvalStatus;

    public Event(String eventName, String location, String startDateTime, String endDateTime, String users, String eventId, String approvalStatus) {
        super();
        this.eventName = eventName;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.location = location;
        this.users = users;
        this.eventId = eventId;
        this.approvalStatus = approvalStatus;
    }


    public String getEventName() {
        return eventName;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public String getStartDateTime() {
        return startDateTime;
    }


    public String getEndDateTime() {
        return endDateTime;
    }

    public String getLocation() {
        return location;
    }

    public String getUsers() {
        return users;
    }

    public  String getEventId() {return  eventId;}
}
