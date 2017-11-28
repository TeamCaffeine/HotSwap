package com.teamcaffeine.hotswap.activity.navigation;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
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
import com.teamcaffeine.hotswap.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentLocationMarker;
    private Circle circle;
    public static final int REQUEST_LOCATION_CODE = 99;
    private ListView lvItems; //Reference to the listview GUI component
    private ListAdapter lvAdapter; // //Reference to the Adapter used to populate the listview.
    private TextView localeMsg;
    private Button locale;

    SearchFragment.SearchFragmentListener SFL;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        return view;


    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lvItems = (ListView)view.findViewById(R.id.itemLists);
//        lvAdapter = new MyCustomAdapter(this.getBaseContext());
        locale = (Button)view.findViewById(R.id.setLocaleButton);
        localeMsg = (TextView)view.findViewById(R.id.setLocaleMsg);
        Intent intent = getActivity().getIntent();
        String city = intent.getStringExtra("city");
        if (city==null) {
            localeMsg.setText("");
        }
        else {
            localeMsg.setText("Items near " + city);
        }
        locale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i2 = new Intent(getActivity(), LocationPrefs.class);
                startActivity(i2);
            }

        });
        lvItems.setAdapter(lvAdapter);
        lvItems.setVisibility(View.INVISIBLE);

        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String value = (String)adapterView.getItemAtPosition(i);

//                i.putExtra("rank", rank);
//                i.putExtra("country", country);
//                i.putExtra("population", population);
//                i.putExtra("position", position);
                //startActivity(new Intent(MapsActivity.this, ItemData.class));
                //Toast.makeText(MapsActivity.this, "Item with id ["+l+"] - Position ["+i+"]", Toast.LENGTH_SHORT).show();
            }
        });


//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){ // marshmellow
//            checkLocationPermission();
//        }
        SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) { // should automatically be at current location
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
//        mMap.setOnMarkerDragListener(this);

        // Set view on current location
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        // how the map zooms into current location
        // if preferences exist
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15.5f));
        MarkerOptions options = new MarkerOptions();
        LatLng vacuumX = new LatLng(42.365014, -71.102660); // hmart coords
        options.position(vacuumX);
        options.title("VacuumX");
        mMap.addMarker(options);

    }

    protected synchronized void buildGoogleApiClient(){
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
            LocationServices.FusedLocationApi.requestLocationUpdates(client,locationRequest,this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;

        if(currentLocationMarker != null){
            currentLocationMarker.remove();
        }
        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(latlng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

//        currentLocationMarker = mMap.addMarker(markerOptions);
//      Sets startup map to current location, probably where you want to change map zoom based on prefs
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
//        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));

        if(client != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }
        Marker stopMarker = mMap.addMarker(new MarkerOptions()
                .draggable(true)
                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                .title("Draggable Marker"));


        CircleOptions circleOptions = new CircleOptions()
                .center(stopMarker.getPosition()).radius(500).strokeWidth(5.0f)
                .strokeColor(Color.parseColor("#00BFFF"))
                .fillColor(Color.argb(
                        50, //This is your alpha.  Adjust this to make it more or less translucent
                        Color.red(Color.BLUE), //Red component.
                        Color.green(Color.BLUE),  //Green component.
                        Color.blue(Color.BLUE)));  //Blue component.);
        circle = mMap.addCircle(circleOptions);


//        SeekBar progress = (SeekBar)findViewById(R.id.circleFilter);
//        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                // progress = progress*10;
//                circle.setRadius(progress);
//                float[] distance = new float[2];
//                Location.distanceBetween(42.365014, -71.102660,
//                        circle.getCenter().latitude, circle.getCenter().longitude, distance);
//
//                if( distance[0] > circle.getRadius()  ){
//                    Toast.makeText(getBaseContext(), "iVacuum X is Outside the circle", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getBaseContext(), "iVacuum X is Inside the circle", Toast.LENGTH_SHORT).show();
//                }
//            }

//            @Override
//            public void onStartTrackingTouch(final SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(final SeekBar seekBar) {
//            }

        }






    public interface SearchFragmentListener {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        SFL = (SearchFragment.SearchFragmentListener) context;
    }

}


