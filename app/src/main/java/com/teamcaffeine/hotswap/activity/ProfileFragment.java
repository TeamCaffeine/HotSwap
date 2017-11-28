package com.teamcaffeine.hotswap.activity;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.activity.login.BaseLoginFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends BaseLoginFragment {


    public ProfileFragment() {
        // Required empty public constructor
    }

    // create objects to reference layout objects
    private TextView name;
    private TextView memberSince;
    private Button logout;

    private FirebaseUser currentUser = getCurrentUser();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate (R.layout.activity_profile, container,false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // get the bundle from the intent
        Bundle bundle = getIntent().getExtras();

        // set references to layout objects
        name = view.findViewById(R.id.txtName);
        memberSince = view.findViewById(R.id.txtMemberSince);
        logout = view.findViewById(R.id.btnLogout);

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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
