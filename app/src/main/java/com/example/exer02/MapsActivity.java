package com.example.exer02;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    EditText userText;
    Boolean firstPin = true;
    Boolean test = false;
    ArrayList<Marker> markers = new ArrayList<Marker>();
    SharedPreferences markerPreferences;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            }
        }
    }
    public void loadData(){
        mMap.clear();
        markers.clear();
        int size = markerPreferences.getInt("listSize",0);
        for(int i=0;i<size;i++){
            double lat = (double) markerPreferences.getFloat("lat"+i,0);
            double longit = (double) markerPreferences.getFloat("long"+i,0);
            String title = markerPreferences.getString("title"+i,"Null");
            markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(lat,longit)).title(title)));
        }
    }

    public void saveData(){
        //https://stackoverflow.com/questions/25438043/store-google-maps-markers-in-sharedpreferences
        SharedPreferences.Editor editor = markerPreferences.edit();
        editor.putInt("listSize", markers.size());
        for (int i = 0 ; i < markers.size(); i++){
            editor.putFloat("lat"+i, (float) markers.get(i).getPosition().latitude);
            editor.putFloat("long"+i, (float) markers.get(i).getPosition().longitude);
            editor.putString("title"+i, markers.get(i).getTitle());

            System.out.println("The title saved: " + markers.get(i).getTitle());
        }
        editor.apply();
    }
    public  void deleteData(){
        markerPreferences.edit().clear().apply();
        mMap.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        markerPreferences = this.getSharedPreferences("com.example.exer02", Context.MODE_PRIVATE);
        userText = findViewById(R.id.userInput);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        loadData();
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(!userText.getText().toString().equals("")){
                    String location = userText.getText().toString();
                    Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    markers.add(marker);
                    //deleteData();
                    saveData();
                    loadData();
                    /*
                    if(markers.size() == 5){ mMap.clear();
                    Toast.makeText(MapsActivity.this, "Restarting", Toast.LENGTH_SHORT).show();
                    test = true;}
                    if(test){
                    for (int i=0; i<3;i++){
                        mMap.addMarker(new MarkerOptions().position((markers.get(i)).getPosition()).title((markers.get(i)).getTitle()));
                    }}
                    System.out.println(markers);*/
                }
                else Toast.makeText(MapsActivity.this, "Please enter details at top", Toast.LENGTH_SHORT).show();
            }
        });

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        Location firstLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        LatLng firstLtLn = new LatLng(firstLocation.getLatitude(),firstLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLtLn, 10));
        mMap.addMarker(new MarkerOptions().position(firstLtLn).title("You are here"));

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }
}