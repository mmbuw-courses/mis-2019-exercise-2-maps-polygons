package com.example.lenovoidea.mis_2019_exercise_2_maps_polygons;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private EditText inputEditText;
    private Button startPolygonButton;
    private SharedPreferences mapPreferences;

    private boolean polygonDrawMode = false;

    private List<LatLng> latLngs;

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

        startPolygonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                polygonDrawMode = !polygonDrawMode;

                if(polygonDrawMode){
                    startPolygonButton.setText("End polygon");
                    latLngs = new ArrayList<>();
                }else{
                    startPolygonButton.setText("Start polygon");
                    if(!latLngs.isEmpty())
                        renderPolygon();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        // Add a marker in Sydney and move the camera
        LatLng germany = new LatLng(50, 10);
        mMap.addMarker(new MarkerOptions().position(germany).title("Marker in Germany"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(germany));

        //removeSavedData();
        getSavedData();
    }

    @Override
    public void onMapLongClick(LatLng latLng){

        if(!polygonDrawMode){
            mMap.addMarker(new MarkerOptions().position(latLng).title(inputEditText.getText().toString()));

            SharedPreferences.Editor editor = mapPreferences.edit();

            editor.putString(latLng.longitude + ":" + latLng.latitude, inputEditText.getText().toString());

            editor.apply();
        }
        else{
            mMap.addMarker(new MarkerOptions().position(latLng));
            latLngs.add(latLng);
        }
    }

    public void getSavedData(){
        Map<String, ?> allMapData = mapPreferences.getAll();

        for (Map.Entry<String, ?> mapData : allMapData.entrySet()) {
            double lon = Double.parseDouble(mapData.getKey().split(":")[0]);
            double lat = Double.parseDouble(mapData.getKey().split(":")[1]);

            LatLng latLng = new LatLng(lat, lon);

            mMap.addMarker(new MarkerOptions().position(latLng).title(mapData.getValue().toString()));
        }
    }

    private void removeSavedData(){
        SharedPreferences.Editor editor = mapPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void renderPolygon(){
        PolygonOptions rectOptions = new PolygonOptions().addAll(latLngs);

        Polygon polygon = mMap.addPolygon(rectOptions);
        polygon.setFillColor(0x5500FF00);
        polygon.setStrokeColor(Color.BLUE);
        polygon.setStrokeWidth(3.0f);
    }
}
