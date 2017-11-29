package com.teamcaffeine.hotswap.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.activity.HomeActivity;

public class LoginActivity extends AppCompatActivity {

    SignInButton btnGoogleSignIn;
    LoginButton btnFacebookLogin;
    Button btnEmailLogin;

    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseDatabase database;
    DatabaseReference users;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // get Firebase authorization instance
        mAuth = FirebaseAuth.getInstance();

        // get reference to user
        currentUser = mAuth.getCurrentUser();

        // get instance of the database
        database = FirebaseDatabase.getInstance();
        // get reference to the users table
        users = database.getReference("Users");








        btnGoogleSignIn = (SignInButton) findViewById(R.id.google_sign_in_button);
        btnFacebookLogin = (LoginButton) findViewById(R.id.facebook_login_button);
        btnEmailLogin = (Button) findViewById(R.id.email_login_button);

        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, GoogleSignInActivity.class);
                startActivity(intent);
            }
        });

        btnFacebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, FacebookLoginActivity.class);
                startActivity(intent);
            }
        });

        btnEmailLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, EmailPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    public void onStart() {
        super.onStart();
        if (currentUser != null) {
            final Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(i);
        }

    }
}
