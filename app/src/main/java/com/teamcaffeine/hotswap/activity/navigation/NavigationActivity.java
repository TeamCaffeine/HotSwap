package com.teamcaffeine.hotswap.activity.navigation;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.teamcaffeine.hotswap.R;

public class NavigationActivity extends AppCompatActivity {

    private final String TAG = "NavigationActivity";

    LinearLayout dynamicContent;

    public BottomNavigationView navigation;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent i;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Log.i(TAG, "nav home: ");
                    i = new Intent(getApplicationContext(), NavigationHomeActivity.class);
                    startActivity(i);
                    finish();
                    return true;
                case R.id.navigation_search:
                    Log.i(TAG, "nav search: ");
                    i = new Intent(getApplicationContext(), NavigationSearchActivity.class);
                    startActivity(i);
                    finish();
                    return true;
                case R.id.navigation_inbox:
                    Log.i(TAG, "nav inbox: ");
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        dynamicContent = (LinearLayout) findViewById(R.id.dynamicContent);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }
}
