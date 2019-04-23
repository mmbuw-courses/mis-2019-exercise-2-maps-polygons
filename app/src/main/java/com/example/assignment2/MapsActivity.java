package com.example.assignment2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;



public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener
{

    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0000;
    private GoogleMap mMap;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected String latitude, longitude;
    protected boolean gps_enabled, network_enabled;
    private boolean mLocationPermissionGranted = false;
    private SharedPreferences sharedPref;



    // Source: https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial
    // Get the permission for the location
    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    // Source: https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial
    // Gets Permission Result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get content view
        setContentView(R.layout.activity_maps);


        // Build the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Asks for permission
        getLocationPermission();


        // Source: https://javapapers.com/android/get-current-location-in-android/
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (mLocationPermissionGranted == true)
        {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 1000, this);
        }

        // Source: https://developer.android.com/training/data-storage/shared-preferences#java
        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);


    }


    // Source: https://javapapers.com/android/get-current-location-in-android/
    @Override
    public void onLocationChanged(Location location) {
        LatLng newMarker = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(newMarker).title("Current Position"));
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                //Add a marker where you pressed long
                EditText newMarker = findViewById(R.id.EnterText);
                String newMarkerText = newMarker.getText().toString();
                mMap.addMarker(new MarkerOptions().position(latLng).title(newMarkerText));

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("Marker Name", newMarkerText);
                editor.putString("Position", latLng.toString());
                editor.apply();

            }
        });

    }
}

