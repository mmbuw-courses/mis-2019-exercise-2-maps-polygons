package com.example.ex_2;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ex_2.R;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Double.parseDouble;
import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;
import static java.lang.Math.toRadians;
import static java.lang.StrictMath.abs;

public class MapsActivity extends FragmentActivity implements OnMapLongClickListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private boolean mlocationPermissioned;
    private static final int LOCATION_REQUEST_ACCESS = 1;
    EditText info;
    int count_marker = 0;

    // Sydney as Default Location, in case of null location
    //https://developers.google.com/maps/documentation/android-sdk/map-with-marker
    LatLng finalLatLng = new LatLng(-34, 151);


    private FusedLocationProviderClient mFusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // https://developer.android.com/reference/android/widget/Button
        final Button button = findViewById(R.id.buttonPoly);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                // https://stackoverflow.com/questions/22089411/how-to-get-all-keys-of-sharedpreferences-programmatically-in-android?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
                SharedPreferences prefs = getSharedPreferences("prefs", 0);
                Map<String, ?> prefsMap = prefs.getAll();

                double latitude = 0.0;
                double longitude = 0.0;
                ArrayList<LatLng> latlngs = new ArrayList<>();

                // go over all markers
                for (int i = 0; i < count_marker; i++) {

                    // https://stackoverflow.com/questions/16311076/how-to-dynamically-add-polylines-from-an-arraylist?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
                    // https://stackoverflow.com/questions/7283338/getting-an-element-from-a-set?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa

                    // https://developer.android.com/reference/android/content/SharedPreferences
                    // https://www.javatpoint.com/substring
                    Set<String> values = (Set<String>) prefs.getStringSet(String.valueOf(i + 1), null);
                    Iterator<String> it =
                            values.iterator();

                    String value = it.next();
                    if (value.startsWith("lat"))
                        latitude =
                                parseDouble(value.substring(3));
                    else if (value.startsWith("lng"))
                        longitude = parseDouble(value.substring(3));

                    value = it.next();
                    if (value.startsWith("lat"))
                        latitude =
                                parseDouble(value.substring(3));
                    else if (value.startsWith("lng"))
                        longitude =
                                parseDouble(value.substring(3));

                    value = it.next();
                    if (value.startsWith("lat"))
                        latitude =
                                parseDouble(value.substring(3));
                    else if (value.startsWith("lng"))
                        longitude =
                                parseDouble(value.substring(3));

                    latlngs.add(new LatLng(latitude, longitude));

                }


                PolygonOptions polygonOptions =
                        new PolygonOptions();
                polygonOptions.addAll(latlngs);
                polygonOptions.strokeColor(Color.RED);

                // https://gist.github.com/lopspower/03fb1cc0ac9f32ef38f4
                polygonOptions.fillColor(Color.parseColor("#66000000"));
                Polygon polygon =
                        mMap.addPolygon(polygonOptions);


                double area = getArea(polygon);
                LatLng centroid = getCentroid(polygon);


                Marker marker =
                        mMap.addMarker(new MarkerOptions().position(centroid).title(String.valueOf(area)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centroid, 18));
                marker.showInfoWindow();    // show area


                //https://stackoverflow.com/questions/3687315/deleting-shared-preferences
                prefs.edit().clear().apply();


                button.setText("Polygon End");

            }
        });
    }

    /**
     * https://developers.google.com/maps/documentation/android-api/location?authuser=2
     * https://github.com/googlemaps/android-samples
     */
    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mlocationPermissioned = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_ACCESS);
        }
    }

     //https://en.wikipedia.org/wiki/Polygon

    private double getArea(Polygon poly_gon) {

        double area = 0.0;

        List<LatLng> points = poly_gon.getPoints();
        int numPoints = points.size();
        double temp = 0;
        int i = 0;
        int j = 0;

        for (i = 0; i < numPoints; i++) {


            if (i == numPoints - 1)
                j = 0;
            else j = i + 1;

            double lat1 = points.get(i).latitude;
            double lat2 = points.get(j).latitude;
            double lng1 = points.get(i).longitude;
            double lng2 = points.get(j).longitude;
            temp = lat1 * lng2 - lat2 * lng1;
            area += temp;
        }

        area = area * 0.5;

        return Math.abs(area);
    }

    /**
     *
     * https://en.wikipedia.org/wiki/Centroid
     */
    private LatLng getCentroid(Polygon polygon) {
        double lat = 0.0;
        double lng = 0.0;

        int i = 0;
        int j = 0;

        for (i = 0; i < polygon.getPoints().size(); i++) {

            if (i == polygon.getPoints().size() - 1)
                j = 0;

            else j = i + 1;

            double xi = polygon.getPoints().get(i).latitude;
            double yi1 = polygon.getPoints().get(j).longitude;

            double xi1 = polygon.getPoints().get(j).longitude;
            double yi = polygon.getPoints().get(i).latitude;

            lat = lat + ((xi + xi1) * (xi * yi1 - xi1 * yi));
            lng = lng + ((yi + yi1) * (xi * yi1 - xi1 * yi));
        }

        lat /= (6 * getArea(polygon));
        lng /= (6 * getArea(polygon));

        LatLng latlng = new LatLng(lat, lng);

        return latlng;
    }


    /**
     * https://github.com/googlemaps/android-samples/tree/master/tutorials/CurrentPlaceDetailsOnMap
     * https://github.com/googlemaps/android-samples
     * https://developers.google.com/maps/documentation/android-sdk/map-with-marker
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getLocationPermission();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mlocationPermissioned) {

                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

                Task<Location> location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {

                        if (task.isSuccessful()) {
                            Location current = task.getResult();
                            finalLatLng = new LatLng(current.getLatitude(), current.getLongitude());
                        } else {
                            Toast.makeText(getApplicationContext(), "Current Location not Available", Toast.LENGTH_SHORT).show();
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(finalLatLng, 18));
                    }
                });
                mMap.setOnMapLongClickListener(this);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }







     //https://developers.google.com/android/reference/com/google/android/gms/maps/GoogleMap.OnMapLongClickListener

    @Override
    public void onMapLongClick(LatLng point) {
        info = findViewById(R.id.editText);
        String infostring = info.getText().toString();
        mMap.addMarker(new MarkerOptions().position(point).title(infostring));


        // https://developer.android.com/training/data-storage/shared-preferences
        // https://androidforums.com/threads/help-with-putstringset-and-getstringset-method.616410/

        Set<String> values = new HashSet<String>();
        values.add("lat" + String.valueOf(point.latitude));
        values.add("lng" + String.valueOf(point.longitude));
        values.add("info" + infostring);
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        count_marker += 1;
        editor.putStringSet(String.valueOf(count_marker), values);
        Toast.makeText(getApplicationContext(), point.toString(), Toast.LENGTH_LONG).show();
        editor.commit();
    }


}