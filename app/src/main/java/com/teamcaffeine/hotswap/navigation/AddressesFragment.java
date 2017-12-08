package com.teamcaffeine.hotswap.navigation;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.login.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class AddressesFragment extends Fragment {

    // Database reference fields
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference users;
    private String userTable = "users";

    // Place codes
    final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    // View references and other fields
    private Button btnAddAddress;
    private Button btnRemoveAddress;
    private ListView listviewAddresses;
    private List<String> addressElementsList;
    private ArrayAdapter<String> addressAdapter;
    private String TAG = "AddressFragment";

    private String selectedAddress;

    // Safely get the address the user has selected
    public String getSelectedAddress() {
        if (selectedAddress != null) {
            return selectedAddress;
        } else {
            return null;
        }
    }

    public AddressesFragment() {
        // Required empty public constructor
    }

    // Standard onCreateView for fragment
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_addresses, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get a reference to our user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        users = database.getReference().child(userTable);

        // Set up our view elements and actions
        btnAddAddress = view.findViewById(R.id.btnAddAddress);
        btnAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Launch the Google Places Intent
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(getActivity());
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    Log.e(TAG, "Google Places Error", e);
                }
            }
        });

        btnRemoveAddress = view.findViewById(R.id.btnRemoveAddress);
        btnRemoveAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Safely handle remove click if there are no addresses or no address is selected
                if (addressElementsList.isEmpty() || listviewAddresses.getCheckedItemPosition() == -1) {
                    return;
                }

                // Remove the address from the database
                DatabaseReference ref = users.child(firebaseUser.getUid());
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);

                        boolean didRemove = user.removeAddress(listviewAddresses.getItemAtPosition(listviewAddresses.getCheckedItemPosition()).toString());
                        if (didRemove) {
                            // Update database
                            Map<String, Object> userUpdate = new HashMap<>();
                            userUpdate.put(firebaseUser.getUid(), user.toMap());
                            users.updateChildren(userUpdate);
                        } else {
                            Log.i(TAG, "User attempted to delete a nonexistent address");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Address update failed", databaseError.toException());
                    }
                });
            }
        });

        // Allow updates to selectedAddress
        listviewAddresses = view.findViewById(R.id.listviewAddresses);
        listviewAddresses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedAddress = addressElementsList.get(position);
            }
        });

        // Top level database listener to synchronize our address list to any database changes
        DatabaseReference ref = users.child(firebaseUser.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                addressElementsList = user.getAddresses();
                addressAdapter = new ArrayAdapter<String>
                        (getContext(), android.R.layout.simple_list_item_1, addressElementsList);
                listviewAddresses.setAdapter(addressAdapter);
                addressAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "The read failed:", databaseError.toException());
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Handle the response from Google Places intent
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                final Place place = PlaceAutocomplete.getPlace(getActivity(), data);

                DatabaseReference ref = users.child(firebaseUser.getUid());
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);

                        boolean didAdd = user.addAddress(place.getAddress().toString());
                        if (didAdd) {
                            // Update database, and trust that the value listener from onCreate will adjust the UI for us
                            Map<String, Object> userUpdate = new HashMap<>();
                            userUpdate.put(firebaseUser.getUid(), user.toMap());
                            users.updateChildren(userUpdate);
                        } else {
                            Log.i(TAG, "User attempted to add a duplicate address");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Address update failed", databaseError.toException());
                    }
                });
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                Log.i(TAG, status.getStatusMessage());
                Toast.makeText(getContext(), R.string.unable_to_add_address, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}
