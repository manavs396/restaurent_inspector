package com.example.surreyrestaurantsreport.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.surreyrestaurantsreport.R;
import com.example.surreyrestaurantsreport.model.Inspection;
import com.example.surreyrestaurantsreport.model.ResList;
import com.example.surreyrestaurantsreport.model.Restaurant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for showing all restaurants (that meets search criteria if applicable) in a listview
 */

public class RestaurantList extends AppCompatActivity{
    ResList restaurantManager = ResList.getInstance();
    ArrayAdapter<Restaurant> adapter;
    TextView title;
    ListView RestaurantList;
    String SharedPref_set_list = "List";
    String default_list = "no favorite restaurant";
    String SP_list_favorite = "SP favorite restaurant";
    List<String> favoriteResTrackNumList = new ArrayList<>();
    private static final String TAG = "RestaurantList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        title = findViewById(R.id.surrey_restaurant_list);
        RestaurantList = findViewById(R.id.restaurant_list);
        title.setText(getResources().getString(R.string.surrey_restaurant_list));

        favoriteResTrackNumList = getFavoriteResTrackNumList();

        wireButtons();
        populateListView();
    }

    private void wireButtons() {
        Button goToMap = findViewById(R.id.gotomap);
        goToMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            }
        });
        Button infoButton = findViewById(R.id.info_button);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go = info.makeIntent(com.example.surreyrestaurantsreport.ui.RestaurantList.this);
                startActivity(go);
            }
        });
    }

    //    populate List of Restaurants
    private void populateListView() {
        adapter = new MyListAdapter();
        RestaurantList.setAdapter(adapter);
        RestaurantList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent restaurantInformationActivityIntent = single_restaurant.makeIntent(getApplicationContext(), position);
                startActivity(restaurantInformationActivityIntent);
            }
        });
    }

    //    ListView adapter build
    private class MyListAdapter extends ArrayAdapter<Restaurant>{
        public MyListAdapter(){
            super(RestaurantList.this, R.layout.listview_each_restaurant, restaurantManager.getRestaurants());
        }
        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            View restaurantView = convertView;

            if (restaurantView == null) {
                restaurantView = getLayoutInflater().inflate(R.layout.listview_each_restaurant, parent, false);
            }

            final Restaurant current_r = restaurantManager.get(position);

            ImageView restaurantLogo = restaurantView.findViewById(R.id.restaurant_image_icon);
            String resourceId = current_r.getRes_id();

            int resId = RestaurantList.this.getResources().getIdentifier(
                    resourceId,
                    "drawable",
                    RestaurantList.this.getPackageName()
            );
            if (resId == 0){
                restaurantLogo.setImageResource(R.drawable.restaurant_logo);
            }
            else{
                restaurantLogo.setImageResource(resId);
            }

            TextView restaurantnameField = restaurantView.findViewById(R.id.restaurant_label_name);
            restaurantnameField.setText(current_r.getName());
            restaurantnameField.setSelected(true);

            TextView restaurantCityField = restaurantView.findViewById(R.id.restaurant_city);
            restaurantCityField.setText(current_r.getPhysicality());

            TextView restaurantAddressField = restaurantView.findViewById(R.id.restaurant_address);
            restaurantAddressField.setText(current_r.getPhysical_address());

//            set favorite icon according to singleton data
            final ImageView favorite = restaurantView.findViewById(R.id.favorite);

//            if the list contains the name
//            if (favoriteRestaurantNameList.contains(current_r.getName())){
//                favorite.setImageResource(android.R.drawable.btn_star_big_on);
//            }else{
//                favorite.setImageResource(android.R.drawable.btn_star_big_off);
//            }

//            check the boolean value in singleton
            if (current_r.isFavorite_clicked()){
                favorite.setImageResource(android.R.drawable.btn_star_big_on);
            }else{
                favorite.setImageResource(android.R.drawable.btn_star_big_off);
            }

//            set onClick listener to favorite icon and set favorite boolean value in restaurant
            favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    if restaurant is favorite
                    if(!current_r.isFavorite_clicked()) {
                        favorite.setImageResource(android.R.drawable.btn_star_big_on);
                        current_r.setFavorite_clicked(true);

                        favoriteResTrackNumList.add(current_r.getTracking_number());

                    } else{
                        favorite.setImageResource(android.R.drawable.btn_star_big_off);
                        current_r.setFavorite_clicked(false);

                        favoriteResTrackNumList.remove(current_r.getTracking_number());

                    }
                    storeFavoriteList(favoriteResTrackNumList);
                }
            });

            Inspection latestInspection = current_r.getLatest();
            int temp_inspection = 0;
            if (latestInspection != null) {
                temp_inspection = latestInspection.getNum_critical() + latestInspection.getNum_noncritical();
            }

            String display;
            TextView problemsFoundForCurrentRestaurantField = restaurantView.findViewById(R.id.restaurant_problemsFound);
            display = String.format(getResources().getString(R.string.restaurant_issuesFound),
                    temp_inspection);
            problemsFoundForCurrentRestaurantField.setText(display);

            ImageView hazardLevelImage = restaurantView.findViewById(R.id.restaurant_image_hazardLevelValue);
            TextView leveltxt = restaurantView.findViewById(R.id.restaurant_hazardLevel);
            if (current_r.getListSize() != 0) {

                String latestInspectionHazardRating = current_r.getLatest().getHazard_rating();

                if (latestInspectionHazardRating.equalsIgnoreCase("High")) {
                    hazardLevelImage.setImageResource(R.drawable.high_risk);
                    restaurantView.setBackground(getDrawable(R.drawable.high_back));
                    display = String.format(getResources().getString(R.string.restaurant_hazardLevel),
                            getResources().getString(R.string.restaurant_hazardLevelHigh_value));
                } else if (latestInspectionHazardRating.equalsIgnoreCase("Moderate")) {
                    hazardLevelImage.setImageResource(R.drawable.mid_risk);
                    restaurantView.setBackground(getDrawable(R.drawable.mid_back));
                    display = String.format(getResources().getString(R.string.restaurant_hazardLevel),
                            getResources().getString(R.string.restaurant_hazardLevelModerate_value));
                } else {
                    hazardLevelImage.setImageResource(R.drawable.low_risk);
                    restaurantView.setBackground(getDrawable(R.drawable.low_back));
                    display = String.format(getResources().getString(R.string.restaurant_hazardLevel),
                            getResources().getString(R.string.restaurant_hazardLevelLow_value));
                }
            }
