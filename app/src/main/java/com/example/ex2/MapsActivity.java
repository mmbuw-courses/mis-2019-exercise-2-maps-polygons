package com.example.ex2;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import static android.app.PendingIntent.getActivity;
// For layout
//https://developer.android.com/guide/topics/ui/layout/linear

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    EditText txt;
    public String savPreferences;
    Button btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        txt = findViewById(R.id.txtMap);
        btnAdd=findViewById(R.id.btnAdd);
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
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;


        //https://www.latlong.net/place/weimar-thuringia-germany-11682.html
        //The above link is used for taking longitude and latitude of weimar
        LatLng weimar = new LatLng( 50.979492, 11.323544);
        mMap.addMarker(new MarkerOptions().position(weimar).title("Marker in weimar"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(weimar));
// to create a marker
        // https://stackoverflow.com/questions/42401131/add-marker-on-long-press-in-google-maps-api-v3
        // https://developers.google.com/maps/documentation/android-sdk/marker

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(txt.getText().toString()!=null){
                    mMap.addMarker(new MarkerOptions().position(latLng).title(txt.getText().toString()));
                }
                else{
                    mMap.addMarker(new MarkerOptions().position(latLng).title("You have typed no message"));
                }


            }
        });
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
