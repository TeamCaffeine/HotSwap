package com.teamcaffeine.hotswap.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.navigation.NavigationActivity;
import com.teamcaffeine.hotswap.utility.SessionHandler;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    EditText editEmail;
    EditText editPassword;
    Button buttonSignIn;
    Button buttonCreateAccount;
    SignInButton googleSignInButton;
    LoginButton facebookLoginButton;

    private String userTable = "users";
    private FirebaseUser firebaseUser;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference users = database.getReference().child(userTable);


    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;

    private static final String EPLogTag = "EmailPasswordSignIn";
    private static final String GoogleLogTag = "GoogleSignIn";
    private static final String FacebookLogTag = "FacebookSignIn";

    public static final int GOOGLE_SIGN_IN = 9001;

    public ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(LoginActivity.this);
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
    protected void onCreate(Bundle savedInstanceState) {
        // Move us out of this activity if we're already logged in
        SessionHandler.alreadyLoggedIn(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // EMAIL-PASSWORD LOGIN UI ELEMENTS
        editEmail = (EditText) findViewById(R.id.editEmail);
        editPassword = (EditText) findViewById(R.id.editPassword);
        buttonSignIn = (Button) findViewById(R.id.buttonSignIn);
        buttonCreateAccount = (Button) findViewById(R.id.buttonCreateAccount);

        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount(editEmail.getText().toString(), editPassword.getText().toString());
            }
        });

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithEmailPassword(editEmail.getText().toString(), editPassword.getText().toString());
            }
        });


        // GOOGLE LOGIN UI ELEMENTS
        googleSignInButton = (SignInButton) findViewById(R.id.googleSignInButton);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(this.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });


        // FACEBOOK LOGIN UI ELEMENTS
        facebookLoginButton = (LoginButton) findViewById(R.id.facebookLoginButton);

        mCallbackManager = CallbackManager.Factory.create();
        facebookLoginButton.setReadPermissions("email", "public_profile");
        facebookLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(FacebookLogTag, "facebook:onSuccess:" + loginResult);
                firebaseAuthWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(FacebookLogTag, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(FacebookLogTag, "facebook:onError", error);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(GoogleLogTag, "Google sign in failed", e);
                // ...
            }
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void addUserEntry() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        User user = new User(firebaseUser.getUid(), firebaseUser.getEmail());

        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put(firebaseUser.getUid(), user.toMap());

        users.updateChildren(userUpdate);
    }

    private void handleFirstTimeUser() {
        // Add the user to the database if they do not already exist
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (!dataSnapshot.hasChild(firebaseUser.getUid())) {
                    addUserEntry();

                    Intent addUserDetails = new Intent(LoginActivity.this, AddUserDetailsActivity.class);
                    startActivity(addUserDetails);
                    finish();
                } else if (dataSnapshot.child(firebaseUser.getUid()).getValue(User.class).getMemberSince().isEmpty()) {
                    Intent addUserDetails = new Intent(LoginActivity.this, AddUserDetailsActivity.class);
                    startActivity(addUserDetails);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("LoginActivity", "The read failed: " + databaseError.getCode());
            }
        });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(GoogleLogTag, "firebaseAuthWithGoogle:" + acct.getId());

        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, send user to app home page
                            Log.d(GoogleLogTag, "signInWithCredential:success");

                            handleFirstTimeUser();

                            Intent nav = new Intent(LoginActivity.this, NavigationActivity.class);
                            startActivity(nav);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(GoogleLogTag, "signInWithCredential:failure", task.getException());
                            if (task.getException() != null) {
                                Toast.makeText(LoginActivity.this, task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(LoginActivity.this, R.string.authentication_failed,
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        hideProgressDialog();
                    }
                });
    }

    private void firebaseAuthWithFacebook(AccessToken token) {
        Log.d(FacebookLogTag, "handleFacebookAccessToken:" + token);

        showProgressDialog();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, send user to app home page
                            Log.d(FacebookLogTag, "signInWithCredential:success");

                            handleFirstTimeUser();

                            Intent nav = new Intent(LoginActivity.this, NavigationActivity.class);
                            startActivity(nav);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(FacebookLogTag, "signInWithCredential:failure", task.getException());
                            if (task.getException() != null) {
                                Toast.makeText(LoginActivity.this, task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(LoginActivity.this, R.string.authentication_failed,
                                        Toast.LENGTH_LONG).show();
                            }
                            LoginManager.getInstance().logOut();
                        }

                        hideProgressDialog();
                    }
                });
    }

    private void createAccount(String email, String password) {
        Log.d(EPLogTag, "createAccount:" + email);
        if (!formIsValid()) {
            return;
        }

        showProgressDialog();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(EPLogTag, "createUserWithEmail:success");

                            handleFirstTimeUser();

                            Intent nav = new Intent(getApplicationContext(), NavigationActivity.class);
                            startActivity(nav);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(EPLogTag, "createUserWithEmail:failure", task.getException());
                            if (task.getException() != null) {
                                Toast.makeText(LoginActivity.this, task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(LoginActivity.this, R.string.authentication_failed,
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        hideProgressDialog();
                    }
                });
    }

    private void signInWithEmailPassword(String email, String password) {
        Log.d(EPLogTag, "signIn:" + email);
        if (!formIsValid()) {
            return;
        }

        showProgressDialog();

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, send user to app home page

                            Log.d(EPLogTag, "signInWithEmail:success");

                            handleFirstTimeUser();

                            Intent nav = new Intent(getApplicationContext(), NavigationActivity.class);
                            startActivity(nav);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(EPLogTag, "signInWithEmail:failure", task.getException());
                            if (task.getException() != null) {
                                Toast.makeText(LoginActivity.this, task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(LoginActivity.this, R.string.authentication_failed,
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        hideProgressDialog();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        this.startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    private boolean formIsValid() {
        boolean valid = true;

        String email = editEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            editEmail.setError("Required.");
            valid = false;
        } else {
            editEmail.setError(null);
        }

        String password = editPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            editPassword.setError("Required.");
            valid = false;
        } else {
            editPassword.setError(null);
        }

        return valid;
    }

}
