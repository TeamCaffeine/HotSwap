package com.teamcaffeine.hotswap.navigation;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.login.LoginActivity;
import com.teamcaffeine.hotswap.login.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    // create objects for Firebase references
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference users;

    // create objects to reference layout objects
    private TextView txtName;
    private TextView txtMemberSince;
    private Button btnLogout;
    private Button btnInviteFriends;
    private TextView txtEmail;
    private TextView txtPhoneNumber;
    private TextView txtAddAddress;
    private TextView txtAddPayment;
    private TextView txtAddItem;
    private TextView txtPastTransactions;

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

    ProfileFragmentListener PFL;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate (R.layout.activity_profile, container,false);

        // format the "Add" textviews to look like hyper links
        // first set the text color to blue
        // then underline the text
        txtAddAddress = view.findViewById(R.id.txtAddAddress);
        txtAddAddress.setTextColor(Color.BLUE);
        txtAddAddress.setPaintFlags(txtAddAddress.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        txtAddPayment = view.findViewById(R.id.txtAddPayment);
        txtAddPayment.setTextColor(Color.BLUE);
        txtAddPayment.setPaintFlags(txtAddPayment.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        txtAddItem = view.findViewById(R.id.txtAddItem);
        txtAddItem.setTextColor(Color.BLUE);
        txtAddItem.setPaintFlags(txtAddItem.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        txtPastTransactions = view.findViewById(R.id.txtPastTransactions);
        txtPastTransactions.setTextColor(Color.BLUE);
        txtPastTransactions.setPaintFlags(txtPastTransactions.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //TODO: figure out how to get login information into the Profile Fragment, like you would with bundles between activities
        user = FirebaseAuth.getInstance().getCurrentUser();

        // get the bundle from the intent
        //****keeping these lines commented out for now, we will need them when we implement the fragment with login
//        Bundle bundle = getIntent().getExtras();
//        String fullName = bundle.getString("fullName");
//        String dateCreated = bundle.getString("dateCreated");
        String fullName = "Joe Smith";
        String dateCreated = "October 31, 2017";


        // set references to layout objects
        txtName = view.findViewById(R.id.txtName);
        txtMemberSince = view.findViewById(R.id.txtMemberSince);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnInviteFriends = view.findViewById(R.id.btnInviteFriends);

        // Get a reference to our posts
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference().child("users").child(firebaseUser.getUid());

        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                txtName.setText(user.getFirstName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });


        // get the date the user created their account from the bundle
        // set "Member Since" equal to the date the user created their account
        txtMemberSince.setText("Member Since: " + dateCreated);

        // Set logout functionality of the Logout button
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

        btnInviteFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View popupView = LayoutInflater.from(getActivity()).inflate(R.layout.profile_invite_popup, null);
                final PopupWindow popupWindow = new PopupWindow(popupView, 800, 800);

                // define view buttons

                Button btnClosePopUp = (Button) popupView.findViewById(R.id.btnClose);
                Button btnSendText = (Button) popupView.findViewById(R.id.btnSendText);
                Button btnSendEmail = (Button) popupView.findViewById(R.id.btnSendEmail);
                Button btnPostToFacebook = (Button) popupView.findViewById(R.id.btnPostToFacebook);

                // finally show up your popwindow
                popupWindow.showAsDropDown(popupView, 100, 300);

                btnClosePopUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupWindow.dismiss();
                    }
                });

                btnSendText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                        sendIntent.setData(Uri.parse("sms:"));
                        String key = "sms_body";
                        sendIntent.putExtra(key, getString(R.string.invite_message));
                        startActivity(sendIntent);
                    }
                });

                btnSendEmail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                        sendIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.invite_email_subject));
                        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.invite_message));
                        startActivity(sendIntent);
                    }
                });

                final ShareDialog shareDialog = new ShareDialog(getActivity());
                btnPostToFacebook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ShareLinkContent content = new ShareLinkContent.Builder()
                                .setContentUrl(Uri.parse(getString(R.string.post_to_FB_url)))
                                .setQuote(getString(R.string.invite_message))
                                .build();
                        shareDialog.show(content);
                    }
                });
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

        showProgressDialog();

        AuthUI.getInstance()
                .signOut(getActivity())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Intent logout = new Intent(getActivity(), LoginActivity.class);
                            startActivity(logout);
                            getActivity().finish();
                        } else {
                            Toast.makeText(getActivity(), R.string.unsuccessfully_signed_out,
                                    Toast.LENGTH_LONG).show();
                        }
                        hideProgressDialog();
                    }
                });
    }
}
