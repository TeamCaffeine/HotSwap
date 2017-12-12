package com.teamcaffeine.hotswap.swap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.maps.model.LatLng;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.login.User;
import com.teamcaffeine.hotswap.navigation.AddressesFragment;
import com.teamcaffeine.hotswap.utility.LatLongUtility;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListItemActivity extends FragmentActivity {

    private String TAG = "ListItemActivity";

    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference users;
    private String userTable = "users";
    private DatabaseReference items;
    private DatabaseReference geoFireRef;
    private StorageReference storage;
    private String itemTable = "items";
    private String geoFireTable = "items_location";
    private Uri imageUri = null;
    private RadioGroup tagList;
    private TextView tagChosen, tag;


    private ImageView itemPhoto;
    private EditText editItemName;
    private EditText editPrice;
    private EditText editDescription;

    private Button listItemButton;
    private List<String> itemList = new ArrayList<String>();

    private int RESULT_ERROR = 88;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_item);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        users = database.getReference(userTable);
        items = database.getReference(itemTable);
        geoFireRef = database.getReference(geoFireTable);

        itemPhoto = (ImageView) findViewById(R.id.itemPhoto);
        editItemName = (EditText) findViewById(R.id.editItemName);
        editPrice = (EditText) findViewById(R.id.editPrice);
        editDescription = (EditText) findViewById(R.id.editDescription);

        final AddressesFragment addressesFragment = new AddressesFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.addressContent, addressesFragment);
        ft.commit();

        listItemButton = (Button) findViewById(R.id.listItemButton);

        // get the  current list of the user's items from the intent
        Bundle extras = getIntent().getExtras();
        itemList = extras.getStringArrayList("itemList");

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        itemList = (ArrayList<String>) args.getSerializable("itemList");

        listItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String itemID = items.push().getKey();
                final String itemName = editItemName.getText().toString();

                final String itemPrice = editPrice.getText().toString();
                final String itemDescription = editDescription.getText().toString();
                final String tagSelected = "Other";

                // FIELD VALIDATION
                if (Strings.isNullOrEmpty(itemID) ||
                        Strings.isNullOrEmpty(itemName) ||
                        Strings.isNullOrEmpty(itemPrice) ||
                        Strings.isNullOrEmpty(itemDescription)) {
                    Toast.makeText(getApplicationContext(), R.string.enter_all_fields, Toast.LENGTH_LONG).show();
                    return;
                }

                final String itemAddress = addressesFragment.getSelectedAddress();

                if (itemAddress == null) {
                    Toast.makeText(getApplicationContext(), R.string.select_address, Toast.LENGTH_LONG).show();
                    return;
                }

                if (imageUri == null) {
                    Toast.makeText(getApplicationContext(), "Please add an image for this item.", Toast.LENGTH_LONG).show();
                    return;
                }

                storage = FirebaseStorage.getInstance().getReference();
                StorageReference imageRef = storage.child("images/items/" + itemID + ".jpg");
                UploadTask upload = imageRef.putFile(imageUri);

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

                        Item newItem = new Item(itemID, itemName, firebaseUser.getUid(), itemDescription, itemPrice, itemAddress, downloadUrl.toString(), tagSelected);

                        // DATA VALIDATION
                        // a user cannot list 2 items with the same name
                        // if they enter a new item with the same name as an existing item, an Toast
                        // will show with an error message
                        if (itemList.contains(itemName)) {
                            Toast.makeText(getBaseContext(), R.string.duplicate_item, Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            // if the user is adding a new item with a new name, submit it to the database
                            submit(newItem);

                            // create an intent to send back to the HomeActivity
                            Intent i = new Intent();


                            // Set the result to indicate adding the item was successful
                            // and finish the activity
                            setResult(Activity.RESULT_OK, i);
                            finish();
                        }
                    }
                });
            }
        });
        tagChosen = (TextView) findViewById(R.id.showTag);

        tag = (TextView) findViewById(R.id.chooseTag);
        SpannableString content = new SpannableString(tag.getText());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        tag.setText(content);

        tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagsPopup();
            }
        });

        itemPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .start(ListItemActivity.this);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                itemPhoto.setImageURI(resultUri);
                imageUri = resultUri;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Unable to select image.", Toast.LENGTH_SHORT).show();
                Exception error = result.getError();
                Log.d(TAG, error.getMessage());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void submit(Item item) {
        Log.i(TAG, "submit method");
        final Item newItem = item;
        final String itemID = newItem.getItemID();
        final String itemName = newItem.getName();
        final String itemAddress = newItem.getAddress();
        final String tagSelected = newItem.getTag();

        items.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange method");
                Map<String, Object> itemUpdate = new HashMap<>();
                itemUpdate.put(itemID, newItem.toMap());
                Log.i(TAG, "item added to database");

                GeoFire geoFire = new GeoFire(geoFireRef);
                LatLng itemLatLng = LatLongUtility.getLatLongForAddress(itemAddress, getString(R.string.locale_key));
                if (itemLatLng != null) {
                    items.updateChildren(itemUpdate);
                    geoFire.setLocation(itemID, new GeoLocation(itemLatLng.lat, itemLatLng.lng));
                    Log.i(TAG, "address found");

                    // Add this item to the users owned items list

                    DatabaseReference ref = users.child(firebaseUser.getUid());
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            user.addOwnedItem(itemID);
                            users.child(firebaseUser.getUid()).updateChildren(user.toMap());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, "The read failed:", databaseError.toException());
                        }
                    });



                } else {
                    // TODO: handle invalid address / location data more gracefully - likely when we put the address fragment here
                    Toast.makeText(getBaseContext(), R.string.unable_to_add_item_due_to_address, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("ListItemFragment", "The read failed: " + databaseError.getCode());
            }
        });



    }

    private void tagsPopup() {
        final View popupView = LayoutInflater.from(this).inflate(R.layout.search_tag, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, 800, 800, true);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        tagList = (RadioGroup) popupView.findViewById(R.id.tagList);
        final RadioButton tag1 = (RadioButton) popupView.findViewById(R.id.radioButton1);
        final RadioButton tag2 = (RadioButton) popupView.findViewById(R.id.radioButton2);
        final RadioButton tag3 = (RadioButton) popupView.findViewById(R.id.radioButton3);
        final RadioButton tag4 = (RadioButton) popupView.findViewById(R.id.radioButton4);
        final RadioButton tag5 = (RadioButton) popupView.findViewById(R.id.radioButton5);
        final RadioButton tag6 = (RadioButton) popupView.findViewById(R.id.radioButton6);

        // define view buttons
        tagList.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.radioButton1){
                    tagChosen.setText(tag1.getText().toString());
                }
                else if(checkedId == R.id.radioButton2){
                    tagChosen.setText(tag2.getText().toString());
                }
                else if(checkedId == R.id.radioButton3){
                    tagChosen.setText(tag3.getText().toString());
                }
                else if(checkedId == R.id.radioButton4){
                    tagChosen.setText(tag4.getText().toString());
                }
                else if(checkedId == R.id.radioButton5){
                    tagChosen.setText(tag5.getText().toString());
                }
                else{ // radiobutton6
                    tagChosen.setText(tag6.getText().toString());
                }
                popupWindow.dismiss();
            }
        });

        // finally show up your popup window
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

    }
}
