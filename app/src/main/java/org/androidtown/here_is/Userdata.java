package org.androidtown.here_is;

/**
 * Created by MIN on 2017-10-27.
 */

public class Userdata {
    private String name;
    private Double latitude;
    private Double longitude;

    Userdata()
    {

    }

    Userdata(String name, Double lat,Double lng)
    {
        this.name = name;
        latitude = lat;
        longitude= lng;
    }
    String getName()
    {
        return name;
    }
    Double getLat()
    {
        return latitude;
    }
    Double getLng()
    {
        return longitude;
    }

}
