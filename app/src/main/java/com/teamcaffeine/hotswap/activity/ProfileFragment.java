package com.teamcaffeine.hotswap.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.teamcaffeine.hotswap.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    // create objects to reference layout objects
    private TextView name;
    private TextView memberSince;
    private Button logout;
    private Button inviteFriends;

    //TODO: figure out how to connect to Firebase to get logout functionality
//    private FirebaseUser currentUser = getCurrentUser();

    private FirebaseAuth mAuth;

    ProfileFragmentListener PFL;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate (R.layout.activity_profile, container,false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //TODO: figure out how to get login information into the Profile Fragment, like you would with bundles between activities
        mAuth = FirebaseAuth.getInstance();

        // get the bundle from the intent
//        Bundle bundle = getIntent().getExtras();
//        String fullName = bundle.getString("fullName");
//        String dateCreated = bundle.getString("dateCreated");
        String fullName = "Joe Smith";
        String dateCreated = "October 31, 2017";


        // set references to layout objects
        name = view.findViewById(R.id.txtName);
        memberSince = view.findViewById(R.id.txtMemberSince);
        logout = view.findViewById(R.id.btnLogout);
        inviteFriends = view.findViewById(R.id.btnInviteFriends);

        // get the user's name from the bundle
        // and set it in the layout
        name.setText(fullName);

        // get the date the user created their account from the bundle
        // set "Member Since" equal to the date the user created their account
        memberSince.setText("Member Since: " + dateCreated);

        // Set logout functionality of the Logout button
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

        inviteFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    public interface ProfileFragmentListener {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        PFL = (ProfileFragment.ProfileFragmentListener) context;
    }

    public void signOut() {
        mAuth.signOut();
        Toast.makeText(getActivity(), R.string.successfully_signed_out,
                Toast.LENGTH_LONG).show();
    }
}
