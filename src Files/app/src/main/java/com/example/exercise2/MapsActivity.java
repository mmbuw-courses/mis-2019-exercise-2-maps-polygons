package com.example.exercise2;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    Button btnPolygons;
    Button btnSearch;
    PolygonOptions markerOps;
    private EditText inputSearch;
    private ImageView gps;
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    int POLYGON_POINTS = 999;
    String convArea;
    private static final String TAG = "MapsActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    double area;
    ArrayList<Marker> markers = new ArrayList<Marker>();
    ArrayList <LatLng> loc = new ArrayList<LatLng>();
    Polygon shape;
    public List<Address> list = new ArrayList<>();
    int count = 0;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    int size;


    @Override
    public void onMapReady(GoogleMap googleMap) {
        btnPolygons = (Button)findViewById(R.id.btnPolygons);
        btnPolygons.setEnabled(false);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        mMap = googleMap;

        if(mLocationPermissionsGranted){
            getDeviceLocation();
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();
        }

        // create marker with mouse long click
        if(mMap!=null){
            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    MapsActivity.this.setMarker( latLng.latitude, latLng.longitude);
                }
            });
        }


    }

    // Create markers. If there are more than 2 markers, the button to create polygon will be enabled.
    //https://developers.google.com/android/reference/com/google/android/gms/maps/model/MarkerOptions
    private void setMarker(double latitude, double longitude) {
        count++;
        MarkerOptions markerOps = new MarkerOptions()
                .title("Marker No."+count)
                .draggable(true)
                .position(new LatLng(latitude, longitude))
                .snippet("about marker no."+count);
        markers.add(mMap.addMarker(markerOps));
        loc.add(new LatLng(latitude, longitude));

        if(markers.size()> 2){
            btnPolygons.setEnabled(true);
        }
        else if(markers.size() < 3){
            btnPolygons.setEnabled(false);
        }

        btnPolygons.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                checkCondition();
            }
        });
    }

    private void checkCondition(){
        Log.d(TAG, "checkCondition entered, marker size:" + markers.size() + " Button name: "+btnPolygons.getText());
        if (btnPolygons.getText().toString().equals("CLEAR")){
            Log.d(TAG, "checkCondition: destroy");
            destroyPoly();
            markers.clear();
            btnPolygons.setText("Start Polygon");
        } else if (btnPolygons.getText().toString().equals("Start Polygon")) {
            Log.d(TAG, "checkCondition: build poly");
            drawPolygon();
            btnPolygons.setText("End Polygon");
        }
    }

    // Create Polygon based on user's markers
    //https://developers.google.com/android/reference/com/google/android/gms/maps/model/Polygon
    private void drawPolygon() {
        markerOps = new PolygonOptions()
                .fillColor(0x330000FF)
                .strokeWidth(3)
                .strokeColor(Color.RED);
        for(int i=0;i<markers.size();i++){
            markerOps.add(markers.get(i).getPosition());
        }
        shape = mMap.addPolygon(markerOps);

        //Computing the polygon's area using computeArea on SphericalUtil
        //https://stackoverflow.com/questions/33253070/cannot-resolve-sphericalutil
        //https://stackoverflow.com/questions/28838287/calculate-the-area-of-a-polygon-drawn-on-google-maps-in-an-android-application
        //https://developers.google.com/maps/documentation/javascript/reference/geometry#spherical.computeArea
        Log.d(TAG, "computedArea: " + SphericalUtil.computeArea(loc) + "m.sq.");

        area = SphericalUtil.computeArea(loc);
        if(area>1000000) {
            area = area / 1000000;
            convArea = "Area: " + area + "km.sq.";
        } else{
            convArea = "Area: " + area + "m.sq.";
        }
        //A marker showing the approximate area of the polygon
        MarkerOptions markerOps = new MarkerOptions()
                .title(convArea)
                .draggable(true)
                .position(getPolygonCenterPoint(loc))
                .snippet("(approx)");
        markers.add(mMap.addMarker(markerOps));

        Log.d(TAG, "loc: " + loc.toString());


    }

    // Getting polygons's center point to add an information marker (area)
    private LatLng getPolygonCenterPoint(ArrayList<LatLng> polygonPointsList){
        LatLng centerLatLng = null;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int i = 0 ; i < polygonPointsList.size() ; i++)
        {
            builder.include(polygonPointsList.get(i));
        }
        LatLngBounds bounds = builder.build();
        centerLatLng =  bounds.getCenter();
        return centerLatLng;
    }

    //https://developer.android.com/reference/android/content/SharedPreferences
    private JSONArray readSharedPref() throws JSONException {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String savedArray = sharedPreferences.getString("Locations", "[]");
        return new JSONArray(savedArray);
    }
    //Save data
    private void addValue(String value) throws JSONException{
        JSONArray jsonArray = this.readSharedPref();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("location", value);
        jsonArray.put(jsonObject);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Location", jsonArray.toString());
        editor.apply();
    }

