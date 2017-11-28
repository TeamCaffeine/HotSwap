package com.teamcaffeine.hotswap.activity.navigation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.activity.messaging.InboxFragment;

public class NavigationActivity extends AppCompatActivity implements InboxFragment.InboxFragmentListener, SearchFragment.SearchFragmentListener, BlankFragment2.BlankFragment2Listener {

    private final String TAG = "NavigationActivity";

    public BottomNavigationView navigation;

    //TODO set your private fragments here
    private InboxFragment inboxFragment;
    private BlankFragment2 blankFragment2;
    private SearchFragment searchFragment;
    final FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction ft;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                //TODO add a case for your fragment
                case R.id.navigation_home:
                    Log.i(TAG, "nav home: ");
                    ft = fragmentManager.beginTransaction();
                    ft.replace(R.id.dynamicContent, blankFragment2);
                    ft.commit();
                    return true;
                case R.id.navigation_search:
                    Log.i(TAG, "nav search: ");
                    ft = fragmentManager.beginTransaction();
                    ft.replace(R.id.dynamicContent, searchFragment);
                    ft.commit();
                    return true;
                case R.id.navigation_inbox:
                    Log.i(TAG, "nav inbox: ");
                    ft = fragmentManager.beginTransaction();
                    ft.replace(R.id.dynamicContent, inboxFragment);
                    ft.commit();
                    return true;
                case R.id.navigation_profile:
                    Log.i(TAG, "nav profile: ");
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //TODO replace these with your fragments
        inboxFragment = new InboxFragment();
        searchFragment = new SearchFragment();
        blankFragment2 = new BlankFragment2();
        int intentFragment = getIntent().getExtras().getInt("frgToLoad");
        switch (intentFragment) {
            case 1:
                ft = fragmentManager.beginTransaction();
                ft.replace(R.id.dynamicContent, searchFragment);
                ft.commit();
        }
    }
}
