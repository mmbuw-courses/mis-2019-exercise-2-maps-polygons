package com.example.misexercise_2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static android.text.TextUtils.split;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    private EditText textInput;
    int counter = 0;
    SharedPreferences.Editor editor;
    SharedPreferences pref;
    String marker;
    Boolean polygonExist = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        final Button button= (Button) findViewById(R.id.buttonPolygon);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        textInput = (EditText)findViewById(R.id.textInput);
        pref = getApplicationContext().getSharedPreferences("Markers", MODE_PRIVATE);
        editor = pref.edit();

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addPolygon();
                if(polygonExist){
                    button.setText(getString(R.string.button_end));
                }
                else{
                    button.setText(getString(R.string.button_start));
                }
            }
        });
    }

    //code for permission and centering from
    //https://stackoverflow.com/questions/21403496/how-to-get-current-location-in-google-map-android/21403526
    public void centreMapOnLocation(Location location, String title) {

        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        //mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centreMapOnLocation(lastKnownLocation, "Your Location");
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Clear possibly remaining markers from the last session
        editor.clear();
        editor.commit();

        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            centreMapOnLocation(lastKnownLocation,"Your Location");
        } else {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                mMap.addMarker(new MarkerOptions().position(point)
                .title(textInput.getText().toString()));

                //Since putStringSet is unordered, we store the latitude and longitude and description as one csv that can be separated later
                marker = Double.toString(point.latitude) + "," + Double.toString(point.longitude) + "," + textInput.getText().toString();
                editor.putString(Integer.toString(counter), marker);
                editor.apply();
                counter++;
            }
        });
    }

    public void addPolygon(){
        if(!polygonExist){
            double lat;
            double lon;
            double centroidX = 0, centroidY = 0;
            double areaComp = 0;
            String ending;
            List<LatLng> area = new ArrayList<>();
            Map<String, ?> keys = pref.getAll();
            Map<Integer, String> sortedMap = new TreeMap<Integer, String>();
            PolygonOptions rectOptions = new PolygonOptions();

            if(keys.size() < 3){
                Toast.makeText(this, "Please add at least 3 Markers to create a polygon.", Toast.LENGTH_SHORT).show();
                return;
            }

            //The map we get back is unordered, there is surely a smarter way to solve this, but passing it to a sorted list works...
            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                sortedMap.put(Integer.parseInt(entry.getKey()), entry.getValue().toString());
            }

            for(SortedMap.Entry<Integer, String> entry : sortedMap.entrySet()){
                System.out.println(entry);
                lat = Double.parseDouble(split(entry.getValue().toString(), ",")[0]);
                lon = Double.parseDouble(split(entry.getValue().toString(), ",")[1]);
                rectOptions.add(new LatLng(lat, lon));
                area.add(new LatLng(lat, lon));
                centroidX += lat;
                centroidY += lon;
            }

            //using a given function from the google API to calculate the area
            //http://googlemaps.github.io/android-maps-utils/javadoc/com/google/maps/android/SphericalUtil.html
            areaComp = SphericalUtil.computeArea(area);
            if(areaComp < 10000)
            {
                ending = " m2";
            }
            else if(areaComp >= 10000 && areaComp < 1000000)
            {
                ending = " ha";
                areaComp /= 10000;
            }
            else{
                ending = " km²";
                areaComp /= 1e+6;
            }

            String area_str = String.format("%.02f", areaComp);

            // Get back the mutable Polygon
            Polygon polygon = mMap.addPolygon(rectOptions.fillColor(0x556aa0f7).strokeWidth(1));
            mMap.addMarker(new MarkerOptions().position(new LatLng(centroidX/keys.size(), centroidY/keys.size()))
                    .title(area_str + ending));
            Toast.makeText(this, "The selected area has a size of " + area_str + ending, Toast.LENGTH_SHORT).show();
            polygonExist = true;
        }
        else{
            mMap.clear();
            //Clear the shared preferences, so that the previous markers are no longer considered for the polygon
            editor.clear();
            editor.commit();
            polygonExist = false;
            counter = 0;
        }
    }
}