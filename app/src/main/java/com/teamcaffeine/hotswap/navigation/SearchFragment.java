package com.teamcaffeine.hotswap.navigation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
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
import com.google.firebase.auth.FirebaseAuth;
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
import com.teamcaffeine.hotswap.utility.LatLongUtility;

import org.w3c.dom.Text;

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
    private int progressSeekbar = 1000;
    private Circle circle;
    public static final int REQUEST_LOCATION_CODE = 99;
    private ListView lvItems; //Reference to the listview GUI component
    private Items lvAdapter; // //Reference to the Adapter used to populate the listview.
    private TextView localeMsg;
    private Button bSearch;
    private TextView locale, tags;
    private SeekBar progress;
    private SharedPreferences prefs;
    private String TAG = "SearchFragment";
    private double lat;
    private double lng;
    private LocationManager locationManager;
    private String provider;
    private boolean currentLocationPermissions = true;
    private LatLng latlng;
    private TextView circleRange;
    private EditText tfLocation;




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

    private void tagsPopup() {
        View popupView = LayoutInflater.from(getActivity()).inflate(R.layout.search_tag, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, 800, 800, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);

        // define view buttons

        // finally show up your popup window
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bSearch = (Button) view.findViewById(R.id.bSearch);
        lvItems = (ListView) view.findViewById(R.id.itemLists);
        circleRange = (TextView) view.findViewById(R.id.distanceInput);
        tfLocation = (EditText) view.findViewById(R.id.tfLocation);

        lvAdapter = new Items(getActivity());

        // Underlines locale and filters
        locale = (TextView) view.findViewById(R.id.setLocaleFilters);
        SpannableString content = new SpannableString(locale.getText());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        locale.setText(content);

        tags = (TextView) view.findViewById(R.id.setTagFilters);
        content = new SpannableString(tags.getText());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        tags.setText(content);

        tags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagsPopup();
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


        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Item item = (Item) adapterView.getItemAtPosition(i);
                Intent itemDetailsIntent = new Intent(getActivity(), ItemDetailsActivity.class);
                itemDetailsIntent.putExtra("itemID", item.getItemID());
                itemDetailsIntent.putExtra("currentCity", city);
                itemDetailsIntent.putExtra("ownerID", item.getOwnerID());
                startActivity(itemDetailsIntent);
            }
        });

