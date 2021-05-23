package com.stejeetech.galampon;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import Fragments.ProfileFragment;

public class ViewOtherUserActivity extends AppCompatActivity {
    String profileId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_other_user);
        Intent intent = getIntent();
        profileId = intent.getStringExtra("profileId");
        getSharedPreferences("PROFILE", MODE_PRIVATE).edit().putString("profileId", profileId).apply();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
            Log.i(">>> ViewOtherUserActivity", "FINISH");
            finish();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }
}