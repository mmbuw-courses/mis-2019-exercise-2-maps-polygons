package com.example.assignment_2;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

   private GoogleMap mMap;
   private EditText location;

   private TextView textView;
   private short index;
   private int size;

   SharedPreferences prefs;
   SharedPreferences.Editor editor;
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_maps);
      SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

      size = 0;
      index = 0;

      mapFragment.getMapAsync(this);
      location = findViewById(R.id.location);
      final Button button = findViewById(R.id.button);
      textView = findViewById(R.id.areaSpace);

      button.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            button.setText("End Polygon");

            PolygonOptions rectOptions = new PolygonOptions();

            double result = calculateArea(rectOptions);

            mMap.addPolygon(rectOptions.strokeColor(Color.RED).strokeWidth(1.5f).fillColor(0x5500ff00));

            LatLng center = calcCentroid(rectOptions);
            createCenterMarker(center, result);
            textView.setText(result + "km^2");
         }
      });
   }

   private double calculateArea(PolygonOptions rectOptions){
      prefs = getPreferences(Context.MODE_PRIVATE);

      LinkedHashMap<Double, Double> points = new LinkedHashMap<>();

      while (size < index && index>=3) {
         Set<String> set = prefs.getStringSet("Marker" + size, null);
         //Source: https://www.javatpoint.com/difference-between-arraylist-and-vector

         List<String> list = new ArrayList<>(set);
         String lat_lng = list.get(0);
         String lat_lng2 = list.get(1);

         int indexOfBracket = -1;
         int indexOfComma = -1;
         int indexOfBracket2 = -1;
         String lat = "";
         String lng = "";
         double latDouble = -1d;
         double lngDouble = -1d;
         // Source: http://www.java67.com/2018/05/java-string-chartat-example-how-to-get-first-last-character.html
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
            //Source: https://www.geeksforgeeks.org/hashset-contains-method-in-java/
            indexOfBracket = lat_lng2.indexOf("(");
            indexOfComma = lat_lng2.indexOf(",");
            indexOfBracket2 = lat_lng2.indexOf(")");

            lat = lat_lng2.substring(indexOfBracket + 1, indexOfComma);
            lng = lat_lng2.substring(indexOfComma + 1, indexOfBracket2);
         }

         // Source: https://stackoverflow.com/questions/1253499/simple-calculations-for-working-with-lat-lon-km-distance
         //         https://www.journaldev.com/18392/java-convert-string-to-double
         //System.out.print("Lat: ");
         //System.out.println(lat);
         //System.out.print("Long: ");
         //System.out.println(lng);
         //System.out.println();

         latDouble = Double.parseDouble(lat);
         double latitudeKilometer = latDouble * 110.567;
         //System.out.println("latInt: " + latDouble);
         lngDouble = Double.parseDouble(lng);
         double longitudeKilometer = lngDouble * 111.320* Math.cos(latitudeKilometer);
         //System.out.println("lngInt: " + lngDouble);

         //System.out.println("lat: " + latDouble);
         //System.out.println("lng: " + lngDouble);

         points.put(latitudeKilometer, longitudeKilometer);
         rectOptions.add(new LatLng(Double.parseDouble(lat),Double.parseDouble(lng)));
         ++size;
      }
      //System.out.println("Initial Mappings are: " + points);
      //System.out.println("The size of the map is " + points.size());

      double x1 = -1;
      double y1 = -1;
      double x2 = -1;
      double y2 = -1;
      double result = 0;

      //Source:  https://stackoverflow.com/questions/1066589/iterate-through-a-hashmap
      //         https://www.mathopenref.com/coordpolygonarea.html
      //         https://www.geeksforgeeks.org/iterate-map-java/
      //         https://stackoverflow.com/questions/31080272/java-iterator-get-next-without-incrementing
      //         https://www.mathopenref.com/coordpolygonarea2.html
      Iterator<Map.Entry<Double, Double>> itr = points.entrySet().iterator();

      Map.Entry<Double, Double> second = null;

      Map.Entry<Double, Double> actual = null;
      Map.Entry<Double, Double> next = null;

      while(itr.hasNext())
      {
         if(second != null){
            x1 = second.getKey();
            y1 = second.getValue();
            next = itr.next();
         }else{
            actual = itr.next();
            x1 = actual.getKey();
            y1 = actual.getValue();
            next = itr.next();
         }
         if (next != null) {
            x2 = next.getKey();
            y2 = next.getValue();
            second = next;
         }
         if(x1 != -1 && x2 != -1 && y1 != -1 && y2 != -1)
         {
            result = result + ((x1 + x2) * (y1 - y2));
         }
      }
      x1 = second.getKey();
      y1 = second.getValue();
      Map.Entry<Double,Double> first = points.entrySet().iterator().next();
      x2 = first.getKey();
      y2 = first.getValue();
      result = result + ((x1 + x2) * (y1 - y2));

      result = result / 2;
      if (result < 0)
      {
         result = result * -1;
      }
      return result;
   }

   private void createCenterMarker(LatLng center, double result){
      LatLng locationMarker = new LatLng(center.latitude, center.longitude);
      MarkerOptions m = new MarkerOptions().position(locationMarker).title(Double.toString(result) + "km^2");
      mMap.addMarker(m);
   }

   // Source: https://math.stackexchange.com/questions/90463/how-can-i-calculate-the-centroid-of-polygon
   private LatLng calcCentroid(PolygonOptions rectOptions){
      List<LatLng> list = rectOptions.getPoints();
      int size = list.size();
      double lat = 0;
      double lng = 0;
      for(int i = 0; i < size; ++i){
         lat += list.get(i).latitude;
         lng += list.get(i).longitude;
      }
      lat = lat/size;
      lng = lng/size;

      return new LatLng(lat,lng);
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
               MarkerOptions m = new MarkerOptions().position(locationMarker).title(titleMarker);
               mMap.addMarker(m);
               prefs = getPreferences(Context.MODE_PRIVATE);
               editor = prefs.edit();
               Set<String> hashSet = new HashSet<>();
               hashSet.add(location.getText().toString());
               hashSet.add(latLng.toString());
               editor.putStringSet("Marker" + index, hashSet);
               editor.commit();
               ++index;
               location.setText("");
            }
         }
      });
   }
}