//         Request location permission if not granted
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
                onLocationChanged(lastLocation);
                Log.i(TAG, Float.toString(zoomlevel));
                mMap.clear();
                onMapReady(mMap);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        currentLocationPermissions = true;
                        DoAfterMapsLoaded();
                    }
                } else {
                    currentLocationPermissions = false;
                    DoAfterMapsLoaded();

                }
                currentLocationPermissions = false;
                DoAfterMapsLoaded();
                return;
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
    private void DoAfterMapsLoaded(){
        if (prefs.contains("zip")) {
            String postalcode = prefs.getString("zip", "02215");
            String key = "https://maps.googleapis.com/maps/api/geocode/json?address=";
            String api = "&key=" + getString(R.string.locale_key);
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
                            latlng = new LatLng(lat, lng);

                            double dragLat = latlng.latitude;
                            double dragLong = latlng.longitude;
                            setLocaleArea(dragLat, dragLong);
                            setQueryinGoogleMaps(latlng);

                            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker) {
                                    return false;
                                }
                            });

                            progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                }

                                @Override
                                public void onStartTrackingTouch(final SeekBar seekBar) {
                                }

                                @Override
                                public void onStopTrackingTouch(final SeekBar seekBar) {
                                    progressSeekbar = seekBar.getProgress();
                                    circleRange.setText(String.format("%.2f", progressSeekbar/1000.0));
                                    System.out.println(progressSeekbar);
                                    circle.setRadius(progressSeekbar);
                                    bSearch.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            setQueryinGoogleMaps(latlng);
                                        }
                                    });
                                }
                            });
                            zoomlevel = 13.5f;
                            zoomlevel=mMap.getCameraPosition().zoom;
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoomlevel));
                        }
                    });
                }
            });
        }
        else { // if preferences do not exist
            // set location to currentlocation
            // if location services not enabled
            // set Toast to tell user to enable location services


            if (currentLocationPermissions == true) {
                locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                if (locationManager.getBestProvider(criteria, false) == null){
                    if (checkLocationPermission() == false) {
                        // LAT LNG OF CENTER OF AMERICA
                        latlng = new LatLng(37.0902, -95.7129);
                        zoomlevel = 3;
                    }
                    else{
                        provider = locationManager.getBestProvider(criteria, false);
                        lastLocation = locationManager.getLastKnownLocation(provider);
                        latlng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                        zoomlevel = 13.5f;
                    }
                }
                else {
                    provider = locationManager.getBestProvider(criteria, false);
                    lastLocation = locationManager.getLastKnownLocation(provider);
                    latlng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    zoomlevel = 13.5f;
                }

            }
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
                    circleRange.setText(String.format("%.2f", progressSeekbar/1000.0));
                    System.out.println(progressSeekbar);
                    circle.setRadius(progressSeekbar);
                    bSearch.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setQueryinGoogleMaps(latlng);
                        }
                    });
                }
            });
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoomlevel));
            zoomlevel = mMap.getCameraPosition().zoom;

        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) { // should automatically be at current location
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        mMap.setOnMarkerDragListener(this);
        checkLocationPermission();

        DoAfterMapsLoaded();


//        if (prefs.contains("zip")) {
//            String postalcode = prefs.getString("zip", "02215");
//            String key = "https://maps.googleapis.com/maps/api/geocode/json?address=";
//            String api = "&key=AIzaSyCdD6V_pMev1dl8LAsoJ6PLG5JLnR-OiUc";
//            String stringUrl = key + postalcode + api;
//
//            OkHttpClient client = new OkHttpClient();
//            Request request = new Request.Builder().url(stringUrl).get().build();
//
//            client.newCall(request).enqueue(new Callback() {
//                @Override
//                public void onFailure(Request request, IOException e) {
//                    // do nothing, POC
//                }
//
//                @Override
//                public void onResponse(Response response) throws IOException {
//                    String jsonData = response.body().string();
//                    Gson gson = new Gson();
//                    JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);
//                    final double lat = jsonObject.getAsJsonArray("results").get(0)
//                            .getAsJsonObject().get("geometry")
//                            .getAsJsonObject().get("location")
//                            .getAsJsonObject().get("lat")
//                            .getAsDouble();
//                    final double lng = jsonObject.getAsJsonArray("results").get(0)
//                            .getAsJsonObject().get("geometry")
//                            .getAsJsonObject().get("location")
//                            .getAsJsonObject().get("lng")
//                            .getAsDouble();
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            latlng = new LatLng(lat, lng);
//
//                            double dragLat = latlng.latitude;
//                            double dragLong = latlng.longitude;
//                            setLocaleArea(dragLat, dragLong);
//                            setQueryinGoogleMaps(latlng);
//
//                            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//                                @Override
//                                public boolean onMarkerClick(Marker marker) {
//                                    return false;
//                                }
//                            });
//
//                            progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                                }
//
//                                @Override
//                                public void onStartTrackingTouch(final SeekBar seekBar) {
//                                }
//
//                                @Override
//                                public void onStopTrackingTouch(final SeekBar seekBar) {
//                                    progressSeekbar = seekBar.getProgress();
//                                    circleRange.setText(String.format("%.2f", progressSeekbar/1000.0));
//                                    System.out.println(progressSeekbar);
//                                    circle.setRadius(progressSeekbar);
//                                    mMap.clear();
//                                    lvAdapter.nuke();
//                                    setQueryinGoogleMaps(latlng);
//                                }
//                            });
//                            zoomlevel = 13.5f;
//                            zoomlevel=mMap.getCameraPosition().zoom;
//                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoomlevel));
//                        }
//                    });
//                }
//            });
//        }
//        else { // if preferences do not exist
//            // set location to currentlocation
//            // if location services not enabled
//            // set Toast to tell user to enable location services
//
//
//            if (currentLocationPermissions == true) {
//                locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
//                Criteria criteria = new Criteria();
//                if (locationManager.getBestProvider(criteria, false) == null){
//                    if (checkLocationPermission() == false) {
//                        // LAT LNG OF CENTER OF AMERICA
//                        latlng = new LatLng(37.0902, -95.7129);
//                        zoomlevel = 3;
//                    }
//                    else{
//                        provider = locationManager.getBestProvider(criteria, false);
//                        lastLocation = locationManager.getLastKnownLocation(provider);
//                        latlng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
//                        zoomlevel = 13.5f;
//                    }
//                }
//                else {
//                    provider = locationManager.getBestProvider(criteria, false);
//                    lastLocation = locationManager.getLastKnownLocation(provider);
//                    latlng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
//                    zoomlevel = 13.5f;
//                }
//
//            }
//            setQueryinGoogleMaps(latlng);
//            progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                }
//
//                @Override
//                public void onStartTrackingTouch(final SeekBar seekBar) {
//                }
//
//                @Override
//                public void onStopTrackingTouch(final SeekBar seekBar) {
//                    progressSeekbar = seekBar.getProgress();
//                    circleRange.setText(String.format("%.2f", progressSeekbar/1000.0));
//                    System.out.println(progressSeekbar);
//                    circle.setRadius(progressSeekbar);
//                    setQueryinGoogleMaps(latlng);
//                }
//            });
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoomlevel));
//            zoomlevel = mMap.getCameraPosition().zoom;
//
//        }




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
            // https://stackoverflow.com/questions/46481789/android-locationservices-fusedlocationapi-deprecated
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
        mMap.clear();
        lvAdapter.nuke();
        currentLocation = new GeoLocation(latlng.latitude, latlng.longitude);
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
        final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latlng.latitude, latlng.longitude), progressSeekbar/1000.0);
        final HashMap<String,MarkerOptions> hashMapMarker = new HashMap<>();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, GeoLocation location) {
                Log.i(TAG, String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));

                final MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(location.latitude, location.longitude));
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                hashMapMarker.put(key,markerOptions);
                ref = database.getReference().child("items").child(key);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i(TAG, "Getting title for key " + key);
                        // create an item object to read each item's contents
                        Item item = dataSnapshot.getValue(Item.class);

                        // if the item has the substring from the search edittext,
                        // add it to the list of user's items
                        // In the scenario we try to find an item using an item location where the item
                        // has already been deleted
                        if(item == null) {
                            return;
                        }
                        if (!item.getOwnerID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            if (item.getName().toLowerCase().contains(tfLocation.getText().toString().toLowerCase())) {
                                // add to listview
                                lvAdapter.putItem(item);
                                String title = item.getName();
                                hashMapMarkerTitle.put(key, title);
                                hashMapMarker.get(key).title(title);
                                mMap.addMarker(hashMapMarker.get(key));
                                lvAdapter.notifyDataSetChanged();
                            }

                        }



