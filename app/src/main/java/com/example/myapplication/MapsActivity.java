package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
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
import java.util.List;
import java.util.Locale;

public class MapsActivity extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener,GoogleMap.OnMarkerClickListener {

    private final String TAG = "MapsActivity";
    private GoogleMap mMap;
    LocationManager locationManager;
    View view;
    public TextInputLayout mainActivityTexInput;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "MAP onCreateView Ready");
        View rootView = inflater.inflate(R.layout.activity_maps, container, false);
        view = rootView;
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);



        return rootView;
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG,"MAP onActivityCreated Ready");


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG,"MapsActivity: ON MAP READY");
           LatLng sydney = new LatLng(-33.852, 151.211);googleMap.addMarker(new MarkerOptions().position(sydney)
               .title("Marker in Sydney"));


        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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
                        Log.d(TAG,"MapsActivity: LOCATION NETWORK PROVIDER");
                        double latitude=location.getLatitude();
                        double longitude=location.getLongitude();

                        LatLng latLng = new LatLng(latitude,longitude);

                        Geocoder geocoder = new Geocoder(getContext());
                        try {
                            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                            String str =addressList.get(0).getLocality()+" ";
                            str+=addressList.get(0).getCountryName();

                            mMap.addMarker(new MarkerOptions().position(latLng).title(str));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,10.2f));

                        }catch (IOException error){
                            error.printStackTrace();
                        }
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
                        Log.d(TAG,"MapsActivity: LOCATION GPS");
                        double latitude=location.getLatitude();
                        double longitude=location.getLongitude();

                        LatLng latLng = new LatLng(latitude,longitude);

                        Geocoder geocoder = new Geocoder(getContext());
                        try {
                            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                            String str =addressList.get(0).getLocality()+" ";
                            str+=addressList.get(0).getCountryName();

                            mMap.addMarker(new MarkerOptions().position(latLng).title(str));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,10.2f));

                        }catch (IOException error){
                            error.printStackTrace();
                        }
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
        MarkerOptions markerOptions=new MarkerOptions().position(latLng).title(latLng.toString());
        mMap.addMarker(markerOptions);

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
