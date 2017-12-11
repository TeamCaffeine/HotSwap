package com.teamcaffeine.hotswap.swap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
import com.teamcaffeine.hotswap.maps.Items;

import java.util.ArrayList;

/**
 * Created by Tkixi on 12/10/17.
 */

public class ItemTransactions extends AppCompatActivity {
    private DatabaseReference items;
    private ListView lvItems;
//    private ListAdapter lvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_transactions);
        lvItems = (ListView) findViewById(R.id.itemLists);
        final ArrayList<String> transaction = new ArrayList<>();
//        lvAdapter = new Items(this);



        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // do stuff
            }
        });

        String itemWithTransaction = "-L-au0kTc3rhO_wd6fca";

        items = FirebaseDatabase.getInstance().getReference().child("items").child(itemWithTransaction);

        // if items is null, show nothing in Listview or show no transactions exist for this item

//        Bundle extras = getIntent().getExtras();
//        itemID = extras.getString("itemID");

        // Set up Firebase
        items.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Item i = dataSnapshot.getValue(Item.class);
//                lvAdapter.putItem(item);
//                i.getTransactions();
                for (Transaction t : i.getTransactions()) {
                    String uid = t.getRequestUserID();
                    final String dist = Double.toString(t.getDistance());

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference users = database.getReference().child("users");
                    users.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User u = dataSnapshot.getValue(User.class);
                            String name = u.getFirstName() + " " + u.getLastName();
                            transaction.add("User name: " + name + " | Distance: " + dist);
                            ArrayAdapter<String> lvAdapter = new ArrayAdapter<String>(ItemTransactions.this, android.R.layout.simple_list_item_1, android.R.id.text1, transaction);
                            lvItems.setAdapter(lvAdapter);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
//                ArrayAdapter<String> lvAdapter = new ArrayAdapter<String>(ItemTransactions.this, android.R.layout.simple_list_item_1, android.R.id.text1, transaction);
//                lvItems.setAdapter(lvAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
