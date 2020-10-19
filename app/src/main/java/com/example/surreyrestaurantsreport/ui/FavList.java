package com.example.surreyrestaurantsreport.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.surreyrestaurantsreport.R;
import com.example.surreyrestaurantsreport.model.ResList;
import com.example.surreyrestaurantsreport.model.Restaurant;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for showing favorite restaurant with updates in a listView
 */

public class FavList extends AppCompatActivity {

    static List<Restaurant> favRestaurantList = new ArrayList<>();
    static List<String> favResTrackNumList = new ArrayList<>();
    static ResList restaurantManager = ResList.getInstance();
    private static final String TAG = "FavList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fav_restaurant_list);

        Button gotoButton = findViewById(R.id.fav_gotomap);
        gotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent (getApplicationContext(), MainActivity.class);
                startActivity(i);
            }
        });

        getFavResList();
        populateListView();

    }

    private void getFavResList() {
        for (String trackNum: favResTrackNumList){
            for (Restaurant r: restaurantManager.getRestaurants()){
                if (trackNum.equals(r.getTracking_number())){
                    favRestaurantList.add(r);
                }
            }
        }
        Log.d(TAG, "getFavResList: favRestaurantList: "+favRestaurantList);
    }

    //    populate List of Restaurants
    private void populateListView() {
        ArrayAdapter<Restaurant> adapter = new FavList.MyListAdapter();
        ListView list = findViewById(R.id.fav_list);
        list.setAdapter(adapter);
    }


    //    ListView adapter build
    private class MyListAdapter extends ArrayAdapter<Restaurant> {
        public MyListAdapter(){
            super(FavList.this, R.layout.listview_each_fav_restaurant, favRestaurantList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView==null){
                itemView = getLayoutInflater().inflate(R.layout.listview_each_fav_restaurant, parent, false);
            }

            Restaurant curr_r = favRestaurantList.get(position);
            TextView name = itemView.findViewById(R.id.fav_restaurant_name);
            name.setText(curr_r.getName());

            TextView recentIns = itemView.findViewById(R.id.fav_latestInspection);
            String display;
            if (curr_r.getListSize() != 0) {
                String inspectionDate = curr_r.getLatest().getFormattedDate(FavList.this);
                display = String.format(
                        getResources().getString(R.string.restaurant_inspectionPerformedOn),
                        inspectionDate);
            } else {
                display = String.format(
                        getResources().getString(R.string.restaurant_inspectionPerformedOn),
                        (getResources().getString(R.string.restaurant_noInspectionAvailable_value)));
            }
            recentIns.setText(display);

            TextView hazard = itemView.findViewById(R.id.fav_hazard_level);
            ImageView hazard_icon = itemView.findViewById(R.id.fav_hazard_icon);
            if (curr_r.getListSize() != 0) {

                String latestInspectionHazardRating = curr_r.getLatest().getHazard_rating();

                if (latestInspectionHazardRating.equalsIgnoreCase("High")) {
                    hazard_icon.setImageResource(R.drawable.high_risk);
                    itemView.setBackground(getDrawable(R.drawable.high_back));
                    display = String.format(getResources().getString(R.string.restaurant_hazardLevel),
                            getResources().getString(R.string.restaurant_hazardLevelHigh_value));
                } else if (latestInspectionHazardRating.equalsIgnoreCase("Moderate")) {
                    hazard_icon.setImageResource(R.drawable.mid_risk);
                    itemView.setBackground(getDrawable(R.drawable.mid_back));
                    display = String.format(getResources().getString(R.string.restaurant_hazardLevel),
                            getResources().getString(R.string.restaurant_hazardLevelModerate_value));
                } else {
                    hazard_icon.setImageResource(R.drawable.low_risk);
                    itemView.setBackground(getDrawable(R.drawable.low_back));
                    display = String.format(getResources().getString(R.string.restaurant_hazardLevel),
                            getResources().getString(R.string.restaurant_hazardLevelLow_value));
                }
            }
//            no inspection found
            else {
                hazard_icon.setImageResource(R.drawable.low_risk);
                itemView.setBackgroundColor(0);
                display = String.format(getResources().getString(R.string.restaurant_hazardLevel),
                        getResources().getString(R.string.restaurant_hazardLevelNo_value));
            }
            hazard.setText(display);


            return itemView;
        }
    }

    public static Intent makeIntent(Context c, List<String> favTrackNumList){
        Intent i = new Intent (c, FavList.class);
        favResTrackNumList = favTrackNumList;
        return i;
    }

}

