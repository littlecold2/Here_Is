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
    String type;

    PlaceData(String name, String address, LatLng location,String type)
    {
        this.location =location;
        this.name = name;
        this.address = address;
        this.type = type;
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

    public String getType() {
        return type;
    }
}
