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
        /*Toolbar toolbar = findViewById(R.id.toolbar);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        final MapsActivity mapsActivity=new MapsActivity();
        mapsActivity.mainActivityTexInput =findViewById(R.id.input_text);
        fragmentTransaction.add(R.id.myMap,mapsActivity);
        fragmentTransaction.commit();

        poligonBtn= (Button) findViewById(R.id.poligonBtn);
        poligonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapsActivity.polygonFlagToggle(poligonBtn);
            }
        });

        clearMarkersBtn= (Button) findViewById(R.id.clearMarkersBtn);
        clearMarkersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapsActivity.clearMarkers();
            }
        });


    }

}
