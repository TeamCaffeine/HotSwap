package com.teamcaffeine.hotswap.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

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

public class AddUserDetailsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private EditText firstName;
    private EditText lastName;
    private EditText phoneNumber;
    private Button addAddress;
    private Button addPayment;
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_details);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        firstName = findViewById(R.id.edtFirstName);
        lastName = findViewById(R.id.edtLastName);
        phoneNumber = findViewById(R.id.edtPhoneNumber);
        addAddress = findViewById(R.id.btnAddress);
        addPayment = findViewById(R.id.btnPayment);
        submit = findViewById(R.id.btnSubmit);

        String firstName_string = firstName.getText().toString();
        String lastName_string = lastName.getText().toString();
        String phoneNumber_string = phoneNumber.getText().toString();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference users = database.getReference().child("Users");

        boolean addedDetails = true;
        String uid = user.getUid();
        String email = user.getEmail();
        DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");
        Date memberSince = new Date();
        User userData = new User(addedDetails,uid,firstName_string, lastName_string,email,dateFormat.format(memberSince),phoneNumber_string);

        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put(uid, userData.toMap());

        users.updateChildren(userUpdate);

        Intent i = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(i);
    }
}
