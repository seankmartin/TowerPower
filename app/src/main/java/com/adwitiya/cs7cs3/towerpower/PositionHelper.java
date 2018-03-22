package com.adwitiya.cs7cs3.towerpower;

import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by stackoverflow on 14/03/2018.
 */

public class PositionHelper {
    private double latitude;
    private double longitude;



    public PositionHelper(){
        this.latitude = 0;
        this.longitude = 0;
    }

    public PositionHelper(double latitude, double longitude){
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LatLng toLatLng(){
        return new LatLng( getLatitude(), getLongitude());
    }

    public String toString(){
        return "Latitude: " + getLatitude() + "\nLongitude: " + getLongitude();
    }

}
