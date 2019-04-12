package com.example.assignment_2;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashSet;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

   private GoogleMap mMap;
   private EditText location;
   private SharedPreferences sharedPref;
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_maps);
      SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
      mapFragment.getMapAsync(this);
      location = findViewById(R.id.location);
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

      //Source: https://www.codota.com/code/java/methods/com.google.android.gms.maps.GoogleMap/setOnMapLongClickListener
      //        https://developers.google.com/maps/documentation/android-sdk/marker
      mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
         @Override
         public void onMapLongClick(LatLng latLng) {

            //TODO: Why does the program always enter the if class even if I don't write something in editText?
            if(location.getText().toString() != "")
            {
               String titleMarker = location.getText().toString();
               LatLng locationMarker = new LatLng(latLng.latitude, latLng.longitude);
               mMap.addMarker(new MarkerOptions().position(locationMarker).title(titleMarker));
               //mMap.moveCamera(CameraUpdateFactory.newLatLng(locationMarker));

               //Source: https://developer.android.com/training/data-storage/shared-preferences
               //        https://stackoverflow.com/questions/32527199/cannot-resolve-method-getactivity
               //        https://stackoverflow.com/questions/5950043/how-to-use-getsharedpreferences-in-android
               //        https://stackoverflow.com/questions/2041778/how-to-initialize-hashset-values-by-construction
               Context context = MapsActivity.this;
               sharedPref = context.getSharedPreferences("locations", MODE_PRIVATE);
               SharedPreferences.Editor prefsEditor;

               prefsEditor = sharedPref.edit();
               Set<String> hashSet = new HashSet<>();
               hashSet.add(location.getText().toString());
               hashSet.add( latLng.toString());
               prefsEditor.clear();
               prefsEditor.putStringSet("Marker", hashSet);
               prefsEditor.commit();
               location.setText("");
            }
            else
            {
               //Source: https://developer.android.com/guide/topics/ui/notifiers/toasts
               Context context = getApplicationContext();
               CharSequence text = "Set a title for the marker!";
               int duration = Toast.LENGTH_SHORT;
               Toast toast = Toast.makeText(context, text, duration);
               toast.show();
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
   void calulatePoly(){
      Set <String> set = sharedPref.getStringSet("Marker", null);

      if(set.size() >= 3)
      {

      }
   }
}
