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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.timessquare.CalendarPickerView;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.messaging.StyledMessagesActivity;
import com.teamcaffeine.hotswap.messaging.models.Channel;
import com.teamcaffeine.hotswap.messaging.models.Subscriptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ItemDetailsActivity extends AppCompatActivity {
    private DatabaseReference chatChannels;
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

        chatChannels = FirebaseDatabase.getInstance().getReference().child("channels");
        Bundle extras = getIntent().getExtras();
        Item item = (Item) extras.getParcelable("item");
        String currentCity = (String) extras.get("currentCity");
        final String channel = extras.getString("channel"); //TODO: Perhaps rename
        final String subscription = extras.getString("subscription"); // TODO: Perhaps rename

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
                // TODO: What if they ALREADY have a chat connection
                // Get a reference to this users channels TODO: What if they don't have any channels?
                DatabaseReference rentee = chatChannels.child("416pQWI7FmXWTLaysqLXFIsFTSo1"); //TODO: This is hard coded
                rentee.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Create the subscription from channel to subscription (renter to lender)
                            Channel renteeChatChannels = dataSnapshot.getValue(Channel.class);
                            renteeChatChannels.addSubscription(subscription);
                            // TODO: How should I push this to the database?
                            chatChannels.updateChildren(renteeChatChannels.toMap());

                            // Now create a subscription from subscription to channel (lender to render)
                            DatabaseReference lender = chatChannels.child(subscription);
                            lender.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        Channel lenderChatChannels = dataSnapshot.getValue(Channel.class);
                                        lenderChatChannels.addSubscription(channel);
                                        chatChannels.updateChildren(lenderChatChannels.toMap());

                                        // Now that we have created the chat between the two, we can start the messaging activity
                                        Intent intent = new Intent(getApplicationContext(), StyledMessagesActivity.class);
                                        intent.putExtra("channel", channel);
                                        intent.putExtra("subscription", subscription);
                                        startActivity(intent);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        // User has not chatted with anyone in the database before
                        else {
                            // Hard coded example
                            List<String> list1 = new ArrayList<>();
                            list1.add("chamathsd@gmail.com");
                            Subscriptions williamSubs = new Subscriptions(list1);
                            Channel williamChannel = new Channel("william@william.com", williamSubs);
                            chatChannels.child("qziMys23rIg1ZMQZaf9TBAioXas2").updateChildren(williamChannel.toMap());

                            // TODO: Handle if the person we are TRYING to talk to has chatted before
                            List<String> list2 = new ArrayList<>();
                            list2.add("william@william.com");
                            Subscriptions chamathSubs = new Subscriptions(list2);
                            Channel chamathChannel = new Channel("chamathsd@gmail.com", chamathSubs);
                            chatChannels.child("416pQWI7FmXWTLaysqLXFIsFTSo1").updateChildren(chamathChannel.toMap());

                            Intent intent = new Intent(getApplicationContext(), StyledMessagesActivity.class);
                            intent.putExtra("channel", "william@william.com");
                            intent.putExtra("subscription", "chamathsd@gmail.com");
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

//                Intent intent = new Intent(getApplicationContext(), StyledMessagesActivity.class);
//                intent.putExtra("channel", channel);
//                intent.putExtra("subscription", subscription);
//                startActivity(intent);
            }
        });


    }

    public String getTagsAsString(List<String> tagsList){
        StringBuilder tagsStringBuilder = new StringBuilder();
        for (int i = 0; i < tagsList.size(); i++) {
            tagsStringBuilder.append(i == 0 ? tagsList.get(i) : ", " + tagsList.get(i));
        }
        return tagsStringBuilder.toString();
    }
}
