package com.example.mis_2019_exercise_2_maps_polygons;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.location.Location;

import android.os.Bundle;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.annotation.NonNull;

import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

import java.util.List;


//https://github.com/googlemaps/android-samples/blob/master/tutorials/CurrentPlaceDetailsOnMap/app/src/main/java/com/example/currentplacedetailsonmap/MapsActivityCurrentPlace.java
public class MapsActivity<fm> extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener{

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Sydney, Australia), when location permission is not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 20;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // last kown location of the device provided by Fused Location Provider
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // name for saved marker preferences
    private static final String SHARED_PREFS = "marker";

    private List<String> markerList;

    private EditText uInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        markerList = new ArrayList<String>();

        // retrieve content view that renders the map
        setContentView(R.layout.activity_maps);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    // manipulate map when it's available
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // start long click listener
        mMap.setOnMapLongClickListener(this);

        // ask for permission to use location data
        checkLocationPermission();

        loadMarkers();
    }

    public void checkLocationPermission()
    {
        if(ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        else {
            mLocationPermissionGranted = true;
            // Immediately get the location of the device
            getDeviceLocation();
        }
    }


    private void getDeviceLocation() {
        // Get the best and most recent location of the device, which may be null in rare
        // cases when a location is not available.
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        }
                        else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        }
        catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    // on long click, create a marker with the entered text and coordinates of the click
    @Override
    public void onMapLongClick(LatLng point){
        uInput  = (EditText) findViewById(R.id.editTitle);
        String markTitle = uInput.getText().toString();

        uInput = (EditText) findViewById(R.id.editMessage);
        String markMessage = uInput.getText().toString();

        markerList.add(String.valueOf(point.latitude));
        markerList.add(String.valueOf(point.longitude));
        markerList.add(markTitle);
        markerList.add(markMessage);

        // add marker to map
        mMap.addMarker(new MarkerOptions()
                .position(point)
                .title(markTitle)
                .snippet(markMessage));

        saveMarker();
    }

    public void saveMarker(){
        // join list into one string, Set seems to be not ideal
        String tmp = "";

        for(String s : markerList){
            tmp += s + ",";
        }

        // store string in preferences
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("marker", tmp);
        editor.apply();
    }

    public void loadMarkers() {
        // access stored string in preferences
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String markerString = sharedPreferences.getString("marker", "");

        // get long marker string and plit it at ,
        String[] markerParts = markerString.split(",");

        // fill marker list with stored marker values
        for(int i = 0; i < markerParts.length; i++){
            markerList.add(markerParts[i]);
        }

        // create new markers from the list, 1 marker is made up by 4 values
        for (int i = 0; i <= markerList.size() - 4; i += 4) {
            try {
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(Double.valueOf(markerList.get(i)),
                                                    Double.valueOf(markerList.get(i + 1))))
                            .title(markerList.get(i + 2))
                            .snippet(markerList.get(i + 3)));
            }

            catch (NumberFormatException e) {
            }
        }
    }
}

