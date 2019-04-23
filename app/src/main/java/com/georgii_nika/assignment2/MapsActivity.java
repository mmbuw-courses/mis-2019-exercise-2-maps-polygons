package com.georgii_nika.assignment2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap myMap;
    private Marker myPositionMarker;
    private boolean myLocationPermissionGranted;

    private boolean isPolygonMode;

    private final int ACCESS_MAP = 100;

    private Button polygonButton;
    private EditText editText;

    private MyPolygon currentPolygon;
    private List<MyPolygon> polygons;

    private List<Marker> markerList;
    private SharedPreferences sharedPref;

    //SharedPreferences.Editor edit = sharedPref.edit();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        isPolygonMode = false;
        polygons = new ArrayList<>();
        sharedPref = getPreferences(Context.MODE_PRIVATE);

        polygonButton = findViewById(R.id.polygonButton);
        editText = findViewById(R.id.editText);

        polygonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPolygonButtonClick();
            }
        });
    }

    private void setPolygonMode(boolean isPolygonMode) {
        this.isPolygonMode = isPolygonMode;
        polygonButton.setText(this.isPolygonMode ? R.string.polygon_end : R.string.polygon_start);
    }

    private void onPolygonButtonClick() {
        if (!isPolygonMode) {
            setPolygonMode(true);
            currentPolygon = new MyPolygon(myMap);
        } else {
            try {
                currentPolygon.closePolygon();
                polygons.add(currentPolygon);
                currentPolygon = null;
                setPolygonMode(false);
            } catch (MyPolygon.NotEnoughPointsException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                currentPolygon = null;
                setPolygonMode(false);
            } catch (MyPolygon.PolygonIsSelfIntersectingException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                currentPolygon.cleanup();
                currentPolygon = null;
                setPolygonMode(false);
            }
        }
    }

    private void setupLocationTracking() throws SecurityException {
        if (!isLocationGranted()) {
            return;
        }

        myMap.setMyLocationEnabled(true);

        myMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                if (myPositionMarker != null) {
                    myPositionMarker.remove();
                }

                myPositionMarker = myMap.addMarker(new MarkerOptions().position(latLng).title("It's Me!"));
            }
        });
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            myLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_MAP);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        myLocationPermissionGranted = false;
        switch (requestCode) {
            case ACCESS_MAP: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    myLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;

        getLocationPermission();
        setupLocationTracking();
        setupCamera();
        markerList = loadMarkers(myMap, sharedPref);

        myMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (isPolygonMode) {
                    currentPolygon.addMarker(latLng);
                }
            }
        });


        myMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                MapsActivity.this.onMapLongClick(latLng);
            }
        });
    }

    private void onMapLongClick(LatLng latLng) {
        String text = editText.getText().toString();

        if (text.length() == 0) {
            Toast.makeText(getApplicationContext(), "Name your marker, my dear", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPolygonMode) {
            Marker marker = myMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(text)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            saveMarker(marker);
            editText.setText("");
        }
    }

    private void saveMarker(Marker marker) {
        SharedPreferences.Editor editor = sharedPref.edit();
        markerList.add(marker);

        int i = markerList.size() - 1;

        int size = markerList.size();
        float lat = (float) marker.getPosition().latitude;
        float lng = (float) marker.getPosition().longitude;
        String title = marker.getTitle();

        editor.putInt("listSize", size);
        editor.putFloat("lat" + i, lat);
        editor.putFloat("long" + i, lng);
        editor.putString("title" + i, title);

        editor.commit();
    }

    public List<Marker> loadMarkers(GoogleMap googleMap, SharedPreferences pref) {

        List<Marker> markers = new ArrayList<>();

        int size = pref.getInt("listSize", 0);
        for (int i = 0; i < size; i++) {
            double lat = (double) pref.getFloat("lat" + i, 0);
            double lng = (double) pref.getFloat("long" + i, 0);

            String title = pref.getString("title" + i, "NULL");
            MarkerOptions options = new MarkerOptions()
                    .position(new LatLng(lat, lng))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title(title);
            Marker marker = googleMap.addMarker(options);
            markers.add(marker);
        }

        return markers;
    }


    private void setupCamera() {
        if (!isLocationGranted()) {
            return;
        }

        Location location = getLastKnownLocation();

        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            myMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
        }
    }


    // https://stackoverflow.com/questions/20438627/getlastknownlocation-returns-null
    private Location getLastKnownLocation() throws SecurityException {
        LocationManager mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    private boolean isLocationGranted() {
        boolean isFineLocationForbidden = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        boolean isCoarseLocationForbidden = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;

        return !(isFineLocationForbidden && isCoarseLocationForbidden);
    }

}
