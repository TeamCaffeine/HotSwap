package com.teamcaffeine.hotswap.activity.login;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.activity.ProfileActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseLoginFragment extends Fragment {


    public BaseLoginFragment() {
        // Required empty public constructor
    }

    private FirebaseAuth mAuth;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null)
        // if user is already signed in, update the Bundle accordingly
//        ////////////FirebaseUser currentUser = mAuth.getCurrentUser();
        final FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            final Intent i = new Intent(getActivity(), ProfileActivity.class);
            i.putExtra("userName", user.getEmail());
            i.putExtra("Uid", user.getUid());

            // Retreive user first and last name
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference users = database.getReference().child("Users/");
            Query userFullName = users.child(user.getUid());

            userFullName.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User currentUser = dataSnapshot.getValue(User.class);
                    String fullName = currentUser.getFirstName() + " " + currentUser.getLastName();
                    i.putExtra("fullName", fullName);
                    i.putExtra("dateCreated", currentUser.getDateCreated());
                    startActivity(i);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }
    // [END on_start_check_user]

    public void signOut() {
        mAuth.signOut();
        LoginManager.getInstance().logOut();
        Toast.makeText(getActivity(), R.string.successfully_signed_out,
                Toast.LENGTH_LONG).show();
    }

    public FirebaseAuth getmAuth() {
        return mAuth;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }
}
