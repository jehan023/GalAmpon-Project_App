package com.stejeetech.galampon;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.OneSignal;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import Fragments.HomeFragment;
import Fragments.NearbyFragment;
import Fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {
    private static final String ONESIGNAL_APP_ID = "e9766fbd-cdb8-4f98-8015-80ca9add301d";

    private BottomNavigationView bottomNavigationView;
    private Fragment selectorFragment;

    public String currentLocationName;
    public Double currentLatitude, currentLongitude;
    private final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    public String LoggedIn_user_Email;
    public String send_email;

    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);

        LoggedIn_user_Email = firebaseUser.getEmail();
        OneSignal.sendTag("User_ID", LoggedIn_user_Email);

        if(!isConnected(MainActivity.this)) {
            buildDialog(MainActivity.this).show();
        }
        setContentView(R.layout.activity_main);

        checkPermission();
        getNotificationChange();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        if(!isConnected(MainActivity.this)){
                            Toast.makeText(getApplicationContext(), "Unavailable to load.", Toast.LENGTH_SHORT).show();
                        }
                        selectorFragment = new HomeFragment();
                        break;

                    case R.id.nav_nearby:
                        if(!isConnected(MainActivity.this)){
                            Toast.makeText(getApplicationContext(), "Unavailable to load.", Toast.LENGTH_SHORT).show();
                        }
                        selectorFragment = new NearbyFragment();
                        break;

                    case R.id.nav_add:
                        selectorFragment = null;
                        startActivity(new Intent(MainActivity.this, PostActivity.class));
                        break;

                    case R.id.nav_profile:
                        selectorFragment = new ProfileFragment();
                        break;
                }
                if (selectorFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectorFragment).commit();
                }

                return true;
            }
        });

        Bundle intent = getIntent().getExtras();
        if (intent != null) {
            String profileId = intent.getString("publisherId");

            getSharedPreferences("PROFILE", MODE_PRIVATE).edit().putString("profileId", profileId).apply();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }
    }

    protected void getNotificationChange(){
        FirebaseDatabase.getInstance().getReference().child("Notifications").child(firebaseUser.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.i("onChildAdded", snapshot.getValue().toString());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.i("onChildChanged", snapshot.getValue().toString());
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.i("onChildRemoved", snapshot.getValue().toString());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



    public String getCurrentLocationName() {
        return currentLocationName;
    }

    public Double getCurrentLatitude() {
        return currentLatitude;
    }

    public Double getCurrentLongitude() {
        return currentLongitude;
    }

    public void checkPermission() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    try {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> addresses = null;
                        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        currentLocationName = addresses.get(0).getAddressLine(0);
                        currentLatitude = addresses.get(0).getLatitude();
                        currentLongitude = addresses.get(0).getLongitude();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();
        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting())) return true;
            else return false;
        } else
            return false;
    }

    public AlertDialog.Builder buildDialog(Context c) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setIcon(R.drawable.ic_nowifi);
        builder.setTitle("No Internet Connection");

        builder.setMessage("You need to have Mobile Data or WiFi to access this.");
        builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        return builder;
    }

    @Override
    public void onRestart() {
        super.onRestart();
        finish();
        Log.i(">>> MainActivity","onRestart");
        startActivity(getIntent());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}