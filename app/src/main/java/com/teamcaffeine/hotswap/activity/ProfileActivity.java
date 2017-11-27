package com.teamcaffeine.hotswap.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.teamcaffeine.hotswap.R;

public class ProfileActivity extends AppCompatActivity {

    private TextView name;
    private TextView memberSince;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Bundle bundle = getIntent().getExtras();

        name = findViewById(R.id.txtName);
        String fullName = bundle.getString("fullName");
        name.setText(fullName);

        memberSince = findViewById(R.id.txtMemberSince);
        String dateCreated = bundle.getString("dateCreated");
        memberSince.setText("Member Since: " + dateCreated);
    }
}
