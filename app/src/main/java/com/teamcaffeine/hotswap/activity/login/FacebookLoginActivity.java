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
import com.teamcaffeine.hotswap.activity.HomeActivity;

/**
 * Firebase Authentication using a Facebook access token.
 */
public class FacebookLoginActivity extends BaseLoginActivity {

    private static final String TAG = "FacebookLogin";

    private TextView mStatusTextView;
    private TextView mDetailTextView;

    private FirebaseAuth mAuth;

    private CallbackManager mCallbackManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook);

        // Views
        mStatusTextView = findViewById(R.id.status);
        mDetailTextView = findViewById(R.id.detail);

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
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });
        // [END initialize_fblogin]
    }

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
                            //TODO: write method to check if user still needs to add user details
                            Intent i = checkIfUserHasSignedInBefore();
                            //***keeping this intent here for now, commented out
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
                            LoginManager.getInstance().logOut();
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

    //TODO: finish this method, check logic
    public Intent checkIfUserHasSignedInBefore() {
        final FirebaseUser user = mAuth.getCurrentUser();
        final Intent i = new Intent(getApplicationContext(), HomeActivity.class);
        i.putExtra("email", user.getEmail());
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
                    i.putExtra("dateCreated", currentUser.getMemberSince());
                    i.putExtra("loginType", "EmailPassword");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            //TODO: finish else
        }

        return i;
    }
    // [END auth_with_facebook]
}
