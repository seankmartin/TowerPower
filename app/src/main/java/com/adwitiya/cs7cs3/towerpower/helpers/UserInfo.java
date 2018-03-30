package com.adwitiya.cs7cs3.towerpower.helpers;

public class UserInfo {
    private long afkTimeOut;
    private String email;
    private PositionHelper location;
    private String name ;
    private String response;
    private String role;
    private boolean shouldSearchAgain;
    private String userID;
    private String photoUrl;

    public UserInfo(long afkTimeOut, String email, PositionHelper location, String name, String response, String role, boolean shouldSearchAgain, String userID, String photoUrl) {
        this.afkTimeOut = afkTimeOut;
        this.email = email;
        this.location = location;
        this.name = name;
        this.response = response;
        this.role = role;
        this.shouldSearchAgain = shouldSearchAgain;
        this.userID = userID;
        this.photoUrl = photoUrl;
    }

    public long getAfkTimeOut() {
        return afkTimeOut;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setAfkTimeOut(int afkTimeOut) {
        this.afkTimeOut = afkTimeOut;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public PositionHelper getLocation() {
        return location;
    }

    public void setLocation(PositionHelper location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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
}
