package com.adwitiya.cs7cs3.towerpower;


import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stackoverflow on 21/03/2018.
 */

public class LocationsFull {
    private double latitude;
    private double longitude;
    ArrayList<LatLng> generated_locations;

    public LocationsFull(){
        this.latitude = 0;
        this.longitude = 0;
        this.generated_locations = new ArrayList<LatLng>();
    }

    public LocationsFull(double latitude, double longitude){
        this.longitude = longitude;
        this.latitude = latitude;
        this.generated_locations = new ArrayList<LatLng>();
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

    public List<LatLng> getGenerated_locations() {
        return generated_locations;
    }

    public void addPosition(double lat, double lon){
        this.generated_locations.add( new LatLng(lat,lon) );
    }

    public void deletePosition(double lat, double lon){
        int i=0;
        for (LatLng pos : this.generated_locations){
            if (pos.getLatitude() == lat && pos.getLongitude() == lon) break;
            i++;
        }
        this.generated_locations.remove(i);
    }

    public String toString(){
        return "Latitude: " + getLatitude() + "\nLongitude: " + getLongitude();
    }
}
