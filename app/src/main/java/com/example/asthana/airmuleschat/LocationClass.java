package com.example.asthana.airmuleschat;

public class LocationClass {
    private double latitude;
    private double longitude;

    public LocationClass() {
    }

    public LocationClass(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {

        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {

        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
