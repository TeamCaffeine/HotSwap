package com.teamcaffeine.hotswap.activity.login2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.teamcaffeine.hotswap.R;

@SuppressLint("Registered")
public class GoogleSignInManager extends AppCompatActivity {
    private LoginActivity loginActivity;
    private final GoogleSignInClient mGoogleSignInClient;

    public GoogleSignInManager(LoginActivity loginActivity) {
        this.loginActivity = loginActivity;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(loginActivity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(loginActivity, gso);
    }

    public void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        loginActivity.startActivityForResult(signInIntent, LoginActivity.RC_SIGN_IN);
    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        Toast.makeText(loginActivity, R.string.successfully_signed_out,
                Toast.LENGTH_LONG).show();
    }
}
