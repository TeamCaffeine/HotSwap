package com.teamcaffeine.hotswap.swap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.login.User;
import com.teamcaffeine.hotswap.messaging.StyledMessagesActivity;

import java.util.Date;

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
        title = (TextView) findViewById(R.id.transTitle);

        Bundle extras = getIntent().getExtras();
        final String itemID = extras.getString("itemID");
        final String itemName = extras.getString("itemName");

        title.setText("Transactions with Item: " + itemName);




        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                // do stuff
                final Transaction t = (Transaction) adapterView.getItemAtPosition(i);
                // show alert box to confirm or reject transaction with this user

                AlertDialog alertDialog = new AlertDialog.Builder(ItemTransactions.this)
                        //set message, title, and buttons
                        .setTitle("Transaction")
                        .setMessage("Are you sure you want to confirm this transaction?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            // when the user clicks the "delete" button, delete the transaction from the database
                            // when the transaction is deleted from the database, the UI is automatically updated
                            // by the value event listener on the database
                            public void onClick(DialogInterface dialog, int whichButton) {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                final DatabaseReference items = database.getReference().child("items");
                                final DatabaseReference users = database.getReference().child("users");

                                items.child(itemID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final Item item = dataSnapshot.getValue(Item.class);

                                        for(Transaction mTransaction: item.getTransactions()) {
                                            if (t.equals(mTransaction)) {
                                                mTransaction.setConfirmed(true);
                                                break;
                                            }
                                        }
                                        items.child(itemID).updateChildren(item.toMap());
                                        // Set the transaction's confirmed field to false to true
                                        // We pull the item, because it may have changed since the time we pulled it in
                                            // items.child(Integer.toString(i)).updateChildren(currentTransaction.toMap());
                                        Date date = t.getRequestedDates().get(0);
                                        String confirmationString = "I have confirmed the request for " + itemName + " for " + date;

                                        // Notify the renter via message that the owner has approved the transaction
                                        sendMessage(confirmationString, t.getRequestUserID());

                                        // Update the renter user to have a new pending item
                                        ActiveTransactionInfo activeTransactionInfo = new ActiveTransactionInfo(item, t.getRequestUserID(), date, Double.parseDouble(item.getRentPrice()) * t.getRequestedDates().size());
                                        users.child(t.getRequestUserID()).child("pending").child(activeTransactionInfo.toKey()).setValue(activeTransactionInfo);
                                    }


                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                // after the item is deleted, dismiss the delete dialog
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                final DatabaseReference items = database.getReference().child("items");

                                items.child(itemID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final Item item = dataSnapshot.getValue(Item.class);
                                        for(Transaction mTransaction: item.getTransactions()) {
                                            if (t.equals(mTransaction)) {
                                                mTransaction.setConfirmed(true);
                                                break;
                                            }
                                        }
                                        items.child(itemID).updateChildren(item.toMap());
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                dialog.dismiss();
                            }
                        })
                        .create();
                alertDialog.show();
            }
        });






        items = FirebaseDatabase.getInstance().getReference().child("items").child(itemID);

        // if items is null, show nothing in Listview or show no transactions exist for this item

        final TransactionsAdapter transactionsAdapter = new TransactionsAdapter(this);

        TextView emptyView = findViewById(R.id.emptyView);
        lvItems.setEmptyView(emptyView);
        lvItems.setAdapter(transactionsAdapter);



        // Set up Firebase
        items.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Item i = dataSnapshot.getValue(Item.class);
                for (final Transaction t : i.getTransactions()) {
                    if(t.isConfirmed() == false) {
                        transactionsAdapter.putTransaction(t);
                        transactionsAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }

    private void sendMessage(final String message, String destinationUserID) {
        DatabaseReference users = FirebaseDatabase.getInstance().getReference().child("users");
        users.child(destinationUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String destinationEmail;
                if (dataSnapshot.exists()) {
                    User destinationUser = dataSnapshot.getValue(User.class);
                    destinationEmail = destinationUser.getEmail();

                    Intent intent = new Intent(getApplicationContext(), StyledMessagesActivity.class); //TODO: is this the right context
                    intent.putExtra("channel", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                    intent.putExtra("subscription", destinationEmail);
                    intent.putExtra("initialMessage", message);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
//                Log.e(TAG, databaseError.getMessage());
            }
        });

    }
}
