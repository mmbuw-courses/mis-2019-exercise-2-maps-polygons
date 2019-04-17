package com.example.lenovoidea.mis_2019_exercise_2_maps_polygons;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private EditText inputEditText;
    private Button startPolygonButton;
    private SharedPreferences mapPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        inputEditText = (EditText) findViewById(R.id.inputEditText);
        startPolygonButton = (Button) findViewById(R.id.startPolygonButton);

        mapPreferences = this.getPreferences(Context.MODE_PRIVATE);
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
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        // Add a marker in Sydney and move the camera
        LatLng germany = new LatLng(50, 10);
        mMap.addMarker(new MarkerOptions().position(germany).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(germany));

        getSavedData();
    }

    @Override
    public void onMapLongClick(LatLng latLng){
        //Log.d("info", "onMapLongClick: latitude" + latLng.latitude+ " long " + latLng.longitude);
        //Toast.makeText(MapsActivity.this, "onMapLongClick:\n" + latLng.latitude + " : " + latLng.longitude, Toast.LENGTH_LONG).show();

        mMap.addMarker(new MarkerOptions().position(latLng).title(inputEditText.getText().toString()));

        SharedPreferences.Editor editor = mapPreferences.edit();

        editor.putString(latLng.longitude + ":" + latLng.latitude, inputEditText.getText().toString());

        editor.apply();
    }

    public void getSavedData(){
        Map<String, ?> allMapData = mapPreferences.getAll();

        for (Map.Entry<String, ?> mapData : allMapData.entrySet()) {
            //Log.d("map values", mapData.getKey() + ": " + mapData.getValue().toString());
            double lon = Double.parseDouble(mapData.getKey().split(":")[0]);
            double lat = Double.parseDouble(mapData.getKey().split(":")[1]);

            LatLng latLng = new LatLng(lat, lon);

            mMap.addMarker(new MarkerOptions().position(latLng).title(mapData.getValue().toString()));
        }
    }
}
