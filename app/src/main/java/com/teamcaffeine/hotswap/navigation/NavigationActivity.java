package com.teamcaffeine.hotswap.navigation;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.utility.SessionHandler;

import java.lang.reflect.Field;
import com.teamcaffeine.hotswap.navigation.ListItemFragment;
import com.teamcaffeine.hotswap.navigation.ProfileFragment;
import com.teamcaffeine.hotswap.navigation.InboxFragment;
import com.teamcaffeine.hotswap.messaging.ChatFragment;

public class NavigationActivity extends AppCompatActivity implements
        InboxFragment.InboxFragmentListener,
        ProfileFragment.ProfileFragmentListener,
        ListItemFragment.ListItemFragmentListener,
        SearchFragment.SearchFragmentListener {

    private final String TAG = "NavigationActivity";

    public BottomNavigationView navigation;

    private ListItemFragment listItemFragment;
    private InboxFragment inboxFragment;
    private ProfileFragment profileFragment;
    private SearchFragment searchFragment;
    private ChatFragment chatFragment;

    final FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction ft;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Log.i(TAG, "nav home: ");
                    ft = fragmentManager.beginTransaction();
                    ft.replace(R.id.dynamicContent, listItemFragment);
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
                    ft.replace(R.id.dynamicContent, chatFragment);
                    ft.commit();
                    return true;
                case R.id.navigation_profile:
                    Log.i(TAG, "nav profile: ");
                    ft = fragmentManager.beginTransaction();
                    ft.replace(R.id.dynamicContent, profileFragment);
                    ft.commit();
                    return true;
            }
            return false;
        }
    };

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionHandler.shouldLogIn(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Log.e("***", "BEFORE DATABASE");
//        FirebaseDatabase kappa = FirebaseDatabase.getInstance();
//        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
//            Log.e("***", "ITS NOT NULL MY BOI");
//        } else {
//            Log.e("***", "ITS NULL MY BOI");
//        }
//        Log.e("***", String.valueOf(kappa.getReference()));

        FirebaseAuth.getInstance().signInWithEmailAndPassword("william@william.com","william").addOnFailureListener(
                this, new OnFailureListener() {

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("***", "DID NOT WORK, FAILURE LISTENER");
                        Log.e("***", e.toString());
                    }
                }
        ).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Log.e("***", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                        } else {
                            Log.e("***", "DID NOT WORK");
                        }
                    }
                });

        Log.e("***", "AFTER DATABASE");

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
        inboxFragment = new InboxFragment();
        profileFragment = new ProfileFragment();
        searchFragment = new SearchFragment();
        chatFragment = new ChatFragment();

        int intentFragment = getIntent().getExtras().getInt("frgToLoad");
        switch (intentFragment) {
            case 1:
                ft = fragmentManager.beginTransaction();
                ft.replace(R.id.dynamicContent, searchFragment);
                ft.commit();
        }

        navigation.setSelectedItemId(R.id.navigation_home);
    }
}
