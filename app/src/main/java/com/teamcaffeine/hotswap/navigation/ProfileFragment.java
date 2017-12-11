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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.base.Strings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
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
    private Button btnRemovePayment;
    private ListView listviewPayment;
    private List<String> paymentElementsList;
    private ArrayAdapter<String> paymentAdapter;
    private TextView txtPastTransactions;
    private String selectedPayment;

    // Progress dialog, to show page is loading
    public ProgressDialog mProgressDialog;

    // Database reference fields
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private StorageReference storage;
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

    // Safely get the payment the user has selected
    public String getSelectedPayment() {
        if (selectedPayment != null) {
            return selectedPayment;
        } else {
            return null;
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

        AddressesFragment addressesFragment = new AddressesFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.layout_Addresses, addressesFragment);
        ft.commit();

        btnAddPayment = view.findViewById(R.id.btnAddPayment);
        btnAddPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPaymentPopup();
            }
        });

        // create a list to hold payments as strings
        paymentElementsList = new ArrayList<String>();
        listviewPayment = view.findViewById(R.id.listviewPayment);
        listviewPayment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPayment = paymentElementsList.get(position);
            }
        });

        btnRemovePayment = view.findViewById(R.id.btnRemovePayment);
        btnRemovePayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Safely handle remove click if there are no addresses or no address is selected
                if (paymentElementsList.isEmpty() || listviewPayment.getCheckedItemPosition() == -1) {
                    return;
                }

                AlertDialog myQuittingDialogBox = new AlertDialog.Builder(getContext())
                        //set message, title, and buttons
                        .setTitle(R.string.delete)
                        .setMessage(R.string.delete_payment_question)
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            // when the user clicks the "delete" button, delete the item from the database
                            // when the item is deleted from the database, the UI is automatically updated
                            // by the value event listener on the database
                            public void onClick(DialogInterface dialog, int whichButton) {
                                DatabaseReference ref = users.child(firebaseUser.getUid());
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User user = dataSnapshot.getValue(User.class);
                                        boolean didRemove = user.removePayment(getSelectedPayment());
                                        if (didRemove) {
                                            // Update database
                                            users.child(firebaseUser.getUid()).updateChildren(user.toMap());
                                            Toast.makeText(getContext(), R.string.card_deleted, Toast.LENGTH_SHORT).show();

                                        } else {
                                            Log.i(TAG, "User attempted to delete a nonexistent payment method");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e(TAG, "Payment update failed", databaseError.toException());
                                    }
                                });
                                // after the item is deleted, dismiss the delete dialog
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                myQuittingDialogBox.show();
            }
        });

        paymentAdapter = new ArrayAdapter<String>
                (getContext(), android.R.layout.simple_list_item_1, paymentElementsList);
        listviewPayment.setAdapter(paymentAdapter);

        txtPastTransactions = view.findViewById(R.id.txtPastTransactions);

        imgPhoto = view.findViewById(R.id.imgPhoto);

        txtName = view.findViewById(R.id.txtName);
        txtMemberSince = view.findViewById(R.id.txtMemberSince);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtPhoneNumber = view.findViewById(R.id.txtPhoneNumber);
        // get references to the invite friends and logout buttons
        btnLogout = view.findViewById(R.id.btnLogout);
        btnInviteFriends = view.findViewById(R.id.btnInviteFriends);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        users = database.getReference().child(userTable);

        DatabaseReference ref = users.child(firebaseUser.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            // get a datasnapshot of the current user to access its data
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                // set the profile picture
                String avatar = user.getAvatar();
                if (!Strings.isNullOrEmpty(avatar)) {
                    Picasso.with(getActivity().getApplicationContext()).load(avatar).into(imgPhoto);
                }

                // set the user's name
                txtName.setText(user.getFirstName() + " " + user.getLastName());

                txtMemberSince.setText(getResources().getString(R.string.member_since) + " " + user.getMemberSince());

                txtEmail.setText(user.getEmail());
                txtPhoneNumber.setText(user.getPhoneNumber());

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

                storage = FirebaseStorage.getInstance().getReference();
                StorageReference imageRef = storage.child("images/users/" + firebaseUser.getUid() + ".jpg");
                UploadTask upload = imageRef.putFile(resultUri);

                // Register observers to listen for when the download is done or if it fails
                upload.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // TODO: Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        DatabaseReference ref = users.child(firebaseUser.getUid());
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User user = dataSnapshot.getValue(User.class);
                                user.setAvatar(downloadUrl.toString());
                                users.child(firebaseUser.getUid()).updateChildren(user.toMap());

                                Toast.makeText(getActivity(), R.string.profile_pic_update_success, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(getActivity(), R.string.profile_pic_update_failed, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "The read failed:", databaseError.toException());
                            }
                        });
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(getActivity(), R.string.unable_change_image, Toast.LENGTH_SHORT).show();
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
                                users.child(firebaseUser.getUid()).updateChildren(user.toMap());

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

        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }


}
