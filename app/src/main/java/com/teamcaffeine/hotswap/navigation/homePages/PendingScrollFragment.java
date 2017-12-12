package com.teamcaffeine.hotswap.navigation.homePages;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.hollyviewpager.HollyViewPagerBus;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.login.User;
import com.teamcaffeine.hotswap.navigation.homeAdapters.RentingPendingItemsCardAdapter;
import com.teamcaffeine.hotswap.swap.ActiveTransactionInfo;

import java.util.Date;

public class PendingScrollFragment extends Fragment {

    private String TAG = "PendingScrollFragment";

    private TextView title;
    private ObservableScrollView scrollView;
    private ListView cardListView;

    private static RentingPendingItemsCardAdapter adapter;

    public static PendingScrollFragment newInstance(String title, RentingPendingItemsCardAdapter listAdapter){
        Bundle args = new Bundle();
        args.putString("title",title);
        PendingScrollFragment fragment = new PendingScrollFragment();
        fragment.setArguments(args);
        adapter = listAdapter;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_card_scroll, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        title = view.findViewById(R.id.pageTitle);
        title.setText(getArguments().getString("title"));

        scrollView = view.findViewById(R.id.obsScrollView);
        HollyViewPagerBus.registerScrollView(getActivity(), scrollView);

        cardListView = view.findViewById(R.id.cardListView);
        cardListView.setAdapter(adapter);

        cardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO check if the user is the owner or renter of item.
                final ActiveTransactionInfo activeTransactionInfo = adapter.getActiveTransactionInfoAtPosition(position);

                if (activeTransactionInfo.getRenterId().equals(FirebaseAuth.getInstance().getUid())) {
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                            //set message, title, and buttons
                            .setTitle("Renting item")
                            .setMessage("Have you picked up the item?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                // when the user clicks the "delete" button, delete the transaction from the database
                                // when the transaction is deleted from the database, the UI is automatically updated
                                // by the value event listener on the database
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    onItemReceived(activeTransactionInfo);
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("Not yet", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create();
                    alertDialog.show();
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                            //set message, title, and buttons
                            .setTitle("Receiving returned item")
                            .setMessage("Did you get your item back?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                // when the user clicks the "delete" button, delete the transaction from the database
                                // when the transaction is deleted from the database, the UI is automatically updated
                                // by the value event listener on the database
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    onItemReturned(activeTransactionInfo);
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("Not yet", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create();
                    alertDialog.show();
                }
            }
        });
    }

    // For the renter when they receive the item they wish to rent
    private void onItemReceived(final ActiveTransactionInfo activeTransactionInfo) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference users = FirebaseDatabase.getInstance().getReference().child("users");
        DatabaseReference user = users.child(firebaseUser.getUid());

        user.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User renter = dataSnapshot.getValue(User.class);
                final double balance = renter.getBalance();
                final double itemPrice = activeTransactionInfo.getPrice();
                if (balance < itemPrice) {
                    Toast.makeText(getActivity(), "You do not have enough balance to cover this payment of $" + Double.toString(itemPrice) + ". Please add balance in your profile page.", Toast.LENGTH_LONG).show();
                    return;
                }

                String activeTransitionKey = activeTransactionInfo.toKey();
                // Check if the date is valid (i.e is today past the start date of our transaction?)
                Date today = new Date();
                if (today.before(activeTransactionInfo.getDate())) {
                    Toast.makeText(getActivity(), "You cannot receive an item before the start date of your rent", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Add pending item to the owner user
                users.child(activeTransactionInfo.getItem().getOwnerID()).child("pending").child(activeTransitionKey).setValue(activeTransactionInfo); //TODO: REPLACE WITH OBJECT OR tOMAP?

                // Remove pending item from the renter user
                users.child(activeTransactionInfo.getRenterId()).child("pending").child(activeTransitionKey).removeValue(); //TODO: SEE IF REMOVE GOT DEPRECATED

                // Add renting item to the renter user
                users.child(activeTransactionInfo.getRenterId()).child("renting").child(activeTransitionKey).setValue(activeTransactionInfo);

                // Deduct money from renter's balance
                renter.deductBalance(itemPrice);
                users.child(firebaseUser.getUid()).child("balance").setValue(renter.getBalance());

                // Add money to owner's balance
                users.child(activeTransactionInfo.getItem().getOwnerID()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User owner = dataSnapshot.getValue(User.class);
                        owner.addBalance(itemPrice);
                        users.child(activeTransactionInfo.getItem().getOwnerID()).child("balance").setValue(owner.getBalance());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Do nothing
            }
        });

    }

    // For the lender when they are returned the item they lent
    private void onItemReturned(ActiveTransactionInfo activeTransactionInfo) {
        DatabaseReference users = FirebaseDatabase.getInstance().getReference().child("users");
        String activeTransitionKey = activeTransactionInfo.toKey();

        // Delete item from renting in renter user
        users.child(activeTransactionInfo.getRenterId()).child("renting").child(activeTransitionKey).removeValue();

        // Delete pending from owner user
        users.child(activeTransactionInfo.getItem().getOwnerID()).child("pending").child(activeTransitionKey).removeValue();
    }
}
