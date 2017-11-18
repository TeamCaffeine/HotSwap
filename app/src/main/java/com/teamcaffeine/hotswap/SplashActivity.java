package com.teamcaffeine.hotswap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the user is logged in - if they are, send them to their home page,
        // if not, send them to the login activity
        Intent appEntry;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            appEntry = new Intent(this, HomeActivity.class);
        } else {
            appEntry = new Intent(this, LoginActivity.class);
        }
        startActivity(appEntry);
        finish();
    }
}
