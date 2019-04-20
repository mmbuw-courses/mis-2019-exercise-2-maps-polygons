package com.example.android.mp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean mLocationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private GoogleMap mMap;
    protected LocationManager locationManager;
    public static final String MyPREFERENCES = "MapLocations";
    SharedPreferences sharedpreferences;
    private boolean polygonStarted = false;
    private ArrayList<LatLng> polygonList = new ArrayList<LatLng>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set-up map
        //https://stackoverflow.com/a/27531283/6118088
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MainActivity.this);

        //Set-up sharedpreferences
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        //Set-up location
        //https://javapapers.com/android/get-current-location-in-android/
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //Check/get location permission
        getLocationPermission();
        if(mLocationPermissionGranted) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 250, this);
            //Automatically open GPS settings if GPS deactivated
            //https://stackoverflow.com/a/16262971/6118088 & https://www.android-examples.com/check-gps-location-services-is-enabled-or-not-in-android/
            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        
        mMap = googleMap;

        //https://www.programcreek.com/java-api-examples/index.php?api=com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng position) {

                //Add marker with input text as title
                EditText nameInput = findViewById(R.id.TextInput);
                String nameText = nameInput.getText().toString();
                mMap.addMarker(new MarkerOptions().position(position).title(nameText));

                //Add  marker's position to temp polygon data if a polygon is currently recorded
                if(polygonStarted){
                    polygonList.add(position);
                }

                //Add marker to sharedpreferences
                SharedPreferences.Editor editor = sharedpreferences.edit();
                //https://www.tutorialspoint.com/android/android_shared_preferences.htm
                editor.putString("Name", nameText);
                editor.putString("Location", position.toString());
                editor.apply();
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {

        //Add marker with device's location
        //https://stackoverflow.com/q/37682414/6118088
        LatLng position = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(position).title("My current location"));
    }
    @Override
    public void onProviderDisabled(String provider) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    public void onPolygonButtonClick (View view){

        //Toggle button
        Button polygonButton = findViewById(R.id.button);
        polygonStarted = !polygonStarted;
        if (polygonStarted) {//Polygon recording started
            polygonButton.setText(R.string.end_polygon);
        }
        else {//Polygon recording stopped
            polygonButton.setText(R.string.start_polygon);

            //Polygon needs at least 3 vertices
            if(polygonList.size() >= 3){
                double area = .0, latitude = .0, longitude = .0;

                //Set-up polygon
                //https://developers.google.com/maps/documentation/android-sdk/shapes
                PolygonOptions rectOptions = new PolygonOptions().fillColor(Color.argb(50, 0, 0, 0));

                //Iterate trough temp polygon data to form polygon and calculate centroid
                //https://stackoverflow.com/a/18444984/6118088
                for(LatLng point : polygonList){
                    rectOptions.add(point);
                    latitude += point.latitude;
                    longitude += point.longitude;
                }
                latitude = latitude / polygonList.size();
                longitude = longitude / polygonList.size();

                //Calculate polygon's area
                //https://developers.google.com/maps/documentation/android-sdk/utility/
                area = SphericalUtil.computeArea(polygonList);

                //Add polygon and centroid marker
                LatLng centroidPosition = new LatLng(latitude, longitude);
                String areaText = area < 1000000 ?  Math.floor(area * 100) / 100 + " m²" : Math.floor((area/1000000) * 100) / 100 + " km²";
                mMap.addMarker(new MarkerOptions().position(centroidPosition).title(areaText));
                mMap.addPolygon(rectOptions);
            }

            //Remove temp data from previous polygon
            polygonList.clear();
        }
    }

    private void getLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted = true;
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

}


