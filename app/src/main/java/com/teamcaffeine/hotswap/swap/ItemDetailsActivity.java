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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ItemDetailsActivity extends AppCompatActivity {
    private DatabaseReference channels;
    private DatabaseReference users;
    private String TAG = "ItemDetailsActivity";

    private TextView txtItemName;
    private TextView txtItemDescription;
    private TextView txtItemTags;
    private TextView txtItemPrice;
    private TextView txtItemLocation;
    private Button btnRequestSwap;
    private Button btnMessageOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        channels = FirebaseDatabase.getInstance().getReference().child("channels");
        users = FirebaseDatabase.getInstance().getReference().child("users");

        Bundle extras = getIntent().getExtras();
        Item item = (Item) extras.getParcelable("item");
        String currentCity = (String) extras.get("currentCity");
        final String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        final String userUID = FirebaseAuth.getInstance().getUid();
        final String ownerID = extras.getString("ownerID");

        txtItemName = (TextView) findViewById(R.id.txtItemName);
        txtItemDescription = (TextView) findViewById(R.id.txtItemDescription);
        txtItemTags = (TextView) findViewById(R.id.txtItemTags);
        txtItemPrice = (TextView) findViewById(R.id.txtItemPrice);
        txtItemLocation = (TextView) findViewById(R.id.txtItemLocation);
        btnRequestSwap = (Button) findViewById(R.id.btnRequestSwap);
        btnMessageOwner = (Button) findViewById(R.id.btnMessageOwner) ;

        String itemName = item.getName();
        String itemDescription = item.getDescription();
        String itemTags = getTagsAsString(item.getTags());
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

        btnMessageOwner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                                        channels.child(ownerID).updateChildren(userEmail);
                                                    }

                                                    Intent intent = new Intent(getApplicationContext(), StyledMessagesActivity.class);
                                                    intent.putExtra("channel", userEmail);
                                                    intent.putExtra("subscription", ownerEmail);
                                                    startActivity(intent);
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
                                                    startActivity(intent);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

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
                                                    startActivity(intent);
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
                                                    startActivity(intent);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

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

                    }
                });


            } // End of OnClick Listener TODO: Delete
        });
    } // End of onCreate TODO: Delete

    public String getTagsAsString(List<String> tagsList){
        StringBuilder tagsStringBuilder = new StringBuilder();
        for (int i = 0; i < tagsList.size(); i++) {
            tagsStringBuilder.append(i == 0 ? tagsList.get(i) : ", " + tagsList.get(i));
        }
        return tagsStringBuilder.toString();
    }
}
