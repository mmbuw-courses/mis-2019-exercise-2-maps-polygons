package com.example.exer02;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.SphericalUtil;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    EditText userText;
    Boolean polygonActive = false;
    ArrayList<Marker> markers = new ArrayList<>();
    SharedPreferences markerPreferences;
    Button polyButton;

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

    public void calculateArea(){
        //https://stackoverflow.com/questions/28838287/calculate-the-area-of-a-polygon-drawn-on-google-maps-in-an-android-application
        //http://googlemaps.github.io/android-maps-utils/javadoc/
        //Area calculation
        List<LatLng> latLngs = new ArrayList<>();
        for(int i=0;i< markerPreferences.getInt("listSize",0);i++){
           latLngs.add(new LatLng(markers.get(i).getPosition().latitude,markers.get(i).getPosition().longitude));
        }
        //https://stackoverflow.com/questions/1583940/how-do-i-get-the-first-n-characters-of-a-string-without-checking-the-size-or-goi
        double area = SphericalUtil.computeArea(latLngs);
        String formatArea = Double.toString(area);
        formatArea = formatArea.substring(0, Math.min(formatArea.length(), 4));


        //Calculating center
        //https://stackoverflow.com/questions/18440823/how-do-i-calculate-the-center-of-a-polygon-in-google-maps-android-api-v2

        double centLat = 0.0;
        double centLong = 0.0;

        for (int i = 0; i < markerPreferences.getInt("listSize",0); i++) {
            centLat += markers.get(i).getPosition().latitude;
            centLong += markers.get(i).getPosition().longitude;
        }

        int totalPoints = markerPreferences.getInt("listSize",0);
        centLat = centLat / totalPoints;
        centLong = centLong / totalPoints;

        LatLng latLng = new LatLng(centLat,centLong);

        //https://stackoverflow.com/questions/22255231/using-superscript-in-android
        mMap.addMarker(new MarkerOptions().position(latLng).title("Roughly: "+formatArea+" "+Html.fromHtml("m<sup>2</sup>")));
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
        polyButton = findViewById(R.id.polygon);
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
                }
                else Toast.makeText(MapsActivity.this, "Please enter details at top", Toast.LENGTH_SHORT).show();
            }
        });

        polyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!polygonActive && markerPreferences.getInt("listSize",0)!= 0 ){
                    //deleteData();
                    loadData();
                    PolygonOptions polygonOptions = new PolygonOptions();
                    int size = markerPreferences.getInt("listSize",0);
                    for (int i=0;i<size;i++){
                        polygonOptions.add(new LatLng(markers.get(i).getPosition().latitude,markers.get(i).getPosition().longitude));
                    }
                    //https://stackoverflow.com/questions/14326482/android-maps-v2-polygon-transparency
                    polygonOptions.strokeColor(Color.argb(125,255,0,0)).fillColor(Color.argb(125,0,255,0));
                    mMap.addPolygon(polygonOptions);
                    calculateArea();
                    polyButton.setText("Stop Polygon");
                    polygonActive = true;
                }else{
                    loadData();
                    polygonActive = false;
                    polyButton.setText("Start Polygon");
                }
                if(markerPreferences.getInt("listSize",0)== 0){
                    Toast.makeText(MapsActivity.this, "Please add custom pins!", Toast.LENGTH_SHORT).show();
                }
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