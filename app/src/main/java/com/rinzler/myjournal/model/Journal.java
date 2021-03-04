package com.rinzler.myjournal.model;

import com.google.firebase.Timestamp;

public class Journal {
    private String userId;
    private String userName;
    private String title;
    private String thought;
    private String imageUrl;
    private Timestamp timeAdded;

    //empty constructor is a must for firebase to work
    public Journal() {
    }

    public Journal(String userId, String userName, String title, String thought, String imageUrl, Timestamp timeAdded) {
        this.userId = userId;
        this.userName = userName;
        this.title = title;
        this.thought = thought;
        this.imageUrl = imageUrl;
        this.timeAdded = timeAdded;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThought() {
        return thought;
    }

    public void setThought(String thought) {
        this.thought = thought;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }
}
