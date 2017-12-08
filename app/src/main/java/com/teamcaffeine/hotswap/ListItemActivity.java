package com.teamcaffeine.hotswap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.common.base.Strings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.model.LatLng;
import com.squareup.timessquare.CalendarPickerView;
import com.teamcaffeine.hotswap.navigation.AddressesFragment;
import com.teamcaffeine.hotswap.swap.Item;
import com.teamcaffeine.hotswap.utility.LatLongUtility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private String itemTable = "items";
    private String geoFireTable = "items_location";

    private EditText editItemName;
    private EditText editTags;
    private EditText editPrice;
    private EditText editDescription;

    private Button listItemButton;
    private CalendarPickerView calendar;
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

        editItemName = (EditText) findViewById(R.id.editItemName);
        editTags = (EditText) findViewById(R.id.editTags);
        editPrice = (EditText) findViewById(R.id.editPrice);
        editDescription = (EditText) findViewById(R.id.editDescription);

        final AddressesFragment addressesFragment = new AddressesFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.addressContent, addressesFragment);
        ft.commit();

        calendar = (CalendarPickerView) findViewById(R.id.calendarView);
        listItemButton = (Button) findViewById(R.id.listItemButton);

        // get the  current list of the user's items from the intent
        Bundle extras = getIntent().getExtras();
        itemList = extras.getStringArrayList("itemList");

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        itemList = (ArrayList<String>) args.getSerializable("itemList");

        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        Date today = new Date();
        calendar.init(today, nextYear.getTime())
                .withSelectedDate(today)
                .inMode(CalendarPickerView.SelectionMode.MULTIPLE);

        calendar.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                v.onTouchEvent(event);
                return true;
            }
        });

        listItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String itemID = items.push().getKey();
                String itemName = editItemName.getText().toString();

                String itemPrice = editPrice.getText().toString();
                String itemDescription = editDescription.getText().toString();
                String itemTags = editTags.getText().toString();

                // FIELD VALIDATION
                if (Strings.isNullOrEmpty(itemID) ||
                        Strings.isNullOrEmpty(itemName) ||
                        Strings.isNullOrEmpty(itemPrice) ||
                        Strings.isNullOrEmpty(itemDescription)) {
                    Toast.makeText(getApplicationContext(), "Please enter all fields.", Toast.LENGTH_LONG).show();
                    return;
                }

                String itemAddress = addressesFragment.getSelectedAddress();

                if (itemAddress == null) {
                    Toast.makeText(getApplicationContext(), "Please select an address.", Toast.LENGTH_LONG).show();
                    return;
                }

                Item newItem = new Item(itemID, itemName, firebaseUser.getUid(), itemDescription, itemPrice, itemAddress);

                // ADDING TAGS TO ITEM
                String[] tags = itemTags.split(",");
                for (String tag : tags) {
                    String tagToAdd = tag.trim();
                    newItem.addTag(tagToAdd);
                }

                // DATA VALIDATION
                // a user cannot list 2 items with the same name
                // if they enter a new item with the same name as an existing item, an Toast
                // will show with an error message
                if (itemList.contains(itemName)) {
                    Toast.makeText(getBaseContext(), R.string.duplicate_item, Toast.LENGTH_LONG).show();
                } else {
                    // if the user is adding a new item with a new name, submit it to the database
                    submit(newItem);

                    // create an intent to send back to the HomeActivity
                    Intent i = new Intent();

                    // send the updated itemList back to the Home Fragment
                    i.putExtra("newItem", itemName);

                    // Set the result to indicate adding the item was successful
                    // and finish the activity
                    setResult(Activity.RESULT_OK, i);
                    finish();
                }
            }
        });
    }

    private void submit(Item item) {
        Log.i(TAG, "submit method");
        final Item newItem = item;
        final String itemID = newItem.getItemID();
        final String itemName = newItem.getName();
        final String itemAddress = newItem.getAddress();

        items.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange method");
                Map<String, Object> itemUpdate = new HashMap<>();
                itemUpdate.put(itemID, newItem.toMap());
                Log.i(TAG, "item added to database");

                GeoFire geoFire = new GeoFire(geoFireRef);
                LatLng itemLatLng = LatLongUtility.getLatLongForAddress(itemAddress);
                if (itemLatLng != null) {
                    items.updateChildren(itemUpdate);
                    geoFire.setLocation(itemID, new GeoLocation(itemLatLng.lat, itemLatLng.lng));
                    Log.i(TAG, "address found");
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
}
