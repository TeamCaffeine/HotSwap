package com.teamcaffeine.hotswap.navigation;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.teamcaffeine.hotswap.ListItemActivity;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.login.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    // create objects for Firebase references
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference users;
    private String userTable = "Users";
    private User user;

    // create objects to reference layout objects
    private Button btnListItem;
    private ListView listviewAllItems;
    private TextView txtCurrentlyRenting;
    private ListView listviewLending;
    private int LIST_ITEM_REQUEST_CODE = 999;

    public ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private HomeFragmentListener HFL;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        users = database.getReference().child(userTable);

        btnListItem = view.findViewById(R.id.btnListItem);
        listviewAllItems = view.findViewById(R.id.listviewAllItems);
        txtCurrentlyRenting = view.findViewById(R.id.txtCurrentlyRenting);
        listviewLending = view.findViewById(R.id.listviewLending);

        // Get a reference to the user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference().child("users").child(firebaseUser.getUid());

        btnListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), ListItemActivity.class);
                startActivityForResult(i, LIST_ITEM_REQUEST_CODE);
            }
        });
    }

    public interface HomeFragmentListener {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        HFL = (HomeFragment.HomeFragmentListener) context;  //context is a handle to the main activity, let's bind it to our interface.
    }
}
