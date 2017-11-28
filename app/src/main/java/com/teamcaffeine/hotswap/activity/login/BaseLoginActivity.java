package com.teamcaffeine.hotswap.activity.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
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
import com.teamcaffeine.hotswap.activity.HomeActivity;

public class BaseLoginActivity extends AppCompatActivity {

    // (Megan) Trying something out:
    // The BaseLoginActivity is what creates the connection to Firebase
    // and then everything else extends the BaseLoginActivity
    // That way, every Activity can access the Firebase connection
    // so, for example, the Profile Activity can use the logout functionality

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null)
        // if user is already signed in, update the Bundle accordingly
        final FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            final Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            i.putExtra("email", user.getEmail());
            i.putExtra("Uid", user.getUid());

            // Retreive user first and last name
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference users = database.getReference().child("Users");
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
        Toast.makeText(BaseLoginActivity.this, R.string.successfully_signed_out,
                Toast.LENGTH_LONG).show();
    }

    // This was here before Megan wanted to try something out

    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

    public FirebaseAuth getmAuth() {
        return mAuth;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

}
