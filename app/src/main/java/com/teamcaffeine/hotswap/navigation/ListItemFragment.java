package com.teamcaffeine.hotswap.navigation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.timessquare.CalendarPickerView;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.swap.Item;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ListItemFragment extends Fragment {

    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference items;
    private String itemTable = "items";

    private EditText editItemName;
    private EditText editTags;
    private EditText editPrice;
    private EditText editDescription;
    private EditText editAddress;
    private Button listItemButton;
    private CalendarPickerView calendar;
    private DatabaseReference databaseF;
    private GeoFire geoFire;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_list_item, null);
        return root;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void onViewCreated(View view, Bundle savedInstanceState) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        items = database.getReference(itemTable);

        editItemName = (EditText) getView().findViewById(R.id.editItemName);
        editTags = (EditText) getView().findViewById(R.id.editTags);
        editPrice = (EditText) getView().findViewById(R.id.editPrice);
        editDescription = (EditText) getView().findViewById(R.id.editDescription);
        editAddress = (EditText) getView().findViewById(R.id.editAddress);
        calendar = (CalendarPickerView) getView().findViewById(R.id.calendarView);
        listItemButton = (Button) getView().findViewById(R.id.listItemButton);

        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        Date today = new Date();
        calendar.init(today, nextYear.getTime())
                .withSelectedDate(today)
                .inMode(CalendarPickerView.SelectionMode.MULTIPLE);

        calendar.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                v.onTouchEvent(event);
                return true;
            }
        });

        listItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });


        databaseF = FirebaseDatabase.getInstance().getReference("items_location");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ItemIDHere");
        geoFire = new GeoFire(databaseF.child(userId));

        geoFire.setLocation("Item example", new GeoLocation(42.365014, -71.102660), new GeoFire.CompletionListener()

        {
            @Override
            public void onComplete (String key, DatabaseError error){
                if (error != null) {
                    System.err.println("There was an error saving the location to GeoFire: " + error);
                } else {
                    System.out.println("Location saved on server successfully!");
                }
            }
        });

    }

    ListItemFragment.ListItemFragmentListener LIFL;

    public interface ListItemFragmentListener {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        LIFL = (ListItemFragment.ListItemFragmentListener) context;  //context is a handle to the main activity, let's bind it to our interface.
    }

    private void submit() {
        items.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String itemName = editItemName.getText().toString();
                String itemAddress = editAddress.getText().toString();
                String itemPrice = editPrice.getText().toString();
                String itemDescription = editDescription.getText().toString();

                Item item = new Item(itemName, firebaseUser.getUid(), itemDescription, itemPrice, itemAddress);

                String key = items.push().getKey();

                Map<String, Object> itemUpdate = new HashMap<>();
                itemUpdate.put(key, item.toMap());

                items.updateChildren(itemUpdate);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("ListItemFragment", "The read failed: " + databaseError.getCode());
            }
        });
    }
}
