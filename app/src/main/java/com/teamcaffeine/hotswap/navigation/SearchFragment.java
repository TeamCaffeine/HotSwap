package com.teamcaffeine.hotswap.navigation;


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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.maps.Items;
import com.teamcaffeine.hotswap.maps.LocationPrefs;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMarkerDragListener{

    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Marker currentLocationMarker;
    private Circle circle;
    public static final int REQUEST_LOCATION_CODE = 99;
    private ListView lvItems; //Reference to the listview GUI component
    private ListAdapter lvAdapter; // //Reference to the Adapter used to populate the listview.
    private TextView localeMsg;
    //private Button locale;
    private Button bSearch;
    TextView locale, filters;
    SeekBar progress;
    CircleOptions circleOptions;
    Marker stopMarker;
    private DatabaseReference database;
    private GeoFire geoFire;
    private GeoQuery geoQuery;
    private Map<String,Marker> markers;
    private Set<GeoQuery> geoQueries = new HashSet<>();




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
        bSearch = (Button)view.findViewById(R.id.bSearch);
        lvItems = (ListView)view.findViewById(R.id.itemLists);
        lvAdapter = new Items(getActivity());
        lvItems.setAdapter(lvAdapter);
        lvItems.setVisibility(View.INVISIBLE);

        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String value = (String)adapterView.getItemAtPosition(i);
            }
        });

        bSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show results
                EditText tfLocation = (EditText)view.findViewById(R.id.tfLocation);
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
      //  locale = (Button)view.findViewById(R.id.setLocaleButton);
        locale = (TextView)view.findViewById(R.id.setLocaleFilters);
        SpannableString content = new SpannableString(locale.getText());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        locale.setText(content);

        filters = (TextView)view.findViewById(R.id.setItemFilters);
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
        localeMsg = (TextView)view.findViewById(R.id.setLocaleMsg);
        Intent intent = getActivity().getIntent();
        String city = intent.getStringExtra("city");
        progress = (SeekBar)view.findViewById(R.id.circleFilter);



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


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){ // marshmellow
            checkLocationPermission();
        }
        SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


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

    public boolean checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            else{
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;
        }
        else{
            return true;
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

        // Set view on current location
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();




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

        if(currentLocationMarker != null){
            currentLocationMarker.remove();
        }

        // how the map zooms into current location
        // if preferences exist
        // TODO: have it handle if intent is null
        Intent intent = getActivity().getIntent();
        Bundle extras = intent.getExtras();
        if (extras.containsKey("zip")) { // if there is location prefs
            String postalcode = intent.getStringExtra("zip");
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
                            // change map camera view
                            LatLng latlng = new LatLng(lat, lng);

                            Marker stopMarker = mMap.addMarker(new MarkerOptions()
                                    .draggable(true)
                                    .position(latlng)
                                    .title("Current Location"));



                            CircleOptions circleOptions = new CircleOptions()
                                    .center(stopMarker.getPosition()).radius(500).strokeWidth(5.0f)
                                    .strokeColor(Color.parseColor("#00BFFF"))
                                    .fillColor(Color.argb(
                                            50, //This is your alpha.  Adjust this to make it more or less translucent
                                            Color.red(Color.BLUE), //Red component.
                                            Color.green(Color.BLUE),  //Green component.
                                            Color.blue(Color.BLUE)));  //Blue component.);
                            circle = mMap.addCircle(circleOptions);

                            progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                    // progress = progress*10;
                                    circle.setRadius(progress);
                                    float[] distance = new float[2];
                                    Location.distanceBetween(42.365014, -71.102660,
                                            circle.getCenter().latitude, circle.getCenter().longitude, distance);

                                    if( distance[0] > circle.getRadius()  ){
                                        Toast.makeText(getActivity(), "iVacuum X is Outside the circle", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getActivity(), "iVacuum X is Inside the circle", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onStartTrackingTouch(final SeekBar seekBar) {
                                }

                                @Override
                                public void onStopTrackingTouch(final SeekBar seekBar) {
                                }
                            });

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15.5f));

                        }
                    });
                }
            });
        }

        // if preferences do not exist
        else {
            LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
            Marker stopMarker = mMap.addMarker(new MarkerOptions()
                    .draggable(true)
                    .position(latlng)
                    .title("Current Location"));
            circleOptions = new CircleOptions()
                    .center(stopMarker.getPosition()).radius(500).strokeWidth(5.0f)
                    .strokeColor(Color.parseColor("#00BFFF"))
                    .fillColor(Color.argb(
                            50, //This is your alpha.  Adjust this to make it more or less translucent
                            Color.red(Color.BLUE), //Red component.
                            Color.green(Color.BLUE),  //Green component.
                            Color.blue(Color.BLUE)));  //Blue component.);
            circle = mMap.addCircle(circleOptions);

            progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // progress = progress*10;
                    circle.setRadius(progress);
                    float[] distance = new float[2];
                    Location.distanceBetween(42.365014, -71.102660,
                            circle.getCenter().latitude, circle.getCenter().longitude, distance);

                    if( distance[0] > circle.getRadius()  ){
                        Toast.makeText(getActivity(), "iVacuum X is Outside the circle", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "iVacuum X is Inside the circle", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onStartTrackingTouch(final SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(final SeekBar seekBar) {
                }
            });
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15.5f));
        }

        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());


        // Set up Firebase with Geofire and respective user
        database = FirebaseDatabase.getInstance().getReference();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ItemIdHere");
        geoFire = new GeoFire(database.child("geofire"));

        /*
        // setup GeoFire
        this.geoFire = new GeoFire(new Firebase(GEO_FIRE_REF));
        // radius in km
        this.geoQuery = this.geoFire.queryAtLocation(INITIAL_CENTER, 1);

        // setup markers
        this.markers = new HashMap<String, Marker>();
        */



        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(latlng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));


        if(client != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }


