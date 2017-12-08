package com.teamcaffeine.hotswap.navigation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.base.Strings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.maps.Items;
import com.teamcaffeine.hotswap.maps.LocationPrefs;

import com.firebase.client.Firebase;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.teamcaffeine.hotswap.swap.Item;
import com.teamcaffeine.hotswap.swap.ItemDetailsActivity;

import java.io.IOException;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMarkerDragListener {
    private float zoomlevel;
    private GoogleMap mMap;
    private GoogleApiClient client;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private int progressSeekbar = 500;
    private Circle circle;
    public static final int REQUEST_LOCATION_CODE = 99;
    private ListView lvItems; //Reference to the listview GUI component
    private Items lvAdapter; // //Reference to the Adapter used to populate the listview.
    private TextView localeMsg;
    private Button bSearch;
    private TextView locale, filters;
    private SeekBar progress;
    private SharedPreferences prefs;
    private String TAG = "696969";
    private double lat;
    private double lng;


    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private String geoFireTable = "items_location";
    private DatabaseReference geoFireRef = database.getReference(geoFireTable);
    private HashMap<String, String> hashMapMarkerTitle = new HashMap<>();
    private GeoFire geoFire = new GeoFire(geoFireRef);
    private GeoLocation currentLocation;
    private DatabaseReference ref;

    private int SET_LOCATION_REQUEST_CODE = 1730;

    SearchFragment.SearchFragmentListener SFL;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        Firebase.setAndroidContext(getActivity());

        // Get shared preferences
        prefs = getActivity().getSharedPreferences(getString(R.string.base_package_name), Context.MODE_PRIVATE);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bSearch = (Button) view.findViewById(R.id.bSearch);
        lvItems = (ListView) view.findViewById(R.id.itemLists);
        lvAdapter = new Items(getActivity());
        lvItems.setAdapter(lvAdapter);
        lvItems.setVisibility(View.INVISIBLE);

        bSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show results
                EditText tfLocation = (EditText) view.findViewById(R.id.tfLocation);
                // String location = tfLocation.getText().toString();
                //if(! location.equals("")) {
                // checks if user entered anything or not "empty string"
                //   if (location.equals("Vacuum")) {
                lvItems.setVisibility(View.VISIBLE);
                //   }
                // }
//                else{
//                    Toast.makeText(getActivity(), "No Items", Toast.LENGTH_SHORT).show();
//                }
            }
        });
        // Underlines locale and filters
        locale = (TextView) view.findViewById(R.id.setLocaleFilters);
        SpannableString content = new SpannableString(locale.getText());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        locale.setText(content);

        filters = (TextView) view.findViewById(R.id.setItemFilters);
        SpannableString itemcontent = new SpannableString(filters.getText());
        itemcontent.setSpan(new UnderlineSpan(), 0, itemcontent.length(), 0);
        filters.setText(itemcontent);
        filters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Show filter options
                Toast.makeText(getActivity(), "Filter Button Pressed", Toast.LENGTH_SHORT).show();
            }
        });
        localeMsg = (TextView) view.findViewById(R.id.setLocaleMsg);
        final String city = prefs.getString("city", "");
        progress = (SeekBar) view.findViewById(R.id.circleFilter);


        if (Strings.isNullOrEmpty(city)) {
            localeMsg.setText("");
        } else {
            localeMsg.setText("Items near " + city);
        }
        locale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i2 = new Intent(getActivity(), LocationPrefs.class);
                startActivityForResult(i2, SET_LOCATION_REQUEST_CODE);
                localeMsg.setText("Items near " + prefs.getString("city", ""));
            }

        });
        lvItems.setAdapter(lvAdapter);
        lvItems.setVisibility(View.INVISIBLE);

        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Item item = (Item) adapterView.getItemAtPosition(i);
                Intent itemDetailsIntent = new Intent(getActivity(), ItemDetailsActivity.class);
                itemDetailsIntent.putExtra("item", item);
                itemDetailsIntent.putExtra("currentCity", city);
                startActivity(itemDetailsIntent);
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // marshmellow
            checkLocationPermission();
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SET_LOCATION_REQUEST_CODE) {
            if (resultCode == 0) {
                localeMsg.setText(prefs.getString("city", ""));
                onLocationChanged(null);
                mMap.clear();
                onMapReady(mMap);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        super.onResume();
        getActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
    }

    @Override
    public void onResume() {
        super.onResume();
        super.onPause();
        getActivity().overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;
        } else {
            return true;
        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) { // should automatically be at current location
        Log.e(TAG, "Calling onMapReady");
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        mMap.setOnMarkerDragListener(this);

        if (prefs.contains("zip")) {
            String postalcode = prefs.getString("zip", "02215");
            String key = "https://maps.googleapis.com/maps/api/geocode/json?address=";
            String api = "&key=AIzaSyCdD6V_pMev1dl8LAsoJ6PLG5JLnR-OiUc";
            String stringUrl = key + postalcode + api;

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(stringUrl).get().build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    // do nothing, POC
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String jsonData = response.body().string();
                    Gson gson = new Gson();
                    JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);
                    final double lat = jsonObject.getAsJsonArray("results").get(0)
                            .getAsJsonObject().get("geometry")
                            .getAsJsonObject().get("location")
                            .getAsJsonObject().get("lat")
                            .getAsDouble();
                    final double lng = jsonObject.getAsJsonArray("results").get(0)
                            .getAsJsonObject().get("geometry")
                            .getAsJsonObject().get("location")
                            .getAsJsonObject().get("lng")
                            .getAsDouble();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final LatLng latlng = new LatLng(lat, lng);

                            mMap.clear();
                            double dragLat = latlng.latitude;
                            double dragLong = latlng.longitude;
                            setLocaleArea(dragLat, dragLong);

                            Marker stopMarker = mMap.addMarker(new MarkerOptions()
                                    .draggable(true)
                                    .position(latlng)
                                    .title("Current Location"));
                            CircleOptions circleOptions = new CircleOptions()
                                    .center(stopMarker.getPosition()).radius(progressSeekbar).strokeWidth(5.0f)
                                    .strokeColor(Color.parseColor("#00BFFF"))
                                    .fillColor(Color.argb(
                                            50, //This is your alpha.  Adjust this to make it more or less translucent
                                            Color.red(Color.BLUE), //Red component.
                                            Color.green(Color.BLUE),  //Green component.
                                            Color.blue(Color.BLUE)));  //Blue component.);
                            circle = mMap.addCircle(circleOptions);
                            setQueryinGoogleMaps(latlng);

                            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker) {
                                        return false;
                                    }

                            });

                            progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                    // progress = progress*10;

                                }

                                @Override
                                public void onStartTrackingTouch(final SeekBar seekBar) {
                                }

                                @Override
                                public void onStopTrackingTouch(final SeekBar seekBar) {
                                    progressSeekbar = seekBar.getProgress();
                                    System.out.println(progressSeekbar);
                                    circle.setRadius(progressSeekbar);
                                    setQueryinGoogleMaps(latlng);


                                }

                            });

                            zoomlevel = 13.5f;
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoomlevel));
                            zoomlevel=mMap.getCameraPosition().zoom;


                        }
                    });
                }
            });
        }
        Log.e(TAG, "MAP IS READY AND LOADED");

