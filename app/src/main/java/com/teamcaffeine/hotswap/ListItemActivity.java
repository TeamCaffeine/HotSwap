package com.teamcaffeine.hotswap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.timessquare.CalendarPickerView;
import com.teamcaffeine.hotswap.swap.Item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListItemActivity extends AppCompatActivity {

    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference items;
    private String itemTable = "items";

    private EditText editItemName;
    private EditText editTags;
    private EditText editPrice;
    private EditText editDescription;
    private EditText editAddress;
    private Button listItemButton;
    private CalendarPickerView calendar;
    private List<String> itemList = new ArrayList<String>();

    private int RESULT_ERROR = 88;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_list_item);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        items = database.getReference(itemTable);

        editItemName = (EditText) findViewById(R.id.editItemName);
        editTags = (EditText) findViewById(R.id.editTags);
        editPrice = (EditText) findViewById(R.id.editPrice);
        editDescription = (EditText) findViewById(R.id.editDescription);
        editAddress = (EditText) findViewById(R.id.editAddress);
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
                String itemAddress = editAddress.getText().toString();
                String itemPrice = editPrice.getText().toString();
                String itemDescription = editDescription.getText().toString();

                Item newItem = new Item(itemID, itemName, firebaseUser.getUid(), itemDescription, itemPrice, itemAddress);

                // DATA VALIDATION
                // a user cannot list 2 items with the same name
                // if they enter a new item with the same name as an existing item, an Toast
                // will show with an error message
                if (itemList.contains(itemName)) {
                    Toast.makeText(getBaseContext(), R.string.duplicate_item, Toast.LENGTH_LONG).show();
                } else {
                    // if the user is adding a new item with a new name, submit it to the database
                    int[] submitStatus = submit(newItem);

                    // create an intent to send back to the HomeActivity
                    Intent i = new Intent();

                    // send the updated itemList back to the Home Fragment
                    i.putExtra("newItem", itemName);

                    // Set the result with this data, and finish the activity
                    setResult(submitStatus[0], i);
                    finish();
                }
            }
        });
    }

    private int[] submit(Item item) {
        final int[] resultCode = new int[1];
        final Item newItem = item;
        final String itemID = newItem.getItemID();
        final String itemName = newItem.getName();

        items.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> itemUpdate = new HashMap<>();
                itemUpdate.put(itemID, newItem.toMap());
                items.updateChildren(itemUpdate);

                // set the result code to indicate that the item was successfully added
                resultCode[0] = RESULT_OK;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("ListItemFragment", "The read failed: " + databaseError.getCode());
                resultCode[0] = RESULT_ERROR;
            }
        });

        return resultCode;
    }
}
