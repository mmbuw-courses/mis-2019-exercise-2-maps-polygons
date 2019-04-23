package com.example.mis_2019_exercise_2_maps_polygons;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.graphics.Color;
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
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.SphericalUtil;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

import java.util.List;


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

    // name for saved marker preferences
    private static final String SHARED_PREFS = "marker1";

    final int duration = Toast.LENGTH_LONG;

    private List<String> markerList;
    private List<LatLng> polygonLatLng;

    private Button polygonButton;
    private Button deleteMarker;
    private EditText eTitle;
    private EditText eMessage;

    // polygon start or end
    private boolean polygonActive = false;
    private Polygon polygon;
    private Toast toast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize lists for the markers
        markerList = new ArrayList<String>();
        polygonLatLng = new ArrayList<LatLng>();

        // retrieve content view that renders the map
        setContentView(R.layout.activity_maps);

        // initialize buttons and text fields
        polygonButton = (Button) findViewById(R.id.polygon_button);
        deleteMarker = (Button) findViewById(R.id.delete_marker_button);
        eTitle = (EditText) findViewById(R.id.editTitle);
        eMessage = (EditText) findViewById(R.id.editMessage);

        final Context context = getApplicationContext();

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        polygonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // change text in Polygon depending on current state
                String tmp = polygonButton.getText().toString();

                if(tmp.equals("Start Polygon")) {
                    polygonButton.setText("End Polygon");
                    polygonActive = true;
                }
                else{
                    polygonButton.setText("Start Polygon");
                    Double area = polygonArea();

                    LatLng latLng = polygonCentroid();

                    String distance = "Square meters: ";

                    if(area > 1000000){
                        area = area/1000000;
                        distance = "Square Kilometers: ";
                    }

                    mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(distance+Double.toString(area)));

                    // empty list again
                    polygonLatLng.clear();
                    polygonActive = false;
                }
            }
        });

        deleteMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.commit();
                polygonLatLng.clear();
                markerList.clear();
                toast.makeText(context, "All markers were deleted", duration).show();
            }
        });
    }


    // manipulate map when it's available
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // ask for permission to use location data
        checkLocationPermission();
        // if just permitted, get location again
        getDeviceLocation();

        // start long click listener
        mMap.setOnMapLongClickListener(this);

        // load saved markers
        loadMarkers();
    }


    //https://developer.android.com/training/permissions/requesting
    public void checkLocationPermission()
    {
        // if not granted ask for permission to use location data
        if(ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        // if permission granted, set it on true
        else {
            mLocationPermissionGranted = true;
        }
    }


    //https://github.com/googlemaps/android-samples/blob/master/tutorials/CurrentPlaceDetailsOnMap/app/src/main/java/com/example/currentplacedetailsonmap/MapsActivityCurrentPlace.java
    private void getDeviceLocation() {
        Context context = getApplicationContext();
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
            toast.makeText(context, e.getMessage(), duration).show();
            Log.e("Exception: %s", e.getMessage());
        }
    }


    // on long click, create a marker with the entered text and coordinates of the click
    @Override
    public void onMapLongClick(LatLng point){
        String markTitle = eTitle.getText().toString();
        String markMessage = eMessage.getText().toString();

        // polygon mode not active
        if(!polygonActive) {
            markerList.add(String.valueOf(point.latitude));
            markerList.add(String.valueOf(point.longitude));

            // fill empty and potentially deadly fields
            if(markTitle.equals("")){
                markTitle = "-";
            }

            if(markMessage.equals("")){
                markMessage = "-";
            }

            markerList.add(markTitle);
            markerList.add(markMessage);

            // add marker to map
            mMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title(markTitle)
                    .snippet(markMessage));

            // save polygons
            saveMarker();
        }
        // polygon mode active
        else{
            // add polygon coordinates to a seperate list
            polygonLatLng.add(point);

            // add marker to map
            mMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title(markTitle)
                    .snippet(markMessage));
        }
    }


    // for the saving of markers this resource was used:
    // https://developer.android.com/training/data-storage/shared-preferences
    public void saveMarker(){
        // join list into one string, "Set" seems to be not ideal
        String tmp = "";

        // turn data into long string
        for(String s : markerList){
            tmp += s + ",";
        }

        //adjust string to make it pretty
        tmp = tmp.substring(1, tmp.length()-1);

        // store string in preferences
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("marker1", tmp);
        editor.commit();
        //System.out.println("This has been stored in your preferences: "+sharedPreferences.getString("marker1", ""));
    }


    public void loadMarkers() {
        Context context = getApplicationContext();

        // access stored string in preferences
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String markerString = sharedPreferences.getString("marker1", "");

        //System.out.println("This has been loaded from your preferences: "+sharedPreferences.getString("marker1", ""));

        // get the long markerString and split it at ,
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
                toast.makeText(context, "Something went wrong with the markers", duration).show();
            }
        }
    }


    // calculate area of polygon http://googlemaps.github.io/android-maps-utils/
    // paint polygon on map https://developers.google.com/maps/documentation/android-sdk/shapes
    public double polygonArea(){
        Context context = getApplicationContext();

        if(polygonLatLng.size() > 2) {
            double area = 0.0;

            PolygonOptions polygonOptions = new PolygonOptions();

            // fill polygon
            for(LatLng latLng : polygonLatLng){
                polygonOptions.add(latLng);
            }

            polygonOptions.strokeColor(Color.RED);
            polygonOptions.fillColor(0x7F0000FF);

            polygon = mMap.addPolygon(polygonOptions);
            area = SphericalUtil.computeArea(polygon.getPoints());
            return area;
        }
        toast.makeText(context, "At least 2 polygon markers are required", duration).show();
        return 0.0;
    }


    // calculate centroid of polygon (set of 2D points) https://stackoverflow.com/questions/18591964/how-to-calculate-centroid-of-an-arraylist-of-points
    public LatLng polygonCentroid(){
        Context context = getApplicationContext();

        double cenX = 0.0;
        double cenY = 0.0;

        if(polygonLatLng.size() > 2) {
            for (int i = 0; i < polygonLatLng.size(); i++) {
                cenX += polygonLatLng.get(i).latitude;
                cenY += polygonLatLng.get(i).longitude;
            }
            cenX = cenX/polygonLatLng.size();
            cenY = cenY/polygonLatLng.size();

            return new LatLng(cenX, cenY);
        }
        else{
            toast.makeText(context, "At least 2 polygon markers are required", duration).show();
            return new LatLng(cenX, cenY);
        }
    }
}

