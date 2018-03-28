package com.adwitiya.cs7cs3.towerpower.Helpers;


import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by stackoverflow on 21/03/2018.
 */

public class LocationsFull {
    private double latitude;
    private double longitude;
    Map<String, Object> generated_locations;
   // ArrayList<LatLng> generated_locations;

    public LocationsFull(){
        this.latitude = 0;
        this.longitude = 0;
        this.generated_locations = new HashMap<>();
    }

    public LocationsFull(double latitude, double longitude){
        this.longitude = longitude;
        this.latitude = latitude;
        this.generated_locations = new HashMap<>();
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

    public Map<String, Object> getGenerated_locations() {
        return generated_locations;
    }

    public void addPosition(String key, double lat, double lon){
        this.generated_locations.put( key, new PositionHelper(lat,lon) );
    }

    public String deletePosition(double lat, double lon){
        int i=0;
        String finalKey=null;
        for ( String key : this.getGenerated_locations().keySet() ){
          //  key = "location"+i;
            PositionHelper pos = (PositionHelper) this.getGenerated_locations().get(key);
            if (pos.getLatitude() == lat && pos.getLongitude() == lon) {
                finalKey = key;
                break;
            }
            i++;
        }
        this.generated_locations.remove(finalKey);
        return finalKey;
    }

    public LatLng toLatLng(){
        return new LatLng( getLatitude(), getLongitude());
    }

    public String toString(){
       // return "Latitude: " + getLatitude() + "\nLongitude: " + getLongitude();
        return this.getGenerated_locations().entrySet().toString();
    }
}
