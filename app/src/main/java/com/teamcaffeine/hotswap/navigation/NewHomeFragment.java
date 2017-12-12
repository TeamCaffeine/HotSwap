package com.teamcaffeine.hotswap.navigation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.github.florent37.hollyviewpager.HollyViewPager;
import com.github.florent37.hollyviewpager.HollyViewPagerConfigurator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.login.User;
import com.teamcaffeine.hotswap.navigation.homeAdapters.OwnedItemsAdapter;
import com.teamcaffeine.hotswap.navigation.homeAdapters.OwnedItemsCardAdapter;
import com.teamcaffeine.hotswap.navigation.homePages.OwnedScrollFragment;
import com.teamcaffeine.hotswap.swap.Item;
import com.teamcaffeine.hotswap.swap.ListItemActivity;

import java.io.Serializable;

public class NewHomeFragment extends Fragment {

    private String TAG = "HomeFragment";

    int pageCount = 1;

    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference items; // reference to the items table, used for onDataChange listening
    private DatabaseReference currentUser;
    private String itemTable = "items";

    private Button listItemButton;

    private OwnedItemsCardAdapter ownedItemsCardAdapter;
    private ValueEventListener itemsEventListener;
    private HollyViewPager hollyViewPager;
    private int LIST_ITEM_REQUEST_CODE = 999;
    private int RESULT_ERROR = 88;

    public NewHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_home, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "onViewCreated");

        // Get a database reference to our user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        hollyViewPager = view.findViewById(R.id.hollyViewPager);

        hollyViewPager.getViewPager().setPageMargin(getResources().getDimensionPixelOffset(R.dimen.viewpager_margin));
        hollyViewPager.setConfigurator(new HollyViewPagerConfigurator() {
            @Override
            public float getHeightPercentForPage(int page) {
                return 0.5f;
            }
        });

        // Initialize adapters
        ownedItemsCardAdapter = new OwnedItemsCardAdapter(getContext());

        hollyViewPager.setAdapter(new FragmentPagerAdapter(getActivity().getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0: return OwnedScrollFragment.newInstance("Owned Items", ownedItemsCardAdapter);
                }
                return OwnedScrollFragment.newInstance("Owned Items", ownedItemsCardAdapter);
            }

            @Override
            public int getCount() {
                return pageCount;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0: return "Owned Items";
                }
                return "TITLE " + position;
            }
        });

        // Get a database reference to our user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // get a reference to our database
        database = FirebaseDatabase.getInstance();
        items = database.getReference().child(itemTable);

        // Initialize listeners
        itemsEventListener = new ValueEventListener() {
            @Override
            // get a data snapshot of the whole table
            public void onDataChange(DataSnapshot dataSnapshot) {
                ownedItemsCardAdapter.clear();
                for (DataSnapshot i : dataSnapshot.getChildren()) {
                    Item item = i.getValue(Item.class);
                    if (item.getOwnerID().equals(firebaseUser.getUid())) {
                        ownedItemsCardAdapter.putItem(item);
                    }
                }
                ownedItemsCardAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "The read failed: " + databaseError.getCode());
            }
        };

        // add the event listener to the items table
        items.addValueEventListener(itemsEventListener);

        listItemButton = view.findViewById(R.id.listItemButton);
        listItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), ListItemActivity.class);
                // send the current list of items with the intent
                Bundle args = new Bundle();
                args.putSerializable("itemList", (Serializable) ownedItemsCardAdapter.getListOfItemNames());
                i.putExtra("BUNDLE", args);

                // start the Activity with the appropriate request code
                startActivityForResult(i, LIST_ITEM_REQUEST_CODE);
            }
        });
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
        //currentUser.removeEventListener(userEventListener);
    }

    // when the fragment is resumed, add the event listener
    @Override
    public void onResume() {
        super.onResume();
        // using the reference to the items table, add the listener
        items.addValueEventListener(itemsEventListener);
        //currentUser.addValueEventListener(userEventListener);
    }
}
