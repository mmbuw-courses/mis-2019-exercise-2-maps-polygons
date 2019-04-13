package com.example.assignment_2;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

   private GoogleMap mMap;
   private EditText location;

   private Button button;
   private short index;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_maps);
      SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
      mapFragment.getMapAsync(this);
      location = findViewById(R.id.location);
      button = findViewById(R.id.button);
      index = 0;
      button.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            int size = 0;
            SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
            Map<Double, Double> points = new HashMap();

            while (size < index) {
               Set<String> set = prefs.getStringSet("Marker" + size, null);
               List<String> list = new ArrayList<String>(set);
               String lat_lng = list.get(0);
               String lat_lng2 = list.get(1);

               int indexOfBracket = -1;
               int indexOfComma = -1;
               int indexOfBracket2 = -1;
               String lat = "";
               String lng = "";
               double latDouble = -1d;
               double lngDouble = -1d;

               if(lat_lng.charAt(0) == 'l')
               {
                  indexOfBracket = lat_lng.indexOf("(");
                  indexOfComma = lat_lng.indexOf(",");
                  indexOfBracket2 = lat_lng.indexOf(")");

                  lat = lat_lng.substring(indexOfBracket + 1, indexOfComma);
                  lng = lat_lng.substring(indexOfComma + 1, indexOfBracket2);
               }
               else if(lat_lng2.charAt(0) == 'l')
               {
                  indexOfBracket = lat_lng2.indexOf("(");
                  indexOfComma = lat_lng2.indexOf(",");
                  indexOfBracket2 = lat_lng2.indexOf(")");

                  lat = lat_lng2.substring(indexOfBracket + 1, indexOfComma);
                  lng = lat_lng2.substring(indexOfComma + 1, indexOfBracket2);
               }
               latDouble = Double.parseDouble(lat);
               double latitudeKilometer = latDouble * 110.574;
               //System.out.println("latInt: " + latDouble);
               lngDouble = Double.parseDouble(lng);
               double longitudeKilometer = lngDouble * 111.320* Math.cos(latitudeKilometer);
               //System.out.println("lngInt: " + lngDouble);

               //System.out.println("lat: " + latDouble);
               //System.out.println("lng: " + lngDouble);

               points.put(latitudeKilometer, longitudeKilometer);

               ++size;
            }

            if(size >= 3) {
               double result = 0;
               //source:  https://www.mathopenref.com/coordpolygonarea.html
               Iterator it = points.entrySet().iterator();
               while (it.hasNext()) {
                  Map.Entry pair1 = (Map.Entry) it.next();
                  Map.Entry pair2 = null;
                  if (it.hasNext()) {
                     pair2 = (Map.Entry) it.next();
                  }
                  result += Double.parseDouble(pair1.getKey().toString()) *
                            Double.parseDouble(pair2.getValue().toString()) -
                            Double.parseDouble(pair1.getValue().toString()) *
                            Double.parseDouble(pair2.getKey().toString());

               }
               result = result / 2;
               if (result < 0) {
                  result = result * -1;
               }
               System.out.println(result + "in km");
            }
         }
      });
   }

   @Override
   public void onMapReady(GoogleMap googleMap) {
      mMap = googleMap;

      //Source: https://www.codota.com/code/java/methods/com.google.android.gms.maps.GoogleMap/setOnMapLongClickListener
      //        https://developers.google.com/maps/documentation/android-sdk/marker
      mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
         @Override
         public void onMapLongClick(LatLng latLng) {
            if(location.getText().toString().matches(""))
            {
               //Source: https://developer.android.com/guide/topics/ui/notifiers/toasts
               Context context = getApplicationContext();
               CharSequence text = "Set a title for the marker!";
               int duration = Toast.LENGTH_SHORT;
               Toast toast = Toast.makeText(context, text, duration);
               toast.show();
            }
            else
            {
               String titleMarker = location.getText().toString();
               LatLng locationMarker = new LatLng(latLng.latitude, latLng.longitude);
               mMap.addMarker(new MarkerOptions().position(locationMarker).title(titleMarker));

               SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
               SharedPreferences.Editor editor = prefs.edit();
               Set<String> hashSet = new HashSet<>();
               hashSet.add(location.getText().toString());
               hashSet.add(latLng.toString());
               editor.putStringSet("Marker" + index, hashSet);
               editor.commit();
               ++index;
            }

         }
      });
      // Add a marker in Sydney and move the camera
      //LatLng sydney = new LatLng(-34, 151);
      //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
      //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

      //prefsEditor.putString("Marker Title", location.getText().toString());
      //prefsEditor.putString("latLng", latLng.toString());
   }

   //Source: https://developers.google.com/maps/documentation/android-sdk/shapes
}
