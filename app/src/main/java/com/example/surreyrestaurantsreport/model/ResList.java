package com.example.surreyrestaurantsreport.model;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Singleton class for holding all restaurant once they are read
 */

public class ResList {
    private List<Restaurant> restaurants;
    private static Context temp1;
    private Set<String> favouriteRestaurantTrackingIDs;

    private ResList() {
        this.restaurants = new ArrayList<>();
        favouriteRestaurantTrackingIDs = new HashSet<>();
    }

    private static ResList instance;
    public static ResList getInstance() {
        if (instance == null) {
            instance = new ResList();
        }
        return instance;
    }

    public static ResList getInstance_withcontext(Context temp) {
        if (instance == null) {
            instance = new ResList();
        }
        temp1 = temp;
        return instance;
    }

    public void clear(){
        this.restaurants = new ArrayList<>();
    }
    public String getName(int id) {
        return restaurants.get(id).getName();
    }

    public String getAddress(int id) {
        return restaurants.get(id).getPhysical_address();
    }

    public double getLatitude(int id){
        return restaurants.get(id).getLatitude();
    }

    public double getLongitude(int id) {
        return restaurants.get(id).getLongitude();
    }

    public List<Restaurant> getRestaurants(){
        return restaurants;
    }

    public Restaurant get(int id){
        return restaurants.get(id);
    }

    public void addRestaurant(Restaurant r){
        restaurants.add(r);
    }

    public void sortRestaurant(){
        Collections.sort(restaurants);
    }

    public Restaurant getRestaurantByTrackingNumber(String Tracking){
        for (Restaurant r : restaurants){
            if(r.getTracking_number().equals(Tracking)){
                return r;
            }
        }
        return null;
    }


    public Restaurant getRestaurantByIndex(int inputIndex) {
        return this.restaurants.get(inputIndex);
    }

    public boolean isFavouriteRestaurant(String trackingNumber) {
        return favouriteRestaurantTrackingIDs.contains(trackingNumber);
    }


    public List<Restaurant> getListOfRestaurants() {
        return Collections.unmodifiableList(restaurants);
    }

    public void updateFavouritesData(Restaurant restaurant, boolean isFavourite) {
        String trackingID = restaurant.getTracking_number();
        String localFavouritesLocation = "favourites.csv";
        favouriteRestaurantTrackingIDs.remove(trackingID);
        if (isFavourite) {
            favouriteRestaurantTrackingIDs.add(trackingID);
        }

        try {
            FileOutputStream fileOutputStream = temp1.openFileOutput(localFavouritesLocation, Context.MODE_PRIVATE);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
            for (String id : favouriteRestaurantTrackingIDs) {
                bufferedWriter.write(id);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Restaurant> getFavouriteRestaurants() {
        List<Restaurant> favouriteRestaurants = new ArrayList<>();
        for (String trackingID : favouriteRestaurantTrackingIDs) {
            for (Restaurant restaurant : restaurants) {
                if (restaurant.getTracking_number().equals(trackingID)) {
                    favouriteRestaurants.add(restaurant);
                    break;
                }
            }
        }
        return favouriteRestaurants;
    }


    public Restaurant getRestaurantByLatLngAndName(LatLng inputLatLng, String inputRestaurantName) {

        for (int i = 0; i < this.restaurants.size(); i++) {

            Restaurant currentRestaurant = this.restaurants.get(i);
            if (currentRestaurant.getLatLng().equals(inputLatLng) && currentRestaurant.getName().equals(inputRestaurantName)) {
                return this.restaurants.get(i);
            }

        }

        return null;
    }


    public int getSizeOfRestaurants() {
        return this.restaurants.size();
    }


}
