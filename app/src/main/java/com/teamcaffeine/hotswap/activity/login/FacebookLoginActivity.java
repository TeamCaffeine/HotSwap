package com.teamcaffeine.hotswap.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
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
 * Firebase Authentication using a Facebook access token.
 */
public class FacebookLoginActivity extends BaseActivity {

    private static final String TAG = "FacebookLogin";

    private TextView mStatusTextView;
    private TextView mDetailTextView;

    //*********************************************
    // (Megan) Trying something out:
    // The BaseActivity has the Firebase connection
    //*********************************************


//    // [START declare_auth]
//    private FirebaseAuth mAuth;
//    // [END declare_auth]

    private FirebaseAuth mAuth;

    private CallbackManager mCallbackManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook);

        // Views
        mStatusTextView = findViewById(R.id.status);
        mDetailTextView = findViewById(R.id.detail);

        //*********************************************
        // (Megan) Trying something out:
        // The BaseActivity has the Firebase connection
        // therefore we do not need the signout button here, we will use the signout button in Profile
        //*********************************************


//        findViewById(R.id.button_facebook_signout).setOnClickListener(this);

        //*********************************************
        // (Megan) Trying something out:
        // The BaseActivity has the Firebase connection
        //*********************************************


//        // [START initialize_auth]
//        // Initialize Firebase Auth
//        mAuth = FirebaseAuth.getInstance();
//        // [END initialize_auth]

        mAuth = getmAuth();

        // [START initialize_fblogin]
        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.button_facebook_login);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // [START_EXCLUDE]
//                updateUI(null);
                // [END_EXCLUDE]
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // [START_EXCLUDE]
//                updateUI(null);
                // [END_EXCLUDE]
            }
        });
        // [END initialize_fblogin]
    }


    //*********************************************
    // (Megan) Trying something out:
    // The BaseActivity has the Firebase connection
    //*********************************************


//    // [START on_start_check_user]
//    @Override
//    public void onStart() {
//        super.onStart();
//        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);
//    }
//    // [END on_start_check_user]

    // [START on_activity_result]
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
    // [END on_activity_result]

    // [START auth_with_facebook]
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, send user to app home page
                            Log.d(TAG, "signInWithCredential:success");
                            Intent i = checkIfUserHasSignedInBefore();
//                            Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                            startActivity(i);
//                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() != null) {
                                Toast.makeText(FacebookLoginActivity.this, task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(FacebookLoginActivity.this, R.string.authentication_failed,
                                        Toast.LENGTH_SHORT).show();
                            }
//                            updateUI(null);
                            LoginManager.getInstance().logOut();
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

    public Intent checkIfUserHasSignedInBefore() {
        final FirebaseUser user = mAuth.getCurrentUser();
        final Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
        i.putExtra("userName", user.getEmail());
        i.putExtra("Uid", user.getUid());

        // Retreive user first and last name
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference users = database.getReference().child("Users/");
        Query userFullName = users.child(user.getUid());

        if (userFullName != null) {
            userFullName.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User currentUser = dataSnapshot.getValue(User.class);
                    String fullName = currentUser.getFirstName() + " " + currentUser.getLastName();
                    i.putExtra("fullName", fullName);
                    i.putExtra("dateCreated", currentUser.getDateCreated());
                    i.putExtra("loginType", "EmailPassword");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {

        }

        return i;
    }
    // [END auth_with_facebook]

    //*********************************************
    // (Megan) Trying something out:
    // The BaseActivity has the Firebase connection
    //*********************************************


//    public void signOut() {
//        mAuth.signOut();
//        LoginManager.getInstance().logOut();
//
//        updateUI(null);
//        Toast.makeText(FacebookLoginActivity.this, R.string.successfully_signed_out,
//                Toast.LENGTH_SHORT).show();
//    }

    //*********************************************
    // (Megan) Trying something out:
    // The BaseActivity has the Firebase connection
    // so the FacebookActivity will open the ProfileActivity
    // so we do not need to update the UI
    // and we do not the the onClick
    //*********************************************

//    private void updateUI(FirebaseUser user) {
//        hideProgressDialog();
//        if (user != null) {
//            mStatusTextView.setText(getString(R.string.facebook_status_fmt, user.getDisplayName()));
//            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));
//
//            findViewById(R.id.button_facebook_login).setVisibility(View.GONE);
//            findViewById(R.id.button_facebook_signout).setVisibility(View.VISIBLE);
//        } else {
//            mStatusTextView.setText(R.string.signed_out);
//            mDetailTextView.setText(null);
//
//            findViewById(R.id.button_facebook_login).setVisibility(View.VISIBLE);
//            findViewById(R.id.button_facebook_signout).setVisibility(View.GONE);
//        }
//    }
//
//    @Override
//    public void onClick(View v) {
//        int i = v.getId();
//        if (i == R.id.button_facebook_signout) {
//            signOut();
//        }
//    }
}
