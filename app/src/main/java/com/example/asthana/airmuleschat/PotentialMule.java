package com.example.asthana.airmuleschat;

public class PotentialMule {
    private String requestID;
    private String muleID;

    public PotentialMule(){}

    public PotentialMule(String requestID, String muleID){
        this.requestID = requestID;
        this.muleID = muleID;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public String getMuleID() {
        return muleID;
    }

    public void setMuleID(String muleID) {
        this.muleID = muleID;
    }
}
