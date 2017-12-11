package com.teamcaffeine.hotswap.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.navigation.AddressesFragment;
import com.teamcaffeine.hotswap.navigation.NavigationActivity;
import com.teamcaffeine.hotswap.navigation.ProfileFragment;
import com.teamcaffeine.hotswap.utility.SessionHandler;
import com.theartofdev.edmodo.cropper.CropImage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddUserDetailsActivity extends AppCompatActivity {

    private String TAG = "AddUserDetailsActivity";

    // create objects for Firebase references
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private StorageReference storage;
    private DatabaseReference users;
    private String userTable = "users";

    // create objects to hold views
    private ImageView imgProfilePhoto;
    private EditText edtFirstName;
    private EditText edtLastName;
    private EditText edtPhoneNumber;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionHandler.shouldLogIn(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_details);

        // populate the listview with the user's addresses
        // step 1: instantiate the Address Fragment
        AddressesFragment addressesFragment = new AddressesFragment();
        // step 2: begin the fragment transaction
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // step 3: add fragment to the activity state
        ft.add(R.id.addressContent, addressesFragment);
        // stop 4: commit the transaction
        ft.commit();

        imgProfilePhoto = findViewById(R.id.imgProfilePhoto);
        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        btnSubmit = findViewById(R.id.btnSubmit);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        users = database.getReference().child(userTable);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });

        // Set onClick functionality for profile picture
        imgProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .start(AddUserDetailsActivity.this);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                imgProfilePhoto.setImageURI(resultUri);

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

                                Toast.makeText(AddUserDetailsActivity.this, R.string.profile_pic_update_success, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(AddUserDetailsActivity.this, R.string.profile_pic_update_failed, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "The read failed:", databaseError.toException());
                            }
                        });
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, R.string.unable_change_image, Toast.LENGTH_SHORT).show();
                Exception error = result.getError();
                Log.d(TAG, error.getMessage());
            }
        }
    }

    // Submit method
    // When the user clicks "submit," their full user account is created in Firebase
    public void submit() {
        // get the strings in each of the Edit Texts
        final String firstName = edtFirstName.getText().toString();
        final String lastName = edtLastName.getText().toString();
        final String phoneNumber = edtPhoneNumber.getText().toString();

        // only allow the user to move forward through login if they have entered a name and phone number
        if (!firstName.isEmpty() && !lastName.isEmpty() && !phoneNumber.isEmpty()) {
            DatabaseReference ref = users.child(firebaseUser.getUid());
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);

                    DateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
                    Date memberSince = new Date();

                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setPhoneNumber(phoneNumber);
                    user.setMemberSince(dateFormat.format(memberSince));
                    users.child(firebaseUser.getUid()).updateChildren(user.toMap());

                    Intent i = new Intent(AddUserDetailsActivity.this, NavigationActivity.class);
                    startActivity(i);
                    finish();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("AddUserDetailsActivity","The read failed: " + databaseError.getCode());
                }
            });
        } else {
            // if the user did not enter all of their details, show a toast to instruct them to enter all detals
            Toast.makeText(AddUserDetailsActivity.this, R.string.enter_all_details,
                    Toast.LENGTH_LONG).show();
        }
    }
}
