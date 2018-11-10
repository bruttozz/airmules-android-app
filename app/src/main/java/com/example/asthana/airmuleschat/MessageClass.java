// Based on https://github.com/firebase/friendlychat-android and Stack Overflow.


package com.example.asthana.airmuleschat;

public class MessageClass {

    private String id;
    private String text;
    private String name;
    private String photoUrl;
    private String imageUrl;

    public MessageClass() {
    }

    public MessageClass(String text, String name, String photoUrl, String imageUrl) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getText() {
        return text;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