//        stopMarker = mMap.addMarker(new MarkerOptions()
//                .draggable(true)
//                .position(new LatLng(location.getLatitude(), location.getLongitude()))
//                .title("Draggable Marker"));
//
//        circleOptions
//                .center(stopMarker.getPosition()).radius(500).strokeWidth(5.0f)
//                .strokeColor(Color.parseColor("#00BFFF"))
//                .fillColor(Color.argb(
//                        50, //This is your alpha.  Adjust this to make it more or less translucent
//                        Color.red(Color.BLUE), //Red component.
//                        Color.green(Color.BLUE),  //Green component.
//                        Color.blue(Color.BLUE)));  //Blue component.);
//        circle = mMap.addCircle(circleOptions);

        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // progress = progress*10;
                circle.setRadius(progress);
                float[] distance = new float[2];
                Location.distanceBetween(42.365014, -71.102660,
                        circle.getCenter().latitude, circle.getCenter().longitude, distance);

                if( distance[0] > circle.getRadius()  ){
                    Toast.makeText(getActivity(), "iVacuum X is Outside the circle", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "iVacuum X is Inside the circle", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
            }
        });



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

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng dragPosition = marker.getPosition();
        double dragLat = dragPosition.latitude;
        double dragLong = dragPosition.longitude;


        mMap.clear();
        LatLng vacuumX = new LatLng(42.365014,-71.102660); // hmart coords
        MarkerOptions options = new MarkerOptions();
        options.position(vacuumX);
        options.title("VacuumX");
        mMap.addMarker(options);

        Marker stopMarker = mMap.addMarker(new MarkerOptions()
                .draggable(true)
                .position(dragPosition)
                .title("Draggable Marker"));


        CircleOptions circleOptions = new CircleOptions().center(dragPosition).radius(500).strokeWidth(5.0f)
                .strokeColor(Color.parseColor("#00BFFF"))
                .fillColor(Color.argb(
                        50, //This is your alpha.  Adjust this to make it more or less translucent
                        Color.red(Color.BLUE), //Red component.
                        Color.green(Color.BLUE),  //Green component.
                        Color.blue(Color.BLUE)));  //Blue component.);
        circle = mMap.addCircle(circleOptions);

        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //  progress = progress*10;
                circle.setRadius(progress);
                float[] distance = new float[2];
                Location.distanceBetween(42.365014, -71.102660,
                        circle.getCenter().latitude, circle.getCenter().longitude, distance);

                if( distance[0] > circle.getRadius()  ){
                    Toast.makeText(getActivity(), "iVacuum X is Outside the circle", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "iVacuum X is Inside the circle", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
            }
        });
    }


    public interface SearchFragmentListener {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        SFL = (SearchFragment.SearchFragmentListener) context;
    }

}