//                        lvAdapter.putItem(item);
//                        String title =  item.getName();
//                        hashMapMarkerTitle.put(key, title);
//                        hashMapMarker.get(key).title(title);
//                        mMap.addMarker(hashMapMarker.get(key));
//                        lvAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Item " + key + "not found.");
                    }
                });
            }
            @Override
            public void onKeyExited(String key) {
                Log.i(TAG, String.format("Key %s is no longer in the search area", key));

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.i(TAG, String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));

            }

            @Override
            public void onGeoQueryReady() {
                Log.i(TAG, "All initial data has been loaded and events have been fired!");
                geoQuery.setCenter(currentLocation);
                geoQuery.setRadius(progressSeekbar/1000.0);

                if(geoQuery != null){
                    geoQuery.removeAllListeners();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e(TAG, "There was an error with this query: " + error);
            }

        });

    }

    public void setLocaleArea(Double lat, Double lng){
        String key = "https://maps.googleapis.com/maps/api/geocode/json?latlng=";
        String latitude = Double.toString(lat);
        String longitude = Double.toString(lng);
        String api = "&key=" + getString(R.string.locale_key);
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
        LatLng dragPosition = marker.getPosition();
        double dragLat = dragPosition.latitude;
        double dragLong = dragPosition.longitude;
        setLocaleArea(dragLat, dragLong);
        final LatLng latlng = new LatLng(dragLat, dragLong);
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
                circleRange.setText(String.format("%.2f", progressSeekbar/1000.0));
                System.out.println(progressSeekbar);
                circle.setRadius(progressSeekbar);
                bSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setQueryinGoogleMaps(latlng);
                    }
                });
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