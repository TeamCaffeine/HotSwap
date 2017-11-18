package com.teamcaffeine.hotswap;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionManager.shouldLogIn(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button logout = (Button) findViewById(R.id.btnFacebookLogout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthUI.getInstance()
                        .signOut(HomeActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Intent logout = new Intent(HomeActivity.this, LoginActivity.class);
                                    startActivity(logout);
                                    finish();
                                } else {
                                    // TODO: Handle unsuccessful sign out
                                }
                            }
                        });
            }
        });
    }
}
