package com.teamcaffeine.hotswap.navigation;


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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.login.LoginActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    // create objects for Firebase references
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference users;

    // create objects to reference layout objects
    private TextView name;
    private TextView memberSince;
    private Button logout;
    private Button inviteFriends;
    private TextView email;
    private TextView phoneNumber;
    private TextView addAddress;
    private TextView addPayment;
    private TextView addItem;

    //TODO: figure out how to connect to Firebase to get logout functionality

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
        // [START] format "add" textviews
        addAddress = view.findViewById(R.id.txtAddAddress);
        addAddress.setTextColor(Color.BLUE);
        addAddress.setPaintFlags(addAddress.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        addPayment = view.findViewById(R.id.txtAddPayment);
        addPayment.setTextColor(Color.BLUE);
        addPayment.setPaintFlags(addPayment.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        addItem = view.findViewById(R.id.txtAddItem);
        addItem.setTextColor(Color.BLUE);
        addItem.setPaintFlags(addItem.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        // [END] format "add" textviews

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
                View popupView = LayoutInflater.from(getActivity()).inflate(R.layout.profile_invite_popup, null);
                final PopupWindow popupWindow = new PopupWindow(popupView, 800, 800);

                // define view buttons

                Button closePopUp = (Button) popupView.findViewById(R.id.btnClose);
                Button sendText = (Button) popupView.findViewById(R.id.btnSendText);
                Button sendEmail = (Button) popupView.findViewById(R.id.btnSendEmail);
                Button postToFacebook = (Button) popupView.findViewById(R.id.btnPostToFacebook);

                // finally show up your popwindow
                popupWindow.showAsDropDown(popupView, 100, 300);

                closePopUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupWindow.dismiss();
                    }
                });

                sendText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                        sendIntent.setData(Uri.parse("sms:"));
                        String message = "Hey I've been using HotSwap to rent items that I need but don't want to buy! Check it out!";
                        sendIntent.putExtra("sms_body", message);
                        startActivity(sendIntent);
                    }
                });

                sendEmail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                        sendIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
                        String subject = "Join HotSwap!";
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                        String message = "Hey I've been using HotSwap to rent items that I need but don't want to buy! Check it out!";
                        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
                        startActivity(sendIntent);
                    }
                });

                final ShareDialog shareDialog = new ShareDialog(getActivity());
                postToFacebook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ShareLinkContent content = new ShareLinkContent.Builder()
                                .setContentUrl(Uri.parse("https://developers.facebook.com"))
                                .setQuote("Hey I've been using HotSwap to rent items that I need but don't want to buy! Check it out!")
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
                            // TODO: Handle unsuccessful sign out
                        }
                    }
                });
        Toast.makeText(getActivity(), R.string.successfully_signed_out,
                Toast.LENGTH_LONG).show();
    }
}
