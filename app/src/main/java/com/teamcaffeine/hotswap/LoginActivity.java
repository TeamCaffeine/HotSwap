package com.teamcaffeine.hotswap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.SignInButton;

public class LoginActivity extends AppCompatActivity {

    SignInButton btnGoogleSignIn;
    LoginButton btnFacebookLogin;
    Button btnEmailLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
