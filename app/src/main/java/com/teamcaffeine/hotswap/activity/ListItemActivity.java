package com.teamcaffeine.hotswap.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.utility.SessionHandler;

public class ListItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionHandler.shouldLogIn(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_item);
    }
}
