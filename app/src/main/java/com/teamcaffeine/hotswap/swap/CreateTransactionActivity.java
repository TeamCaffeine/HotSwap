package com.teamcaffeine.hotswap.swap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.timessquare.CalendarPickerView;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.navigation.NavigationActivity;
import com.teamcaffeine.hotswap.utility.LatLongUtility;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreateTransactionActivity extends AppCompatActivity {

    private final String TAG = "CTActivity";

    // View references
    private TextView txtItemName;
    private TextView txtItemPrice;
    private TextView txtTotalPrice;
    private EditText edtAddNote;
    private CalendarPickerView calendarPickerView;
    private Button btnGetSwapping;

    // Firebase connections
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference users;
    private String userTable = "users";
    private DatabaseReference items;
    private String itemTable = "items";
    private String geoFireTable = "items_location";


    // Relevant item for this transaction
    private String itemID;
    private Item item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_transaction);

        // Set up Firebase
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        users = database.getReference(userTable);
        items = database.getReference(itemTable);

        // Find our views
        txtItemName = findViewById(R.id.txtItemName);
        txtItemPrice = findViewById(R.id.txtItemPrice);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        edtAddNote = findViewById(R.id.edtAddNote);
        btnGetSwapping = findViewById(R.id.btnGetSwapping);
        calendarPickerView = findViewById(R.id.calendarView);
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);
        Date today = new Date();
        calendarPickerView.init(today, nextYear.getTime())
                .withSelectedDate(today)
                .inMode(CalendarPickerView.SelectionMode.RANGE);
        calendarPickerView.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                // Safely calculate the total price with proper error handling and try catch statements
                txtTotalPrice.setText(String.format("$%s", Double.parseDouble(item.getRentPrice()) * calendarPickerView.getSelectedDates().size()));
            }

            @Override
            public void onDateUnselected(Date date) {
                // Do nothing, this won't ever be called
            }
        });

        // Ensure we were passed an item. This activity cannot exist without one.
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            itemID = extras.getString("itemID");
        } else {
            goHomeSafely("Bundle extras are null. Returning to previous intent.");
            return;
        }

        // Get our item
        items.child(itemID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                item = dataSnapshot.getValue(Item.class);

                txtItemName.setText(item.getName());
                txtItemPrice.setText(String.format("$%s", item.getRentPrice()));
                txtTotalPrice.setText(String.format("$%s", item.getRentPrice()));
//        for (Date d : item.getAvailableDates()) {
//            //TODO set the calendar to gray out the dates that it has already been rented on
//            calendarPickerView.selectDate(d);
//        }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Do nothing
            }
        });

        btnGetSwapping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Transaction transaction = new Transaction();
                transaction.setConfirmed(false);
                transaction.setInitialMessage(edtAddNote.getText().toString());
                transaction.setRequestedDates(calendarPickerView.getSelectedDates());

                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(CreateTransactionActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CreateTransactionActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }

                Criteria criteria = new Criteria();
                String provider = lm.getBestProvider(criteria, false);
                Location location = lm.getLastKnownLocation(provider);

                transaction.setDistance(LatLongUtility.getDistanceToAddress(item.getAddress(), location));
                transaction.setRequestUserID(FirebaseAuth.getInstance().getCurrentUser().getUid());

                // At this point the transaction is fully constructed. We can now add it to the item and update the database.
                if (item.addTransaction(transaction)) {
                    items.child(itemID).updateChildren(item.toMap());
                } else {
                    Toast.makeText(getApplicationContext(), "You already have a transaction for this item for these days.", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void goHomeSafely(String msg) {
        Log.e(TAG, msg);
        Toast.makeText(getApplicationContext(), "Whoops! Something went wrong.", Toast.LENGTH_LONG).show();
        Intent i = new Intent(getApplicationContext(), NavigationActivity.class);
        startActivity(i);
        finish();
    }
}
