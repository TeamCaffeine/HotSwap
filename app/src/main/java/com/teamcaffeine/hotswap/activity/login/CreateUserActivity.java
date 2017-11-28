package com.teamcaffeine.hotswap.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.activity.HomeActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreateUserActivity extends BaseLoginActivity {

    String TAG = "FirebaseAuth";

    private EditText edtNewUser;
    private EditText edtNewPass;
    private EditText edtFirstName;
    private EditText edtLastName;
    private Button btnSubmitNewUser;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        mAuth = FirebaseAuth.getInstance();

        edtNewUser = findViewById(R.id.edtEmail);
        edtNewPass = findViewById(R.id.edtPassword);
        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        btnSubmitNewUser = findViewById(R.id.btnSubmitNewUser);

        btnSubmitNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAuth.createUserWithEmailAndPassword(edtNewUser.getText().toString(), edtNewPass.getText().toString())
                        .addOnCompleteListener(CreateUserActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Intent i = new Intent(getApplicationContext(), HomeActivity.class);

                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference users = database.getReference().child("Users");

                                    String userKey = user.getUid();
                                    DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");
                                    Date dateCreated = new Date();
                                    User userData = new User(userKey, edtNewUser.getText().toString(), dateFormat.format(dateCreated), edtFirstName.getText().toString(), edtLastName.getText().toString(),
                                            new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());

                                    Map<String, Object> userUpdate = new HashMap<>();
                                    userUpdate.put(userKey, userData.toMap());

                                    users.updateChildren(userUpdate);

                                    i.putExtra("userName", user.getEmail());
                                    i.putExtra("Uid", userKey);
                                    i.putExtra("fullName", edtFirstName.getText().toString() + " "  + edtLastName.getText().toString());
                                    i.putExtra("dateCreated", dateFormat.format(dateCreated));
                                    i.putExtra("loginType", "EmailPassword");
                                    startActivity(i);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(CreateUserActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}
