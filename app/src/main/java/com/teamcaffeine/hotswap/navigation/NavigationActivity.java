package com.teamcaffeine.hotswap.navigation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.utility.SessionHandler;

import java.lang.reflect.Field;

public class NavigationActivity extends AppCompatActivity implements
        ChatFragment.ChatFragmentListener,
        ProfileFragment.ProfileFragmentListener,
        ListItemFragment.ListItemFragmentListener,
        SearchFragment.SearchFragmentListener {

    private final String TAG = "NavigationActivity";

    public BottomNavigationView navigation;

    private ListItemFragment listItemFragment;
    private ProfileFragment profileFragment;
    private SearchFragment searchFragment;
    private ChatFragment chatFragment;

    SharedPreferences prefs;
    private String navigationIndexKey;

    final FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction ft;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            return selectNavigationItem(item);
        }
    };

    private boolean selectNavigationItem(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                Log.i(TAG, "nav home");
                inflateFragment(listItemFragment, 0);
                return true;
            case R.id.navigation_search:
                Log.i(TAG, "nav search");
                inflateFragment(searchFragment, 1);
                return true;
            case R.id.navigation_inbox:
                Log.i(TAG, "nav chat");
                inflateFragment(chatFragment, 2);
                return true;
            case R.id.navigation_profile:
                Log.i(TAG, "nav profile");
                inflateFragment(profileFragment, 3);
                return true;
            default:
                Log.e(TAG, "Unable to locate fragment to launch with id: " + item.getItemId());
        }
        return false;
    }

    private void inflateFragment(Fragment fragment, int index) {
        prefs.edit().putInt(navigationIndexKey, index).apply();
        ft = fragmentManager.beginTransaction();
        ft.replace(R.id.dynamicContent, fragment);
        ft.commit();
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionHandler.shouldLogIn(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        prefs = this.getSharedPreferences(
                getString(R.string.base_package_name), Context.MODE_PRIVATE);
        navigationIndexKey = getString(R.string.base_package_name) + "navigation.index";

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navigation.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                //noinspection RestrictedApi
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                //noinspection RestrictedApi
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "Unable to get shift mode field", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Unable to change value of shift mode", e);
        }


        listItemFragment = new ListItemFragment();
        profileFragment = new ProfileFragment();
        searchFragment = new SearchFragment();
        chatFragment = new ChatFragment();

        navigation.setSelectedItemId(navigation.getMenu().getItem(prefs.getInt(navigationIndexKey, 0)).getItemId());
    }
}