//https://stackoverflow.com/questions/37719687/need-to-remove-last-polyline-and-create-current-polyline
    private void destroyPoly(){
        mMap.clear();
        markers.clear();
        loc.clear();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        inputSearch= (EditText)findViewById(R.id.input_search);
        gps=(ImageView)findViewById(R.id.ic_gps);
        getLocationPermission();
    }

    private void init(){
        //search for location input
        btnSearch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                geoLocate();
            }
        });

        //get device's current location
        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });
    }

    //https://developers.google.com/maps/documentation/javascript/geocoding
    private void geoLocate(){
        String searchString = inputSearch.getText().toString();
        Geocoder geocoder = new Geocoder(MapsActivity.this);

        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "Geocoder: IOException: "+ e.getMessage());
        }

        //move camera to match
        if (list.size()>0){
            Address address = list.get(0);
            moveCamera(
                    new LatLng(address.getLatitude(), address.getLongitude()),
                    DEFAULT_ZOOM,
                    address.getAddressLine(0));
        }
    }

    // Move the maps position to current location
    //https://developer.android.com/training/location/retrieve-current
    //https://developers.google.com/android/reference/com/google/android/gms/location/LocationServices.html#getFusedLocationProviderClient(android.app.Activity)
    private void getDeviceLocation(){
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionsGranted){
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "Found location!");
                            Location currentLocation = (Location)task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM,
                                    "My Location");

                        }else {
                            Log.d(TAG, "Current location is null");
                            Toast.makeText(MapsActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException" + e.getMessage());
        }
    }

    //moving / changing the maps position to certain location
    //https://developers.google.com/android/reference/com/google/android/gms/maps/CameraUpdateFactory
    private void moveCamera(LatLng latLng, float zoom, String title){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(!title.equals("My Location")){
            MarkerOptions markerOps = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(markerOps);
        }
    }
    //Adding Google Maps
    //https://developers.google.com/android/reference/com/google/android/gms/maps/SupportMapFragment
    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }
    //Adding permission
    //https://developer.android.com/reference/android/Manifest.permission
    private void getLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;

        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for (int i = 0;i < grantResults.length;i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: Permission FAILED!");
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    //initialize maps
                    initMap();
                }
            }
        }
    }

    private void SavePreferences(){
        Log.d(TAG, "SavePref: entered");
        sharedPreferences = getPreferences(MODE_PRIVATE);
        editor = sharedPreferences.edit();

        editor.putInt("listSize", markers.size());

        for(int i = 0; i <markers.size(); i++){
            editor.putFloat("lat"+i, (float) markers.get(i).getPosition().latitude);
            editor.putFloat("long"+i, (float) markers.get(i).getPosition().longitude);
            editor.putString("title"+i, markers.get(i).getTitle());
        }

        editor.commit();
        Log.d(TAG, "SavePref: saved");
        Log.d(TAG, "SharedPref: " + sharedPreferences.getAll());
    }

    //The implementation was left as values even though are not null when input into marker gives null pointer exception

//    private boolean LoadPreferences(){
//        sharedPreferences = getPreferences(MODE_PRIVATE);
//
//        size = sharedPreferences.getInt("listSize", 0);
//        Log.d(TAG, "LoadPref int: " +size);
//        Log.d(TAG, "LoadPref: " + sharedPreferences.getAll());
//        if(size>0) {
//            for (int i = 0; i < size; i++) {
//
//                Log.d(TAG, "LoadPref lat: " + sharedPreferences.getFloat("lat" + i, 0));
//                Log.d(TAG, "LoadPref long: " + sharedPreferences.getFloat("long" + i, 0));
//                Log.d(TAG, "LoadPref title: " + sharedPreferences.getString("title" + i, "NULL"));
//                //double lat = (double) sharedPreferences.getFloat("lat" + i, 0);
//                //double longit = (double) sharedPreferences.getFloat("long" + i, 0);
//                //String title = sharedPreferences.getString("title" + i, "NULL");
//                //markers.add(mMap.addMarker(new MarkerOptions()
//                //      .position(new LatLng(lat, longit))
//                //    .title(title)));
//
//                MarkerOptions markerOps = new MarkerOptions()
//                        .title(sharedPreferences.getString("title" + i, "NULL"))
//                        .position(new LatLng(sharedPreferences.getFloat("lat" + i, 0), sharedPreferences.getFloat("long" + i, 0)));
//                mMap.addMarker(markerOps);
//                //loc.add(new LatLng(lat, longit));
//            }
//            return true;
//        }
//        else
//            return false;
//    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if(LoadPreferences()) {
//            checkCondition();
//        }
//    }
//    @Override
//    protected void onStart() {
//        super.onStart();
//        if(LoadPreferences()) {
//            checkCondition();
//        }
//    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        SavePreferences();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        SavePreferences();
    }

    @Override
    public void onStop(){
        super.onStop();
        SavePreferences();
    }

}