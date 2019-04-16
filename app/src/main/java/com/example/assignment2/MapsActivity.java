package com.example.assignment2;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
{

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;


    // geographical Location where the device is currently located
    private Location mLastKnownLocation;

    // Keys for storing activity state
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private final LatLng mDefaultLocation;
    private static final int DEFAULT_ZOOM = 15;

    //warum???
    public MapsActivity(LatLng mDefaultLocation) {
        this.mDefaultLocation = mDefaultLocation;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //source: https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Get content view
        setContentView(R.layout.activity_maps);


        // Build the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    //source: https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial
    private void getDeviceLocation (@NonNull Task<Location> task)
    {
        if (task.isSuccessful())
        {
            // Set the map's camera position to the current location of the device.
            mLastKnownLocation = task.getResult();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

            // Set a marker at current position
            mMap.addMarker(new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(),
                    mLastKnownLocation.getLongitude())).title("Marker in your current position"));
        }
        else
        {
            Log.d(TAG, "Current location is null. Using defaults.");
            Log.e(TAG, "Exception: %s", task.getException());
            mMap.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap)
        {
        mMap = googleMap;



        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(mLastKnownLocation).title("Marker in your current position"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(mCameraPosition));
        }
}

