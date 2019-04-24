package com.example.myapplication;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;

public class MainActivity extends FragmentActivity {

    private Button poligonBtn;
    private Button clearMarkersBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Get the fragment this was achieve by Stackoverflow post
        //Sadly loose the link
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        final MapsActivity mapsActivity=new MapsActivity();
        mapsActivity.mainActivityTexInput =findViewById(R.id.input_text);
        fragmentTransaction.add(R.id.myMap,mapsActivity);
        fragmentTransaction.commit();

        //Track the button for Polygon drawing
        poligonBtn= findViewById(R.id.poligonBtn);
        poligonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapsActivity.polygonFlagToggle(poligonBtn);
            }
        });

        //Add a function to clear the screen of saved markers, it was a little
        //difficult to work after a while
        clearMarkersBtn= findViewById(R.id.clearMarkersBtn);
        clearMarkersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapsActivity.clearMarkers();
            }
        });


    }

}
