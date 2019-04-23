package com.example.lenovoidea.mis_2019_exercise_2_maps_polygons;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
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
import com.google.maps.android.SphericalUtil;

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.pow;
import static java.lang.StrictMath.sqrt;

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
                    if(!latLngs.isEmpty()) {
                        renderPolygon();
                        //calculateArea();      //  my failed area calculation

                        LatLng areaMarker = calculateCentroid();
                        mMap.addMarker(new MarkerOptions().position(areaMarker).title(calculateArea_2()+" sq. km"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(areaMarker));
                    }
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

        //removeSavedData();        //      to clean all saved marks
        getSavedData();
    }

    @Override
    public void onMapLongClick(LatLng latLng){

        if(!polygonDrawMode){
            //      ref     https://developer.android.com/training/data-storage/shared-preferences
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

    //  calculates the area of the polygon
    //  ref     https://www.wikihow.com/Calculate-the-Area-of-a-Polygon
    public double calculateArea(){
        double sumX = 0, sumY = 0, difference, areaRaw, areaKm2;
        for(int i = 0; i < latLngs.size(); i++){
            //Log.d("All LatLng :",latLngs.get(i).latitude + " : " + latLngs.get(i).longitude);

             if(i == latLngs.size()-1){
                 sumX += latLngs.get(i).latitude * latLngs.get(0).longitude;
                 sumY += latLngs.get(0).latitude * latLngs.get(i).longitude;
             }else{
                 sumX += latLngs.get(i).latitude * latLngs.get(i + 1).longitude;
                 sumY += latLngs.get(i + 1).latitude * latLngs.get(i).longitude;
             }
        }

        Log.d("All Summ :",sumX + " : " + sumY);

        //difference = abs (sumX - sumY);
        difference = sumX - sumY;

        Log.d("All Difference :",difference + "");

        areaRaw = difference/2;     //      polygon area is ok

        //      i could not convert the polygon area into the geographic area :(

        /**************   Test to compute the area of geography  **************/

        //areaKm2 = areaRaw * 1.609344d * 1.609344d;
        areaKm2 = abs(areaRaw * 6371 * 6371);

        Log.d("All area :",areaKm2 + "");

        testDist();     //  more test to find a ratio

        /**************   Test to compute the area of geography  **************/

        return areaRaw;
    }

    //  after failing to calculate the geographic area i used this library
    //  ref     https://stackoverflow.com/questions/28838287/calculate-the-area-of-a-polygon-drawn-on-google-maps-in-an-android-application
    //  ref     http://googlemaps.github.io/android-maps-utils/
    public double calculateArea_2(){
        double area = SphericalUtil.computeArea(latLngs)/1000000;  // converts m2 to km2
        //Log.d("All area :",area + "");
        return area;
    }

    //  calculation of the centroid
    //  used the polygon area which i calculated before calculateArea()
    //  ref     https://www.seas.upenn.edu/~sys502/extra_materials/Polygon%20Area%20and%20Centroid.pdf
    public LatLng calculateCentroid(){
        LatLng centroid;
        double sumX = 0, sumY = 0;

        for(int i = 0; i < latLngs.size(); i++){
            double xi,xi1,yi,yi1;

            xi = latLngs.get(i).latitude;
            yi = latLngs.get(i).longitude;

            if(i == latLngs.size()-1){
                xi1 = latLngs.get(0).latitude;
                yi1 = latLngs.get(0).longitude;
            }else{
                xi1 = latLngs.get(i+1).latitude;
                yi1 = latLngs.get(i+1).longitude;
            }

            sumX += (xi+xi1) * (xi*yi1 - xi1*yi);
            sumY += (yi+yi1) * (xi*yi1 - xi1*yi);

            Log.d("All sumX", sumX + "");
            Log.d("All sumY", sumY + "");
        }

        sumX = sumX/(6 * calculateArea());
        sumY = sumY/(6 * calculateArea());

        Log.d("All sumX", sumX + "");
        Log.d("All sumY", sumY + "");

        centroid = centroid = new LatLng(sumX, sumY);

        return centroid;
    }

    //  more tests to find a ratio to convert polygon area to geographic area
    //  no luck
    //  inconsistent ratio
    private void testDist(){
        Location locA = new Location("Point A");
        Location locB = new Location("Point B");

        locA.setLatitude(latLngs.get(0).latitude);
        locA.setLatitude(latLngs.get(0).longitude);
        locB.setLatitude(latLngs.get(1).latitude);
        locB.setLatitude(latLngs.get(1).longitude);

        double distance = locA.distanceTo(locB)/1000;

        double cartesianDistance = sqrt(pow((latLngs.get(0).latitude - latLngs.get(1).latitude), 2) + pow((latLngs.get(0).longitude - latLngs.get(1).longitude), 2));

        double ratio = distance / cartesianDistance;

        Log.d("All distance ", distance+"");
        Log.d("All cartesian ", cartesianDistance+"");
        Log.d("All ratio ", ratio+"");
    }
}