//            no inspection found
            else {
                hazardLevelImage.setImageResource(R.drawable.low_risk);
                restaurantView.setBackgroundColor(0);
                display = String.format(getResources().getString(R.string.restaurant_hazardLevel),
                        getResources().getString(R.string.restaurant_hazardLevelNo_value));
            }
            leveltxt.setText(display);

            TextView latestInspectionTimeField =
                    restaurantView.findViewById(R.id.restaurant_label_latestInspection);
            if (current_r.getListSize() != 0) {
                String inspectionDate = current_r.getLatest().getFormattedDate(RestaurantList.this);

                display = String.format(
                        getResources().getString(R.string.restaurant_inspectionPerformedOn),
                        inspectionDate);
                latestInspectionTimeField.setText(display);
            } else
                display = String.format(
                        getResources().getString(R.string.restaurant_inspectionPerformedOn),
                        (getResources().getString(R.string.restaurant_noInspectionAvailable_value)));
                latestInspectionTimeField.setText(display);

            return restaurantView;
        }
    }

    private List<String> getFavoriteResTrackNumList(){
        SharedPreferences prefs = getSharedPreferences(SharedPref_set_list, MODE_PRIVATE);
        String favRestaurant = prefs.getString(SP_list_favorite, default_list);
        BufferedReader reader;
        reader = new BufferedReader(new StringReader(favRestaurant));
        String line;
        try {
            while((line = reader.readLine()) != null){
                favoriteResTrackNumList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Restaurant r: restaurantManager.getRestaurants()){
            if (favoriteResTrackNumList.contains(r.getTracking_number())){
                r.setFavorite_clicked(true);
            }
        }
        return favoriteResTrackNumList;
    }

    private void storeFavoriteList(List<String> data){
        StringBuilder dataString = new StringBuilder();
        if (data==null){
            return;
        }
        SharedPreferences prefs = getSharedPreferences(SharedPref_set_list, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        for (String s: data) {
            dataString.append(s).append("\n");
        }
        editor.putString(SP_list_favorite, dataString.toString());
        editor.commit();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

}