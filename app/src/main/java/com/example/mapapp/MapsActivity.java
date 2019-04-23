package com.example.mapapp;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final String TAG = MapsActivity.class.getSimpleName();
    /*https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial*/
    private CameraPosition mCameraPosition;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private Location mLastKnownLocation;
    private static final int DEFAULT_ZOOM = 15;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);

    //edittext and shared prefrences
    EditText messageView;
    SharedPreferences sharedPreferences;

    //polygon
    LinkedList<String[]> markerList;
    LinkedList<LatLng> markerLatLngList;
    Polygon polygon;
    Marker areaMarker;

    //https://developers.google.com/maps/documentation/android/start.
    //https://developers.google.com/maps/documentation/android/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mFusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final Button startEndPolygonButton= findViewById(R.id.start_end_polygon);
        startEndPolygonButton.setTag(1);
        startEndPolygonButton.setText("Start Polygon");
        startEndPolygonButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                final int status =(Integer) v.getTag();
                if(status == 1) {
                    createPolygon();
                    startEndPolygonButton.setText("End Polygon");
                    v.setTag(0); //pause
                } else {
                    endPolygon();
                    startEndPolygonButton.setText("Start Polygon");
                    v.setTag(1); //pause
                }
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "onMapReady called" );
        mMap = googleMap;

        getLocationPermission();
        // Add a marker in Sydney and move the camera
        getMyCurrentPosition();
        addStoredMarkers();

        /*LatLng sydney = new LatLng(0,0);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/


        /*Log.i(TAG, "outside log press listner :" + infomessage);*/

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                messageView=(EditText)findViewById(R.id.Message);
                final String infomessage=messageView.getText().toString();
                Log.i(TAG, "inside log press listner :" + infomessage);
                mMap.addMarker(new MarkerOptions().position(latLng).title(infomessage));
                PreferenceHelper preferenceHelper=PreferenceHelper.getInstance(getApplicationContext());
                LinkedHashSet valueSet=new LinkedHashSet();
                valueSet.add(String.valueOf(latLng.latitude));
                valueSet.add(String.valueOf(latLng.longitude));
                valueSet.add(infomessage);
                preferenceHelper.saveData(String.valueOf(latLng.latitude),String.valueOf(latLng.longitude),infomessage);


            }
        });


    }
    //https://github.com/googlemaps/android-samples/tree/master/ApiDemos
    private void getMyCurrentPosition(){
        try {
            Log.i(TAG, "inside getMyCurrentPosition");
            if (mLocationPermissionGranted) {
                @SuppressLint("MissingPermission") Task<Location> myLocation = mFusedLocationProviderClient.getLastLocation();
                myLocation.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            Log.i(TAG, "setting initial marker");
                            mLastKnownLocation = task.getResult();
                            mMap.addMarker(new MarkerOptions().position(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude())).title("Current Position"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
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
        catch (Exception e ){
            Log.d(TAG, "Sme exception");
            e.printStackTrace();
        }

    }
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
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
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
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
    //https://developers.google.com/maps/documentation/android/marker
    private void addStoredMarkers(){
        Log.i(TAG,"inside addStoredMarkers");
        PreferenceHelper preferenceHelper=PreferenceHelper.getInstance(getApplicationContext());
        markerList=preferenceHelper.getAllMarkers();
        markerLatLngList=new LinkedList();
        for (String[] marker : markerList){
            Log.i(TAG,"values "+marker);
           // Log.i(TAG,"lon : "+entry.getValue());

            double latitude=Double.parseDouble(marker[0]);
            double longitude=Double.parseDouble(marker[1]);
            markerLatLngList.add(new LatLng(latitude,longitude));
            String message=marker[2];
            Log.i(TAG,"latitude :" +latitude+" "+longitude+" " +message);
            mMap.addMarker(new MarkerOptions().position(
                    new LatLng(latitude,longitude)).title(message));
        }}

    //https://developers.google.com/maps/documentation/android/shapes
    public void createPolygon(){
        addStoredMarkers();
        PolygonOptions polygonOptions=new PolygonOptions().addAll(markerLatLngList).fillColor(0x7F000000);
        polygon=mMap.addPolygon(polygonOptions);
        double area=MapHelper.calculateArea(markerList);
        Log.i(TAG,"area : "+area);
        double[] latLng=MapHelper.calculateCentroidE(markerList);
        Log.i(TAG,"lat long : "+latLng[0]+"  "+latLng[1]);
        areaMarker=mMap.addMarker(new MarkerOptions().position(
                new LatLng(latLng[0],latLng[1])).title(String.valueOf(area)+" km square"));

    }

    public void endPolygon(){
        areaMarker.remove();
        polygon.remove();


    }



    @Override
    protected void onResume(){
        super.onResume();
        //addStoredMarkers();



    }

    //https://developer.android.com/training/basics/activitylifecycle/pausing.html
    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG,"onStop called");
        PreferenceHelper preferenceHelper=PreferenceHelper.getInstance(getApplicationContext());
        preferenceHelper.deleteData();

    }
}
