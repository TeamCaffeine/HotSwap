package com.teamcaffeine.hotswap.login;

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
import com.teamcaffeine.hotswap.navigation.NavigationActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddUserDetailsActivity extends AppCompatActivity {

    private FirebaseUser user;
    private EditText firstName;
    private EditText lastName;
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_details);

        firstName = findViewById(R.id.edtFirstName);
        lastName = findViewById(R.id.edtLastName);
        submit = findViewById(R.id.btnSubmit);

        user = FirebaseAuth.getInstance().getCurrentUser();

        String firstName_string = firstName.getText().toString();
        String lastName_string = lastName.getText().toString();

        Intent i = new Intent(getApplicationContext(), NavigationActivity.class);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference users = database.getReference().child("Users");

        String uid = user.getUid();
        String email = user.getEmail();
        DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");
        Date dateCreated = new Date();
        User userData = new User(uid, email, dateFormat.format(dateCreated), firstName_string, lastName_string,
                new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(),
                new ArrayList<String>(), new ArrayList<String>());

        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put(uid, userData.toMap());

        users.updateChildren(userUpdate);

        i.putExtra("email", user.getEmail());
        i.putExtra("Uid", uid);
        i.putExtra("fullName", firstName_string + " "  + lastName_string);
        i.putExtra("dateCreated", dateFormat.format(dateCreated));
        startActivity(i);
    }
}