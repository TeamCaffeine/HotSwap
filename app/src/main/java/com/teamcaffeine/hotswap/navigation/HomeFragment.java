package com.teamcaffeine.hotswap.navigation;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.teamcaffeine.hotswap.ListItemActivity;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.login.User;
import com.teamcaffeine.hotswap.swap.Item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * The Home Fragment is the landing page for the user when they first open the app.
 * Its purpose is to display all of ht items they have listed and show what items they
 * are currently renting and lending. This is also the page where they have the option
 * to list new items with the "list items" button. This button takes them to the List
 * Item Activity.
 */
public class HomeFragment extends Fragment {

    private String TAG = "HomeFragment";

    // create objects to reference layout objects
    private Button btnListItem;
    private ListView listviewAllItems;
    private List<String> itemsElementsList;
    private ArrayAdapter<String> itemsAdapter;
    private TextView txtCurrentlyRenting;
    private ListView listviewLending;
    private List<String> lendingElements;
    private ArrayAdapter<String> lendingAdapter;
    private int LIST_ITEM_REQUEST_CODE = 999;
    private int RESULT_ERROR = 88;

    // progress dialog to show page is loading
    public ProgressDialog mProgressDialog;

    // Database reference fields
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference users;
    private String userTable = "users";
    private String itemTable = "items";

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

        // get the view from the layout
        btnListItem = view.findViewById(R.id.btnListItem);
        listviewAllItems = view.findViewById(R.id.listviewAllItems);
        txtCurrentlyRenting = view.findViewById(R.id.txtCurrentlyRenting);
        listviewLending = view.findViewById(R.id.listviewLending);

        // Get a database reference to our user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        users = database.getReference().child(userTable);

        // instantiate the list that wil hold all of the user's items
        itemsElementsList = new ArrayList<String>();

