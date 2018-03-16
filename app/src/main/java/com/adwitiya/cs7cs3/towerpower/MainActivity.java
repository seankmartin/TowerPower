package com.adwitiya.cs7cs3.towerpower;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import com.adwitiya.cs7cs3.towerpower.AudioPlay;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    View mDecorView;
    private ImageView CollectIV;
    private TextView CollectTV;
    private ImageView DestroyIV;
    private TextView DestroyTV;
    private ImageView DefendIV;
    private TextView DefendTV;
    private TextView group5;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //hideSystemUI();
        setContentView(R.layout.activity_main);
        setAnimation(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        checkFirebaseAuth(navigationView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AudioPlay.stopAudio();

    }

    private void setAnimation(Context context){
        DestroyIV = (ImageView)findViewById(R.id.destroy_logo);
        DestroyTV = (TextView)findViewById(R.id.destroy_text);

        CollectIV = (ImageView)findViewById(R.id.collect_logo);
        CollectTV = (TextView)findViewById(R.id.collect_text);

        DefendIV = (ImageView)findViewById(R.id.defend_logo);
        DefendTV = (TextView)findViewById(R.id.defend_text);

        group5 = (TextView)findViewById(R.id.group_text);


        Animation anim = AnimationUtils.loadAnimation(this,R.anim.transition);
        Animation anim1 = AnimationUtils.loadAnimation(this,R.anim.transition2);
        Animation anim2 = AnimationUtils.loadAnimation(this,R.anim.transition3);
        CollectTV.startAnimation(anim);
        CollectIV.startAnimation(anim);

        DestroyTV.startAnimation(anim1);
        DestroyIV.startAnimation(anim1);

        DefendTV.startAnimation(anim2);
        DefendIV.startAnimation(anim2);

        CollectIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Collect all the treasures and puzzles to beat the game", Snackbar.LENGTH_LONG)
                        .setAction("Tower Power", null).show();
            }
        });
        DestroyIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Destroy all the towers before times runs out", Snackbar.LENGTH_LONG)
                        .setAction("Tower Power", null).show();
            }
        });
        DefendIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Defend your bases against the attacks ", Snackbar.LENGTH_LONG)
                        .setAction("Tower Power", null).show();
            }
        });

        group5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/adwitiya/TowerPower")));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setAnimation(this);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        checkFirebaseAuth(navigationView);
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

    @Override
    public void onBackPressed() {
        //hideSystemUI();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        //hideSystemUI();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //hideSystemUI();
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //hideSystemUI();
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
        }   else if (id == R.id.nav_home) {
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
            Intent ToolsActivity = new Intent(getApplicationContext(),ToolsActivity.class);
            startActivity(ToolsActivity);
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
    // This snippet hides the system bars.
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        mDecorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LOW_PROFILE;
        mDecorView.setSystemUiVisibility(uiOptions);
    }
}
