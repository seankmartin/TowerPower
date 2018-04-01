package com.adwitiya.cs7cs3.towerpower.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
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
import com.adwitiya.cs7cs3.towerpower.helpers.AudioPlay;
import com.adwitiya.cs7cs3.towerpower.helpers.GameInfo;
import com.adwitiya.cs7cs3.towerpower.helpers.PositionHelper;
import com.adwitiya.cs7cs3.towerpower.R;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
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
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.lang.StrictMath.cos;
import static java.lang.StrictMath.sin;

public class LiveMaps extends AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener,LocationEngineListener, PermissionsListener {
    private String MapBoxZoom;
    private MapView mapView;
    // variables for adding location layer
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private LocationLayerPlugin locationPlugin;
    private LocationEngine locationEngine;
    private Location originLocation;
    private static final String TAG = LiveMaps.class.getSimpleName();
    private GoogleApiClient googleApiClient;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private FirebaseFirestore mDatabase;
    private GameInfo gameInfo;
    private String teamID;
    private String gameID;
    private final long gameduration = 600000;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_maps);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.BLACK);
        setSupportActionBar(toolbar);

        //Firebase Database
        FirebaseApp.initializeApp(this);
        mDatabase = FirebaseFirestore.getInstance();

        requestEnableGPS();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        checkFirebaseAuth(navigationView);

        //MapBox Code
        MapBoxZoom = getString(R.string.mapbox_zoom);
        final Double MapBoxZoomDouble = Double.parseDouble(MapBoxZoom);
        Mapbox.getInstance(this,getString(R.string.mapbox_key));
        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        //give a marker location to the map
        //Get the positions from DB
        List<PositionHelper> positionList = new ArrayList<>();
        retrieveMultiLocFromDB();

        //Handle ShowInventory Button Click
        FloatingActionButton showInventory = findViewById(R.id.showInv);
        showInventory.setOnClickListener(v -> showCustomView());
    }

    //Inventory menu function
    private void showCustomView() {
        String materials, hints;

        MaterialDialog dialog =
                new MaterialDialog.Builder(this)
                        .title(R.string.inv_menu)
                        .customView(R.layout.dialog_customview, true)
                        .build();

        dialog.show();
        if (gameInfo != null) {
            View CustomView = dialog.getCustomView();
            materials = "" + gameInfo.getMaterialsInventory();
            TextView collectedMaterials = CustomView.findViewById(R.id.collectedMaterials);
            collectedMaterials.setText(materials);

            hints = "" + gameInfo.getHintsInventory();
            TextView collectedHints = CustomView.findViewById(R.id.collectedHints);
            collectedHints.setText(hints);
        }
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
        result.setResultCallback(result1 -> {
            final Status status = result1.getStatus();
            final LocationSettingsStates state = result1.getLocationSettingsStates();
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

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavigationView navigationView = findViewById(R.id.nav_view);
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
        if (soundPref) {
           AudioPlay.playAudio(this);
        }
        else if (!soundPref){
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
            Intent ToolsActivity = new Intent(getApplicationContext(), com.adwitiya.cs7cs3.towerpower.activities.ToolsActivity.class);
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
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            builder.setPositiveButton("Send", (dialog, which) -> {
                String m_Text = input.getText().toString();
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto",m_Text, null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Tower Power - Location based Android Game");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello Friend,\n\nEnjoy this awesome game\nTower Power, a location based Android app. \nDownload Today\nhttps://scss.tcd.ie/~chakraad");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void checkFirebaseAuth(NavigationView view){
        // Code to check fire base Auth instance
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
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
            TextView UserName = header.findViewById(R.id.user_name);
            TextView UserEmail = header.findViewById(R.id.user_email);
            ImageView ProfilePic = header.findViewById(R.id.profile_pic);
            UserName.setText(user_name);
            UserEmail.setText(email);
            if (photoUrl != null) {
                Picasso.with(this).load(photoUrl).into(ProfilePic);
            }
        }
        if (user == null){
            View header = view.getHeaderView(0);
            TextView UserName = header.findViewById(R.id.user_name);
            TextView UserEmail = header.findViewById(R.id.user_email);
            ImageView ProfilePic = header.findViewById(R.id.profile_pic);
            UserName.setText(getText(R.string.def_user));
            UserEmail.setText(getText(R.string.def_email));
            ProfilePic.setImageResource(R.drawable.def_icon);
        }
    }
    private void retrieveMultiLocFromDB() {
        //mDatabase.collection("teams").document()
        String notFoundMsg = "You are not in a game.";
        SharedPreferences TeamPrefs = getSharedPreferences("com.adwitiya.cs7cs3.towerpower", MODE_PRIVATE);
        teamID = TeamPrefs.getString("TeamID", notFoundMsg);

        if (teamID.compareTo(notFoundMsg)==0){
            Toast.makeText(LiveMaps.this, notFoundMsg,
                    Toast.LENGTH_SHORT).show();
        }
        else {
            initialRenderMarkers();
            }
    }

    private void initialRenderMarkers(){
        if ( teamID != null) {
            CollectionReference colRef = mDatabase.collection("teams").document(teamID).collection("games");
            if (colRef != null) {
            colRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        if (doc != null && doc.exists()) {
                            gameID = doc.getId();
                            gameInfo = new GameInfo();
                            decodeGameDBStructure(doc);
                            if (timer != null) {
                                timer.cancel();
                            }
                            createGameTimer();
                        }
                        break; // N.B. THIS IS TO RETRIEVE ONLY ONE
                    }
                    mapView.getMapAsync(mapboxMap -> {
                        map = renderMap(mapboxMap);
                        // handle onClick of Markers
                        map.setOnMarkerClickListener(marker -> collect(marker));
                        enableLocationPlugin();
                        updateLocations();
                    });
                }
            });
        }
        } else{
            Toast.makeText(LiveMaps.this, "You are not in a game.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void decodeGameDBStructure(DocumentSnapshot doc){
        Map<String, Object> gameInfoMap = doc.getData();
        double lat = -1, lon = -1;
        Object tmp = gameInfoMap.get("latitude");
        if (tmp != null) lat = (double) tmp;
        tmp = gameInfoMap.get("longitude");
        if (tmp != null) lon = (double) tmp;
        gameInfo.setStartLocation(new PositionHelper(lat,lon));

        Map<String, Object> basesMap = (Map<String, Object>) gameInfoMap.get("bases");
        if (basesMap != null) {
            for ( String key : basesMap.keySet() ){
                Map<String, Object> locationMap = (Map<String, Object>) basesMap.get(key);
                tmp = locationMap.get("latitude");
                if (tmp != null) lat = (double) tmp;
                tmp = locationMap.get("longitude");
                if (tmp != null) lon = (double) tmp;
                if (tmp != null) gameInfo.addBase(key,  lat, lon);
            }
        }
        Map<String, Object> hintsMap = (Map<String, Object>) gameInfoMap.get("hints");
        if (hintsMap != null) {
            for ( String key : hintsMap.keySet() ){
                Map<String, Object> locationMap = (Map<String, Object>) hintsMap.get(key);
                tmp = locationMap.get("latitude");
                if (tmp != null) lat = (double) tmp;
                tmp = locationMap.get("longitude");
                if (tmp != null) lon = (double) tmp;
                if (tmp != null) gameInfo.addHint(key, lat, lon);
            }
        }
        Map<String, Object> towersMap = (Map<String, Object>) gameInfoMap.get("towers");
        if (towersMap != null) {
            for ( String key : towersMap.keySet() ){
                Map<String, Object> locationMap = (Map<String, Object>) towersMap.get(key);
                tmp = locationMap.get("latitude");
                if (tmp != null) lat = (double) tmp;
                tmp = locationMap.get("longitude");
                if (tmp != null) lon = (double) tmp;
                if (tmp != null) gameInfo.addTower(key,  lat, lon);
            }
        }
        Map<String, Object> materialsMap = (Map<String, Object>) gameInfoMap.get("materials");
        if (materialsMap != null) {
            for ( String key : materialsMap.keySet() ){
                Map<String, Object> locationMap = (Map<String, Object>) materialsMap.get(key);
                tmp = locationMap.get("latitude");
                if (tmp != null) lat = (double) tmp;
                tmp = locationMap.get("longitude");
                if (tmp != null) lon = (double) tmp;
                if (tmp != null) gameInfo.addMaterial(key,  lat, lon);
            }
        }
        Map<String, Object> inventoryMap = (Map<String, Object>) gameInfoMap.get("inventory");
        if (inventoryMap != null) {
            tmp = inventoryMap.get("hints");
            if (tmp != null) gameInfo.setHintsInventory( (long) tmp);
            tmp = inventoryMap.get("materials");
            if (tmp != null) gameInfo.setMaterialsInventory( (long) tmp);

        }
        tmp =  gameInfoMap.get("start_time");
        Date startTime;
        if (tmp != null) {
            startTime = (Date) tmp;
            gameInfo.setStartTime(startTime);
        }
        tmp =  gameInfoMap.get("time_bonus");
        long timeBonus;
        if (tmp != null) {
            timeBonus = (long) tmp;
            DocumentReference ref = mDatabase.collection("teams").document(teamID).collection("games").document(gameID);
            if (timeBonus == -1 && !gameInfo.isWon()) {
                gameInfo.setTimeBonus(-2);
                gameInfo.setWon();
                WriteBatch batch = mDatabase.batch();
                batch.update(ref, "time_bonus", gameInfo.getTimeBonus());
                batch.commit();
                if ( isGameWon() ) {
                    setTitle("You Win !");
                } else {
                    setTitle("You Lost !");
                }
            } else if (timeBonus == -2 && !gameInfo.isWon()) {
                gameInfo.setTimeBonus(-3);
                gameInfo.setWon();if ( isGameWon() ) {
                    setTitle("You Win !");
                } else {
                    setTitle("You Lost !");
                }
                ref.delete();
                mDatabase.collection("teams").document(teamID).delete();
            } else {
                gameInfo.setTimeBonus(timeBonus);
            }
        }
    }


    private boolean collect(Marker marker){
        LatLng currentMarker = marker.getPosition();
        LatLng myPosition = new LatLng(originLocation.getLatitude(), originLocation.getLongitude());
        double distance = myPosition.distanceTo(currentMarker);
        if (distance >= 800){
            Toast.makeText(LiveMaps.this, "Collectible out of reach!",
                    Toast.LENGTH_SHORT).show();
        }
        else {
            LatLng pos = marker.getPosition();
            //String deletedKey = gameLocations.deletePosition(pos.getLatitude(), pos.getLongitude());
            String deletedKey = gameInfo.collect(pos.getLatitude(), pos.getLongitude());
            if (deletedKey.equals(GameInfo.MORE_HINTS_NEEDED_KEY)){
                Toast.makeText(LiveMaps.this, "Collect four hints to destroy towers", Toast.LENGTH_SHORT).show();
            }
            else if (deletedKey.equals(GameInfo.MORE_MATERIALS_NEEDED_KEY)) {
                Toast.makeText(LiveMaps.this, "Collect five materials to fortify bases", Toast.LENGTH_SHORT).show();
            }
            else{
                DocumentReference ref =  mDatabase.collection("teams").document(teamID).collection("games").document(gameID);
                if ( deletedKey.contains("hint") ) {
                    collectHint(deletedKey, ref);
                }
                else if ( deletedKey.contains("material") ) {
                    collectMaterial(deletedKey, ref);
                }
                else if ( deletedKey.contains("base") ) {
                    fortifyBase(ref);

                }
                else if ( deletedKey.contains("tower") ) {
                    destroyTower(deletedKey, ref);
                }
            }
        }
        return false;
    }

    private void destroyTower(String key, DocumentReference gameRef) {
        WriteBatch batch = mDatabase.batch();
        batch.update(gameRef, "inventory.hints", gameInfo.getHintsInventory());
        batch.update(gameRef, "towers." + key, FieldValue.delete());
        batch.commit();
        Toast.makeText(LiveMaps.this, "Tower destroyed!",
                Toast.LENGTH_SHORT).show();
        if ( isGameWon() ){
            gameInfo.setTimeBonus(-1);
            gameInfo.setWon();
            setTitle("You Win !");
            gameRef.update("time_bonus", gameInfo.getTimeBonus());
        }
    }

    private void fortifyBase(DocumentReference gameRef) {
        //update inventory layout and update bonus on DB
        WriteBatch batch = mDatabase.batch();
        batch.update(gameRef,"inventory.materials", gameInfo.getMaterialsInventory());
        gameInfo.setTimeBonus( gameInfo.getTimeBonus()+1 );
        batch.update(gameRef,"time_bonus", gameInfo.getTimeBonus());
        batch.commit();
        Toast.makeText(LiveMaps.this, "Base fortified!",
                Toast.LENGTH_SHORT).show();
    }

    private void collectMaterial(String key, DocumentReference gameRef) {
        WriteBatch batch = mDatabase.batch();
        batch.update(gameRef,"materials." + key, FieldValue.delete());
        batch.update(gameRef,"inventory.materials",gameInfo.getMaterialsInventory());
        batch.commit();
        Toast.makeText(LiveMaps.this, "Material collected",
                Toast.LENGTH_SHORT).show();
    }

    private void collectHint(String key, DocumentReference gameRef) {
        WriteBatch batch = mDatabase.batch();
        batch.update(gameRef, "hints." + key, FieldValue.delete());
        batch.update(gameRef, "inventory.hints",gameInfo.getHintsInventory());
        batch.commit();
        Toast.makeText(LiveMaps.this, "Hint collected",
                Toast.LENGTH_SHORT).show();
    }

    private boolean isGameWon(){
        return gameInfo.getTowers().size() == 0;
    }

    private static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private MapboxMap renderMap(MapboxMap map) {
        ///DO NOT INVOKE IF GAME DOES NOT EXIST
        Bitmap bitmap = getBitmapFromVectorDrawable(this,R.drawable.ic_base);
        Icon icon = IconFactory.getInstance(LiveMaps.this).fromBitmap(bitmap);
        if (gameInfo != null) {
            for (String key : gameInfo.getBases().keySet()) {
                PositionHelper position = (PositionHelper) gameInfo.getBases().get(key);
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(position.getLatitude(), position.getLongitude()))
                        .title(getString(R.string.title_snip_base))
                        .snippet(key)
                        .icon(icon));
            }
            bitmap = getBitmapFromVectorDrawable(this, R.drawable.ic_hint);
            icon = IconFactory.getInstance(LiveMaps.this).fromBitmap(bitmap);
            for (String key : gameInfo.getHints().keySet()) {
                PositionHelper position = (PositionHelper) gameInfo.getHints().get(key);
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(position.getLatitude(), position.getLongitude()))
                        .title(getString(R.string.title_snip_hint))
                        .snippet(key)
                        .icon(icon));
            }
            bitmap = getBitmapFromVectorDrawable(this, R.drawable.ic_material);
            icon = IconFactory.getInstance(LiveMaps.this).fromBitmap(bitmap);
            for (String key : gameInfo.getMaterials().keySet()) {
                PositionHelper position = (PositionHelper) gameInfo.getMaterials().get(key);
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(position.getLatitude(), position.getLongitude()))
                        .title(getString(R.string.title_snip_material))
                        .snippet(key)
                        .icon(icon));
            }
            bitmap = getBitmapFromVectorDrawable(this, R.drawable.ic_tower);
            icon = IconFactory.getInstance(LiveMaps.this).fromBitmap(bitmap);
            for (String key : gameInfo.getTowers().keySet()) {
                PositionHelper position = (PositionHelper) gameInfo.getTowers().get(key);
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(position.getLatitude(), position.getLongitude()))
                        .title(getString(R.string.title_snip_tower))
                        .snippet(key)
                        .icon(icon));
            }
        }
        if (gameInfo != null)
            drawCircle(map, gameInfo.getStartLocation());
        return map;
    }

    private void updateLocations(){
        if (teamID != null && gameID!= null) {
            DocumentReference colRef = mDatabase.collection("teams").document(teamID).collection("games").document(gameID);
            if (colRef != null) {
                colRef.addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {

                        decodeGameDBStructure(snapshot);
                        map.clear();
                        map = renderMap(map);
                        Log.d(TAG, gameInfo.toString());
                    } else {
                        Log.d(TAG, "Current data: null");
                    }
                });
            }
        }
        else{
            Toast.makeText(LiveMaps.this, "You are not in a game",
                    Toast.LENGTH_LONG).show();
        }
    }

    private static void drawCircle(MapboxMap map, PositionHelper position) {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLACK);
        polylineOptions.width(3.0f); // change the line width here
        polylineOptions.addAll(getCirclePoints(position, (double) 500));
        map.addPolyline(polylineOptions);
    }

    private static ArrayList<LatLng> getCirclePoints(PositionHelper position, double radius) {
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
    private void createGameTimer(){
        // Game Timer Logic
        Date current = new Date();
        long millisecond = (current.getTime() - gameInfo.getStartTime().getTime());
        millisecond =  gameduration - millisecond;
        timer = new CountDownTimer(millisecond, 1000) {

            public void onTick(long millisUntilFinished) {
                if ( !isGameWon() ){
                    millisUntilFinished = millisUntilFinished / 1000;
                    long minsUntilFinished = millisUntilFinished / 60;
                    long secsUntilFinished = millisUntilFinished % 60;
                    if ( secsUntilFinished < 10){
                       setTitle("Live Maps \t\t" +minsUntilFinished +":0" + secsUntilFinished );
                    }
                    else{
                       setTitle("Live Maps \t\t" +minsUntilFinished +":" + secsUntilFinished );
                    }
                }
            }
            public void onFinish() {
                if ( !gameInfo.isWon() ) {
                    gameInfo.setTimeBonus(-1);
                    gameInfo.setWon();
                    DocumentReference ref = mDatabase.collection("teams").document(teamID).collection("games").document(gameID);
                    ref.update("time_bonus", gameInfo.getTimeBonus());
                }
                if ( !isGameWon() ) {
                    gameInfo.setWon();
                    setTitle("You Lost !");
                }
            }
        }.start();
    }
}




