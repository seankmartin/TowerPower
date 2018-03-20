package com.adwitiya.cs7cs3.towerpower;

/**
 * Created by Stefano on 20/03/2018.
 */

public class UserMatchingInfo {
    PositionHelper location;
    boolean shouldSearchAgain;
    String userID;
    String email;
    String name;
    String role;
    int afkTimeOut;

    public UserMatchingInfo(PositionHelper location, boolean shouldSearchAgain, String userID, String email, String name, String role, int afkTimeOut) {
        this.location = location;
        this.shouldSearchAgain = shouldSearchAgain;
        this.userID = userID;
        this.email = email;
        this.name = name;
        this.role = role;
        this.afkTimeOut = afkTimeOut;
    }

    public UserMatchingInfo() {
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
