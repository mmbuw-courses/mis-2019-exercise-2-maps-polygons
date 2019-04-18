package com.example.misexercise_2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.math.BigDecimal;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.SphericalUtil;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    DecimalFormat df = new DecimalFormat("#.##");
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        textInput = (EditText)findViewById(R.id.textInput);
        pref = getApplicationContext().getSharedPreferences("Markers", MODE_PRIVATE);
        editor = pref.edit();
        Button button= (Button) findViewById(R.id.buttonPolygon);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addPolygon();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        System.out.println("HERE");
        Intent intent = getIntent();
        editor.clear();
        editor.commit();

        System.out.println("HERE2");
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
        //centreMapOnLocation(location,"Your Location");
            @Override
            public void onLocationChanged(Location location) {
                //centreMapOnLocation(location,"Your Location");
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

                marker = Double.toString(point.latitude) + "," + Double.toString(point.longitude) + "," + textInput.getText().toString();
                editor.putString("marker" + Integer.toString(counter), marker);
                editor.apply();
                counter++;

                //Map<String,?> keys = pref.getAll();
                //System.out.println(keys.size());
                //for(Map.Entry<String,?> entry : keys.entrySet()){
                 //   Log.d("map values",entry.getKey() + ": " + entry.getValue().toString());
                //}

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
            PolygonOptions rectOptions = new PolygonOptions();

            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                lat = Double.parseDouble(split(entry.getValue().toString(), ",")[0]);
                lon = Double.parseDouble(split(entry.getValue().toString(), ",")[1]);
                rectOptions.add(new LatLng(lat, lon));
                area.add(new LatLng(lat, lon));
                centroidX += lat;
                centroidY += lon;
            }

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

            // Get back the mutable Polygon
            Polygon polygon = mMap.addPolygon(rectOptions.fillColor(0x556aa0f7).strokeWidth(1));
            mMap.addMarker(new MarkerOptions().position(new LatLng(centroidX/keys.size(), centroidY/keys.size()))
                    .title(Double.toString(round(areaComp, 2)) + ending));
            polygonExist = true;

        }
        else{
            mMap.clear();
            editor.clear();
            editor.commit();
            polygonExist = false;
        }
    }
    //round method from https://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}