package com.example.surreyrestaurantsreport.ui;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * This class implements the ClusterItem class to display the markers on the map.
 * It uses the same functionality as the ClusterItem class without any additional
 * features
 */
public class Marker implements ClusterItem {

    /* CLASS DATA MEMBERS */
    private final LatLng position;
    private final String title;
    private final String snippet;


    /* CLASS MEMBER FUNCTIONS */
    public Marker(double inputLatitude, double inputLongitude, String inputTitle, String inputSnippet) {

        this.position = new LatLng(inputLatitude, inputLongitude);
        this.title = inputTitle;
        this.snippet = inputSnippet;
        return;

    }

    @Override
    public LatLng getPosition() {
        return this.position;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getSnippet() {
        return this.snippet;
    }

}
