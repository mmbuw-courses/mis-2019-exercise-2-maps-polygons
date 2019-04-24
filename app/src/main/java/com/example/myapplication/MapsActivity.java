package com.example.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.maps.android.SphericalUtil;

public class MapsActivity extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener,GoogleMap.OnMarkerClickListener {

    //TAG for log mostly for error handling
    private final String TAG = "MapsActivity";

    private GoogleMap mMap;
    LocationManager locationManager;
    View view;

    //TextView on main Activity
    public TextView mainActivityTexInput;

    //Sets to save the data of the markers and share pref object to save
    //Is really bad to have two different list for data, in the future think
    // in a string structure maybe with JSON or other data format structure
    Set<String> appLocationList = new HashSet<String>();
    Set<String> appMessageList = new HashSet<String>();
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    //List for lat and long coordinates and list for
    //specific messages area and address
    private String sharedPrefKeyLocation ="myAppGoogleMapsMISLocation";
    private String sharedPrefKeyMessages ="myAppGoogleMapsMISMessages";
    private boolean createPolygonFlag =false;

    //List to store and draw the information of the polygons
    List<List<LatLng>> listPolyList= new ArrayList<>();
    List<LatLng> polygonList = new ArrayList<>();

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
        appLocationList =sharedPref.getStringSet(sharedPrefKeyLocation,new HashSet<String>());
        appMessageList =sharedPref.getStringSet(sharedPrefKeyMessages,new HashSet<String>());
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Add the markers saved from the last session
        addSavedMarkers();

