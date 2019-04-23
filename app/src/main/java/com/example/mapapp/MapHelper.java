package com.example.mapapp;

import java.util.LinkedList;

//https://stackoverflow.com/questions/18440823/how-do-i-calculate-the-center-of-a-polygon-in-google-maps-android-api-v2
//https://www.mathopenref.com/coordpolygonarea2.html
public class MapHelper {

    public static Double calculateArea(LinkedList<String[]> markerList){

        int vertices=markerList.size();
        double area=0.0;
        int j=vertices-1;

        String[] p1,p2;
        for(int i=0;i<vertices;j=i++){
            p1=markerList.get(i);
            p2=markerList.get(j);

            area += Double.valueOf(p1[0])*Double.valueOf(p2[1]);
            area -= Double.valueOf(p1[1])*Double.valueOf(p2[0]);
        }
        area=(java.lang.Math.abs(area)/2)*10000;

        return (double)Math.round(area*100000d)/100000d;//convert to km sq

    }


    public static double[] calculateCentroid(LinkedList<String[]> markerList){

        int vertices=markerList.size();
        double x=0; double y=0;
        double f;
        int j=vertices-1;

        String[] p1,p2;
        for(int i=0;i<vertices;j=i++){
            p1=markerList.get(i);
            p2=markerList.get(j);

            f=Double.valueOf(p1[0])*Double.valueOf(p2[1]) - Double.valueOf(p2[0])*Double.valueOf(p1[1]);
            x+= Double.valueOf(p1[0])+Double.valueOf(p2[0])*f;
            y+= Double.valueOf(p1[1])+Double.valueOf(p2[1])*f;
        }

        f=calculateArea(markerList)*6;
        double[] latLan=new double[2];
        latLan[0]=x/f;
        latLan[1]=y/f;

        return latLan;

    }

    public static double[] calculateCentroidE(LinkedList<String[]> markerList){
        int vertices=markerList.size();

        String[] point;
        double[] latLan=new double[2];
        for(int i=0;i<vertices;i++){
            point=markerList.get(i);

            latLan[0]+= Double.valueOf(point[0]);
            latLan[1]+= Double.valueOf(point[1]);
        }

        latLan[0]=latLan[0]/vertices;
        latLan[1]=latLan[1]/vertices;

        return latLan;

    }



}
