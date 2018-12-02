package com.example.asthana.airmuleschat;

import java.util.Date;

public class Request {
    public static final String NO_MULE = "NO MULE";
    public static final String NO_PAYMENT = "NOT PAID";
    public static final String PAID = "PAYMENT PENDING";
    public static final String COMPLETE = "COMPLETE";

    //Unfortunately we need to track it here, so that we can pass it between activities...
    private String transactionID;

    private String status = NO_MULE;

    private String customer;
    private String mule;

    private ItemData itemData;
    private float reward;

    private LocationInfo arrival;
    private LocationInfo departure;

    private String chatID;
    private String flightNumber;

    public Request(){}

    public Request(String transactionID, String customer){
        this.transactionID = transactionID;
        this.customer = customer;
    }

    public String getTransactionID(){
        return transactionID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustomer(){
        return customer;
    }

    public String getMule() {
        return mule;
    }

    public void setMule(String mule) {
        this.mule = mule;
    }

    public ItemData getItemData() {
        return itemData;
    }

    public void setItemData(ItemData itemData) {
        this.itemData = itemData;
    }

    public float getReward() {
        return reward;
    }

    public void setReward(float reward) {
        this.reward = reward;
    }

    public LocationInfo getArrival() {
        return arrival;
    }

    public void setArrival(LocationInfo arrival) {
        this.arrival = arrival;
    }

    public LocationInfo getDeparture() {
        return departure;
    }

    public void setDeparture(LocationInfo departure) {
        this.departure = departure;
    }

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }


    public static class ItemData{
        private String name;
        private int weight;
        private float height;
        private float length;
        private float width;

        public ItemData(){}

        public ItemData(String name, int weight, float height, float length, float width){
            this.name = name;
            this.weight = weight;
            this.height = height;
            this.length = length;
            this.width = width;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public float getHeight() {
            return height;
        }

        public void setHeight(float height) {
            this.height = height;
        }

        public float getLength() {
            return length;
        }

        public void setLength(float length) {
            this.length = length;
        }

        public float getWidth() {
            return width;
        }

        public void setWidth(float width) {
            this.width = width;
        }
    }

    public static class LocationInfo{
        public static final String DATE_DELIMITER = "-";
        public static final int DAY_INDEX = 0;
        public static final int MONTH_INDEX = 1;
        public static final int YEAR_INDEX = 2;

        private String city;
        private String country;
        private String date;

        public LocationInfo(){}

        public LocationInfo(String city, String country, String date) {
            this.city = city;
            this.country = country;
            this.date = date;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getLocationString(){
            return getCity() + ", " + getCountry();
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }
}
