package com.example.assignment_2;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker centroid_marker;

    private EditText location;
    private Button button;
    private TextView view_area;

    private static int private_mode = 0x0000;

    SharedPreferences pref_object;
    SharedPreferences.Editor edit_object;
    private int idx = 0;

    private ArrayList<Double> latitude_list = new ArrayList<>();
    private ArrayList<Double> longtitude_list = new ArrayList<>();
    private List<LatLng> latLngs = new ArrayList<LatLng>();

    Polygon polygon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        location = findViewById(R.id.location);
        button = findViewById(R.id.button);
        view_area = findViewById(R.id.areaSpace);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(idx < 2){
                    Context context = getApplicationContext();
                    String input_text = "You need at lease 3 marker !";
                    int required_time = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, input_text, required_time);
                    toast.show();
                }else {
                    button.setText("End Polygon");
                    double result = polygon_area_calculation(latitude_list,longtitude_list);
                    view_area.setText(result + "km^2");
                    PolygonOptions polygonOptions = new PolygonOptions();
                    polygonOptions.addAll(latLngs).strokeWidth(2).fillColor(0x7F1817C0);
                    mMap.addPolygon(polygonOptions);

                    LatLng center_point = Centroid_position(latLngs);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(center_point));
                    mMap.addMarker(new MarkerOptions().position(center_point).title("This is a centroid point").snippet("of polygon area").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                }

            }
        });
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

        // Add a marker in Sydney and move the camera
        LatLng started_point = new LatLng(50.980806, 11.332357);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(started_point,5.0f));


        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(location.getText().length() == 0)
                {
                    //Source: https://developer.android.com/guide/topics/ui/notifiers/toasts
                    Context context = getApplicationContext();
                    String input_text = "You have to put a title for the marker!";
                    int required_time = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, input_text, required_time);
                    toast.show();
                }
                else
                {
                    double marker_latitude, marker_longtitude ;
                    String text_marker = location.getText().toString();
                    marker_latitude = latLng.latitude;
                    marker_longtitude = latLng.longitude;
                    latitude_list.add(marker_latitude);
                    longtitude_list.add(marker_longtitude);
                    LatLng new_marker = new LatLng(marker_latitude,marker_longtitude);
                    latLngs.add(new_marker);
                    String location_inform = latLng.toString();
                    String marker_inform = location.getContext().toString();
                    LatLng location_marker = new LatLng(marker_latitude, marker_longtitude);
                    MarkerOptions marker_point = new MarkerOptions().position(location_marker).title(text_marker).snippet(new_marker.toString());
                    mMap.addMarker(marker_point);
                    location.setText("");
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(location_marker));
                    pref_object = getPreferences(private_mode);
                    edit_object = pref_object.edit();
                    Set<String> location_list = new LinkedHashSet<>();

                    location_list.add(marker_inform);
                    location_list.add(location_inform);
                    edit_object = pref_object.edit().putStringSet("Marker number " + idx, location_list);
                    idx += 1;
                    edit_object.commit();
                }
            }
        });
    }

    // Calculate area
    // https://stackoverflow.com/questions/1340223/calculating-area-enclosed-by-arbitrary-polygon-on-earths-surface
    private double polygon_area_calculation(ArrayList<Double> lats, ArrayList<Double> lons)
    {
        double sum = 0;
        double prevcolat = 0;
        double prevaz = 0;
        double colat0 = 0;
        double az0 = 0;
        for (int i=0;i<lats.size();i++)
        {
            double colat=2*Math.atan2(Math.sqrt(Math.pow(Math.sin(lats.get(i)*Math.PI/180/2), 2)+ Math.cos(lats.get(i)*Math.PI/180)*Math.pow(Math.sin(lons.get(i)*Math.PI/180/2), 2)),Math.sqrt(1-  Math.pow(Math.sin(lats.get(i)*Math.PI/180/2), 2)- Math.cos(lats.get(i)*Math.PI/180)*Math.pow(Math.sin(lons.get(i)*Math.PI/180/2), 2)));
            double az=0;
            if (lats.get(i)>=90)
            {
                az=0;
            }
            else if (lats.get(i)<=-90)
            {
                az=Math.PI;
            }
            else
            {
                az=Math.atan2(Math.cos(lats.get(i)*Math.PI/180) * Math.sin(lons.get(i)*Math.PI/180),Math.sin(lats.get(i)*Math.PI/180))% (2*Math.PI);
            }
            if(i==0)
            {
                colat0=colat;
                az0=az;
            }
            if(i>0 && i<lats.size())
            {
                sum=sum+(1-Math.cos(prevcolat  + (colat-prevcolat)/2))*Math.PI*((Math.abs(az-prevaz)/Math.PI)-2*Math.ceil(((Math.abs(az-prevaz)/Math.PI)-1)/2))* Math.signum(az-prevaz);
            }
            prevcolat=colat;
            prevaz=az;
        }
        sum=sum+(1-Math.cos(prevcolat  + (colat0-prevcolat)/2))*(az0-prevaz);
        double result = 5.10072E14* Math.min(Math.abs(sum)/4/Math.PI,1-Math.abs(sum)/4/Math.PI);
        if(result > 1000000)
            result /= 1000000;
        return result;
    }

    private LatLng Centroid_position(List<LatLng> marker_list){
        Double latitude = new Double(0.0);
        Double longtitude = new Double(0.0);
        int num_marker = latLngs.size();
        for (int i=0; i< num_marker; i++){
            latitude += latitude_list.get(i);
            longtitude += longtitude_list.get(i);
        }

        return new LatLng(latitude/num_marker, longtitude/num_marker);

    }


}
