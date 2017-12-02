package com.teamcaffeine.hotswap.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.navigation.NavigationActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddUserDetailsActivity extends AppCompatActivity {

    // create objects for Firebase references
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference users;
    private String userTable = "Users";

    // create objects to hold views
    private EditText edtFirstName;
    private EditText edtLastName;
    private EditText edtPhoneNumber;
    private Button btnAddAddress;
    private Button btnAddPayment;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_details);

        // get an instance of the Firebase and get the reference to the current user
        user = FirebaseAuth.getInstance().getCurrentUser();

        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        btnAddAddress = findViewById(R.id.btnAddress);
        btnAddPayment = findViewById(R.id.btnPayment);
        btnSubmit = findViewById(R.id.btnSubmit);


        // get an instance of the database
        database = FirebaseDatabase.getInstance();
        // get a reference to the Users table
        users = database.getReference().child(userTable);
        // set the functionality of the "Submit" button
        // see the "submit()" method below
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });

        // set functionality of the "add address" button
        btnAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // set functionality of the "add payment" button
        btnAddPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    // Submit method
    // When the user clicks "submit," their full user account is created in Firebase
    public void submit() {
        // get the strings in each of the Edit Texts
        String firstName = edtFirstName.getText().toString();
        String lastName = edtLastName.getText().toString();
        String phoneNumber = edtPhoneNumber.getText().toString();

        // only allow the user to move forward through login if they have entered a name and phone number
        if (firstName.isEmpty() && lastName.isEmpty() && phoneNumber.isEmpty()) {

            // now that their details have been added, we set the addDetails flag to true
            // this flag will be used on login in the future to indicate that the user does not need to
            // go to the AddUserDetailsActivity next time they login
            boolean addedDetails = true;
            // get the user ID and email from the existing database entry that was created on the initial login
            String uid = user.getUid();
            String email = user.getEmail();
            // create a date format for the memberSince attribute
            DateFormat dateFormat = new SimpleDateFormat("MMMM, yyyy");
            // set the memberSince attribute to the current date
            // this will show on the user's profile to indicate how long they have been a HotSwap member
            Date memberSince = new Date();

            // create a new User object will all user details
            User userData = new User(addedDetails, uid, firstName, lastName, email, dateFormat.format(memberSince), phoneNumber);

            // create a hashmap object
            // this will be used to enter the data into firebase
            Map<String, Object> userUpdate = new HashMap<>();
            // enter the information from the User object into the hashmap
            userUpdate.put(uid, userData.toMap());

            // update the existing user database entry to include the new information
            users.updateChildren(userUpdate);

            // now that te user information has been updated, the user can enter the app
            Intent i = new Intent(AddUserDetailsActivity.this, NavigationActivity.class);
            startActivity(i);
        } else {
            // if the user did not enter all of their details, show a toast to instruct them to enter all detals
            Toast.makeText(AddUserDetailsActivity.this, "@string/enter_all_details",
                    Toast.LENGTH_LONG).show();
        }
    }
}
