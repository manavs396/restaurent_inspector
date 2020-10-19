package com.example.surreyrestaurantsreport.ui;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.surreyrestaurantsreport.model.Inspection;
import com.example.surreyrestaurantsreport.model.ResList;
import com.example.surreyrestaurantsreport.model.Restaurant;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.Collection;
import java.util.List;

/**
 * MarkerClusterRenderer is to make a custom marker cluster
 * This class make markers different colors by each restaurant's recent inspection hazard level
 * If there are more than 1 restaurant in a place, it will be a cluster shown the number of restaurant
 */
public class MarkerClusterRenderer extends DefaultClusterRenderer<Marker> {

    /* CLASS DATA MEMBERS */
    private static final int MARKER_DIMENSION = 48;
    private final Context context;
    private final ImageView markerIcon;
    private final IconGenerator iconGenerator;
    private ResList restaurantManager;

    /* CLASS MEMBER FUNCTIONS */
    public MarkerClusterRenderer(Context inputContext, GoogleMap inputMap, ClusterManager<Marker> inputClusterManager) {

        super(inputContext, inputMap, inputClusterManager);

        this.context = inputContext;
        this.iconGenerator = new IconGenerator(inputContext);
        this.markerIcon = new ImageView(inputContext);
        this.markerIcon.setLayoutParams(new ViewGroup.LayoutParams(MARKER_DIMENSION, MARKER_DIMENSION));
        iconGenerator.setContentView(markerIcon);

        this.restaurantManager = ResList.getInstance_withcontext(this.context);

        return;

    }

    @Override
    protected void onBeforeClusterItemRendered(Marker inputItem, MarkerOptions inputMarkerOptions) {

        inputMarkerOptions.title(inputItem.getTitle());
        inputMarkerOptions.snippet(inputItem.getSnippet());
        inputMarkerOptions.position(inputItem.getPosition());

        Restaurant currentRestaurant = this.restaurantManager.getRestaurantByLatLngAndName(inputItem.getPosition(),inputItem.getTitle());
        List<Inspection> inspectionsForCurrentRestaurant = currentRestaurant.getInspectionsList();
        if (inspectionsForCurrentRestaurant.size() != 0) {

            String latestInspectionHazardRating = currentRestaurant.getLatest().getHazard_rating();
            if (latestInspectionHazardRating.equalsIgnoreCase("High")) {
                inputMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            } else if (latestInspectionHazardRating.equalsIgnoreCase("Moderate")) {
                inputMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            } else if (latestInspectionHazardRating.equalsIgnoreCase("Low")) {
                inputMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }

        } else {
            inputMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }

        return;

    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<Marker> inputCluster) {

        return inputCluster.getSize() > 1 && !allDuplicateItemPositions(inputCluster);

    }

    private boolean allDuplicateItemPositions(Cluster<Marker> inputCluster) {

        Collection<Marker> items = inputCluster.getItems();
        Marker firstItem = items.iterator().next();
        LatLng position = firstItem.getPosition();

        for (Marker item : inputCluster.getItems()) {
            LatLng curPosition = item.getPosition();
            if (!curPosition.equals(position)) {
                return false;
            }
        }
        return true;

    }

}
