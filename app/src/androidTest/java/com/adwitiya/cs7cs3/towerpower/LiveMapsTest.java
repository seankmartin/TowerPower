package com.adwitiya.cs7cs3.towerpower;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static android.util.Log.println;
import static org.junit.Assert.*;

/**
 * Created by Anant Gupta on 3/20/2018.
 */
@RunWith(AndroidJUnit4.class)
public class LiveMapsTest {
    private List<PositionHelper> positionList;

    private FirebaseFirestore mDatabase;

    @Test
    public void onCreate() throws Exception {

    }

    @Test
    public void onConnected() throws Exception {
    }

    @Test
    public void onLocationChanged() throws Exception {
    }

    @Test
    public void onStart() throws Exception {
    }

    @Test
    public void retrieveMultiLocFromDB() throws Exception{

        CollectionReference colRef = mDatabase.collection("locations");
        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        if (doc != null && doc.exists()) {
                            Map<String, Object> map = doc.getData();
                            double lat=0, lon=0;
                            Object tmp =  map.get("latitude");
                            if (tmp!=null) lat = (double) tmp;
                            tmp =  map.get("longitude");
                            if (tmp!=null) lon = (double) tmp;
                            positionList.add(new PositionHelper(lat,lon));


                            Map<String, Object> map2 = (Map<String, Object>) map.get("generated_locations");
                            if (map2!=null){
                                ArrayList<Map<String, Object>> map3 = (ArrayList<Map<String, Object>>) map2.get("locations");
                                for (Map<String, Object> locationMap: map3) {
                                    tmp =  locationMap.get("lat");
                                    if (tmp!=null) lat = (double) tmp;
                                    tmp =  locationMap.get("lng");
                                    if (tmp!=null) lon = (double) tmp;
                                    positionList.add(new PositionHelper(lat,lon));
                                }
                            }

                        }
                    }

                }
            }


        });
        int pos_size = 60;
        assertEquals(pos_size, positionList.size());

        for (int i= 0; i<positionList.size(); i++) {
            PositionHelper temp = positionList.get(i);
            //println(temp);
            double temp_lat = temp.getLatitude();
            String tempstr = String.valueOf(temp_lat);
            double temp_long = temp.getLongitude() ;
            String tempstrlong = String.valueOf((temp_long));
            assertNotNull(tempstr);
            assertNotNull(tempstrlong);
        }
    }
}