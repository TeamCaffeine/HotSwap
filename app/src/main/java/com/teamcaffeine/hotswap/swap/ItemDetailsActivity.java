package com.teamcaffeine.hotswap.swap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.login.User;
import com.teamcaffeine.hotswap.messaging.StyledMessagesActivity;
import com.teamcaffeine.hotswap.messaging.models.Channel;
import com.teamcaffeine.hotswap.messaging.models.Subscriptions;

import java.util.ArrayList;
import java.util.List;

public class ItemDetailsActivity extends AppCompatActivity {
    private DatabaseReference channels;
    private String TAG = "ItemDetailsActivity";

    private String currentCity;

    private ImageView imgItemImage;
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
    private Button btnMessageOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        channels = FirebaseDatabase.getInstance().getReference().child("channels");
        users = FirebaseDatabase.getInstance().getReference().child("users");

        Bundle extras = getIntent().getExtras();
        itemID = extras.getString("itemID");

        imgItemImage = (ImageView) findViewById(R.id.imgItemImage);
        txtItemName = (TextView) findViewById(R.id.txtItemName);
        txtItemDescription = (TextView) findViewById(R.id.txtItemDescription);
        txtItemTags = (TextView) findViewById(R.id.txtItemTags);
        txtItemPrice = (TextView) findViewById(R.id.txtItemPrice);
        txtItemLocation = (TextView) findViewById(R.id.txtItemLocation);
        btnRequestSwap = findViewById(R.id.btnRequestSwap);

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
                String downloadUrl = item.getHeaderPicture();
                String itemLocation;
                if (!Strings.isNullOrEmpty(downloadUrl)) {
                    Picasso.with(getApplicationContext()).load(downloadUrl).into(imgItemImage);
                }

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
        String currentCity = (String) extras.get("currentCity");
        final String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        final String userUID = FirebaseAuth.getInstance().getUid();
        final String ownerID = extras.getString("ownerID");

        btnRequestSwap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), CreateTransactionActivity.class);
                i.putExtra("itemID", itemID);
                i.putParcelableArrayListExtra("transactions", item.getTransactions());
                startActivity(i);
            }
        });
        btnMessageOwner = (Button) findViewById(R.id.btnMessageOwner) ;

        btnMessageOwner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemDetailsActivity.this.createChatChannels(ownerID, userUID, userEmail);
            }
        });
    }

    private void createChatChannels(final String ownerID, final String userUID, final String userEmail) {
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
                    Toast.makeText(getApplicationContext(), R.string.unable_to_find_owner, Toast.LENGTH_SHORT);
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
