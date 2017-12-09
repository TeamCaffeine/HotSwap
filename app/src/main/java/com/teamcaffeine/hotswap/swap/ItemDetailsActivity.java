package com.teamcaffeine.hotswap.swap;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.timessquare.CalendarPickerView;
import com.teamcaffeine.hotswap.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ItemDetailsActivity extends AppCompatActivity {
    private String TAG = "ItemDetailsActivity";

    private String currentCity;

    private TextView txtItemName;
    private TextView txtItemDescription;
    private TextView txtItemTags;
    private TextView txtItemPrice;
    private TextView txtItemLocation;
    private Button btnRequestSwap;

    // Firebase connections
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference users;
    private String userTable = "users";
    private DatabaseReference items;
    private String itemTable = "items";


    private String itemID;
    private Item item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        Bundle extras = getIntent().getExtras();
        itemID = extras.getString("itemID");

        // Set up Firebase
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        users = database.getReference(userTable);
        items = database.getReference(itemTable);
        items.child(itemID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                item = dataSnapshot.getValue(Item.class);
                String itemName = item.getName();
                String itemDescription = item.getDescription();
                String itemTags = item.getTagsToString();
                String itemPrice = "$" + item.getRentPrice();
                String itemLocation;

                try {
                    itemLocation = item.getAddress().split(",")[1];
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.e(TAG, "Out of bounds when getting city from address: " + item.getAddress(), e);
                    itemLocation = currentCity;
                }

                txtItemName.setText(itemName);
                txtItemDescription.setText(itemDescription);
                txtItemTags.setText(itemTags);
                txtItemPrice.setText(itemPrice);
                txtItemLocation.setText(itemLocation);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Do nothing
            }
        });

        currentCity = (String) extras.get("currentCity");

        txtItemName = (TextView) findViewById(R.id.txtItemName);
        txtItemDescription = (TextView) findViewById(R.id.txtItemDescription);
        txtItemTags = (TextView) findViewById(R.id.txtItemTags);
        txtItemPrice = (TextView) findViewById(R.id.txtItemPrice);
        txtItemLocation = (TextView) findViewById(R.id.txtItemLocation);
        btnRequestSwap = findViewById(R.id.btnRequestSwap);
        btnRequestSwap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), CreateTransactionActivity.class);
                i.putExtra("itemID", itemID);
                startActivity(i);
            }
        });
    }
}
