package com.teamcaffeine.hotswap.login;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.teamcaffeine.hotswap.utility.SessionHandler;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Check if the user is logged in - if they are, send them to their home page,
        // if not, send them to the login activity
        if (!SessionHandler.shouldLogIn(this)) {
            SessionHandler.alreadyLoggedIn(this);
        }
    }
}
