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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.login.LoginActivity;
import com.teamcaffeine.hotswap.login.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    // create objects for Firebase references
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference users;
    private String userTable = "Users";
    private User user;

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
    private ListView lvAddresses;
    private ListView lvPayment;
    private ListAdapter addressesAdapter;
    private ListAdapter paymentAdapter;

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
        View view = inflater.inflate(R.layout.activity_profile, container, false);
        txtAddAddress = view.findViewById(R.id.txtAddAddress);
        txtAddPayment = view.findViewById(R.id.txtAddPayment);
        txtPastTransactions = view.findViewById(R.id.txtPastTransactions);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        users = database.getReference().child(userTable);

        Query userQuery = users.equalTo(currentUser.getUid());
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    user = singleSnapshot.getValue(User.class);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.toException().toString(), Toast.LENGTH_LONG).show();
            }
        });

        // get the user info
//        String fullName = user.getFirstName() + " " + user.getLastName();
//        String memberSince = user.getMemberSince();
//        String email = user.getEmail();
//        String phoneNumber = user.getPhoneNumber();
        String fullName = "Megan";
        String memberSince = "November 2017";
        String email = "megan@test.com";
        String phoneNumber = "555";


        // set references to layout objects
        txtName = view.findViewById(R.id.txtName);
        txtMemberSince = view.findViewById(R.id.txtMemberSince);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnInviteFriends = view.findViewById(R.id.btnInviteFriends);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtPhoneNumber = view.findViewById(R.id.txtPhoneNumber);

        // set display
        txtName.setText(fullName);
        txtMemberSince.setText("Member Since: " + memberSince);
        txtEmail.setText(email);
        txtPhoneNumber.setText(phoneNumber);

        lvAddresses = (ListView) view.findViewById(R.id.listviewAddresses);
        addressesAdapter = new AddressesListAdapter(getContext(), user);  //instead of passing the boring default string adapter, let's pass our own, see class MyCustomAdapter below!
        lvAddresses.setAdapter(addressesAdapter);

        lvPayment = (ListView) view.findViewById(R.id.listviewPayment);
        paymentAdapter = new PaymentListAdapter(getContext(), user);  //instead of passing the boring default string adapter, let's pass our own, see class MyCustomAdapter below!
        lvPayment.setAdapter(paymentAdapter);

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
                inviteFriendsPopup();
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

    private void signOut() {

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

    public void inviteFriendsPopup() {
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

}

class AddressesListAdapter extends BaseAdapter {
    private String placeName;
    private String address;

    public AddressesListAdapter(Context context, User user) {
        placeName = "X";
        address = "Y";
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }
}

class PaymentListAdapter extends BaseAdapter {
    private String cardType;
    private String cardNumber;

    public PaymentListAdapter(Context context, User user) {
        cardType = "X";
        cardNumber = "Y";
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }
}
