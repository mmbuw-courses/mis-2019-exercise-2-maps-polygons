//@Author : Siva Bathala
//Matriknumber : 119484
//Mobile Information Systems SoSe-19
//Assignment 2
// Reference Sources are written in Corresponding functions

package com.example.mapspolygons;
import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    // Defining all the Variables used in the program which inludes Textbox and button variables for their opertaions
    private GoogleMap mMap;
    private EditText editText;
    //Coordinates has been taken from following website
    //https://latitude.to/articles-by-country/de/germany/19728/bauhaus-university-weimar
    private LatLng currentPos = new LatLng(50.98, 11.33);
    //To Retrieve to Location Updates
    private LocationManager locationManager;
    //Array list to store the points
    private ArrayList pointLst = new ArrayList();
    private Button sPoly;
    private Polygon polyg;
    private PolygonOptions pOpt;
    //Taking boolean values so we can decide whether button has to show start polygon or Stop Polygon
    private boolean polyS = false;
    //Shared Preferences are inspired from https://www.journaldev.com/9412/android-shared-preferences-example-tutorial
    SharedPreferences sharedP;
    private SharedPreferences.Editor editor;
    private int Inc = 0;
    //private static final double EARTH_RADIUS = 6371000;// meters
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //https://stackoverflow.com/questions/21085497/how-to-use-android-locationmanager-and-listener--shiva
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // Checking the Permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        // Code is inspired from : https://developer.android.com/guide/topics/location/strategies
        //Cheking NETWORK_PROVIDER Enabled or not This will return a boolean values true- if enabled , false - not enabled
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    currentPos = new LatLng(latitude, longitude);
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addList = geocoder.getFromLocation(latitude, longitude, 1);
                        String myLoc = addList.get(0).getLocality()+",";
                        myLoc += addList.get(0).getCountryName();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }
        //Cheking GPS_PROVIDER Enabled or not This will return a boolean values true- if enabled , false - not enabled
        else if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    currentPos = new LatLng(latitude, longitude);

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });

        }
        //If both are Not enabled it will return the Bauhaus University Location
        else {
                   currentPos =  new LatLng(50.00, 11.33);
        }

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
       //Assigning Varibles to the elements in the Layout
        mMap = googleMap;
        editText = (EditText) findViewById(R.id.editText);
        Button clearButton = (Button) findViewById(R.id.clearButton);
        sPoly = (Button) findViewById(R.id.sPoly);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 14.0f));
        mMap.addMarker(new MarkerOptions().position(currentPos).title("My Location"));
       sharedP = getSharedPreferences("Locations", Context.MODE_PRIVATE);

        Inc = sharedP.getInt("Increment",0);

        pointLst.clear();
        //Adding Markers that are presented in the Shared Preferences
         if(Inc != 0){
            String latitude = "";
            String longitude = "";
            String text = "";

            for(int i = 0; i<Inc; i++){
                String s = Integer.toString(i);
                latitude = sharedP.getString("lat"+s, "0");
                longitude = sharedP.getString("lng" +s,"0");
                text = sharedP.getString("text"+s, "0");
                LatLng position = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                //Adding Marker with Infobox
                mMap.addMarker(new MarkerOptions().position(position).title(text));
            }
        }
        // Adding onclick listener for the Button "Clear" Button
        // Function : Clearing the shared preferences Data and Markers in the Map
        //Setting Text to the Start Polygon
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedP = getSharedPreferences("Locations", Context.MODE_PRIVATE);
                editor = sharedP.edit();
                editor.clear();
                editor.commit();
                mMap.clear();
                polyS = false;
                editText.getText().clear();
                sPoly.setText("Start Polygon");

            }
        });
        // Adding Long Click listener for the Map
        // Function : Adding Marker to the Map and store the Locations in Sharedpreferences
        //https://stackoverflow.com/questions/25438043/store-google-maps-markers-in-sharedpreferences

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener(){
            @Override
            public void onMapLongClick(LatLng latLng) {
                editText.getText().clear();
                String text = editText.getText().toString();
                currentPos = latLng;
                sharedP = getSharedPreferences("Locations", Context.MODE_PRIVATE);
                Inc++;
                editor = sharedP.edit();
                String sLat =  Double.toString(latLng.latitude);
                String sLang =  Double.toString(latLng.longitude);
                String cntString = Integer.toString(Inc);
                editor.putInt("Counter", Inc);
                editor.putString("lat" + cntString, sLat);
                editor.putString("lng" + cntString,sLang );
                editor.putString("text" + cntString, text);

                if (polyS == true) {
                    pointLst.add(latLng);
                }
                editor.commit();
                mMap.addMarker(new MarkerOptions().position(latLng).title(text));

                if (pointLst.isEmpty() == false) {
                    polyg.setPoints(pointLst);
                }


            }
        });

         // Adding Click listener for Creating the polygon for Markers
        // Function : Adding polygon to created markers and changing the text of the button
        // If when click on start polygon calculating the Cebtroid and Area
        sPoly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (polyS == false)
                {
                    //Onclick on Start Polygon Text will change to End polgon
                    polyS = true;
                    editText.getText().clear();
                    sPoly.setText("End Polygon");
                    //Filling the transperent color for polygon
                    pOpt = new PolygonOptions()
                            .add(new LatLng(0, 0))
                            .strokeColor(Color.argb(255, 102, 204, 255))
                            .fillColor(Color.argb(100, 102, 204, 255));
                    polyg = mMap.addPolygon(pOpt);
                }
                else
                 {  //Calculating the Centroid after click on the End polgon and changing the Text to Start Polygon
                    LatLng centroid = getCentroid(pointLst);
                    mMap.addMarker(new MarkerOptions().position(centroid).title(getPloyArea(pointLst)));
                    pointLst.clear();
                    polyS = false;
                    editText.getText().clear();
                    sPoly.setText("Start Polygon");
                }
            }});
    }
    // Adding a private method to calculate the Area of the Polygon
    // Function : It will calculate the area based on Latitude and longitude
    //private static DecimalFormat df = new DecimalFormat("0.00");
    private String getPloyArea(ArrayList<LatLng> points){
        double size;
        double Total = 0.0;
        //double fsize;
        double X1 = points.get(0).latitude;
        double X2 = points.get(points.size()-1).latitude;
        double Y1 = points.get(0).longitude;
        double Y2 = points.get(points.size()-1).longitude;

        for (int i=0; i<(points.size()-1); i++){

            double x1 = points.get(i).latitude;
            double y1 = points.get(i).longitude;
            double x2 = points.get(i+1).latitude;
            double y2 = points.get(i+1).longitude;

            Total = Total + ((x1 * y2) - (y1 * x2));

        }
        Total = Total + (X2*Y1 - Y2*X1);

        size = Math.abs(Total) / 2;
        //fsize = df.format(size);
        //return Double.toString(df.format(fsize));
        return Double.toString(size);
    }
    //https://stackoverflow.com/questions/18440823/how-do-i-calculate-the-center-of-a-polygon-in-google-maps-android-api-v2
    //Finding out the Centroid of the polygon
    private LatLng getCentroid(ArrayList<LatLng> points){
        int pointCount = points.size();
        double[] Centroid = { 0.0, 0.0 };


        for (int i = 0; i<points.size(); i++){

            Centroid[0] += points.get(i).latitude;
            Centroid[1] += points.get(i).longitude;
        }

        Centroid[0] /= pointCount;
        Centroid[1] /= pointCount;

        LatLng centroid = new LatLng(Centroid[0], Centroid[1]);


        return centroid;
    }

}