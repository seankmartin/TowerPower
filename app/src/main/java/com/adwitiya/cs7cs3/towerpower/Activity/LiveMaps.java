package com.adwitiya.cs7cs3.towerpower.Activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.adwitiya.cs7cs3.towerpower.Helpers.AudioPlay;
import com.adwitiya.cs7cs3.towerpower.Helpers.LocationsFull;
import com.adwitiya.cs7cs3.towerpower.Helpers.PositionHelper;
import com.adwitiya.cs7cs3.towerpower.R;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.location.LocationEnginePriority;
import com.mapbox.services.android.telemetry.location.LostLocationEngine;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.StrictMath.cos;
import static java.lang.StrictMath.sin;

public class LiveMaps extends AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener,LocationEngineListener, PermissionsListener {
    String MapBoxZoom;
    private MapView mapView;
    // variables for adding location layer
    private MapboxMap map;
    private EditText passwordInput;
    private PermissionsManager permissionsManager;
    private LocationLayerPlugin locationPlugin;
    private LocationEngine locationEngine;
    private Location originLocation;
    private List<PositionHelper> positionList;
    private static final String TAG = LiveMaps.class.getSimpleName();
    private GoogleApiClient googleApiClient;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private FirebaseFirestore mDatabase;
    private LocationsFull gameLocations;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_maps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.BLACK);
        setSupportActionBar(toolbar);

        //Firebase Database
        FirebaseApp.initializeApp(this);
        mDatabase = FirebaseFirestore.getInstance();

        requestEnableGPS();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        checkFirebaseAuth(navigationView);

        //MapBox Code
        MapBoxZoom = getString(R.string.mapbox_zoom);
        final Double MapBoxZoomDouble = Double.parseDouble(MapBoxZoom);
        Mapbox.getInstance(this,getString(R.string.mapbox_key));
        mapView = (MapView)findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        //give a marker location to the map
        //Get the positions from DB
        positionList = new ArrayList<>();
        retrieveMultiLocFromDB();

        //Handle ShowInventory Button Click
        FloatingActionButton showInventory = (FloatingActionButton)findViewById(R.id.showInv);
        showInventory.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showCustomView();
            }
        });
    }

    //Inventory menu function
    public void showCustomView() {
        MaterialDialog dialog =
                new MaterialDialog.Builder(this)
                        .title(R.string.inv_menu)
                        .customView(R.layout.dialog_customview, true)
                        .positiveText(R.string.guess_pwd)
                        .negativeText(R.string.go_back)
                        .onPositive(
                                (dialog1, which) ->
                                        Toast.makeText(LiveMaps.this, "WRONG!",
                                                Toast.LENGTH_LONG).show())
                        .onNegative((dialog1, which) -> dialog1.hide())
                        .build();
        dialog.show();
    }

    private void requestEnableGPS(){
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getApplicationContext()).addApi(LocationServices.API).build();
            googleApiClient.connect();
        }
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(LiveMaps.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
// Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        //startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        requestEnableGPS();//keep asking if imp or do whatever
                        break;
                }
                break;
        }
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationPlugin() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Create an instance of LOST location engine
            initializeLocationEngine();

            locationPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
            locationPlugin.setLocationLayerEnabled(LocationLayerMode.TRACKING);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressWarnings( {"MissingPermission"})
    private void initializeLocationEngine() {
        locationEngine = new LostLocationEngine(this);
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
            drawCircle(map, new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude()), Color.BLACK, 500);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    private void setCameraPosition(Location location) {
        Double MapBoxZoomDouble = Double.parseDouble(MapBoxZoom);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()),MapBoxZoomDouble));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        //
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationPlugin();
        } else {
            finish();
        }
    }
    @Override
    @SuppressWarnings( {"MissingPermission"})
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            originLocation = location;
            setCameraPosition(location);
            drawCircle(map, new LatLng(location.getLatitude(),location.getLongitude()), Color.BLACK, 500);
            locationEngine.removeLocationEngineListener(this);
        }
    }



    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
        }
        if (locationPlugin != null) {
            locationPlugin.onStart();
        }
        mapView.onStart();

    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        AudioPlay.stopAudio();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates();
        }
        if (locationPlugin != null) {
            locationPlugin.onStop();
        }
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (locationEngine != null) {
            locationEngine.deactivate();
        }
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        checkFirebaseAuth(navigationView);
        mapView.onResume();
        switchSound();

    }


    private void switchSound(){
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.theme);
        SharedPreferences soundPrefs = getSharedPreferences("com.adwitiya.cs7cs3.towerpower", MODE_PRIVATE);
        Boolean soundPref = soundPrefs.getBoolean("SoundState",true);

        //Theme song
        if (soundPref == true) {
           AudioPlay.playAudio(this,R.raw.theme);
        }
        else if (soundPref == false){
           AudioPlay.stopAudio();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_account) {
            // Navigate to Account Activity
            Intent FireBaseLoginIntent = new Intent(getApplicationContext(),FirebaseLogin.class);
            startActivity(FireBaseLoginIntent);
        } else if (id == R.id.nav_game) {
            // Navigate to Game Activity
            Intent GameIntent = new Intent(getApplicationContext(),GameSearch.class);
            startActivity(GameIntent);
        } else if (id == R.id.nav_map) {
            // Navigate to Map Activity
            Intent LiveMap = new Intent(getApplicationContext(),LiveMaps.class);
            startActivity(LiveMap);
        } else if (id == R.id.nav_home) {
            // Navigate to Home Activity
            Intent HomeIntent = new Intent(getApplicationContext(),MainActivity.class);
            HomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(HomeIntent);
        }else if (id == R.id.nav_tools) {
            // Navigate to Tools Activity
            Intent ToolsActivity = new Intent(getApplicationContext(), com.adwitiya.cs7cs3.towerpower.Activity.ToolsActivity.class);
            startActivity(ToolsActivity);
        } else if (id == R.id.nav_chat) {
            // Navigate to Map Activity
            Intent Chat = new Intent(getApplicationContext(),ChatActivity.class);
            startActivity(Chat);
        } else if (id == R.id.nav_share) {
            // Navigate to Share Activity
        } else if (id == R.id.nav_send) {
            // Navigate to Send Activity
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter Email ID");
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String m_Text = input.getText().toString();
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto",m_Text, null));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Tower Power - Location based Android Game");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello Friend,\n\nEnjoy this awesome game\nTower Power, a location based Android app. \nDownload Today\nhttps://scss.tcd.ie/~chakraad");
                    startActivity(Intent.createChooser(emailIntent, "Send email..."));
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void checkFirebaseAuth(NavigationView view){
        // Code to check fire base Auth instance
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String user_name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            View header = view.getHeaderView(0);
            TextView UserName = (TextView) header.findViewById(R.id.user_name);
            TextView UserEmail = (TextView) header.findViewById(R.id.user_email);
            ImageView ProfilePic = (ImageView) header.findViewById(R.id.profile_pic);
            UserName.setText(user_name);
            UserEmail.setText(email);
            if (photoUrl != null) {
                Picasso.with(this).load(photoUrl).into(ProfilePic);
            }
        }
        if (user == null){
            View header = view.getHeaderView(0);
            TextView UserName = (TextView) header.findViewById(R.id.user_name);
            TextView UserEmail = (TextView) header.findViewById(R.id.user_email);
            ImageView ProfilePic = (ImageView) header.findViewById(R.id.profile_pic);
            UserName.setText(getText(R.string.def_user));
            UserEmail.setText(getText(R.string.def_email));
            ProfilePic.setImageResource(R.drawable.def_icon);
        }
    }
    private void retrieveMultiLocFromDB() {
        //mDatabase.collection("teams").document()
        String notFoundMsg = "TeamInfo not found";
        SharedPreferences TeamPrefs = getSharedPreferences("com.adwitiya.cs7cs3.towerpower", MODE_PRIVATE);
        String teamID = TeamPrefs.getString("TeamID", notFoundMsg);

        if (teamID.compareTo(notFoundMsg)==0){
            Toast.makeText(LiveMaps.this, notFoundMsg,
                    Toast.LENGTH_LONG).show();
        }
        else {
            initialRenderMarkers();

            }
    }

    public void initialRenderMarkers(){
        CollectionReference colRef = mDatabase.collection("teams").document("leinster2").collection("games");
        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        if (doc != null && doc.exists()) {
                            String docID = doc.getId();
                            if (docID.compareTo("leinster")==0 ) {
                                Map<String, Object> map = doc.getData();
                                double lat = -1, lon = -1;
                                Object tmp = map.get("latitude");
                                if (tmp != null) lat = (double) tmp;
                                tmp = map.get("longitude");
                                if (tmp != null) lon = (double) tmp;
                                positionList.add(new PositionHelper(lat, lon));
                                if (tmp != null) gameLocations = new LocationsFull(lat, lon);


                                Map<String, Object> map2 = (Map<String, Object>) map.get("generated_locations");
                                if (map2 != null) {
                                    int i;
                                    map2 = (Map<String, Object>) map2;
                                    for ( String key : map2.keySet() ){
                                        // String name = "location" + i;
                                        Map<String, Object> locationMap = (Map<String, Object>) map2.get(key);
                                        tmp = locationMap.get("latitude");
                                        if (tmp != null) lat = (double) tmp;
                                        tmp = locationMap.get("longitude");
                                        if (tmp != null) lon = (double) tmp;
                                        positionList.add(new PositionHelper(lat, lon));
                                        if (tmp != null) gameLocations.addPosition(key, lat, lon);
                                    }
                                }
                                // Get a new write batch
                                WriteBatch batch = mDatabase.batch();
                                DocumentReference ref = mDatabase.collection("teams").document(docID);
                                batch.update(ref, "generated_locations", FieldValue.delete());
                                batch.set(ref, gameLocations, SetOptions.merge());
                                // Commit the batch
                                batch.commit();
                                Log.d(TAG, gameLocations.toString());
                            }
                        }
                    }
                    mapView.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(MapboxMap mapboxMap) {
                            for (String key : gameLocations.getGenerated_locations().keySet() ){
                                PositionHelper position = (PositionHelper) gameLocations.getGenerated_locations().get(key);
                                String snip = ""+position.getLatitude();
                                snip.toString();
                                mapboxMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(position.getLatitude(), position.getLongitude()))
                                        .title(getString(R.string.map_title))
                                        .snippet(snip));
                            }
                            map = mapboxMap;
                            //List<Marker> markers = map.getMarkers();
                            // handle onClick of Markers
                            map.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(@NonNull Marker marker) {

                                    LatLng currentMarker = marker.getPosition();
                                    LatLng myPosition = new LatLng(originLocation.getLatitude(), originLocation.getLongitude());
                                    double distance = myPosition.distanceTo(currentMarker);
                                    if (distance >= 800){
                                        Toast.makeText(LiveMaps.this, "Collectible out of reach!",
                                                Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        Toast.makeText(LiveMaps.this, "Collected",
                                                Toast.LENGTH_LONG).show();
                                        LatLng pos = marker.getPosition();
                                        String deletedKey = gameLocations.deletePosition(pos.getLatitude(), pos.getLongitude());

                                        DocumentReference ref =  mDatabase.collection("teams").document("leinster2").collection("games").document("leinster");
                                        ref.update("generated_locations."+deletedKey, FieldValue.delete());
                                        marker.remove();
                                    }
                                    return false;
                                }
                            });
                            enableLocationPlugin();
                            updateLocations();
                        }

                    });
                }
            }
        });

    }

    public void updateLocations(){
        DocumentReference colRef = mDatabase.collection("teams").document("leinster2").collection("games").document("leinster");
        colRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {

                    String docID = snapshot.getId();
                    if (docID.compareTo("leinster")==0 ) {
                        Map<String, Object> documenetMap = snapshot.getData();
                        double lat = -1, lon = -1;
                        Object tmp = documenetMap.get("latitude");
                        if (tmp != null) lat = (double) tmp;
                        tmp = documenetMap.get("longitude");
                        if (tmp != null) lon = (double) tmp;
                        if (tmp != null) gameLocations = new LocationsFull(lat, lon);


                        Map<String, Object> map2 = (Map<String, Object>) documenetMap.get("generated_locations");
                        if (map2 != null) {
                            int i;
                            map2 = (Map<String, Object>) map2;
                            for (String key : map2.keySet()) {
                                // String name = "location" + i;
                                Map<String, Object> locationMap = (Map<String, Object>) map2.get(key);
                                tmp = locationMap.get("latitude");
                                if (tmp != null) lat = (double) tmp;
                                tmp = locationMap.get("longitude");
                                if (tmp != null) lon = (double) tmp;
                                if (tmp != null) gameLocations.addPosition(key, lat, lon);
                            }
                        }
                        map.clear();
                        for (String key : gameLocations.getGenerated_locations().keySet()) {
                            PositionHelper position = (PositionHelper) gameLocations.getGenerated_locations().get(key);
                            String snip = "" + position.getLatitude();
                            snip.toString();
                            map.addMarker(new MarkerOptions()
                                    .position(new LatLng(position.getLatitude(), position.getLongitude()))
                                    .title(getString(R.string.map_title))
                                    .snippet(snip));
                        }
                        Log.d(TAG, gameLocations.toString());
                    }
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    public static void drawCircle(MapboxMap map, LatLng position, int color, double radiusMeters) {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLACK);
        polylineOptions.width(3.0f); // change the line width here
        polylineOptions.addAll(getCirclePoints(position, radiusMeters));
        map.addPolyline(polylineOptions);
    }

    private static ArrayList<LatLng> getCirclePoints(LatLng position, double radius) {
        int degreesBetweenPoints = 10; // change here for shape
        int numberOfPoints = (int) Math.floor(360 / degreesBetweenPoints);
        double distRadians = radius / 6371000.0; // earth radius in meters
        double centerLatRadians = position.getLatitude() * Math.PI / 180;
        double centerLonRadians = position.getLongitude() * Math.PI / 180;
        ArrayList<LatLng> polygons = new ArrayList<>(); // array to hold all the points
        for (int index = 0; index < numberOfPoints; index++) {
            double degrees = index * degreesBetweenPoints;
            double degreeRadians = degrees * Math.PI / 180;
            double pointLatRadians = Math.asin(sin(centerLatRadians) * cos(distRadians)
                    + cos(centerLatRadians) * sin(distRadians) * cos(degreeRadians));
            double pointLonRadians = centerLonRadians + Math.atan2(sin(degreeRadians)
                            * sin(distRadians) * cos(centerLatRadians),
                    cos(distRadians) - sin(centerLatRadians) * sin(pointLatRadians));
            double pointLat = pointLatRadians * 180 / Math.PI;
            double pointLon = pointLonRadians * 180 / Math.PI;
            LatLng point = new LatLng(pointLat, pointLon);
            polygons.add(point);
        }
        // add first point at end to close circle
        polygons.add(polygons.get(0));
        return polygons;
    }

}




