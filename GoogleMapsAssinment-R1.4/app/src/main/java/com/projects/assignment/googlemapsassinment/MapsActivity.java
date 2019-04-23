package com.projects.assignment.googlemapsassinment;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.SphericalUtil;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.projects.assignment.googlemapsassinment.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, LocationServiceManager.LocationServiceManagerListener, View.OnClickListener {

    // Instance of Google Map
    private GoogleMap mMap;

    // Progress Loading Spin
    private ProgressBar progressBarLoading;

    // Top edit box
    private EditText editTextCustomMessage;

    // Bottom start/stop polygon button
    private Button buttonStartStopPolygon;

    // Coordinator Layout
    private CoordinatorLayout coordinatorLayout;

    // Custom message text
    private String customMessage = "";

    // Is polygon started
    private Boolean isPolygonStarted = false;

    // List of polygon points
    private List<LatLng> polygonPoints = new ArrayList<>();

    // The last location
    private LatLng lastLocation = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initializing the UI
        progressBarLoading = findViewById(R.id.progressBarLoading);
        editTextCustomMessage = findViewById(R.id.editTextCustomMessage);
        buttonStartStopPolygon = findViewById(R.id.buttonStartStopPolygon);
        buttonStartStopPolygon.setOnClickListener(this);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        // Checking the location permissions
        checkPermissions();

    }

    /**
     * Checking the location permissions and loading map if granted
     */
    private void checkPermissions() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);
                        mapFragment.getMapAsync(MapsActivity.this);

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {/* ... */}

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                }).check();
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

        // Set the Google map instance
        mMap = googleMap;

        // Start getting the current location
        new LocationServiceManager(MapsActivity.this).initLocationService();

        // Hide the loading spinner
        progressBarLoading.setVisibility(View.GONE);


    }


    /**
     * Create a marker
     * @param latLng    Lat/Lng of the marker
     * @param title     Title of the marker
     */
    private void createMarker(LatLng latLng, String title) {

        if (!isPolygonStarted)
            mMap.clear();


        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng)
                        .title(title));
        marker.showInfoWindow();
    }


    /**
     * Save the last selected location to SharedPreferences
     */
    private void saveLastLocation(){
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("lat", String.valueOf(lastLocation.latitude));
        inputMap.put("lng", String.valueOf(lastLocation.longitude));
        inputMap.put("msg", customMessage);

        SharedPreferences pSharedPref = getApplicationContext().getSharedPreferences("latlng", Context.MODE_PRIVATE);
        if (pSharedPref != null){
            JSONObject jsonObject = new JSONObject(inputMap);
            String jsonString = jsonObject.toString();
            SharedPreferences.Editor editor = pSharedPref.edit();
            editor.remove("app_map").commit();
            editor.putString("app_map", jsonString);
            editor.commit();
        }
    }

    /**
     * Load the last location from SharedPreferences
     */
    private void loadLastLocation(){
        Map<String,String> outputMap = new HashMap<String,String>();
        SharedPreferences pSharedPref = getApplicationContext().getSharedPreferences("latlng", Context.MODE_PRIVATE);
        try{
            if (pSharedPref != null){
                String jsonString = pSharedPref.getString("app_map", (new JSONObject()).toString());
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator<String> keysItr = jsonObject.keys();

                Double lat = 0.0;
                Double lng = 0.0;

                while(keysItr.hasNext()) {
                    String key = keysItr.next();
                    String value = (String) jsonObject.get(key);
                    outputMap.put(key, value);

                    if (key.equals("lat"))  lat = Double.parseDouble(value);

                    if (key.equals("lng")) lng = Double.parseDouble(value);

                    if (key.equals("msg")) customMessage = value;
                }

                lastLocation = new LatLng(lat,lng);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Callback from Location Manager with the current location
     * @param location  The current location
     */
    @Override
    public void processLocation(Location location) {
        Log.i("AppInfo", "Location changed to " + location);

        lastLocation = new LatLng(location.getLatitude(), location.getLongitude());

        saveLastLocation();

        if (lastLocation == null) {
            loadLastLocation();
        }

        createMarker(lastLocation, "Your Location");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 17));

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                customMessage = editTextCustomMessage.getText().toString();

                createMarker(latLng, customMessage);

                if (isPolygonStarted){
                    polygonPoints.add(latLng);
                }
            }
        });


    }

    /**
     * OnClick event for the button
     * @param view
     */
    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.buttonStartStopPolygon){
            if (isPolygonStarted == false){
                mMap.clear();
                polygonPoints.clear();
                isPolygonStarted = true;
                buttonStartStopPolygon.setText("End Polygon");

            }else{

                if (polygonPoints.size() >= 3) {
                    isPolygonStarted = false;
                    buttonStartStopPolygon.setText("Start Polygon");
                    drawAndCalculateArea();
                }else{
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "Cannot create polygon with less than 3 points", Snackbar.LENGTH_SHORT);

                    snackbar.show();
                }

            }
        }

    }

    /**
     * Draw and calculate the polygon with centroid
     */
    private void drawAndCalculateArea() {
        // Instantiates a new Polygon object and adds points to define a rectangle
        PolygonOptions rectOptions = new PolygonOptions();
        for (int i = 0; i < polygonPoints.size(); i++) {
            rectOptions.add(polygonPoints.get(i));
        }

        rectOptions.fillColor(getResources().getColor(R.color.trans_blue));

        // Get back the mutable Polygon
        Polygon polygon = mMap.addPolygon(rectOptions);

        double[] cent = centroid(polygonPoints);
        // Creating a marker
        MarkerOptions markerOptions = new MarkerOptions();

        // Setting the position for the marker
        markerOptions.position(new LatLng(cent[0], cent[1]));

        Double area = SphericalUtil.computeArea(polygonPoints);
        String unit = "m2";

        if (area > 10000){
            area = area/1000000;
            unit = "km2";
        }

        markerOptions.title(String.format("%.2f", area) + " " + unit);

        mMap.addMarker(markerOptions).showInfoWindow();
    }

    /**
     * Calculate the center of the polygon
     * @param points
     * @return
     */
    public double[] centroid(List<LatLng> points) {
        double[] centroid = { 0.0, 0.0 };

        for (int i = 0; i < points.size(); i++) {
            centroid[0] += points.get(i).latitude;
            centroid[1] += points.get(i).longitude;
        }

        int totalPoints = points.size();
        centroid[0] = centroid[0] / totalPoints;
        centroid[1] = centroid[1] / totalPoints;

        return centroid;
    }

    
}
