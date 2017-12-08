package com.teamcaffeine.hotswap.navigation;

import android.annotation.SuppressLint;
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

import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.utility.SessionHandler;

import java.lang.reflect.Field;

/*
* Thanks to Segun Famisa for reference initialization
* of persistent BottomNavigationView.
*
* https://github.com/segunfamisa/bottom-navigation-demo
*/

public class NavigationActivity extends AppCompatActivity implements
        ChatFragment.ChatFragmentListener,
        ProfileFragment.ProfileFragmentListener,
        SearchFragment.SearchFragmentListener,
        HomeFragment.HomeFragmentListener {

    private final String TAG = "NavigationActivity";

    private BottomNavigationView navigation;

    private Fragment profileFragment;
    private Fragment searchFragment;
    private Fragment chatFragment;
    private Fragment homeFragment;

    private Fragment[] fragArr;

    // State maintenance
    private static final String SELECTED_ITEM = "arg_selected_item";
    private int mSelectedItem;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionHandler.shouldLogIn(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return selectNavigationItem(item);
            }
        });

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

        MenuItem selectedItem;
        if (savedInstanceState != null) {
            // Our fragments already exist, fetch their reference by tag
            mSelectedItem = savedInstanceState.getInt(SELECTED_ITEM, 0);
            selectedItem = navigation.getMenu().findItem(mSelectedItem);

            FragmentManager fragmentManager = getSupportFragmentManager();
            homeFragment = fragmentManager.findFragmentByTag("homeFragment");
            searchFragment = fragmentManager.findFragmentByTag("searchFragment");
            chatFragment = fragmentManager.findFragmentByTag("chatFragment");
            profileFragment = fragmentManager.findFragmentByTag("profileFragment");
        } else {
            // No fragments exist yet, instantiate them
            selectedItem = navigation.getMenu().getItem(0);

            homeFragment = new HomeFragment();
            searchFragment = new SearchFragment();
            chatFragment = new ChatFragment();
            profileFragment = new ProfileFragment();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.container, homeFragment, "homeFragment");
            ft.add(R.id.container, searchFragment, "searchFragment");
            ft.add(R.id.container, chatFragment, "chatFragment");
            ft.add(R.id.container, profileFragment, "profileFragment");

            ft.commit();
        }

        fragArr = new Fragment[]{homeFragment, searchFragment, chatFragment, profileFragment};

        selectNavigationItem(selectedItem);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_ITEM, mSelectedItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        MenuItem homeItem = navigation.getMenu().getItem(0);
        if (mSelectedItem != homeItem.getItemId()) {
            // Go back home if we're not on the home page
            selectNavigationItem(homeItem);
        } else {
            super.onBackPressed();
        }
    }

    private boolean selectNavigationItem(MenuItem item) {
        Fragment frag = null;
        switch (item.getItemId()) {
            case R.id.navigation_home:
                Log.i(TAG, "nav home");
                frag = homeFragment;
                break;
            case R.id.navigation_search:
                Log.i(TAG, "nav search");
                frag = searchFragment;
                break;
            case R.id.navigation_inbox:
                Log.i(TAG, "nav chat");
                frag = chatFragment;
                break;
            case R.id.navigation_profile:
                Log.i(TAG, "nav profile");
                frag = profileFragment;
                break;
            default:
                Log.e(TAG, "Unable to locate fragment to launch with id: " + item.getItemId());
                return false;
        }

        mSelectedItem = item.getItemId();

        if (frag != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            // Build transaction for existing fragments
            for (int i = 0; i < navigation.getMenu().size(); i++) {
                MenuItem menuItem = navigation.getMenu().getItem(i);
                if (menuItem.getItemId() == item.getItemId()) {
                    menuItem.setChecked(true);
                    ft.show(fragArr[i]);
                } else {
                    ft.hide(fragArr[i]);
                }
            }

            ft.commit();
            return true;
        }
        return false;
    }
}
