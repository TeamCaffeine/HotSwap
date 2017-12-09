package com.teamcaffeine.hotswap.navigation;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
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
import com.google.firebase.database.ValueEventListener;
import com.stripe.android.model.Card;
import com.stripe.android.view.CardMultilineWidget;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.login.LoginActivity;
import com.teamcaffeine.hotswap.login.User;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private String TAG = "ProfileFragment";

    // Place codes
    final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    // create objects to reference layout objects
    private ImageView imgPhoto;
    private TextView txtName;
    private TextView txtMemberSince;
    private Button btnLogout;
    private Button btnInviteFriends;
    private TextView txtEmail;
    private TextView txtPhoneNumber;
    private Button btnAddPayment;
    private ListView listviewPayment;
    private List<String> paymentElementsList;
    private ArrayAdapter<String> paymentAdapter;
    private TextView txtPastTransactions;

    // Progress dialog, to show page is loading
    public ProgressDialog mProgressDialog;

    // Database reference fields
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference users;
    private String userTable = "users";

    // progress dialog to show page is loading
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        // show progress dialog to indicate to user that the page is loading
        mProgressDialog.show();
    }

    // hide progress dialog when page shows
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    // fragment listener for inter-fragment communication
    ProfileFragmentListener PFL;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // populate the listview with the user's addresses
        // step 1: instantiate the Address Fragment
        AddressesFragment addressesFragment = new AddressesFragment();
        // step 2: begin the fragment transaction
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        // step 3: add fragment to the activity state
        ft.add(R.id.layout_Addresses, addressesFragment);
        // step 4: commit the transaction
        ft.commit();

        // instantiate the button to add a payment method to the user's profile
        btnAddPayment = view.findViewById(R.id.btnAddPayment);
        // set on click listener to open a popup to add the payment using a Stripe widget
        // see method below
        btnAddPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPaymentPopup();
            }
        });

        // create a list to hold item names as strings
        paymentElementsList = new ArrayList<String>();
        // instantiate the listview to hold the list of item names
        listviewPayment = view.findViewById(R.id.listviewPayment);

        /**
         * DELETE AN ITEM
         */

        // set an onClick listener so that when a user clicks on an item,
        // they get a dialog to delete the item
        listviewPayment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                // create an alert dialog that asks the user if they want to delete the item,
                // and gives them the option to delete or cancel
                AlertDialog myQuittingDialogBox = new AlertDialog.Builder(getContext())
                        //set message, title, and buttons
                        .setTitle(R.string.delete)
                        .setMessage(R.string.delete_payment_question)
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            // when the user clicks the "delete" button, delete the item from the database
                            // when the item is deleted from the database, the UI is automatically updated
                            // by the value event listener on the database
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // get a reference to the current user
                                DatabaseReference ref = users.child(firebaseUser.getUid());
                                // add a single value event listener to the user reference to
                                // delete the single item from their account
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    // get a datasnapshot of the current user to access its data
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        // create a user object from the datasnapshot
                                        User user = dataSnapshot.getValue(User.class);

                                        // remove the payment item that was selected in the listview from
                                        // the user's list  of payments
                                        // the removePayment method in the User class returns a boolean value on
                                        // success or failure of removal
                                        boolean didRemove = user.removePayment(listviewPayment.getItemAtPosition(position).toString());
                                        // when removePayment is successfuly and returns true, we can delete the payment method
                                        // from the database
                                        // we only ever want to delete from the database when we delete from the in-app list,
                                        // and vice versa, to make sure the UI and the backend database remain synced
                                        if (didRemove) {
                                            // Update database
                                            // step 1: create hashmap object
                                            Map<String, Object> userUpdate = new HashMap<>();
                                            // step 2: put the data from the user object into the hashmap
                                            userUpdate.put(firebaseUser.getUid(), user.toMap());
                                            // step 3: replace the existing hashmap for the user in the database with the
                                            // update hashmap that does not contain the deleted payment method
                                            users.updateChildren(userUpdate);

                                            // show a toast to tell the user the card was deleted
                                            Toast.makeText(getContext(), R.string.card_deleted, Toast.LENGTH_SHORT).show();

                                        // if removing the payment method was not successful, log the error
                                        } else {
                                            Log.i(TAG, "User attempted to delete a nonexistent payment method");
                                        }
                                    }

                                    // if the user closed the delete dialog, log the error
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e(TAG, "Payment update failed", databaseError.toException());
                                    }
                                });
                                // after the item is deleted, dismiss the delete dialog
                                dialog.dismiss();
                            }
                        })
                        // if the user clicks cancel, close the delete dialog
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        // once all of the functionality is added to the delete dialog, create the dialog
                        .create();
                // once the dialog is created, show it
                myQuittingDialogBox.show();
            }
        });

        // create an adapter for the listview that displays the payments
        // the adapter handles UI updates when the list that populates
        // the listview is changed
        paymentAdapter = new ArrayAdapter<String>
                (getContext(), android.R.layout.simple_list_item_1, paymentElementsList);
        // set the adapater on the listview
        listviewPayment.setAdapter(paymentAdapter);

        // get the reference to the past transactions button (actually a textview for display purposes) on the UI
        txtPastTransactions = view.findViewById(R.id.txtPastTransactions);

        // get reference to profile picutre
        imgPhoto = view.findViewById(R.id.imgPhoto);

        // get references to all user info textviews
        txtName = view.findViewById(R.id.txtName);
        txtMemberSince = view.findViewById(R.id.txtMemberSince);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtPhoneNumber = view.findViewById(R.id.txtPhoneNumber);
        // get references to the invite friends and logout buttons
        btnLogout = view.findViewById(R.id.btnLogout);
        btnInviteFriends = view.findViewById(R.id.btnInviteFriends);

        // Get an authentication reference to the current user
        // we will use this to get the user's ID in our database, which we
        // will use to pull the user's information
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        // get database references
        database = FirebaseDatabase.getInstance();
        users = database.getReference().child(userTable);

        // get a database reference to the current user, user the auth reference above
        DatabaseReference ref = users.child(firebaseUser.getUid());
        // add a value event listener to the user reference
        // this value event listener populates all of the user's data in the profile UI
        // by being a Value Event Listener and not a Single Value Event Listener, it will
        // constantly listen to the database for changes and automatically update the UI
        // when the user's data is changed
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            // get a datasnapshot of the current user to access its data
            public void onDataChange(DataSnapshot dataSnapshot) {
                // create a user object from the datasnapshot
                User user = dataSnapshot.getValue(User.class);
                // set the user's name
                txtName.setText(user.getFirstName() + " " + user.getLastName());

                // set the value of their member since field
                // like of most social media platforms, this field indicates how long a user
                // has been using the app, which could indicate reliability and experience
                // to potential renters
                txtMemberSince.setText(getResources().getString(R.string.member_since) + " " + user.getMemberSince());

                // set the user's email
                txtEmail.setText(user.getEmail());
                // set the user's phone number
                txtPhoneNumber.setText(user.getPhoneNumber());

                // get the user's payment methods
                paymentElementsList = user.getPayments();
                paymentAdapter = new ArrayAdapter<String>
                        (getContext(), android.R.layout.simple_list_item_1, paymentElementsList);
                listviewPayment.setAdapter(paymentAdapter);
                paymentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "The read failed:", databaseError.toException());
            }
        });


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

        // Set onClick functionality for profile picture
        imgPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .start(getContext(), ProfileFragment.this);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                imgPhoto.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d(TAG, error.getMessage());
            }
        }
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

    private void inviteFriendsPopup() {
        View popupView = LayoutInflater.from(getActivity()).inflate(R.layout.profile_invite_popup, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, 800, 800, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);

        // define view buttons

        Button btnClosePopUp = (Button) popupView.findViewById(R.id.btnClose);
        Button btnSendText = (Button) popupView.findViewById(R.id.btnSendText);
        Button btnSendEmail = (Button) popupView.findViewById(R.id.btnSendEmail);
        Button btnPostToFacebook = (Button) popupView.findViewById(R.id.btnPostToFacebook);

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

        // finally show up your popup window
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    private void addPaymentPopup() {
        View popupView = LayoutInflater.from(getActivity()).inflate(R.layout.add_payment_popup, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, 800, 800, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);

        final CardMultilineWidget mCardMultilineWidget = popupView.findViewById(R.id.card_multiline_widget);

        Button btnAddCard = (Button) popupView.findViewById(R.id.btnAddCard);
        btnAddCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Card cardToSave = mCardMultilineWidget.getCard();
                if (cardToSave == null) {
                    Log.i(TAG, "User attempted to add an invalid payment");
                } else {

                    DatabaseReference ref = users.child(firebaseUser.getUid());
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);

                            boolean didAdd = user.addPayment(cardToSave.getNumber());
                            if (didAdd) {
                                // Update database
                                Map<String, Object> userUpdate = new HashMap<>();
                                userUpdate.put(firebaseUser.getUid(), user.toMap());
                                users.updateChildren(userUpdate);

                                // tell user card was successfully added
                                Toast.makeText(getContext(), R.string.card_added, Toast.LENGTH_SHORT).show();
                                popupWindow.dismiss();

                            } else {
                                Log.i(TAG, "User attempted to add a duplicate payment");
                                Toast.makeText(getContext(), R.string.duplicate_card, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, "Payments update failed", databaseError.toException());
                        }
                    });
                    //TODO: add info to Stripe database
                }
            }
        });


        // define view buttons
        Button btnClosePopUp = (Button) popupView.findViewById(R.id.btnClose);
        btnClosePopUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        // finally show up your popup window
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }


}
