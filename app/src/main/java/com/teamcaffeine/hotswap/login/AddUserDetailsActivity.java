package com.teamcaffeine.hotswap.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.navigation.AddressesFragment;
import com.teamcaffeine.hotswap.navigation.NavigationActivity;
import com.teamcaffeine.hotswap.utility.SessionHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddUserDetailsActivity extends AppCompatActivity {

    // create objects for Firebase references
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference users;
    private String userTable = "users";

    // create objects to hold views
    private EditText edtFirstName;
    private EditText edtLastName;
    private EditText edtPhoneNumber;
    private Button btnAddPayment;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionHandler.shouldLogIn(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_details);

        // populate the listview with the user's addresses
        // step 1: instantiate the Address Fragment
        AddressesFragment addressesFragment = new AddressesFragment();
        // step 2: begin the fragment transaction
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // step 3: add fragment to the activity state
        ft.add(R.id.addressContent, addressesFragment);
        // stop 4: commit the transaction
        ft.commit();

        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        btnAddPayment = findViewById(R.id.btnPayment);
        btnSubmit = findViewById(R.id.btnSubmit);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        users = database.getReference().child(userTable);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });

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
        final String firstName = edtFirstName.getText().toString();
        final String lastName = edtLastName.getText().toString();
        final String phoneNumber = edtPhoneNumber.getText().toString();

        // only allow the user to move forward through login if they have entered a name and phone number
        if (!firstName.isEmpty() && !lastName.isEmpty() && !phoneNumber.isEmpty()) {
            DatabaseReference ref = users.child(firebaseUser.getUid());
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);

                    DateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
                    Date memberSince = new Date();

                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setPhoneNumber(phoneNumber);
                    user.setMemberSince(dateFormat.format(memberSince));

                    Map<String, Object> userUpdate = new HashMap<>();
                    userUpdate.put(firebaseUser.getUid(), user.toMap());

                    users.updateChildren(userUpdate);

                    Intent i = new Intent(AddUserDetailsActivity.this, NavigationActivity.class);
                    startActivity(i);
                    finish();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("AddUserDetailsActivity","The read failed: " + databaseError.getCode());
                }
            });
        } else {
            // if the user did not enter all of their details, show a toast to instruct them to enter all detals
            Toast.makeText(AddUserDetailsActivity.this, R.string.enter_all_details,
                    Toast.LENGTH_LONG).show();
        }
    }
}
