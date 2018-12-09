package com.example.asthana.airmuleschat;

public class UserClass {
    private String name;
    private float money;
    private float rating;
    private int numRatings;
    private double latitude;
    private double longitude;

    public UserClass() {

    }

    public UserClass(String name, float money, float rating, int numRatings) {
        this.name = name;
        this.money = money;
        this.rating = rating;
        this.numRatings = numRatings;
        this.latitude = 0;
        this.longitude = 0;
    }

    public String getName() {
        return this.name;
    }

    public float getMoney() {
        return this.money;
    }

    public void setMoney(float money) {
        this.money = money;
    }

    public float getRating() {
        return this.rating;
    }

    public int getNumRatings() {
        return this.numRatings;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }


}


