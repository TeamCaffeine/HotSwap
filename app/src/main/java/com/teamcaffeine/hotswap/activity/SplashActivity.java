package com.teamcaffeine.hotswap.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.teamcaffeine.hotswap.activity.login2.LoginActivity;
import com.teamcaffeine.hotswap.activity.navigation.NavigationActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the user is logged in - if they are, send them to their home page,
        // if not, send them to the login activity
        Intent appEntry;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            appEntry = new Intent(this, NavigationActivity.class);
        } else {
            appEntry = new Intent(this, LoginActivity.class);
        }
        startActivity(appEntry);
        finish();
    }
}
