package com.teamcaffeine.hotswap.swap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_transactions);
        lvItems = (ListView) findViewById(R.id.itemLists);
        final ArrayList<String> transaction = new ArrayList<>();
        title = (TextView) findViewById(R.id.transTitle);




        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // do stuff
                User u = (User) adapterView.getItemAtPosition(i);
                Toast.makeText(getApplicationContext(), "THIS USER IS : " + u.getName(), Toast.LENGTH_LONG).show();
                // show alert box to confirm or reject transaction with this user
            }
        });



         Bundle extras = getIntent().getExtras();
         String itemID = extras.getString("itemID");
         String itemName = extras.getString("itemName");

         title.setText("Transactions with Item: " + itemName);


        items = FirebaseDatabase.getInstance().getReference().child("items").child(itemID);

        // if items is null, show nothing in Listview or show no transactions exist for this item

        final Users itemAdapter = new Users(this);

        lvItems.setAdapter(itemAdapter);


        // Set up Firebase
        items.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Item i = dataSnapshot.getValue(Item.class);
                for (final Transaction t : i.getTransactions()) {
                    final String uid = t.getRequestUserID();
                    final String dist = String.format("%.2f", t.getDistance());
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference users = database.getReference().child("users");
                    users.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User u = dataSnapshot.getValue(User.class);
                            itemAdapter.putUser(u);
                            itemAdapter.notifyDataSetChanged();
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
}
