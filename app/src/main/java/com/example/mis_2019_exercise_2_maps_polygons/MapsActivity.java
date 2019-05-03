package com.example.mis_2019_exercise_2_maps_polygons;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.SphericalUtil;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private EditText editText;
    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Polygon polygon;
    Marker marker;
    Integer intMark;
    Button buttonPoly;
    static public final int REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initiate();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Weimar, Germany.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        currentLocation();
        ArrayList<MarkerDetail> markerDetailArray = getMarkers();

        //Showing previously saved markers on the Map
        if (markerDetailArray != null) {
            for (MarkerDetail marker : markerDetailArray) {
                showMarker(marker, mMap, BitmapDescriptorFactory.HUE_AZURE);
            }
        }

        //Listening to LongClick that will add a new marker to the map
        //Source: "https://developers.google.com/maps/documentation/android-sdk/marker"
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {
                //Get Message attributed to marker
                String message = editText.getText().toString();
                if (message.isEmpty()) {
                    editText.setText("");
                } else {
                    showMarker(new MarkerDetail( message,
                                    String.valueOf(latLng.latitude),
                                    String.valueOf(latLng.longitude)),
                            mMap,
                            BitmapDescriptorFactory.HUE_VIOLET);
                    editText.setText("");
                    saveMarkers(message,latLng);
                }
            }
        });
    }

    //When polygon button is clicked
    public void processPolygon(View view) {

        buttonPoly = findViewById(R.id.buttonPoly);

        if (buttonPoly.getText().equals("Start Polygon")) {
            Boolean successfullExecution = createPolygon();
            if (successfullExecution) {
                buttonPoly.setText("End Polygon");
            }
        }
        else {
            polygon.remove();
            marker.remove();
            buttonPoly.setText("Start Polygon");
        }
    }

    //Creating polygon from the >= 3 markers after button's clicked
    public Boolean createPolygon() {

        ArrayList<LatLng> markerLocations = new ArrayList<>();
        ArrayList<MarkerDetail> markerDetailArray = getMarkers();

        //Creating a polygon from the markers on the map
        if (markerDetailArray != null) {
            for (MarkerDetail marker : markerDetailArray) {
                Log.d("Marker: " , marker.message);
                markerLocations.add(new LatLng( Double.valueOf(marker.latitude),
                        Double.valueOf(marker.longitude)));
            }
        }

        if (markerLocations.size() >= 3) {

            LatLng[] latLngArray = new LatLng[markerLocations.size()];
            latLngArray = markerLocations.toArray(latLngArray);
            PolygonOptions vertices = new PolygonOptions().add(latLngArray).strokeWidth(7).strokeColor(Color.argb(225,225,225,225)).fillColor(Color.argb(137,137,137,137));
            polygon =  mMap.addPolygon(vertices);
            Double polygonArea = calculatePolygonArea(markerLocations);
            String units;

            if (polygonArea > 1000) {
                polygonArea = polygonArea/1000;
                units = String.format("%.2f",polygonArea)  + "km\u00B2" ;
            }
            else{
                units = String.format("%.2f",polygonArea)  + "m\u00B2" ;
            }

            LatLng centroid = calculateCentroid(markerLocations);
            marker = showMarker( new MarkerDetail("Area of Polygon is: " + units, String.valueOf(centroid.latitude), String.valueOf(centroid.longitude)), mMap, BitmapDescriptorFactory.HUE_ROSE);
            return true;
        }
        else {
            showToastMessage("Requires at least 3 markers to create a polygon!");
            return false;
        }
    }

    //Calculate polygon area using Google's Sphere Util
    //Source: "http://googlemaps.github.io/android-maps-utils/javadoc/com/google/maps/android/SphericalUtil.html#computeArea-java.util.List-"
    public double calculatePolygonArea(List<LatLng> markerLocations) {
        return SphericalUtil.computeArea(markerLocations);
    }

    //Calculate the centroid of the polygon
    //Source: "https://gis.stackexchange.com/questions/77425/how-to-calculate-centroid-of-a-polygon-defined-by-a-list-of-longitude-latitude-p"
    public LatLng calculateCentroid(List<LatLng> markerLocations) {

        LatLng centroid;
        Double latitude = 0.0,longitude = 0.0;

        for (LatLng marker : markerLocations) {
            latitude += marker.latitude;
            longitude += marker.longitude;
        }

        Integer locSize = markerLocations.size();
        return new LatLng(latitude/locSize, longitude/locSize);
    }

    //Find current location
    public void currentLocation() {
        LatLng latLng = getCurrentLocation();
        setLocation(latLng, mMap);
        showMarker( new MarkerDetail("Current Location" ,
                String.valueOf(latLng.latitude),
                String.valueOf(latLng.longitude)) , mMap, BitmapDescriptorFactory.HUE_MAGENTA);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    //When app is first opened, initiation
    private void initiate() {
        editText = findViewById(R.id.editText);
        String locationName = getString(R.string.Location_Preferences); //In strings.xml
        intMark = 0;
        sharedPreferences = MapsActivity.this.getSharedPreferences(locationName, Context.MODE_PRIVATE);
        editor = this.sharedPreferences.edit();
        checkLocationPermission();
    }

    //Check location permission after initiation
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Check permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
    }

    //Get current location
    //Source: "https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial"
    private LatLng getCurrentLocation() {

        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return new LatLng(50.9804,11.3316);
        }
        else {
            LocationProvider high = mLocationManager.getProvider(mLocationManager.getBestProvider(createFineCriteria(),true));
            Location currentLocation = mLocationManager.getLastKnownLocation(high.getName());

            if( currentLocation != null){
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                return new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            }
            else {
                return new LatLng(50.9804,11.3316);
            }
        }
    }

    public static Criteria createFineCriteria() {
        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_FINE);
        c.setAltitudeRequired(false);
        c.setBearingRequired(false);
        c.setSpeedRequired(false);
        c.setCostAllowed(true);
        c.setPowerRequirement(Criteria.POWER_HIGH);
        return c;
    }

    //Show current location to user
    private void setLocation(LatLng latLng, GoogleMap gMap) {
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
        gMap.animateCamera(CameraUpdateFactory.zoomIn());
        gMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f),2000,null);
    }

    //Show markers on the map
    private Marker showMarker( MarkerDetail marker, GoogleMap gMap, Float hue ){
        LatLng location = new LatLng(Double.parseDouble(marker.latitude), Double.parseDouble(marker.longitude));
        Marker mapMarker;
        mapMarker = mMap.addMarker(new MarkerOptions() //new MarkerOptions()
                .position(location)
                .title(marker.message)
                .snippet(marker.message)
                //.flat(true)
                .icon(BitmapDescriptorFactory.defaultMarker(hue)));
        return mapMarker;
    }

    //Displaying Toast
    public void showToastMessage(String message){
        Toast toast = Toast.makeText(MapsActivity.this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0 ,0);
        toast.show();
    }

    //Save marker to shared preferences
    //Source:
    private void saveMarkers(String message, LatLng latLng) {

        String latitude = String.valueOf(latLng.latitude);
        String longitude = String.valueOf(latLng.longitude);
        Set<String> markerDetailSet = new HashSet<>();

        markerDetailSet.add("Message:" + message);
        markerDetailSet.add("Latitude:" + latitude);
        markerDetailSet.add("Longitude:" + longitude);

        Map<String, ?> existingMarker = sharedPreferences.getAll();
        Integer oldMarkersListSize;
        if( existingMarker != null ){
            oldMarkersListSize = existingMarker.size();
            intMark = oldMarkersListSize;
        }
        editor.putStringSet(String.valueOf(intMark),markerDetailSet);
        intMark++;
        editor.apply();
    }

    //Get markers from shared preferences
    //Source:
    private ArrayList<MarkerDetail> getMarkers() {
        Map<String, ?> existingMarker = sharedPreferences.getAll();
        Iterator it = existingMarker.entrySet().iterator();
        MarkerDetail marker;
        ArrayList<MarkerDetail> markerDetailArray = new ArrayList<>();
        Map.Entry key;
        Set<String> markerStringSet;
        Iterator<String> markerSetIt;
        for (int count = 0; count < existingMarker.size(); count++) {
            markerStringSet = sharedPreferences.getStringSet(String.valueOf(count), null);
            if (!markerStringSet.equals(null)) {
                markerSetIt = markerStringSet.iterator();
                String message = "", latitude = "", longitude = "";
                marker = new MarkerDetail();
                while (markerSetIt.hasNext()) {

                    String markerDetail = markerSetIt.next();
                    String[] detailsArray = markerDetail.split(":", 2);

                    if (detailsArray[0].equals("Message")) {
                        marker.message = detailsArray[1];
                    } else if (detailsArray[0].equals("Latitude")) {
                        marker.latitude = detailsArray[1];
                    } else if (detailsArray[0].equals("Longitude")) {
                        marker.longitude = detailsArray[1];
                    }
                }
                markerDetailArray.add(marker);
            }
        }
        return markerDetailArray;
    }

    //https://developer.android.com/reference/android/view/View.OnLongClickListener
    @Override
    public void onMapLongClick(LatLng latLng) {

    }

}

class MarkerDetail{

    String message;
    String latitude;
    String longitude;
    public MarkerDetail(){}
    public MarkerDetail( String message, String latitude, String longitude){
        this.message = message;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
