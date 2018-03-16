package com.adwitiya.cs7cs3.towerpower;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

public class FirebaseLogin extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener{
    View mDecorView;
    private static final Class[] CLASSES = new Class[]{
            EmailPasswordActivity.class,
            GoogleSignInActivity.class
    };

    private static final int[] DESCRIPTION_IDS = new int[] {
            R.string.desc_emailpassword,
            R.string.desc_google_sign_in
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Code to check fire base Auth instance
        checkFirebaseAuth(navigationView);

        ListView listView = findViewById(R.id.list_view);

        MyArrayAdapter adapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_2, CLASSES);
        adapter.setDescriptionIds(DESCRIPTION_IDS);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
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

    public static class MyArrayAdapter extends ArrayAdapter<Class> {

        private Context mContext;
        private Class[] mClasses;
        private int[] mDescriptionIds;

        public MyArrayAdapter(Context context, int resource, Class[] objects) {
            super(context, resource, objects);

            mContext = context;
            mClasses = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(android.R.layout.simple_list_item_2, null);
            }

            ((TextView) view.findViewById(android.R.id.text1)).setText(mClasses[position].getSimpleName());
            ((TextView) view.findViewById(android.R.id.text2)).setText(mDescriptionIds[position]);

            return view;
        }

        public void setDescriptionIds(int[] descriptionIds) {
            mDescriptionIds = descriptionIds;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       Class clicked = CLASSES[position];
       if (clicked == EmailPasswordActivity.class){
           Intent EmailPassActivity = new Intent(getApplicationContext(),EmailPasswordActivity.class);
           startActivity(EmailPassActivity);
       }
        if (clicked == GoogleSignInActivity.class){
            Intent GoogleSignInActivity = new Intent(getApplicationContext(),GoogleSignInActivity.class);
            startActivity(GoogleSignInActivity);
        }

       //startActivity(new Intent(this, clicked));
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
    protected void onResume() {
        super.onResume();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        checkFirebaseAuth(navigationView);
        switchSound();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AudioPlay.stopAudio();
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

        }  else if (id == R.id.nav_home) {
            // Navigate to Home Activity
            Intent HomeIntent = new Intent(getApplicationContext(),MainActivity.class);
            HomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(HomeIntent);
        }  else if (id == R.id.nav_map) {
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
}
