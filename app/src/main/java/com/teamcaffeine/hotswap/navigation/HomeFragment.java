package com.teamcaffeine.hotswap.navigation;

/**
 * IMPORTANT FILE FOR THIS PR
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamcaffeine.hotswap.login.User;
import com.teamcaffeine.hotswap.navigation.homeAdapters.OwnedItemsAdapter;
import com.teamcaffeine.hotswap.navigation.homeAdapters.RentingPendingItemsAdapter;
import com.teamcaffeine.hotswap.swap.ListItemActivity;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.swap.Item;

import java.io.Serializable;


public class HomeFragment extends Fragment {

    private String TAG = "HomeFragment";

    // create objects to reference layout objects
    private Button btnListItem;
    private ListView listviewOwnedItems;
    private OwnedItemsAdapter ownedItemsAdapter;
    private ListView listviewRenting;
    private RentingPendingItemsAdapter rentingAdapter;
    private ListView listviewPending;
    private RentingPendingItemsAdapter pendingAdapter;
    private int LIST_ITEM_REQUEST_CODE = 999;
    private int RESULT_ERROR = 88;

    // progress dialog to show page is loading
    public ProgressDialog mProgressDialog;

    // Database reference fields
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference itemslocation;
    private DatabaseReference items; // reference to the items table, used for onDataChange listening
    private DatabaseReference currentUser;
    private String itemTable = "items";
    private String userTable = "users";
    private String itemlocationsTable="items_location";

    private ValueEventListener itemsEventListener;
    private ValueEventListener userEventListener;

    // progress dialog to show page is loading
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    // hide progress dialog once page loads
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    // fragment listener for inter-fragment communication from the Navigation Activity
    private HomeFragmentListener HFL;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "onViewCreated");

        // get the views from the layout
        btnListItem = view.findViewById(R.id.btnListItem);

        listviewOwnedItems = view.findViewById(R.id.listviewAllItems);
        listviewPending = view.findViewById(R.id.listviewPending);
        listviewRenting = view.findViewById(R.id.listviewRenting);

        // instantiate the list that wil hold all of the user's items
        ownedItemsAdapter = new OwnedItemsAdapter(getContext());
        rentingAdapter = new RentingPendingItemsAdapter(getContext());
        pendingAdapter = new RentingPendingItemsAdapter(getContext());

        // set tbe adapter on the listview in the UI
        listviewOwnedItems.setAdapter(ownedItemsAdapter);
        listviewRenting.setAdapter(rentingAdapter);
        listviewPending.setAdapter(pendingAdapter);

        // Get a database reference to our user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // get a reference to our database
        database = FirebaseDatabase.getInstance();

        // get a reference to the items table, current user, and items location table
        items = database.getReference().child(itemTable);
        currentUser = database.getReference().child(userTable).child(firebaseUser.getUid());
        itemslocation = database.getReference().child(itemlocationsTable);

        itemsEventListener = new ValueEventListener() {
            @Override
            // get a data snapshot of the whole table
            public void onDataChange(DataSnapshot dataSnapshot) {
                ownedItemsAdapter.clear();
                for (DataSnapshot i : dataSnapshot.getChildren()) {
                    Item item = i.getValue(Item.class);
                    if (item.getOwnerID().equals(firebaseUser.getUid())) {
                        ownedItemsAdapter.putItem(item);
                    }
                }
                ownedItemsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "The read failed: " + databaseError.getCode());
            }
        };

        // add the event listener to the items table
        items.addValueEventListener(itemsEventListener);

        // Create the event listener to listen to database changes
        userEventListener = new ValueEventListener() {
            @Override
            // get a data snapshot of the whole table
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                rentingAdapter.putItems(user.getRenting());
                pendingAdapter.putItems(user.getPending());

                rentingAdapter.notifyDataSetChanged();
                pendingAdapter.notifyDataSetChanged();
            }

            // if the read of the items table failed, log the error message
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "The read failed: " + databaseError.getCode());
            }
        };

        currentUser.addValueEventListener(userEventListener);

        btnListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), ListItemActivity.class);
                // send the current list of items with the intent
                Bundle args = new Bundle();
                args.putSerializable("itemList",(Serializable) ownedItemsAdapter.getListOfItemNames());
                i.putExtra("BUNDLE",args);

                // start the Activity with the appropriate request code
                startActivityForResult(i, LIST_ITEM_REQUEST_CODE);
            }
        });
    }

    public interface HomeFragmentListener {
        // required constructer for the interface for inter-fragment communication
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        HFL = (HomeFragment.HomeFragmentListener) context;  //context is a handle to the main activity, let's bind it to our interface.
    }

    // Once the user lists a new item in the List Item Activity, it will be added to their items list
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult called");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LIST_ITEM_REQUEST_CODE) {
            Log.i(TAG, "request code = List Item Request Code");
            Log.i(TAG, "result code = " + resultCode);
            if (resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "item added to list");
                Toast.makeText(getContext(), R.string.item_added, Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_ERROR) {
                Log.i(TAG, "item not added");
                Toast.makeText(getContext(), R.string.unable_to_add_item, Toast.LENGTH_SHORT).show();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i(TAG, "request canceled, result code = " + resultCode);
                // The user canceled the operation.
            }
        }
    }

    // when the fragment is paused, remove the value event listener
    @Override
    public void onPause() {
        super.onPause();
        // using the reference to the items table, remove the listener
        items.removeEventListener(itemsEventListener);
        currentUser.removeEventListener(userEventListener);
    }

    // when the fragment is resumed, add the event listener
    @Override
    public void onResume() {
        super.onResume();
        // using the reference to the items table, add the listener
        items.addValueEventListener(itemsEventListener);
        currentUser.addValueEventListener(userEventListener);
    }
}