        //Prepare location manager for find current locations
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        //Add event listeners for the interaction with the map and the markers
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);

        //Use this source but only for the ACCESS FINE LOCATION was necessary
        //https://stackoverflow.com/questions/44370162/get-location-permissions-from-user-in-android-application
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //If there is no permissions ask for permissions
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }else {

            //If there is permissions find the current location
            //There is two type of location providers
            if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new LocationListener() {
                    @Override
                    //This method gives the current location fo the device
                    public void onLocationChanged(Location location) {
                        findCurrentLocation(new LatLng(location.getLatitude(),location.getLongitude()));
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
                },Looper.myLooper());
            }else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,new LocationListener() {

                    //This method gives the current location fo the device
                    @Override
                    public void onLocationChanged(Location location) {
                        findCurrentLocation(new LatLng(location.getLatitude(),location.getLongitude()));
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
                }, Looper.myLooper());
            }
        }
    }

    //Add the marker and center the map on the current location
    private void findCurrentLocation(LatLng latLng){
        Toast.makeText(getContext(),
                "Finding Current Location", Toast.LENGTH_LONG)
                .show();
        addMarkersToMap(latLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,10.2f));

    }

    //https://stackoverflow.com/questions/16097143/google-maps-android-api-v2-detect-long-click-on-map-and-add-marker-not-working
    @Override
    public void onMapLongClick(LatLng latLng) {
        addMarkersToMap(latLng);

            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses.isEmpty()) {
                    mainActivityTexInput.setText("Waiting for Location");
                }else {
                    if (addresses.size() > 0) {
                        mainActivityTexInput.setText(addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());
                    }
                }
            }catch (IOException error){
                Log.d(TAG,error.toString());
            }
    }

    private void savePrefMarker(LatLng latLng,String message){
        appLocationList.add(latLng.toString());
        appMessageList.add(message);
        //Before save the new info clearing the editor fixed some issues
        editor.clear();
        editor.putStringSet(sharedPrefKeyLocation, appLocationList);
        editor.apply();
        editor.putStringSet(sharedPrefKeyMessages, appMessageList);
        //Apply the changes to the editor
        editor.apply();
    }

    //https://stackoverflow.com/questions/38858628/show-address-on-tap-of-marker-in-google-map-in-android
    @Override
    public boolean onMarkerClick(Marker marker) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(marker.getPosition().latitude,marker.getPosition().longitude, 1);
            if (addresses.isEmpty()) {
                mainActivityTexInput.setText("Waiting for Location");
            }else {
                if (addresses.size() > 0) {
                    mainActivityTexInput.setText(addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());
                }
            }
        }catch (IOException error){
            Log.d(TAG,error.toString());
        }
        return false;
    }

    //Make the switch for the polygon tool
    public void polygonFlagToggle(Button button){

        //When the polygon finish, start polygon is called and adds the centroid
        //and the specific polygon draw
        if(createPolygonFlag){
            createPolygonFlag =false;
            button.setText("Start Polygon");
            if(polygonList.size()>0){
                listPolyList.add(polygonList);
                createPolygonFromMarkers();
            }
        }else{
            //This flag allows to not saving the long press markers and start to fill
            // the markers for the polygon list in the addMarkersToMap function
            createPolygonFlag =true;
            Toast.makeText(getContext(),
                    "Touch long to mark each side of the Polygon", Toast.LENGTH_LONG)
                    .show();
            //Empty the list for the next polygon
            polygonList.clear();
            button.setText("End Polygon");
        }
    }

    //Must be a more Elegant solution using REGEX but this works for know
    //https://stackoverflow.com/questions/27261670/convert-string-to-latlng
    public void addSavedMarkers() {

        Iterator<String> iteratorLocation= appLocationList.iterator();
        Iterator<String> iteratorMessage= appMessageList.iterator();
        Log.d(TAG,"Location Size:"+appLocationList.size());
        Log.d(TAG,"Message Size:"+appMessageList.size());
        //If because fo some issue one list if different size of the other,
        //Clear both, some problem because implementing the second list at different
        //development phases
        if(appLocationList.size()!=appMessageList.size()){
            appLocationList.clear();
            appMessageList.clear();
        }

        Log.d(TAG,"ENTER Add Saved Markers");
        while(iteratorLocation.hasNext() && iteratorMessage.hasNext()){
            Log.d(TAG,"ADD Saved Markers");
            String mymapValues=iteratorLocation.next();
            mymapValues=mymapValues.replace("lat/lng: (","");
            mymapValues=mymapValues.replace(")","");

            String[] latlong =  mymapValues.split(",");
            double latitude = Double.parseDouble(latlong[0]);
            double longitude = Double.parseDouble(latlong[1]);

            addMarkersToMap(new LatLng(latitude, longitude),iteratorMessage.next());
        }
    }

    //Create functions with different signatures to avoid repeat code
    public void addMarkersToMap(LatLng latLng){
        //Get Address of the market
        String title =getStreetFromGoogleMaps(latLng);
        createAndAddMarker(latLng,title);
    }

    public void addMarkersToMap(LatLng latLng, String info){
        createAndAddMarker(latLng,info);
    }

    //
    private void createAndAddMarker(LatLng latLng,String info){
        MarkerOptions markerOptions=new MarkerOptions().position(latLng).title(info);
        mMap.addMarker(markerOptions);

        if(createPolygonFlag){
            //add marker to polygon list
            addMarkerToPolygonList(markerOptions);
        }else{
            //Save marker in pref
            savePrefMarker(latLng,info);
            Log.d(TAG,"Save Marker");
        }
    }

    private String getStreetFromGoogleMaps(LatLng latLng){
        String str="";
        Geocoder geocoder = new Geocoder(getContext());
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude,latLng.longitude, 1);

            //Depending of the position of the marker some times there is no address
            //avoid crush by checking the list and filling the value with not found
            if(addressList.size()>0) {
                str = addressList.get(0).getLocality() + " ";
                str += addressList.get(0).getCountryName();
            }else{
                str="Address not found";
            }
        }catch (IOException error){
            error.printStackTrace();
        }
        return str;
    }

    private void addMarkerToPolygonList(MarkerOptions marker){
        polygonList.add(marker.getPosition());
    }

    //https://developers.google.com/maps/documentation/android-sdk/shapes
    private void createPolygonFromMarkers(){
        //Clear the Map, is needed to clear the screen of old drawings

        //After clear we add again the saved markers
        addSavedMarkers();
        Log.d(TAG,"Poly List:"+listPolyList.size());
        for(int i=0;i<listPolyList.size();i++) {
            Log.d(TAG,"Draw Area");
            //Draw the polygon in the map
            PolygonOptions polygonOptions = new PolygonOptions();
            polygonOptions.addAll(listPolyList.get(i)).fillColor(Color.argb(50, 50, 50, 50));
            mMap.addPolygon(polygonOptions);

            //After drawing do the final process of calculating the area and the centroid
            calculateAreaAndCentroid(polygonOptions.getPoints());
        }
    }

    //https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android
    //Delete Everything
    public void clearMarkers(){

        new AlertDialog.Builder(getContext()).setTitle("Clear Markers").setMessage("Sure?, Delete All the markers?").
                setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                listPolyList.clear();
                mMap.clear();
                appLocationList.clear();
                editor.putStringSet(sharedPrefKeyLocation, appLocationList);
                editor.apply();
            }
        })    // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton("NO", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    //https://en.wikipedia.org/wiki/Centroid
    //https://en.m.wikipedia.org/wiki/Shoelace_formula
    private void calculateAreaAndCentroid(List<LatLng> latLngsList){

        double centroidXAvg=0;
        double centroidYAvg=0;

        String unitMesure="";

        //Did some iterations trying to work out some formulas but needed more time
        for(int i=0;i<latLngsList.size()-1;i++){

            double x1=latLngsList.get(i).latitude;
            double y2=latLngsList.get(i).longitude;

            centroidXAvg+=x1;
            centroidYAvg+=y2;
        }

        centroidXAvg=centroidXAvg/(latLngsList.size()-1);
        centroidYAvg=centroidYAvg/(latLngsList.size()-1);

        //The marker with the centroid is added after
        LatLng centroidLatLng;
        centroidLatLng = new LatLng(centroidXAvg,centroidYAvg);

        Double sphereArea=SphericalUtil.computeArea(latLngsList);
        https://www.javatips.net/api/com.google.maps.android.sphericalutil

        if (sphereArea > 1000000){
            sphereArea/=1000 ;
            unitMesure=" KM^2";
        }else {
            unitMesure=" M^2";
        }
        addMarkersToMap(centroidLatLng,(sphereArea)+unitMesure);
    }

}
