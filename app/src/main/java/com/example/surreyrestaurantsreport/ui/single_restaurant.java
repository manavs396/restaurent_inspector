package com.example.surreyrestaurantsreport.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Displays information of a single restaurant, including name, address, gps coords
 * and a list of all inspections
 */

public class single_restaurant extends AppCompatActivity {

    private static int selectedRestaurantIndex;
    private static boolean activityThroughMap;
    private static LatLng selectedRestaurantLatLng;
    private static String selectedRestaurantName;
    private static LatLng restaurantLatLngByMap;
    private Restaurant r;
    private ResList restaurantManager;
    private List<Inspection> inspections;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_restaurant);
        Toolbar toolbar = findViewById(R.id.res_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getDrawable(R.drawable.ic_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        restaurantManager = ResList.getInstance();
        // if a name is not passed, go by index
        if (selectedRestaurantName == null){
            r = restaurantManager.get(selectedRestaurantIndex);
        }
        // if a name is passed, go by name
        else{
            r = restaurantManager.getRestaurantByLatLngAndName(selectedRestaurantLatLng, selectedRestaurantName);
        }

        inspections = r.getInspectionsList();

        // reset name to avoid affecting next search
        selectedRestaurantName = null;

        // setup text view about current Restaurant
        setRestaurantInfo();

        //setup listview
        if (inspections.size() > 0){
            populateListView();
        }

    }

    private void setRestaurantInfo(){
        String name = r.getName();
        String address = r.getPhysical_address();
        String city = r.getPhysicality();
        String GPS = r.getGPS();

        TextView r_name = findViewById(R.id.resName);
        r_name.setText(name);

        TextView r_info = findViewById(R.id.resDetail);
        String info = String.format(this.getString(R.string.restaurant_info), address, city, GPS);
        r_info.setText(info);

        r_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MainActivity.makeIntent(single_restaurant.this);
                intent.putExtra("tag", 1);
                intent.putExtra("latlng", r.getLatLng());
                intent.putExtra("title", r.getName());
                startActivity(intent);
            }
        });
    }

    private void populateListView(){
        ArrayAdapter<Inspection> adapter = new MyListAdapter();
        ListView list = findViewById(R.id.insList);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent inspection_Intent = single_inspection.makeIntent(single_restaurant.this, selectedRestaurantIndex, position);
                startActivity(inspection_Intent);

            }
        });
    }

    private class MyListAdapter extends ArrayAdapter<Inspection>{
        public MyListAdapter() {
            super(single_restaurant.this, R.layout.listview_each_inspection, inspections);
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View resView = convertView;
            if (resView == null){
                resView = getLayoutInflater().inflate(R.layout.listview_each_inspection, parent,false);
            }

//            find the inspection to work with
            Inspection curr = inspections.get(position);
            String rate = curr.getHazard_rating();
            int num_critical = curr.getNum_critical();
            int num_noncritical = curr.getNum_noncritical();
            String display;

//            set icon to its hazard level
            ImageView icon = resView.findViewById(R.id.ins_icon);
            if (rate.equalsIgnoreCase("High" )){
                icon.setImageResource(R.drawable.high_risk);
                resView.setBackground(getDrawable(R.drawable.high_back));
                rate = getResources().getString(R.string.restaurant_hazardLevelHigh_value);
            }
            else if (rate.equalsIgnoreCase("Moderate" )){
                icon.setImageResource(R.drawable.mid_risk);
                resView.setBackground(getDrawable(R.drawable.mid_back));
                rate = getResources().getString(R.string.restaurant_hazardLevelModerate_value);
            }
            else {
                icon.setImageResource(R.drawable.low_risk);
                resView.setBackground(getDrawable(R.drawable.low_back));
                rate = getResources().getString(R.string.restaurant_hazardLevelLow_value);
            }
//            date
            TextView dateText = resView.findViewById(R.id.date);

            String date = curr.getFormattedDate(single_restaurant.this);
            dateText.setText(date);

//            hazard level
            TextView hazardText = resView.findViewById(R.id.harzard);
            display = String.format(
                    getResources().getString(R.string.restaurant_hazardLevel), rate);
            hazardText.setText(display);

//            num critical
            TextView criticalText = resView.findViewById(R.id.critical);
            display = String.format(
                    getResources().getString(R.string.inspection_num_critical), num_critical);
            criticalText.setText(display);

//            num noncritical
            TextView noncriticalText = resView.findViewById(R.id.non_critical);
            display = String.format(
                    getResources().getString(R.string.inspection_num_noncritical), num_noncritical);
            noncriticalText.setText(display);

            return resView;
        }
    }

    public static Intent makeIntentByLat(Context inputContext, LatLng inputLatLng) {

        Intent intent = new Intent(inputContext, single_restaurant.class);
        restaurantLatLngByMap = inputLatLng;

        return intent;

    }

    public static Intent makeIntent(Context inputContext, LatLng inputRestaurantLatLng, String inputRestaurantName) {

        activityThroughMap = true;
        Intent intent = new Intent(inputContext, single_restaurant.class);
        selectedRestaurantLatLng = inputRestaurantLatLng;
        selectedRestaurantName = inputRestaurantName;
        return intent;

    }

    public static Intent makeIntent(Context inputContext, int restaurantIndex) {
        Intent intent = new Intent(inputContext, single_restaurant.class);
        selectedRestaurantIndex = restaurantIndex;
        return intent;
    }

}