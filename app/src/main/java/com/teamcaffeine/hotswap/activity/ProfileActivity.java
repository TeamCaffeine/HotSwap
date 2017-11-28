package com.teamcaffeine.hotswap.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.activity.login.BaseLoginActivity;

public class ProfileActivity extends BaseLoginActivity {

    // create objects to reference layout objects
    private TextView name;
    private TextView memberSince;
    private Button logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // get the bundle from the intent
        Bundle bundle = getIntent().getExtras();

        // set references to layout objects
        name = findViewById(R.id.txtName);
        memberSince = findViewById(R.id.txtMemberSince);
        logout = findViewById(R.id.btnLogout);

        // get the user's name from the bundle
        // and set it in the layout
        String fullName = bundle.getString("fullName");
        name.setText(fullName);

        // get the date the user created their account from the bundle
        // set "Member Since" equal to the date the user created their account
        String dateCreated = bundle.getString("dateCreated");
        memberSince.setText("Member Since: " + dateCreated);

        // Set logout functionality of the Logout button
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
    }
}
