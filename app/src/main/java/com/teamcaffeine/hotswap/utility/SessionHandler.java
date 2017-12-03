package com.teamcaffeine.hotswap.utility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamcaffeine.hotswap.login.AddUserDetailsActivity;
import com.teamcaffeine.hotswap.login.LoginActivity;
import com.teamcaffeine.hotswap.login.User;
import com.teamcaffeine.hotswap.navigation.NavigationActivity;

/**
 * Safety class to route users away from activities if their login status is incorrect
 */
public final class SessionHandler {

    private static final String userTable = "users";

    public static boolean shouldLogIn(final Context context) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent login = new Intent(context, LoginActivity.class);
            context.startActivity(login);
            ((Activity) context).finish();
            return true;
        }
        return false;
    }

    public static boolean alreadyLoggedIn(final Context context) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference().child(userTable).child(firebaseUser.getUid());

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);

                    if (!user.getMemberSince().isEmpty()) {
                        // We've already filled out basic user data, go straight to nav
                        Intent appEntry = new Intent(context, NavigationActivity.class);
                        context.startActivity(appEntry);
                        ((Activity) context).finish();
                    } else {
                        // We still need to fill out basic user data, go to add user details activity
                        Intent appEntry = new Intent(context, AddUserDetailsActivity.class);
                        context.startActivity(appEntry);
                        ((Activity) context).finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("SessionHandler", "The read failed: " + databaseError.getCode());
                }
            });

            return true;
        }
        return false;
    }
}
