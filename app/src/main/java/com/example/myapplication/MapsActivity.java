package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.Tag;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MapsActivity extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener,GoogleMap.OnMarkerClickListener {

    private final String TAG = "MapsActivity";
    private GoogleMap mMap;
    LocationManager locationManager;
    View view;
    public TextInputLayout mainActivityTexInput;
    Set<String> myMapList = new HashSet<String>();
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    private String sharedPrefKey="myAppGoogleMapsMIS";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "MAP onCreateView Ready");
        View rootView = inflater.inflate(R.layout.activity_maps, container, false);
        view = rootView;
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor= sharedPref.edit();
        myMapList=sharedPref.getStringSet(sharedPrefKey,new HashSet<String>());
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        addSavedMarkers();
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"MapsActivity: PERMITIONS GRANTED");
            if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
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
                });
            }else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
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
                });

            }
            }else {
                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            Log.d(TAG, "MapsActivity: PERMITIONS NOT GRANTED");
            Log.d(TAG, "MapsActivity: PERMITIONS  ACCESS FINE LOCATION :"+(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED));
            Log.d(TAG, "MapsActivity: PERMITIONS  ACCESS FINE LOCATION :"+(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED));
        }

    }

    //https://stackoverflow.com/questions/16097143/google-maps-android-api-v2-detect-long-click-on-map-and-add-marker-not-working
    @Override
    public void onMapLongClick(LatLng latLng) {
        addMarkers(latLng);

        Toast.makeText(getContext(),
                "New marker added@" + latLng.toString(), Toast.LENGTH_LONG)
                .show();
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addresses.isEmpty()) {
                        mainActivityTexInput.getEditText().setText("Waiting for Location");
                    }else {
                        if (addresses.size() > 0) {
                            mainActivityTexInput.getEditText().setText(addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());
                            //Toast.makeText(getApplicationContext(), "Address:- " + addresses.get(0).getFeatureName() + addresses.get(0).getAdminArea() + addresses.get(0).getLocality(), Toast.LENGTH_LONG).show();
                        }
                    }
                }catch (IOException error){
                    Log.d(TAG,error.toString());
                }
    }

    //https://stackoverflow.com/questions/27261670/convert-string-to-latlng
    public void addSavedMarkers() {
        Iterator<String> iterator=myMapList.iterator();
        while(iterator.hasNext()){
            String mymapValues=iterator.next();
            mymapValues=mymapValues.replace("lat/lng: (","");
            mymapValues=mymapValues.replace(")","");
            String[] latlong =  mymapValues.split(",");
            Log.d(TAG,"MARKERS INFO____:"+latlong[0]);
            Log.d(TAG,"MARKERS INFO____:"+latlong[1]);
            double latitude = Double.parseDouble(latlong[0]);
            double longitude = Double.parseDouble(latlong[1]);
            LatLng location = new LatLng(latitude, longitude);
            addMarkers(location );
        }

    }

    public void addMarkers(LatLng latLng){
        MarkerOptions markerOptions=new MarkerOptions().position(latLng).title(latLng.toString());
        mMap.addMarker(markerOptions);
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,10.2f));
        myMapList.add(latLng.toString());
        editor.clear();
        editor.putStringSet(sharedPrefKey,myMapList);
        editor.apply();
        Log.d(TAG,"Number of SETs:"+myMapList.size());
        Log.d(TAG,""+sharedPref.getStringSet(sharedPrefKey,new HashSet<String>()).size());
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(marker.getPosition().latitude,marker.getPosition().longitude, 1);
            if (addresses.isEmpty()) {
                mainActivityTexInput.getEditText().setText("Waiting for Location");
            }else {
                if (addresses.size() > 0) {
                    mainActivityTexInput.getEditText().setText(addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());
                    //Toast.makeText(getApplicationContext(), "Address:- " + addresses.get(0).getFeatureName() + addresses.get(0).getAdminArea() + addresses.get(0).getLocality(), Toast.LENGTH_LONG).show();
                }
            }
        }catch (IOException error){
            Log.d(TAG,error.toString());
        }
        return false;
    }
}