        // DELETE AN ITEM
        // This onClick listener allows the user the delete an item when they click on it
        // in the list view.
        listviewAllItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                // when the item is clicked, an alert dialog shows asking the user if they acutally
                // want to delete the item
                AlertDialog myQuittingDialogBox = new AlertDialog.Builder(getContext())
                        //set message, title, and icon
                        .setTitle(R.string.delete)
                        .setMessage(R.string.delete_item_question)
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            // if they click the "delete" button, delete the item
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //TODO: data validation
                                // get the name of the item to be deleted
                                final String itemToRemove = listviewAllItems.getItemAtPosition(position).toString();
                                // get a reference to the items table in the database, where the item also needs to be removed
                                DatabaseReference items = database.getReference().child(itemTable);
                                // add a listener to the items table in the database to delete the item
                                items.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    // get a snapshot of the ENTIRE items table, so the onDataChange will detect when an
                                    // item is deleted from the items table (since items are not stored per user, every item
                                    // is stored in the data table with a reference to the appropriate user as one of the
                                    // attributes)
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        // loop through all of the items in the table from the dataSnapshot
                                        for (DataSnapshot i: dataSnapshot.getChildren()) {
                                            // create an item object so you can read the item's contents
                                            Item item = i.getValue(Item.class);
                                            // check if the item you are currently looping through belongs to the current user
                                            if (item.getOwnerID().equals(firebaseUser.getUid())){
                                                // if the item does belong to the user, get its name
                                                String iteratingItem = item.getName();
                                                // if the item you are currenlty looping thorugh is the item the user wants to remove,
                                                // remove it
                                                if (iteratingItem.equalsIgnoreCase(itemToRemove)){
                                                    // first remove it from the item list for the listView in the UI
                                                    // the boolean didRemove will indicate when the item has been successfully
                                                    // deleted from the list. Only 0nce it has been successfully deleted from
                                                    // the UI, delete it from the database.
                                                    boolean didRemove = itemsElementsList.remove(listviewAllItems.getItemAtPosition(position).toString());
                                                    if (didRemove) {
                                                        // notify the listView adapter that the data has changed so that the UI is updated
                                                        itemsAdapter.notifyDataSetChanged();

                                                        // Remove the item and update database
                                                        i.getRef().removeValue();
                                                    } else {
                                                        // if the item was NOT successfully removed from the item list, log the error message
                                                        Log.i(TAG, "User attempted to delete a nonexistent item");
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // if the user cancels the operation, log the error message
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e(TAG, "Item update failed", databaseError.toException());
                                    }
                                });
                                // after the item has been deleted, dismiss the alert dialog
                                dialog.dismiss();
                            }
                        })
                        // if the user clicks "cancel" instead of "delete," dismiss the dialog and do not
                        // do anything to the database
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        // once all of the above functionality had been set create the alert dialog box
                        .create();
                // once the dialog box has been created, show it
                myQuittingDialogBox.show();
            }
        });


        // ADDING THE ITEM LIST TO THE UI
        // get a reference to the items table in the database
        DatabaseReference items = database.getReference().child(itemTable);
        // add a listener to the items table
        items.addListenerForSingleValueEvent( new ValueEventListener(){
            @Override
            // get a data snapshot of the whole table
            public void onDataChange(DataSnapshot dataSnapshot) {
                // loop through all of the items in the items table
                // we need to loop thorugh all of the items become items do not belong to users,
                // each items is in the table and the appropriate user is stored as an attribute.
                // When we want to retrieve a list of a user's items, we need to loop thorugh all
                // of them and select the user's items.
                for(DataSnapshot i : dataSnapshot.getChildren() ){
                    // create an item objecct to read each item's contents
                    Item item = i.getValue(Item.class);
                    // if the item belongs to the user, add it to the list of the user's items
                    if (item.getOwnerID().equals(firebaseUser.getUid())){
                        itemsElementsList.add(item.getName());
                    }
                }
                // once we have a list of the user's items, create the listview adapater
                itemsAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, itemsElementsList);
                // set tbe adapter on the listview in the UI
                listviewAllItems.setAdapter(itemsAdapter);
                // notify the adapter that the dataset has changed so that it shows the new
                // list of the user's items
                itemsAdapter.notifyDataSetChanged();
            }

            // if the read of the items table failed, log the error message
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        // LIST A NEW ITEM
        // set an onClick listener for the List Item button
        btnListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // spawn an intent to the List Item Activity
                Intent i = new Intent(getActivity(), ListItemActivity.class);
                // send the current list of items with the intent
                // so when a new item is added, it is added to the list
                Bundle args = new Bundle();
                args.putSerializable("itemList",(Serializable)itemsElementsList);
                i.putExtra("BUNDLE",args);


                // start the Activity with the appropriate request code
                startActivityForResult(i, LIST_ITEM_REQUEST_CODE);
            }
        });
    }

    // required constructer for the interface for inter-fragment communication
    public interface HomeFragmentListener {
    }

    // attach the listener for inter-fragment communcation with the Navigation Activity
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        HFL = (HomeFragment.HomeFragmentListener) context;  //context is a handle to the main activity, let's bind it to our interface.
    }

    //TODO: Figure out why the list of items doesn't get updated after adding a new item, but shows all items, including added ones, when the Home Fragment is built in onCreate
    // ADDING A NEW ITEM
    // Once the user lists a new item in the List Item Activity, it will be added to their items list
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult called");
        super.onActivityResult(requestCode, resultCode, data);
        // The intent to list a new item was called with startActivityForResult, so we used a request code.
        // here we make sure that we are getting a response only from the intent that we called with
        // our request code.
        if (requestCode == LIST_ITEM_REQUEST_CODE) {
            Log.i(TAG, "request code = List Item Request Code");
            // startActivityForResult also returns a result code, which checks the result of the List Item Activity.
            // Here, RESULT_OK indicates that the item was successfully added to the database. We want to make sure
            // that items are only added to the UI if they are added to the database, and vice versa.
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "result OK");
//                // set the items list equal to the updated items list that we got in the callback
//                // from the List Item Activity
//                 Bundle args = data.getBundleExtra("BUNDLE");
//                itemsElementsList = (ArrayList<String>) args.getSerializable("updatedItemList");
                // get the new item that was added in List Item from the Intent
                Bundle extras = data.getExtras();
                String newItem = extras.getString("newItem");
                // add the new item to the list
                itemsElementsList.add(newItem);

                // notify the adapter that the dataset has changed so that it updates the UI
                itemsAdapter.notifyDataSetChanged();
//                // get a reference to the items table in the database
//                DatabaseReference items = database.getReference().child(itemTable);
//                Log.i(TAG, "items table reference created");
//                // add a listener to the items table
//                items.addListenerForSingleValueEvent( new ValueEventListener(){
//                    @Override
//                    // get a dataSnapshot of all items in the table
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        Log.i(TAG, "onDataChange called");
//                        // loop through all items in the table
//                        for(DataSnapshot i : dataSnapshot.getChildren() ){
//                            // create an item object to read each item's contents
//                            Item item = i.getValue(Item.class);
//                            if (item.getOwnerID().equals(firebaseUser.getUid())){
//                                itemsElementsList.add(item.getName());
//                            }
//                        }
//                        itemsAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, itemsElementsList);
//                        listviewAllItems.setAdapter(itemsAdapter);
//                        itemsAdapter.notifyDataSetChanged();
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                        System.out.println("The read failed: " + databaseError.getCode());
//                    }
//                });
                // show a toast to notify the user that their item was successfully added
                Toast.makeText(getContext(), "New item added", Toast.LENGTH_LONG).show();

            } else if (resultCode == RESULT_ERROR) {
                Log.i(TAG, "item not added");
                Toast.makeText(getContext(), R.string.unable_to_add_item, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}