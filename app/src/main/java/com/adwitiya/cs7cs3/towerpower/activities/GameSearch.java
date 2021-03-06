package com.adwitiya.cs7cs3.towerpower.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.adwitiya.cs7cs3.towerpower.helpers.AudioPlay;
import com.adwitiya.cs7cs3.towerpower.helpers.PositionHelper;
import com.adwitiya.cs7cs3.towerpower.R;
import com.adwitiya.cs7cs3.towerpower.helpers.UserInfo;
import com.adwitiya.cs7cs3.towerpower.helpers.UserMatchingInfo;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.mapboxsdk.Mapbox;
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
import java.util.List;
import java.util.Map;

public class GameSearch extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener,View.OnClickListener,LocationListener,LocationEngineListener,PermissionsListener{


    private FirebaseFirestore mDatabase;
    private PermissionsManager permissionsManager;
    private LocationLayerPlugin locationPlugin;
    private LocationEngine locationEngine;
    private Location originLocation;    private static final String TAG = LiveMaps.class.getSimpleName();
    private MapView mapView;
    private MapboxMap map;
    private String user_id;
    private Uri photoUrl;
    private String teamID;
    private ArrayList<UserInfo> team;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_search);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Code to check fire base Auth instance
        checkFirebaseAuth(navigationView);

        //Firebase Database
        FirebaseApp.initializeApp(this);
        mDatabase = FirebaseFirestore.getInstance();
        //Start of Mapbox Async Task
        //This task needs to be finished before activating the Search Game Button
        Mapbox.getInstance(this,getString(R.string.mapbox_key));
        mapView = findViewById(R.id.map_game);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> {
            map = mapboxMap;
            enableLocationPlugin();
            });
        //Make Search Button Disabled
        Button SearchBtn = findViewById(R.id.SearchGame);
        SearchBtn.setEnabled(false);
        findViewById(R.id.SearchGame).setOnClickListener(GameSearch.this);

        //Disable some buttons
        Button acceptBtn = findViewById(R.id.acceptBtn);
        acceptBtn.setEnabled(false);
        acceptBtn.setOnClickListener(GameSearch.this);
        Button refuseBtn = findViewById(R.id.declineBtn);
        refuseBtn.setEnabled(false);
        refuseBtn.setOnClickListener(GameSearch.this);
    }

    private void checkFirebaseAuth(NavigationView view){
        // Code to check fire base Auth instance
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String user_name = user.getDisplayName();
            String email = user.getEmail();
            photoUrl = user.getPhotoUrl();
            user_id = user.getUid();

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
    private void fillYourInfo(){
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
            TextView UserName = findViewById(R.id.playername1);
            ImageView ProfilePic = findViewById(R.id.playerimg1);
            UserName.setText(user_name);
            if (photoUrl != null) {
                Picasso.with(this).load(photoUrl).into(ProfilePic);
            }
        }
        if (user == null){
            TextView UserName = findViewById(R.id.playername1);
            ImageView ProfilePic = findViewById(R.id.playerimg1);
            UserName.setText(getText(R.string.def_user));
            ProfilePic.setImageResource(R.drawable.def_icon);
        }
    }

    @Override
    public void onClick(View view) {
        FirebaseUser gameuser = FirebaseAuth.getInstance().getCurrentUser();
        int BtnID = view.getId();
        if (BtnID == R.id.SearchGame && gameuser!=null) {
            String photoUrlstr = "";
            TextView tvu = findViewById(R.id.user_name);
            String user_name = "";
            Uri pUrl = gameuser.getPhotoUrl();
            if (pUrl != null)
            {
                photoUrlstr = photoUrl.toString();
            }

            if (tvu !=null){
                user_name = tvu.getText().toString();
            }
            TextView tvemail = findViewById(R.id.user_email);
            String user_email = "";
            if (tvemail !=null){
                user_email = tvemail.getText().toString();
            }

            if ( user_name == getText(R.string.def_user) && (user_email == getText(R.string.def_email))){
                Toast.makeText(this, "Please Sign-in before initiating a Game Search !!",
                        Toast.LENGTH_SHORT).show();
            }else{
                //Fill Player Profile Infos
                fillYourInfo();
                UserMatchingInfo user = new UserMatchingInfo(new PositionHelper(originLocation.getLatitude(), originLocation.getLongitude()),
                        user_id, user_email, user_name, photoUrlstr);
                //Log.d(TAG,user.getEmail() + user.getName() + user.getLocation());
                mDatabase.collection("matchmaking").document(user_id).set(user);
                Button searchBtn = findViewById(R.id.SearchGame);
                searchBtn.setEnabled(false);


                DocumentReference userRef = mDatabase.collection("users").document(user_id);
                userRef.addSnapshotListener(GameSearch.this, (snapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        //Log.d(TAG, "Current data: " + snapshot.getData());
                        Map<String, Object> map = snapshot.getData();
                        Object tmp = map.get("team_id");
                        if (tmp != null) teamID = (String) tmp;
                        //Get TeamInfo ID and Store it to SharedPreference
                        SharedPreferences.Editor editor_team = getSharedPreferences("com.adwitiya.cs7cs3.towerpower", MODE_PRIVATE).edit();
                        editor_team.putString("TeamID",teamID);
                        editor_team.apply();
                        getTeam();
                    } else {
                        Log.d(TAG, "Current data: null");
                    }
                });
            }
        }
        else if (BtnID == R.id.acceptBtn){
            Intent gameIntent = new Intent(getApplicationContext(),LiveMaps.class);
            startActivity(gameIntent);
            Button acceptBtn = findViewById(R.id.acceptBtn);
            acceptBtn.setEnabled(false);
        }
    }

    private void getTeam(){
        DocumentReference teamRef = mDatabase.collection("teams").document(teamID);
        teamRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                team = new ArrayList<>();

                if (document != null && document.exists()) {
                    Map<String, Object> map = document.getData();
                    int i;
                    for (i=0; i<3; i++) {
                        Object tmp = map.get("user"+i);
                        if (tmp != null){
                            UserInfo user = getUserInfo(tmp);

                            if (i==0){
                                displayPlayer(user, R.id.playername1, R.id.playerrole1, R.id.playerimg1, user.getName(), user.getRole());
                            }

                            if (i==1){
                                displayPlayer(user, R.id.playername2, R.id.playerrole2, R.id.playerimg2, user.getName(), user.getRole());
                            }

                            if (i==2){
                                displayPlayer(user, R.id.playername3, R.id.playerrole3, R.id.playerimg3, user.getName(), user.getRole());
                                //Enable the confirm button when all the players are found
                                Button acceptBtn = findViewById(R.id.acceptBtn);
                                acceptBtn.setEnabled(true);
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    private void displayPlayer(UserInfo user, int playername, int playerrole, int playerimg, String name, String role) {
        TextView player_name = findViewById(playername);
        TextView player_role = findViewById(playerrole);
        ImageView profile_pic = findViewById(playerimg);

        player_name.setText(name);
        player_role.setText(role);

        if (user.getPhotoUrl() != "") {
            Picasso.with(GameSearch.this).load(user.getPhotoUrl()).into(profile_pic);
        }
    }

    @NonNull
    private UserInfo getUserInfo(Object tmp) {
        Map<String, Object> userMap;
        userMap = (Map<String, Object>) tmp;
        long afkTimeOut=-1;
        tmp = userMap.get("afkTimeOut");
        if (tmp != null) afkTimeOut = (long) tmp;
        String email="";
        tmp = userMap.get("email");
        if (tmp != null) email = (String) tmp;
        Map<String, Object> locationMap=null;
        tmp = userMap.get("location");
        if (tmp != null) locationMap = (Map<String, Object>) tmp;
        tmp = locationMap.get("latitude");
        double lat = 0;
        if (tmp != null) lat = (double) tmp;
        tmp = locationMap.get("longitude");
        double lng = 0;
        if (tmp != null) lng = (double) tmp;

        String name="";
        tmp = userMap.get("name");
        if (tmp != null) name = (String) tmp;
        String response="";
        tmp = userMap.get("response");
        if (tmp != null) response = (String) tmp;
        String role="";
        tmp = userMap.get("role");
        if (tmp != null) role = (String) tmp;
        boolean shouldSearchAgain=false;
        tmp = userMap.get("shouldSearchAgain");
        if (tmp != null) shouldSearchAgain = (boolean) tmp;
        String userID="";
        tmp = userMap.get("userID");
        if (tmp != null) userID = (String) tmp;

        String photoUrl="";
        tmp = userMap.get("photoUrl");
        if (tmp != null) photoUrl = (String) tmp;

        UserInfo user = new UserInfo(afkTimeOut, email, new PositionHelper(lat,lng), name, response, role, shouldSearchAgain, userID,photoUrl );
        team.add(user);
        return user;
    }


    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            originLocation = location;
            //Whenever we get the location we enable the button
            Button SearchBtn = findViewById(R.id.SearchGame);
            SearchBtn.setEnabled(true);
            locationEngine.removeLocationEngineListener(this);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
    protected void onResume() {
        super.onResume();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        checkFirebaseAuth(navigationView);
        switchSound();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        AudioPlay.stopAudio();
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
        } else if (id == R.id.nav_home) {
            // Navigate to Home Activity
            Intent HomeIntent = new Intent(getApplicationContext(),MainActivity.class);
            HomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(HomeIntent);
        }
        else if (id == R.id.nav_map) {
            // Navigate to Map Activity
            Intent LiveMap = new Intent(getApplicationContext(),LiveMaps.class);
            startActivity(LiveMap);

        } else if (id == R.id.nav_tools) {
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
    @SuppressWarnings( {"MissingPermission"})
    public void onConnected() {
        locationEngine.requestLocationUpdates();
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
            Button SearchBtn = findViewById(R.id.SearchGame);
            SearchBtn.setEnabled(true);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
}
