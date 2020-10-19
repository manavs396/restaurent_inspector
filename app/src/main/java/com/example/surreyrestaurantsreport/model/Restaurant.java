package com.example.surreyrestaurantsreport.model;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Class that holds information of a single restaurant
 */
public class Restaurant implements Comparable<Restaurant>{
    private String tracking_number;
    private String name;
    private String physical_address;
    private String physicality;
    private String fac_type;
    private String res_id;
    private Double latitude;
    private Double longitude;
    private LatLng latLng;
    private List<Inspection> inspections;
    private boolean favorite_clicked;

    public boolean isFavorite_clicked() {
        return favorite_clicked;
    }

    public void setFavorite_clicked(boolean favorite_clicked) {
        this.favorite_clicked = favorite_clicked;
    }



    public Restaurant() {
        this.inspections = new ArrayList<>();
    }

    public String getTracking_number() {
        return tracking_number;
    }

    public void setTracking_number(String tracking_number) {
        this.tracking_number = tracking_number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {

        this.name = name;
        IconLib icon_lib = IconLib.getInstance();
        String[] res_ids = icon_lib.getLibrary();
        String modifiedName = name.replaceAll("'|-|,|\\s", "")
                .replace("&", "and");

        for (String r: res_ids){
            if (modifiedName.contains(r)){
                res_id = r.toLowerCase().replaceAll("[0-9]","");
                return;
            }
        }

        res_id = "generic";
    }

    public String getRes_id() { return res_id; }

    public String getPhysical_address() {
        return physical_address;
    }

    public void setPhysical_address(String physical_address) {
        this.physical_address = physical_address;
    }

    public String getPhysicality() {
        return physicality;
    }

    public void setPhysicality(String physicality) {
        this.physicality = physicality;
    }

    public String getFac_type() {
        return fac_type;
    }

    public void setFac_type(String fac_type) {
        this.fac_type = fac_type;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatLng(double x, double y) {
        this.latLng = new LatLng(x, y);
    }

    public LatLng getLatLng() {

        return latLng;

    }


    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void addInspection(Inspection ins) {
        inspections.add(ins);
        Collections.sort(inspections, Collections.<Inspection>reverseOrder());
    }
    public List<Inspection> getInspectionsList(){
        return inspections;
    }

    public int getListSize() {
        return inspections.size();
    }

    public String getGPS(){
        String lat_s = String.format(Locale.CANADA, "%.4f", latitude);
        String lon_s = String.format(Locale.CANADA, "%.4f", longitude);
        return "(" + lat_s + ", " + lon_s + ")";
    }

    public Inspection getInspection(int id) {
        return inspections.get(id);
    }

    public Inspection getLatest() {
        if (inspections.size() == 0)
            return null;
        return inspections.get(0);
    }

    @Override
    @NonNull
    public String toString() {
        return "Restaurant{" +
                "tracking_number='" + tracking_number + '\'' +
                ", name='" + name + '\'' +
                ", physical_address='" + physical_address + '\'' +
                ", physicality='" + physicality + '\'' +
                ", fac_type='" + fac_type + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    public int compareTo(Restaurant o) {
        return this.getName().compareTo(o.getName());
    }
}
