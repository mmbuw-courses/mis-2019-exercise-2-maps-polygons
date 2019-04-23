package com.awesometek.mis2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private FusedLocationProviderClient fusedLocationClient;
    private int PERMISSIONS_ACCESS_FINE_LOCATION = 0;
    private Boolean locationPermissionGranted = false;

    private EditText messageText;
    private SharedPreferences.Editor sharedPrefEditor;
    private int markerCount = 0;

    private Button polygonButton;
    private Boolean polygonMode = false;
    private Set<Polygon> polygons = new HashSet<>();
    private Polygon currPolygon;
    private PolygonOptions currPolygonOpts;
    private Set<Marker> currPolygonMarkers = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        messageText = findViewById(R.id.messageText);

        // shared preferences handling adapted from
        // https://developer.android.com/training/data-storage/shared-preferences#java
        SharedPreferences sharedPref = getApplication()
                .getSharedPreferences("markers", Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();

        polygonButton = findViewById(R.id.polygonButton);
        polygonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                polygonMode = !polygonMode;
                if(polygonMode) {
                    currPolygonOpts = new PolygonOptions()
                            .fillColor(getResources().getColor(R.color.polygonFill))
                            .strokeColor(getResources().getColor(R.color.polygonFill));
                    polygonButton.setText("END POLYGON");
                    polygonButton.setBackgroundColor(
                            getResources().getColor(R.color.polygonButton));
                } else {
                    finishCurrPolygon();
                    polygonButton.setText("START POLYGON");
                    polygonButton.setBackgroundColor(
                            getResources().getColor(R.color.colorPrimaryDark));
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(mMap == null) {
            mMap = googleMap;
        }

        requestLocationPermission();
        setMapToCurrentLocation();

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng pos) {
                if(!polygonMode) {
                    String boxTitle = messageText.getText().toString();
                    mMap.addMarker(new MarkerOptions().position(pos).title(boxTitle));
                    Set<String> prefData = new HashSet();
                    prefData.add(boxTitle);
                    prefData.add(String.valueOf(pos.latitude));
                    prefData.add(String.valueOf(pos.longitude));
                    sharedPrefEditor.putStringSet(String.valueOf(markerCount), prefData);
                    markerCount += 1;
                } else {
                    currPolygonMarkers.add(mMap.addMarker(new MarkerOptions().position(pos).icon(
                            BitmapDescriptorFactory.defaultMarker(168))));
                    addVertexToCurrPolygon(pos);
                }
            }
        });
    }

    // location permission adapted from
    // https://github.com/googlemaps/android-samples/blob/master/tutorials/
    // CurrentPlaceDetailsOnMap/app/src/main/java/com/example/
    // currentplacedetailsonmap/MapsActivityCurrentPlace.java
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        locationPermissionGranted = false;
        if(requestCode == PERMISSIONS_ACCESS_FINE_LOCATION && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        }
        setMapToCurrentLocation();
    }

    // adapted from https://developer.android.com/training/location/retrieve-current.html#java
    @SuppressLint("MissingPermission")
    public void setMapToCurrentLocation() {
        if(mMap == null) {
            return;
        }
        if(locationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng currLoc = new LatLng(location.getLatitude(),
                                        location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currLoc, 15));
                            }
                        }
                    });
        } else {
            requestLocationPermission();
        }
    }

    public void addVertexToCurrPolygon(LatLng pos) {
        if(!polygonMode || currPolygonOpts == null) {
            return;
        }
        if(currPolygon != null) {
            currPolygon.remove();
        }
        currPolygonOpts.add(pos);
        currPolygon = mMap.addPolygon(currPolygonOpts);
    }

    public void finishCurrPolygon() {
        if(currPolygon == null || currPolygonOpts == null) {
            return;
        }
        for(Marker marker : currPolygonMarkers) {
            marker.remove();
        }
        currPolygonMarkers.clear();

        List<LatLng> polygonPoints = currPolygonOpts.getPoints();

        // centroid calculation adapted from https://stackoverflow.com/a/18444984
        double[] centroid = {0.0, 0.0};
        for(LatLng markerPos : polygonPoints) {
            centroid[0] += markerPos.latitude;
            centroid[1] += markerPos.longitude;
        }
        centroid[0] /= polygonPoints.size();
        centroid[1] /= polygonPoints.size();
        LatLng pos = new LatLng(centroid[0], centroid[1]);

        // magic conversion number from https://en.wikipedia.org/wiki/Decimal_degrees
        // may be very rough; alternatively SphericalUtil#computeArea could be used
        double[] meterLats = new double[polygonPoints.size()];
        double[] meterLngs = new double[polygonPoints.size()];
        for(int i = 0; i < polygonPoints.size(); ++i) {
            meterLats[i] = polygonPoints.get(i).latitude * 111320;
            meterLngs[i] = polygonPoints.get(i).longitude * 111320;
        }

        // area computation adapted from https://www.mathopenref.com/coordpolygonarea2.html
        double area = 0.0;
        int j = polygonPoints.size() - 1;

        for (int i = 0; i < polygonPoints.size(); ++i) {
            area += (meterLngs[j] + meterLngs[i]) *
                    (meterLats[j] - meterLats[i]);
            j = i;
        }
        area = Math.abs(area / 2);

        String areaText = "";
        if(area < 10000) {
            areaText = String.valueOf((float) area) + " m²";
        } else {
            areaText = String.valueOf((float) area / 1000000) + " km²";
        }
        mMap.addMarker(new MarkerOptions().position(pos)
                .icon(BitmapDescriptorFactory.defaultMarker(168))
                .title(areaText));
        polygons.add(currPolygon);
        currPolygon = null;
    }
}
