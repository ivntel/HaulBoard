package com.tman.ivntel.haulboard.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by ivnte on 2016-06-11.
 */
public class HaulData {
    private String item;
    private String date;
    private String time;
    private String contact;
    private String lat;
    private String lng;
    private String deviceID;

    public HaulData(String item, String date, String time, String contact, String lat, String lng, String deviceID) {
        this.item = item;
        this.date = date;
        this.time = time;
        this.contact = contact;
        this.lat = lat;
        this.lng = lng;
        this.deviceID = deviceID;
    }

    public HaulData() {

    }

    public String getItem() { return item; }

    public void setItem(String item) {
        this.item = item;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }
}
