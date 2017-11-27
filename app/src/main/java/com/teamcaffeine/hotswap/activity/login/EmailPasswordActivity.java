package com.teamcaffeine.hotswap.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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

public class EmailPasswordActivity extends BaseActivity {

    String TAG = "FirebaseAuth";

    private FirebaseAuth mAuth;

    private EditText edtUser;
    private EditText edtPass;
    private Button btnSubmitLogin;
    private Button btnCreateUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        edtUser = findViewById(R.id.edtEmail);
        edtPass = findViewById(R.id.edtPassword);
        btnSubmitLogin = findViewById(R.id.btnSignIn);
        btnCreateUser = findViewById(R.id.btnCreateUser);

        btnSubmitLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAuth.signInWithEmailAndPassword(edtUser.getText().toString(), edtPass.getText().toString())
                        .addOnCompleteListener(EmailPasswordActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    final FirebaseUser user = mAuth.getCurrentUser();
                                    final Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
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
                                            i.putExtra("dateCreated",currentUser.getDateCreated());
                                            i.putExtra("loginType", "EmailPassword");
                                            startActivity(i);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });


                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        btnCreateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), CreateUserActivity.class);
                startActivity(i);
            }
        });
    }
}
