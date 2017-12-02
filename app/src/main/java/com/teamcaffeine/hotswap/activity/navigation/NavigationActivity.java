package com.teamcaffeine.hotswap.activity.navigation;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.teamcaffeine.hotswap.activity.ListItemFragment;
import com.teamcaffeine.hotswap.activity.ProfileFragment;
import com.teamcaffeine.hotswap.activity.messaging.InboxFragment;
import com.teamcaffeine.hotswap.messaging.ChatFragment;

public class NavigationActivity extends AppCompatActivity implements
        InboxFragment.InboxFragmentListener,
        ProfileFragment.ProfileFragmentListener,
        ListItemFragment.ListItemFragmentListener,
        SearchFragment.SearchFragmentListener {

    private final String TAG = "NavigationActivity";

    public BottomNavigationView navigation;

    //TODO set your private fragments here
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
                //TODO add a case for your fragment
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        //TODO replace these with your fragments
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
