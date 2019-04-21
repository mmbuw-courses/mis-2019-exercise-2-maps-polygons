package com.georgii_nika.assignment2;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.vividsolutions.jts.algorithm.CentroidArea;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MyPolygon {
    private final DecimalFormat format = new DecimalFormat("#.##");

    private List<Marker> markers;
    private List<Polyline> polylines;
    private Polygon polygon;
    private Marker centroid;

    private GoogleMap map;

    public MyPolygon(GoogleMap map) {
        this.markers = new LinkedList<>();
        this.polylines = new LinkedList<>();
        this.map = map;
    }

    public void addMarker(LatLng latLng) {
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        Marker current = map.addMarker(options);

        if (markers.size() > 0) {
            Marker previous = markers.get(markers.size() - 1);

            PolylineOptions polylineOptions = new PolylineOptions()
                    .add(current.getPosition())
                    .add(previous.getPosition())
                    .color(Color.BLUE);

            Polyline polyline = map.addPolyline(polylineOptions);
            polylines.add(polyline);
        }

        markers.add(current);
    }

    public void closePolygon() throws MyPolygon.NotEnoughPointsException, MyPolygon.PolygonIsSelfIntersectingException {
        if (markers.size() < 3) {
            throw new NotEnoughPointsException();
        }

        if (!isNonSelfIntersecting()) {
            throw new PolygonIsSelfIntersectingException();
        }

        int red = getRandomColor();
        int green = getRandomColor();
        int blue = getRandomColor();

        PolygonOptions polygonOptions = getPolygonOptionsForMarkers(markers)
                .fillColor(Color.argb(51, red, green, blue))
                .strokeWidth(0);

        polygon = map.addPolygon(polygonOptions);

        addCentroidMarker();

        cleanup();
    }

    public void cleanup() {
        for (int i = 0; i < markers.size(); i++) {
            markers.get(i).remove();
        }

        for (int i = 0; i < polylines.size(); i++) {
            polylines.get(i).remove();
        }
    }

    private void addCentroidMarker() {
        CentroidArea centroidArea = new CentroidArea();
        centroidArea.add(getPolygonGeometry());
        Coordinate centroidPoint = centroidArea.getCentroid();
        LatLng centroidPosition = new LatLng(centroidPoint.x, centroidPoint.y);

        double area = SphericalUtil.computeArea(polygon.getPoints()) / 1000;

        MarkerOptions centroidMarkerOptions = new MarkerOptions()
                .position(centroidPosition)
                .title(format.format(area) + " km\u00B2")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
        map.addMarker(centroidMarkerOptions);
    }

    private Geometry getPolygonGeometry() {
        Coordinate[] coordinates = convertMarkersToCoordinates(markers);

        GeometryFactory factory = new GeometryFactory();

        LinearRing ring = new LinearRing(new CoordinateArraySequence(coordinates), factory);

        return new com.vividsolutions.jts.geom.Polygon(ring, null, factory);
    }

    private boolean isNonSelfIntersecting() {
        Geometry geo = getPolygonGeometry();
        return geo.isSimple();
    }

    private Coordinate[] convertMarkersToCoordinates(List<Marker> markers) {
        List<Coordinate> coordinates = new ArrayList<>();
        Coordinate first = null;
        for (int i = 0; i < markers.size(); i++) {

            LatLng pos = this.markers.get(i).getPosition();
            Coordinate coordinate = new Coordinate(pos.latitude, pos.longitude);
            coordinates.add(coordinate);
            if (i == 0) {
                first = coordinate;
            }
        }
        coordinates.add(first);

        return coordinates.toArray(new Coordinate[]{});
    }

    private int getRandomColor() {
        return (int) Math.floor(Math.random() * 255);
    }

    private PolygonOptions getPolygonOptionsForMarkers(List<Marker> markers) {
        PolygonOptions polygonOptions = new PolygonOptions();
        for (int i = 0; i < markers.size(); i++) {
            Marker marker = markers.get(i);
            polygonOptions.add(marker.getPosition());
        }
        return polygonOptions;
    }

    public class PolygonIsSelfIntersectingException extends RuntimeException {
        PolygonIsSelfIntersectingException() {
            super("Polygon is self intersecting, it is forbidden");
        }
    }

    public class NotEnoughPointsException extends RuntimeException {
        NotEnoughPointsException() {
            super("Not enough points in polygon");
        }
    }
}
