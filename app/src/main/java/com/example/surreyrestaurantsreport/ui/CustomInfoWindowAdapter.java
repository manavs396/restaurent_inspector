package com.example.surreyrestaurantsreport.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.surreyrestaurantsreport.R;
import com.example.surreyrestaurantsreport.model.Inspection;
import com.example.surreyrestaurantsreport.model.ResList;
import com.example.surreyrestaurantsreport.model.Restaurant;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

/**
 * This class makes a custom information windows / display block to be shown on the
 * map when a restaurant is clicked. It shows the restaurant's name, address and the
 * most recent hazard level using the same standardized application hazard icons.
 */
public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    /* CLASS DATA MEMBERS */
    private final View window;
    private Context context;
    private ResList restaurantManager;


    /* CLASS MEMBER FUNCTIONS */
    public CustomInfoWindowAdapter(Context inputContext) {

        this.context = inputContext;
        this.window = LayoutInflater.from(inputContext).inflate(R.layout.custom_map_restaurant_popup, null);
        this.restaurantManager = ResList.getInstance_withcontext(context);

    }

    private void generateWindow(Marker inputMarker, View inputView) {

        String markerTitle = inputMarker.getTitle();
        TextView windowTitle = inputView.findViewById(R.id.mapRestaurantPopup_title);

        if (!markerTitle.equals("")) {
            windowTitle.setText(markerTitle);
        }

        String markerSnippet = inputMarker.getSnippet();
        TextView windowSnippet = inputView.findViewById(R.id.mapRestaurantPopup_snippet);

        if (!markerSnippet.equals("")) {
            windowSnippet.setText(markerSnippet);
        }

        ImageView windowHazardIcon = inputView.findViewById(R.id.mapRestaurantPopup_hazardIcon);
        windowHazardIcon.setImageResource(R.drawable.low_risk);

        Restaurant currentRestaurant = this.restaurantManager.getRestaurantByLatLngAndName(inputMarker.getPosition(), inputMarker.getTitle());
        List<Inspection> inspectionsForCurrentRestaurant = currentRestaurant.getInspectionsList();

        if (inspectionsForCurrentRestaurant.size() != 0) {
            String latestInspectionHazardRating = currentRestaurant.getLatest().getHazard_rating();

            if (latestInspectionHazardRating.equalsIgnoreCase("High")) {
                windowHazardIcon.setImageResource(R.drawable.high_risk);
            } else if (latestInspectionHazardRating.equalsIgnoreCase("Moderate")) {
                windowHazardIcon.setImageResource(R.drawable.mid_risk);
            } else if (latestInspectionHazardRating.equalsIgnoreCase("Low")) {
                windowHazardIcon.setImageResource(R.drawable.low_risk);
            }

        }
    }

    @Override
    public View getInfoWindow(Marker inputMarker) {
        this.generateWindow(inputMarker, this.window);

        return this.window;

    }

    @Override
    public View getInfoContents(Marker inputMarker) {
        this.generateWindow(inputMarker, this.window);

        return this.window;

    }
}
