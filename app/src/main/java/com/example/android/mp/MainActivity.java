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
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean mLocationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private GoogleMap mMap;

    protected LocationManager locationManager;
    public static final String MyPREFERENCES = "MapLocations";
    SharedPreferences sharedpreferences;
    private boolean polygonStarted = false;
    private ArrayList<LatLng> polyList = new ArrayList<LatLng>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //https://stackoverflow.com/a/27531283/6118088
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MainActivity.this);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        //https://javapapers.com/android/get-current-location-in-android/
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        getLocationPermission();
        if(mLocationPermissionGranted) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 250, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Toast.makeText(getApplicationContext(), "Map ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;

        //https://www.programcreek.com/java-api-examples/index.php?api=com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng position) {

                SharedPreferences.Editor editor = sharedpreferences.edit();

                EditText nameInput = findViewById(R.id.TextInput);
                String nameText = nameInput.getText().toString();

                mMap.addMarker(new MarkerOptions().position(position).title(nameText));

                //https://www.tutorialspoint.com/android/android_shared_preferences.htm
                editor.putString("Name", nameText);
                editor.putString("Location", position.toString());
                editor.apply();

                if(polygonStarted){
                    polyList.add(position);
                }
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {

        //https://stackoverflow.com/q/37682414/6118088
        LatLng position = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(position).title("My current location"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
    }
    public void onProviderDisabled(String provider) {}
    public void onProviderEnabled(String provider) {}
    public void onStatusChanged(String provider, int status, Bundle extras) {}


    public void onPolygonButtonClick (View view){
        Button polygonButton = findViewById(R.id.button);

        polygonStarted = !polygonStarted;
        if (polygonStarted) {
            polygonButton.setText(R.string.end_polygon);
        }
        else {
            polygonButton.setText(R.string.start_polygon);

            if(polyList.size() >= 3){
                double area = 0;

                //https://developers.google.com/maps/documentation/android-sdk/shapes
                PolygonOptions rectOptions = new PolygonOptions().fillColor(Color.argb(50, 0, 0, 0));


                double latSum = .0;
                double longSum = .0;

                //https://stackoverflow.com/a/18444984/6118088
                for(LatLng point : polyList){
                    rectOptions.add(point);
                    latSum += point.latitude;
                    longSum += point.longitude;
                }

                latSum = latSum / polyList.size();
                longSum = longSum / polyList.size();

                mMap.addPolygon(rectOptions);

                //https://developers.google.com/maps/documentation/android-sdk/utility/
                area = SphericalUtil.computeArea(polyList);

                LatLng centroidPosition = new LatLng(latSum, longSum);
                String areaText = area < 1000000 ?  Math.floor(area * 100) / 100 + " m²" : Math.floor((area/1000000) * 100) / 100 + " km²";
                mMap.addMarker(new MarkerOptions().position(centroidPosition).title(areaText));
            }

            polyList.clear();
        }
    }

    private void getLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
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


