package com.example.ridealarmandalert.models;

public class AlarmModel {
    String id;
    String title;
    long PID;
    long time;

    public AlarmModel(String id, String title, long PID, long time) {
        this.id = id;
        this.title = title;
        this.PID = PID;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getPID() {
        return PID;
    }

    public void setPID(long PID) {
        this.PID = PID;
    }
}
