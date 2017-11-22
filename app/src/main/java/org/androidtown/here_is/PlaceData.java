package org.androidtown.here_is;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Pig on 2017-11-23.
 */

public class PlaceData {

    LatLng location;
    String name;
    String address;
    //String icon;

    PlaceData(String name, String address, LatLng location)
    {
        this.location =location;
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public LatLng getLocation() {
        return location;
    }
}
