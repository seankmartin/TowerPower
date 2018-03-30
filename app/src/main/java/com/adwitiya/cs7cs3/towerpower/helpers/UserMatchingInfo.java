package com.adwitiya.cs7cs3.towerpower.helpers;

public class UserMatchingInfo {
    private PositionHelper location;
    private boolean shouldSearchAgain;
    private String userID;
    private String email;
    private String name;
    private String role;
    private int afkTimeOut;
    private String photoUrl;

    public UserMatchingInfo(PositionHelper location, String userID, String email, String name, String photoUrl) {
        this.location = location;
        this.shouldSearchAgain = false;
        this.userID = userID;
        this.email = email;
        this.name = name;
        this.role = "random";
        this.afkTimeOut = 0;
        this.photoUrl = photoUrl;
    }

    public UserMatchingInfo() {
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public PositionHelper getLocation() {
        return location;
    }

    public void setLocation(PositionHelper location) {
        this.location = location;
    }

    public boolean isShouldSearchAgain() {
        return shouldSearchAgain;
    }

    public void setShouldSearchAgain(boolean shouldSearchAgain) {
        this.shouldSearchAgain = shouldSearchAgain;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getAfkTimeOut() {
        return afkTimeOut;
    }

    public void setAfkTimeOut(int afkTimeOut) {
        this.afkTimeOut = afkTimeOut;
    }
}
