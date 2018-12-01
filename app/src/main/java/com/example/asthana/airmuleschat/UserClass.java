package com.example.asthana.airmuleschat;

public class UserClass {
    private String name;
    private float money;
    private float rating;
    private int numRatings;

    public UserClass() {
        
    }

    public UserClass(String name, float money, float rating, int numRatings) {
        this.name = name;
        this.money = money;
        this.rating = rating;
        this.numRatings = numRatings;
    }

    public String getName() {
        return this.name;
    }

    public float getMoney() {
        return this.money;
    }

    public void setMoney( float money) {
        this.money = money;
    }

    public float getRating() {return this.rating;}

    public int getNumRatings(){return this.numRatings;}

    public float updateRating(float newRating){
        this.rating = (this.rating+newRating)/((float)(this.numRatings+1));
        return this.rating;
    }

}


