package com.example.mapapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.LinkedList;

//https://developer.android.com/guide/topics/ui/settings  ---sharedprefrences
//https://stackoverflow.com/questions/23024831/android-shared-preferences-example

public class PreferenceHelper {
    private static  PreferenceHelper prefernceInstance ;
    private SharedPreferences sharedPreferences;
    private static final String TAG = PreferenceHelper.class.getSimpleName();


    public static PreferenceHelper getInstance(Context context) {
        if (prefernceInstance == null) {
            prefernceInstance = new PreferenceHelper(context);
        }
        return prefernceInstance;
    }

    private PreferenceHelper(Context context) {
        sharedPreferences=context.getSharedPreferences("preference",0);
    }

    public void saveData(String latitude,String longitude,String infoMsg){
        int totalMarker=getTotalMarkers();
        int keyValue=totalMarker+1;
        SharedPreferences.Editor editor=sharedPreferences.edit();
        //Log.i(TAG,"set values : "+values.toString());
        editor.putString(String.valueOf("latitude" + keyValue),latitude);
        editor.putString(String.valueOf("longitude" + keyValue),longitude);
        editor.putString(String.valueOf("infoMsg" + keyValue),infoMsg);
        editor.putInt("Marker_Size",totalMarker+1);
        editor.commit();
    }

    public LinkedList<String[]> getAllMarkers(){

        LinkedList returnlist=new LinkedList();
        Log.i(TAG,"inside getAllMarkers");

        String latitude;
        String longitude;
        String infoMsg;

        int totalMarkers=sharedPreferences.getInt("Marker_Size",0);
        Log.i(TAG,"inside getAllMarkers totalMarkers"+totalMarkers);
        for (int i=1;i<=totalMarkers;i++){
            //Log.i(TAG,"values"+sharedPreferences.getStringSet("Marker" + i, null).toString());
            latitude=sharedPreferences.getString("latitude" + i, null);
            longitude=sharedPreferences.getString("longitude" + i, null);
            infoMsg=sharedPreferences.getString("infoMsg" + i, null);

            String[] markerInfo={latitude,longitude,infoMsg};

            returnlist.add( markerInfo);
        }
        return returnlist;
    }

    private int getTotalMarkers(){

        return sharedPreferences.getInt("Marker_Size",0);
    }

    public void deleteData(){
        int totalMarker=getTotalMarkers();
        int keyValue=totalMarker+1;
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }


}
