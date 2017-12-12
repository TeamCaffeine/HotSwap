package com.teamcaffeine.hotswap.swap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.timessquare.CalendarPickerView;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.login.User;
import com.teamcaffeine.hotswap.messaging.StyledMessagesActivity;
import com.teamcaffeine.hotswap.messaging.models.Channel;
import com.teamcaffeine.hotswap.messaging.models.Subscriptions;
import com.teamcaffeine.hotswap.navigation.NavigationActivity;
import com.teamcaffeine.hotswap.utility.LatLongUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

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
    private DatabaseReference channels;
    private String itemTable = "items";
    private String geoFireTable = "items_location";
    private String channelTable = "channels";


    // Relevant item for this transaction
    private String itemID;
    private Item item;
    private ArrayList<Transaction> transactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_transaction);

        // Set up Firebase
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        users = database.getReference(userTable);
        items = database.getReference(itemTable);
        channels = database.getReference(channelTable);

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

        // Set our listeners

        calendarPickerView.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                // if date range contains invalid date, disable selection
                Date previousDate = calendarPickerView.getSelectedDates().get(0);
                for (Date d : calendarPickerView.getSelectedDates()) {
                    if (previousDate.equals(d)) {
                        // Ignore. This is the first iteration.
                    } else if (d.getDay() == previousDate.getDay() + 1){
                        previousDate = (Date) d.clone();
                    } else {
                        // We must have skipped a day. Log and handle appropriately.
                        Log.i(TAG, "Selected range with disabled day inside of it.");
                        Toast.makeText(getApplicationContext(), R.string.invalid_date_selected, Toast.LENGTH_LONG).show();
                        calendarPickerView.selectDate(calendarPickerView.getSelectedDate());
                    }
                }

                // Safely calculate the total price with proper error handling and try catch statements
                txtTotalPrice.setText(String.format("$%s", Double.parseDouble(item.getRentPrice()) * calendarPickerView.getSelectedDates().size()));
            }

            @Override
            public void onDateUnselected(Date date) {
                // Do nothing, this won't ever be called
            }
        });

        calendarPickerView.setOnInvalidDateSelectedListener(new CalendarPickerView.OnInvalidDateSelectedListener() {
            @Override
            public void onInvalidDateSelected(Date date) {
                Toast.makeText(getApplicationContext(), R.string.invalid_date_selected, Toast.LENGTH_LONG).show();
            }
        });

        calendarPickerView.setDateSelectableFilter(new CalendarPickerView.DateSelectableFilter() {
            @Override
            public boolean isDateSelectable(Date date) {
                for (Transaction t : transactions) {
                    for (Date d : t.getRequestedDates()) {
                        if (d.equals(date) && t.isConfirmed()) {
                            return false;
                        } else {
                            // Do nothing
                        }
                    }
                }
                return true;
            }
        });

        // Ensure we were passed an item. This activity cannot exist without one.
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            itemID = extras.getString("itemID");
            transactions = extras.getParcelableArrayList("transactions");
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
                transaction.setInitialMessage(Strings.isNullOrEmpty(edtAddNote.getText().toString()) ? "" : edtAddNote.getText().toString());
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

                transaction.setDistance(LatLongUtility.getDistanceToAddress(item.getAddress(), location, getString(R.string.locale_key)));
                transaction.setRequestUserID(FirebaseAuth.getInstance().getCurrentUser().getUid());

                // At this point the transaction is fully constructed. We can now add it to the item and update the database.
                if (item.addTransaction(transaction)) {
                    items.child(itemID).updateChildren(item.toMap());
                    String msg = String.format("I'd like to rent your %s. %s", item.getName(), transaction.getInitialMessage());
                    createChatChannels(item.getOwnerID(), transaction.getRequestUserID(), FirebaseAuth.getInstance().getCurrentUser().getEmail(), msg);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.existing_transaction, Toast.LENGTH_LONG).show();
                }
            }
        });

        // Initialize the calendar. During this, the filter listener will be applied.
        calendarPickerView.init(today, nextYear.getTime())
                .inMode(CalendarPickerView.SelectionMode.RANGE);
    }

    private void goHomeSafely(String msg) {
        Log.e(TAG, msg);
        Toast.makeText(getApplicationContext(), R.string.something_wrong, Toast.LENGTH_LONG).show();
        Intent i = new Intent(getApplicationContext(), NavigationActivity.class);
        startActivity(i);
        finish();
    }

    private void createChatChannels(final String ownerID, final String userUID, final String userEmail, final String initialMessage) {
        // First we must get the email of the owner of the item
        users.child(ownerID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    // IMPORTANT: Owner's email
                    User owner = dataSnapshot.getValue(User.class);
                    final String ownerEmail = owner.getEmail();

                    // Now we can start creating chat; check if user has chatted before
                    channels.child(userUID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // User has chatted before
                            if(dataSnapshot.exists()) {
                                Channel userChannels = dataSnapshot.getValue(Channel.class);
                                // If the user does not already have a subscription to the owner
                                if (!userChannels.getSubscriptions().findChannel(ownerEmail)) {
                                    userChannels.addSubscription(ownerEmail);
                                    channels.child(userUID).updateChildren(userChannels.toMap());
                                }

                                // Now we must check if the owner has chatted before
                                channels.child(ownerID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        // Owner has chatted before
                                        if (dataSnapshot.exists()) {
                                            Channel ownerChannels = dataSnapshot.getValue(Channel.class);
                                            // If the owner does not already have a subscription to the user
                                            if (!ownerChannels.getSubscriptions().findChannel(userEmail)) {
                                                ownerChannels.addSubscription(userEmail);
                                                channels.child(ownerID).updateChildren(ownerChannels.toMap());
                                            }

                                            Intent intent = new Intent(getApplicationContext(), StyledMessagesActivity.class);
                                            intent.putExtra("channel", userEmail);
                                            intent.putExtra("subscription", ownerEmail);
                                            intent.putExtra("initialMessage", initialMessage);
                                            startActivity(intent);
                                            finish();
                                        }
                                        // Owner has not chatted before
                                        else {
                                            ArrayList<String> list2 = new ArrayList<>();
                                            list2.add(userEmail);
                                            Subscriptions ownerSubs = new Subscriptions(list2);
                                            Channel ownerChannels = new Channel(ownerEmail, ownerSubs);
                                            channels.child(ownerID).updateChildren(ownerChannels.toMap());

                                            // Now that owner has a subscription to the user, we can start the chat
                                            Intent intent = new Intent(getApplicationContext(), StyledMessagesActivity.class);
                                            intent.putExtra("channel", userEmail);
                                            intent.putExtra("subscription", ownerEmail);
                                            intent.putExtra("initialMessage", initialMessage);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e(TAG, databaseError.getMessage());
                                    }
                                });

                            }
                            // User has not chatted before
                            else {
                                // Add subscription to owner
                                ArrayList<String> list1 = new ArrayList<>();
                                list1.add(ownerEmail);
                                Subscriptions userSubs = new Subscriptions(list1);
                                Channel userChannels = new Channel(userEmail, userSubs);
                                channels.child(userUID).updateChildren(userChannels.toMap());

                                // Check if owner has chatted before
                                channels.child(ownerID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        // Owner has chatted before
                                        if (dataSnapshot.exists()) {
                                            // Add the new subscription to the user
                                            Channel ownerChannels = dataSnapshot.getValue(Channel.class);
                                            // If the owner does not already have a subscription to the user
                                            if (!ownerChannels.getSubscriptions().findChannel(userEmail)) {
                                                ownerChannels.addSubscription(userEmail);
                                                channels.child(ownerID).updateChildren(ownerChannels.toMap());
                                            }

                                            // Then start chat between two user and owner
                                            Intent intent = new Intent(getApplicationContext(), StyledMessagesActivity.class);
                                            intent.putExtra("channel", userEmail);
                                            intent.putExtra("subscription", ownerEmail);
                                            intent.putExtra("initialMessage", initialMessage);
                                            startActivity(intent);
                                            finish();
                                        }
                                        // Owner also has not chatted before
                                        else {
                                            ArrayList<String> list2 = new ArrayList<>();
                                            list2.add(userEmail);
                                            Subscriptions ownerSubs = new Subscriptions(list2);
                                            Channel ownerChannels = new Channel(ownerEmail, ownerSubs);
                                            channels.child(ownerID).updateChildren(ownerChannels.toMap());

                                            // Now that we have created channel entries for both the user and owner
                                            // both of which have not chatted, before, we can start a chat
                                            Intent intent = new Intent(getApplicationContext(), StyledMessagesActivity.class);
                                            intent.putExtra("channel", userEmail);
                                            intent.putExtra("subscription", ownerEmail);
                                            intent.putExtra("initialMessage", initialMessage);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e(TAG, databaseError.getMessage());
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, databaseError.getMessage());
                        }
                    });
                }
                else {
                    Toast.makeText(getApplicationContext(), "Unable to find owner in database, cannot start chat", Toast.LENGTH_SHORT);
                    return;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
            }
        });
    }
}
