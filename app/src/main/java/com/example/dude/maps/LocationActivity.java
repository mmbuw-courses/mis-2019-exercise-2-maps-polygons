package com.example.dude.maps;

import android.location.Location;
import android.os.Bundle;

interface LocationActivity {


    void onLocationChanged(Location location);

    void onStatusChanged(String s, int i, Bundle bundle);

    void onProviderEnabled(String s);

    void onProviderDisabled(String s);
}
