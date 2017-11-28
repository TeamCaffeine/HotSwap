package com.teamcaffeine.hotswap.activity.login;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.activity.ProfileActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateUserFragment extends Fragment {


    public CreateUserFragment() {
        // Required empty public constructor
    }

    String TAG = "FirebaseAuth";

    private EditText edtNewUser;
    private EditText edtNewPass;
    private EditText edtFirstName;
    private EditText edtLastName;
    private Button btnSubmitNewUser;

    private FirebaseAuth mAuth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate (R.layout.activity_create_user, container,false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        edtNewUser = view.findViewById(R.id.edtEmail);
        edtNewPass = view.findViewById(R.id.edtPassword);
        edtFirstName = view.findViewById(R.id.edtFirstName);
        edtLastName = view.findViewById(R.id.edtLastName);
        btnSubmitNewUser = view.findViewById(R.id.btnSubmitNewUser);

        btnSubmitNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAuth.createUserWithEmailAndPassword(edtNewUser.getText().toString(), edtNewPass.getText().toString())
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Intent i = new Intent(getActivity(), ProfileActivity.class);

                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference users = database.getReference().child("Users");

                                    String userKey = user.getUid();
                                    DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");
                                    Date dateCreated = new Date();
                                    User userData = new User(userKey, edtNewUser.getText().toString(), edtNewPass.getText().toString(), dateFormat.format(dateCreated), edtFirstName.getText().toString(), edtLastName.getText().toString(),
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
                                    Toast.makeText(getActivity(), "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
