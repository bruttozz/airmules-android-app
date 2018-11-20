package com.example.asthana.airmuleschat;

public class UserClass {
    private String name;
    private float money;

    public UserClass() {
        
    }

    public UserClass(String name, float money) {
        this.name = name;
        this.money = money;
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


}