//        else{
//            LatLng currentLocale = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocale, zoomlevel));
//        }


//
//        // if preferences do not exist
//        else {
//            LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
//            Marker stopMarker = mMap.addMarker(new MarkerOptions()
//                    .draggable(true)
//                    .position(latlng)
//                    .title("Current Location"));
//            circleOptions = new CircleOptions()
//                    .center(stopMarker.getPosition()).radius(500).strokeWidth(5.0f)
//                    .strokeColor(Color.parseColor("#00BFFF"))
//                    .fillColor(Color.argb(
//                            50, //This is your alpha.  Adjust this to make it more or less translucent
//                            Color.red(Color.BLUE), //Red component.
//                            Color.green(Color.BLUE),  //Green component.
//                            Color.blue(Color.BLUE)));  //Blue component.);
//            circle = mMap.addCircle(circleOptions);
//
//            progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                    // progress = progress*10;
////                    circle.setRadius(progress);
////                    float[] distance = new float[2];
////                    Location.distanceBetween(42.365014, -71.102660,
////                            circle.getCenter().latitude, circle.getCenter().longitude, distance);
////
////                    if (distance[0] > circle.getRadius()) {
////                        Toast.makeText(getActivity(), "iVacuum X is Outside the circle", Toast.LENGTH_SHORT).show();
////                    } else {
////                        Toast.makeText(getActivity(), "iVacuum X is Inside the circle", Toast.LENGTH_SHORT).show();
////                    }
//                }
//
//                @Override
//                public void onStartTrackingTouch(final SeekBar seekBar) {
//                }
//
//                @Override
//                public void onStopTrackingTouch(final SeekBar seekBar) {
//                    circle.setRadius(seekBar.getProgress());
//                    database = FirebaseDatabase.getInstance();
//                    geoFireRef = database.getReference(geoFireTable);
//                    GeoFire geoFire = new GeoFire(geoFireRef);
//
//                    GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latlng.latitude, latlng.longitude), progress/1000.0);
//                    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
//                        @Override
//                        public void onKeyEntered(String key, GeoLocation location) {
//                            System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
//                            Marker markerKey = mMap.addMarker(new MarkerOptions()
//                                    .draggable(true)
//                                    .position(new LatLng(location.latitude, location.longitude))
//                                    .title("Item"));
//                            HashMap<String,Marker> hashMapMarker = new HashMap<>();
//                            hashMapMarker.put(key,markerKey);
//
//                        }
//
//                        @Override
//                        public void onKeyExited(String key) {
//                                          mMap.clear()
//                            // redo geoquery
//                            System.out.println(String.format("Key %s is no longer in the search area", key));
//
//                        }
//
//                        @Override
//                        public void onKeyMoved(String key, GeoLocation location) {
//                            System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
//
//                        }
//
//                        @Override
//                        public void onGeoQueryReady() {
//                            System.out.println("All initial data has been loaded and events have been fired!");
//                        }
//
//                        @Override
//                        public void onGeoQueryError(DatabaseError error) {
//                            System.err.println("There was an error with this query: " + error);
//                        }
//                    });
//                                    float[] distance = new float[2];
//                                    Location.distanceBetween(42.365014, -71.102660,
//                                            circle.getCenter().latitude, circle.getCenter().longitude, distance);
//
//                                    if (distance[0] > circle.getRadius()) {
//                                        Toast.makeText(getActivity(), "iVacuum X is Outside the circle", Toast.LENGTH_SHORT).show();
//                                    } else {
//                                        Toast.makeText(getActivity(), "iVacuum X is Inside the circle", Toast.LENGTH_SHORT).show();
//                                    }
//                }
//            });
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15.5f));
//        }
//


        lvItems.setVisibility(View.VISIBLE);
    }

    protected synchronized void buildGoogleApiClient() {
        client = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        client.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Google Developer Explanation on Deprecated FusionLocationApi
            // Please continue using the FusedLocationProviderApi class and
            // don't migrate to the FusedLocationProviderClient class until
            // Google Play services version 12.0.0 is available, which is
            // expected to ship in early 2018. Using the FusedLocationProviderClient
            // before version 12.0.0 causes the client app to crash when Google
            // Play services is updated on the device. We apologize for any
            // inconvenience this may have caused.
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;

        if (client != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    public void setQueryinGoogleMaps(final LatLng latlng){
        currentLocation = new GeoLocation(latlng.latitude, latlng.longitude);
        mMap.clear();
        lvAdapter.nuke();
        final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latlng.latitude, latlng.longitude), progressSeekbar/1000.0);
        final HashMap<String,MarkerOptions> hashMapMarker = new HashMap<>();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, GeoLocation location) {
                Log.e(TAG, String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));

                final MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(location.latitude, location.longitude));
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                hashMapMarker.put(key,markerOptions);
                ref = database.getReference().child("items").child(key);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Item item = dataSnapshot.getValue(Item.class);
                        lvAdapter.putItem(item);
                        String title =  item.getName();
                        hashMapMarkerTitle.put(key, title);
                        hashMapMarker.get(key).title(title);
                        mMap.addMarker(hashMapMarker.get(key));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Item " + key + "not found.");
                    }
                });
            }
            @Override
            public void onKeyExited(String key) {
                System.out.println(String.format("Key %s is no longer in the search area", key));

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));

            }

            @Override
            public void onGeoQueryReady() {
                Log.e(TAG, "All initial data has been loaded and events have been fired!");
                geoQuery.setCenter(currentLocation);
                geoQuery.setRadius(progressSeekbar/1000.0);
                Marker stopMarker = mMap.addMarker(new MarkerOptions()
                        .draggable(true)
                        .position(latlng)
                        .title("Current Location"));
                CircleOptions circleOptions = new CircleOptions()
                        .center(stopMarker.getPosition()).radius(progressSeekbar).strokeWidth(5.0f)
                        .strokeColor(Color.parseColor("#00BFFF"))
                        .fillColor(Color.argb(
                                50, //This is your alpha.  Adjust this to make it more or less translucent
                                Color.red(Color.BLUE), //Red component.
                                Color.green(Color.BLUE),  //Green component.
                                Color.blue(Color.BLUE)));  //Blue component.);
                circle = mMap.addCircle(circleOptions);

                if(geoQuery != null){
                    geoQuery.removeAllListeners();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
            }

        });


    }

    public void setLocaleArea(Double lat, Double lng){
        String key = "https://maps.googleapis.com/maps/api/geocode/json?latlng=";
        String latitude = Double.toString(lat);
        String longitude = Double.toString(lng);
        String api = "&key=AIzaSyCdD6V_pMev1dl8LAsoJ6PLG5JLnR-OiUc";
        String stringUrl = key+latitude+","+longitude+api;
        System.out.println(stringUrl);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(stringUrl).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                // do nothing, POC
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String jsonData = response.body().string();
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);

                JsonArray jsonArray = jsonObject.getAsJsonArray("results").get(0)
                        .getAsJsonObject().getAsJsonArray("address_components");
                String city = "";
                for (int i = 0; i < jsonArray.size(); i++) {
                    if (jsonArray.get(i).getAsJsonObject().get("types").getAsJsonArray().get(0).getAsString().equals("locality")){
                        city = jsonArray.get(i).getAsJsonObject().get("long_name").getAsString();
                        System.out.println(city);
                        //   result.setText(city);
                    }
                }
                final String finalCity = city;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        localeMsg.setText("Items near " + finalCity);
                    }
                });
            }
        });
    }
    @Override
    public void onMarkerDragEnd(Marker marker) {
        mMap.clear();
        LatLng dragPosition = marker.getPosition();
        double dragLat = dragPosition.latitude;
        double dragLong = dragPosition.longitude;
        setLocaleArea(dragLat, dragLong);
        final LatLng latlng = new LatLng(dragLat, dragLong);

        Marker stopMarker = mMap.addMarker(new MarkerOptions()
                .draggable(true)
                .position(latlng)
                .title("Current Location"));
        CircleOptions circleOptions = new CircleOptions()
                .center(stopMarker.getPosition()).radius(progressSeekbar).strokeWidth(5.0f)
                .strokeColor(Color.parseColor("#00BFFF"))
                .fillColor(Color.argb(
                        50, //This is your alpha.  Adjust this to make it more or less translucent
                        Color.red(Color.BLUE), //Red component.
                        Color.green(Color.BLUE),  //Green component.
                        Color.blue(Color.BLUE)));  //Blue component.);
        circle = mMap.addCircle(circleOptions);
        setQueryinGoogleMaps(latlng);

        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
                progressSeekbar = seekBar.getProgress();
                System.out.println(progressSeekbar);
                circle.setRadius(progressSeekbar);
                setQueryinGoogleMaps(latlng);
            }

        });

        zoomlevel=mMap.getCameraPosition().zoom;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoomlevel));
    }


    public interface SearchFragmentListener {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        SFL = (SearchFragment.SearchFragmentListener) context;
    }

}




