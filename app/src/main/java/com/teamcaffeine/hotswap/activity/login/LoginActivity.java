package com.teamcaffeine.hotswap.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.activity.HomeActivity;

public class LoginActivity extends BaseLoginActivity {

    SignInButton btnGoogleSignIn;
    LoginButton btnFacebookLogin;
    Button btnEmailLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent i = new Intent(this, HomeActivity.class);
            startActivity(i);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
}
