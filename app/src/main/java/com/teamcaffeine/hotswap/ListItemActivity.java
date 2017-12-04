package com.teamcaffeine.hotswap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.timessquare.CalendarPickerView;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.swap.Item;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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
                submit();
            }
        });
    }

    private void submit() {
        items.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String itemName = editItemName.getText().toString();
                String itemAddress = editAddress.getText().toString();
                String itemPrice = editPrice.getText().toString();
                String itemDescription = editDescription.getText().toString();

                Item item = new Item(itemName, firebaseUser.getUid(), itemDescription, itemPrice, itemAddress);

                String key = items.push().getKey();

                Map<String, Object> itemUpdate = new HashMap<>();
                itemUpdate.put(key, item.toMap());

                items.updateChildren(itemUpdate);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("ListItemFragment", "The read failed: " + databaseError.getCode());
            }
        });
    }
}
