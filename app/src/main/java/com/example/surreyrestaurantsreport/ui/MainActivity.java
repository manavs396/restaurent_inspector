package com.example.surreyrestaurantsreport.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.surreyrestaurantsreport.R;
import com.example.surreyrestaurantsreport.model.Inspection;
import com.example.surreyrestaurantsreport.model.ResList;
import com.example.surreyrestaurantsreport.model.Restaurant;
import com.example.surreyrestaurantsreport.model.VioLib;
import com.example.surreyrestaurantsreport.model.Violation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.ClusterManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * The class is for map activity, and it is the default activity when running the app.
 * When user run the app, the default view would be his location
 * User can get his location by press the gps button, and also can find a restaurant in map by a list of restaurant
 * User is also able to click the marker to see the simple information about the activity. or click the information window to see more detail
 * The map would update the device's location every 3 min , and move camera to user's current location
 */
public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final float DEFAULT_ZOOM = 15f;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private ClusterManager<Marker> clusterManager;
    private MarkerClusterRenderer markerClusterRenderer;
    private List<Marker> markerList = new ArrayList<>();
    private List<Inspection> inspectionsBeforeUpdate;

    ResList restaurantManager = ResList.getInstance();

    private static final String TAG = "MainActivity";
    final String SharedPref_set_time = "Time", SharedPref_set_data = "Data";
    final String SP_time_userUpdate = "SP time user", SP_time_restaurant = "SP time restaurant", SP_time_inspection = "SP time inspection";
    final String SP_data_restaurant = "SP data restaurant", SP_data_inspection = "SP data inspection";
    final String default_time = "no time recorded", default_data = "no data stored yet";
    String SharedPref_set_list = "List";
    String default_list = "no favorite restaurant";
    String SP_list_favorite = "SP favorite restaurant";
    String restaurant_format, restaurant_url, restaurant_last_modified;
    String inspection_format, inspection_url, inspection_last_modified;
    String restaurant_list,inspection_list;
    static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    SimpleDateFormat getDate_format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
    SimpleDateFormat GET_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSS");
    boolean dialog_opened = false;
    boolean mLocationPermissionGranted = false;
    boolean got_restaurant_data = false, got_inspection_data = false;
    int restaurant_num = 0, inspection_num = 0;
    static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    Handler mainHandler = new Handler();
    List<String> favoriteResTrackNumList = new ArrayList<>();
    RequestQueue RQueue;
    AlertDialog alert_pleaseWait;

    @Override
    public void onCreate(Bundle inputSavedInstanceState) {
        super.onCreate(inputSavedInstanceState);
        setContentView(R.layout.activity_main);

        getLocationPermission();
        RQueue = Volley.newRequestQueue(this);
        checkForUpdate();
        wireButtons();

        if (restaurantManager.getSizeOfRestaurants()==0) {
            readViolationLib();
            readRestaurantData();
            readInspectionData();
        }
    }

//    will execute once the map is ready (when location permission granted)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, getResources().getString(R.string.map_ready), Toast.LENGTH_SHORT).show();
        mMap = googleMap;
//        get device location once location permission is granted
        if (mLocationPermissionGranted) {

            if (ActivityCompat.checkSelfPermission(this, FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            searchBarSetUp();
            setUpClusters();
            Intent inputData = getIntent();
            if (inputData.getIntExtra("tag", 0) != 0){
                final LatLng restaurantLatLon = inputData.getExtras().getParcelable("latlng");
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(restaurantLatLon, 19.0f));
                Log.d(TAG, "onMapReady: got here");
                final String title = inputData.getStringExtra("title");
                final Marker obj = getMarkerObjByName(title);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        com.google.android.gms.maps.model.Marker m = getMarkerFromObj(obj);
                       if (m != null){
                            m.showInfoWindow();
                        }
                    }
                }, 3000);
            }
            else{
                getDeviceLocation();
            }

        }
    }

//    remove pegs from map
    private void removePegFromMap(Restaurant r) {
        for (Marker m: markerList){
            if (m.getSnippet().equals(r.getPhysical_address())){
                clusterManager.removeItem(m);
                break;
            }
        }
    }

//    ask for permission if have not asked yet
    private void getLocationPermission(){
        String[] permissionsRequired = {FINE_LOCATION, COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Do Nothing As Permission Already Granted
                mLocationPermissionGranted = true;
                initMap();
            }
            else {
                ActivityCompat.requestPermissions(this, permissionsRequired, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        else {
            ActivityCompat.requestPermissions(this, permissionsRequired, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

//    initialize map
    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.restaurantMapFragment_map_1);
        mapFragment.getMapAsync(MainActivity.this);
    }

    //    set up the filter
    private void searchBarSetUp(){
//        set up keyword field
        final EditText editText_keyword = findViewById(R.id.keyword);
        final String[] keyword = {editText_keyword.getText().toString()};
        editText_keyword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER) {
                    keyword[0] = editText_keyword.getText().toString();
                }
                return false;
            }
        });

//        set up hazard level field
        Spinner spinner_hazardLevel = findViewById(R.id.spinner_hazardLevel);
        ArrayAdapter<String> adapter_hazardLevel = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.hazard_list));
        adapter_hazardLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_hazardLevel.setAdapter(adapter_hazardLevel);
        final String[] hazardLevel = new String[1];
        final List<String> hazardList = Arrays.asList(getResources().getStringArray(R.array.hazard_list));
        spinner_hazardLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                hazardLevel[0] = hazardList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        set up critical comparison field (<= / >=)
        Spinner spinner_criticalCom = findViewById(R.id.spinner_criticalVio);
        ArrayAdapter<String> adapter_criticalCom = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.criticalVio_list));
        adapter_criticalCom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_criticalCom.setAdapter(adapter_criticalCom);
        final String[] criticalCompare = new String[1];
        final List<String> criticalList = Arrays.asList(getResources().getStringArray(R.array.criticalVio_list));
        spinner_criticalCom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                criticalCompare[0]=criticalList.get(position);
                Log.d(TAG, "onItemSelected: critical Compare: " + criticalCompare[0]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        set up critical number field (combine with critical comparison field later)
        final EditText editText_criticalNum = findViewById(R.id.criticalNum);
        final String[] criticalNum = {editText_criticalNum.getText().toString()};
        editText_criticalNum.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER) {
                    criticalNum[0] = editText_criticalNum.getText().toString();
                }
                return false;
            }
        });

//        set up favorite field
        Spinner spinner_favorite = findViewById(R.id.spinner_favorite);
        ArrayAdapter<String> adapter_favorite = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.favorite_list));
        adapter_favorite.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_favorite.setAdapter(adapter_favorite);
        final boolean[] favorite = new boolean[1];
        spinner_favorite.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position==1){
                    Toast.makeText(MainActivity.this, "This is working abc", Toast.LENGTH_SHORT).show();
                    favorite[0] = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

//        take results of filter and execute through filterSearch()
        Button search = findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterSearch(keyword[0], hazardLevel[0], criticalCompare[0], criticalNum[0], favorite[0]);
            }
        });
    }

    //    take inputs of filter and filter restaurants
    private void filterSearch(String keyword, String hazardLevel, String criticalCompare, String criticalNum, boolean favorite) {
        List<Restaurant> list = restaurantManager.getRestaurants();
        List<Restaurant> removeList = new ArrayList<>();
        Log.d(TAG, "name for search: "+ keyword);
//        if keyword field is not empty, then check through restaurants if the name contains the keyword
        if (!keyword.equals("")){
            for (Restaurant r: list){
                if (!(r.getName().toLowerCase().contains(keyword))){
                    removeList.add(r);
                }
            }
        }

//        set hazardLevel variable to {Low, Moderate, High} to be used later
        List<String> hazardList = Arrays.asList(getResources().getStringArray(R.array.hazard_list));
        if (hazardLevel.equalsIgnoreCase(hazardList.get(1))){
            hazardLevel = "Low";
        }
        else if (hazardLevel.equalsIgnoreCase(hazardList.get(2))){
            hazardLevel = "Moderate";
        }
        else if (hazardLevel.equalsIgnoreCase(hazardList.get(3))){
            hazardLevel = "High";
        }

//        if hazard level field is not default value, then check the hazard level of latest inspection of every restaurant
        if (!hazardLevel.equals(hazardList.get(0))){
            for (Restaurant r: list){
                Inspection latestIns = r.getLatest();
                if (latestIns!=null) {
                    String latest_hazard = latestIns.getHazard_rating();
                    if (!latest_hazard.equalsIgnoreCase(hazardLevel)) {
                        removeList.add(r);
                    }
                } else{
                    removeList.add(r);
                }
            }
        }


        List<String> criticalList = Arrays.asList(getResources().getStringArray(R.array.criticalVio_list));
//        if critical compare field is not default value and critical num field is not empty
        if (!(criticalCompare.equals(criticalList.get(0)) || criticalNum.equals(""))){
            for (Restaurant r: list){
//                if latest inspection exists
                if (r.getLatest()==null){
//                    add to remove list if input is >= (since does not quality filter automatically)
                    if (criticalCompare.equals(criticalList.get(2))) {
                        Log.d(TAG, "filterSearch: no inspection for this restaurant");
                        removeList.add(r);
                    }
                    continue;
                }

//                find num of critical inspection from last year
                List<Inspection> insList = r.getInspectionsList();
                int criticalNumTotal=0;
                for (Inspection i: insList){
                    SimpleDateFormat newFormat = new SimpleDateFormat("MMMM dd, yyyy");
                    try {
                        Date insDate = newFormat.parse(i.getFullDate());
                        Date currDate = new Date();
                        long diff = currDate.getTime() - insDate.getTime();
                        diff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                        if (diff<=365){
                            criticalNumTotal+=i.getNum_critical();
                            Log.d(TAG, "checking for dates "+ criticalNumTotal);
                        }else{
                            break;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
//                if input is <=
                if (criticalCompare.equals(criticalList.get(1))) {
                    if (!(criticalNumTotal <= Integer.parseInt(criticalNum))) {
                        removeList.add(r);
                        Log.d(TAG, "filterSearch: critical num in last year: "+ criticalNumTotal);
                    }
//                    if input is >=
                } else if (criticalCompare.equals(criticalList.get(2))) {
                    if (!(criticalNumTotal >= Integer.parseInt(criticalNum))) {
                        removeList.add(r);
                        Log.d(TAG, "filterSearch: critical num in last year: "+ criticalNumTotal);
                    }
                }
            }
        }

//        if favorite filter is selected, add not favorite restaurant to remove list
        if (favorite){
            for (Restaurant r: list){
                if (!r.isFavorite_clicked()){
                    removeList.add(r);
                }
            }
        }
//        remove every restaurant in remove list from map and list singleton
        for (Restaurant r: removeList){
            removePegFromMap(r);
//            to avoid removing restaurants that is already removed
            if (list.remove(r)){

            }
        }

//        reset map and set up cluster again with filtered results
        mMap.clear();
        setUpClusters();
    }

//    check the server for update if time has been > 20 hours
    private void checkForUpdate() {
        String lastUpdate = getLastUpdatedTime(SP_time_userUpdate);
        Date lastUpdateTime;

        Date currentTime = new Date();
        if(have_not_updated()){
            checkRestaurantUrl();
            checkInspectionUrl();
        }else {
            try {
                lastUpdateTime = getDate_format.parse(lastUpdate);
                long time_diff = currentTime.getTime() - lastUpdateTime.getTime();
                time_diff = TimeUnit.MILLISECONDS.convert(time_diff, TimeUnit.MILLISECONDS);
                if (time_diff > 20) {
                    checkRestaurantUrl();
                    checkInspectionUrl();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    }

    private List<Inspection> getLatestInspectionsForRestaurants(List<Restaurant> restaurants) {
        List<Inspection> latestInspections = new ArrayList<>();
        for (Restaurant restaurant : restaurants) {
            String trackingID = restaurant.getTracking_number();
            latestInspections.add(restaurant.getInspection(Integer.parseInt(trackingID)));
        }
        return latestInspections;
    }

    // Helper function to give restraunt list
    private Restaurant getRestaurantInListWithTrackingID(List<Restaurant> restaurants, String trackingID) {
        for (Restaurant restaurant : restaurants) {
            if (restaurant.getTracking_number().equals(trackingID)) {
                return restaurant;
            }
        }
        return null;
    }

    // Helper Function to give inspection list with tracking ID
    private Inspection getInspectionInListWithTrackingID(List<Inspection> inspections, String trackingID) {
        for (Inspection inspection : inspections) {
            if (inspection != null) {
                if (inspection.getTracking_number().equals(trackingID)) {
                    return inspection;
                }
            }
        }
        return null;
    }

    //    get last updated time through Shared Preference
    private String getLastUpdatedTime(String SP_set_item) {
        SharedPreferences prefs = getSharedPreferences(SharedPref_set_time, MODE_PRIVATE);
        String extractedText = prefs.getString(SP_set_item, default_time);
        return extractedText;
    }

    //    store update time in Shared Preference
    private void storeUpdateTIme(String SharedPref_ref){
        Date currentDate = new Date();
        SharedPreferences prefs = getSharedPreferences(SharedPref_set_time, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SharedPref_ref, currentDate.toString());

        editor.commit();
    }

//    check if data has been updated
    private boolean have_not_updated() {
        String last_update = getLastUpdatedTime(SP_time_userUpdate);
        if (last_update.compareTo(default_time) == 0) {
            Log.d("have not updated", "last update = "+last_update);
            return true;
        }
        return false;
    }

//    check restaurant link for any new data
    private void checkRestaurantUrl() {
        String url = "https://data.surrey.ca/api/3/action/package_show?id=restaurants";
//        get JSON data
        JsonObjectRequest request = new JsonObjectRequest
                (com.android.volley.Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject result = response.getJSONObject("result");
                            JSONArray jsonArray = result.getJSONArray("resources");
                            JSONObject resource = jsonArray.getJSONObject(0);
                            restaurant_format = resource.getString("format");
                            restaurant_url = resource.getString("url");
                            restaurant_last_modified = resource.getString("last_modified");
                            if(have_not_updated()){
                                openAskUserForUpdateDialog();
                            } else {
                                String restaurant_last_update = getLastUpdatedTime(SP_time_restaurant);
                                Date restaurant_last_modified_date = null;
                                Date restaurant_last_update_date = null;
                                try {
                                    restaurant_last_modified_date = GET_format.parse(restaurant_last_modified);
                                    restaurant_last_update_date = getDate_format.parse(restaurant_last_update);
//                                  if new data
                                    if (restaurant_last_modified_date.compareTo(restaurant_last_update_date) > 0) {
                                        openAskUserForUpdateDialog();
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        request.setTag(TAG);
        RQueue.add(request);
    }

//    check Inspection link for any new data
    private void checkInspectionUrl() {

        String url = "https://data.surrey.ca/api/3/action/package_show?id=fraser-health-restaurant-inspection-reports";

        JsonObjectRequest request = new JsonObjectRequest
                (com.android.volley.Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject result = response.getJSONObject("result");
                            JSONArray jsonArray = result.getJSONArray("resources");
                            JSONObject resource = jsonArray.getJSONObject(0);
                            inspection_format = resource.getString("format");
                            inspection_url = resource.getString("url");
                            inspection_last_modified = resource.getString("last_modified");

                            if(have_not_updated()){
                                openAskUserForUpdateDialog();
                            } else {
                                String inspection_last_update = getLastUpdatedTime(SP_time_inspection);
                                Date inspection_last_update_date = null;
                                Date inspection_last_modified_date = null;
                                try {
                                    inspection_last_modified_date = GET_format.parse(inspection_last_modified);
                                    inspection_last_update_date = getDate_format.parse(inspection_last_update);
                                    if (inspection_last_modified_date.compareTo(inspection_last_update_date)>0){
                                        openAskUserForUpdateDialog();
                                    }

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        request.setTag(TAG);
        RQueue.add(request);
    }

//    open "ask user for update" dialog
    public void openAskUserForUpdateDialog() {
        if (!dialog_opened) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle(getResources().getString(R.string.update_available));
            alertDialog.setMessage(getResources().getString(R.string.do_you_want_to_get_the_updated_data));
            alertDialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    openPleaseWaitDialog();
                }
            });
            alertDialog.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog alert = alertDialog.create();
            alert.show();
            dialog_opened=true;
        }
    }

//    open "please wait" dialog if user wants to update
    private void openPleaseWaitDialog() {
//        set up loading icon
        RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setInterpolator(new LinearInterpolator());
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setDuration(1200);
        ImageView loading_image = new ImageView(this);
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.loading);
        bmp = Bitmap.createScaledBitmap(bmp,100,100,true);
        loading_image.setImageBitmap(bmp);
        AlertDialog.Builder pleaseWaitDialog = new AlertDialog.Builder(MainActivity.this);
        pleaseWaitDialog.setTitle(getResources().getString(R.string.please_wait));
        loading_image.startAnimation(rotate);
        pleaseWaitDialog.setView(loading_image);

        pleaseWaitDialog.setNegativeButton(getResources().getString(R.string.cancel_download), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onCancelClicked();
            }
        });
//        create alert dialog
        alert_pleaseWait = pleaseWaitDialog.create();
        alert_pleaseWait.show();

//        get data from server
        getRestaurantDataFromServer();
        getInspectionDataFromServer();
//        keep checking if the data is grabbed from server. Update local data once downloading is finished
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                while(true){
                    if(got_restaurant_data && got_inspection_data){
                        break;
                    }
                }
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateLocalData();
                    }
                });
            }
        };
        Thread myThread = new Thread(myRunnable);
        myThread.start();
    }

    //    store UpdateTime and downloaded data to Shared Preferences, and then set up clusters with new data
    private void updateLocalData() {
        //        get data from fav list SP (res with old inspection)
        getFavoriteRestaurantNameList();
        List <Restaurant> ResList = convertNameToResList(favoriteResTrackNumList);

        storeUpdateTIme(SP_time_userUpdate);
        storeUpdateTIme(SP_time_restaurant);
        storeUpdateTIme(SP_time_inspection);
        storeDownloadedData(SP_data_restaurant, restaurant_list);
        storeDownloadedData(SP_data_inspection, inspection_list);
        alert_pleaseWait.dismiss();

        restaurantManager.clear();
        readRestaurantData();
        readInspectionData();
        readViolationLib();

        setUpClusters();
        showFavoriteList(ResList);


    }

    private void showFavoriteList(List<Restaurant> favList) {
        List<Restaurant> ResList = restaurantManager.getRestaurants();
        List<Restaurant> tobeRemoved = new ArrayList<>();
//        contains names of updated restaurant list
        List<String> trackNumList = new ArrayList<>();
        List<String> fav_TrackNumList = new ArrayList<>();
        for (Restaurant r: ResList){
            trackNumList.add(r.getTracking_number());
        }
        boolean newIns = false;

//        check if favorite restaurants have any new inspection
        for(Restaurant r: favList){
            if (trackNumList.contains(r.getTracking_number())){
                Restaurant updatedResData = ResList.get(trackNumList.indexOf(r.getTracking_number()));
//                if the last inspection date are the same, remove it from list to be shown later
                if (r.getLatest().getFullDate().equals(updatedResData.getLatest().getFullDate())){
                    tobeRemoved.add(r);
                }else{
                    newIns=true;
                }
            }
        }
        favList.removeAll(tobeRemoved);
        for (Restaurant r: favList){
            fav_TrackNumList.add(r.getTracking_number());
        }
        if (newIns) {
            Log.d(TAG, "showFavoriteList: fav_TrackNumList: "+fav_TrackNumList);
//        only pass in restaurants with new inspections
            Intent i = FavList.makeIntent(MainActivity.this, fav_TrackNumList);
            startActivity(i);
        }
    }

    private List<Restaurant> convertNameToResList(List<String> trackNumList){
        List<Restaurant>  ResList = new ArrayList<>();
        for (Restaurant r: restaurantManager.getRestaurants()){
            for (String trackNum: trackNumList){
                if (r.getTracking_number().equals(trackNum)){
                    ResList.add(r);
                }
            }
        }
        return ResList;
    }

    //    get restaurant data from server
    private void getRestaurantDataFromServer() {
        OkHttpClient restaurant_client = new OkHttpClient();
        Request restaurant_request = new Request.Builder()
                .url(restaurant_url)
                .build();
        restaurant_client.newCall(restaurant_request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                if (response.isSuccessful()) {
                    restaurant_list = response.body().string();
                    got_restaurant_data = true;
                }
            }
        });
    }

//    get inspection data from server
    private void getInspectionDataFromServer() {
        OkHttpClient inspection_client = new OkHttpClient();
        Request inspection_request = new Request.Builder()
                .url(inspection_url)
                .build();
        inspection_client.newCall(inspection_request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                if (response.isSuccessful()) {
                    inspection_list = response.body().string();
                    got_inspection_data = true;
                }
            }
        });
    }

//    when the cancel button is clicked while downloading
    public void onCancelClicked() {
        super.onStop();
        if (RQueue != null) {
            RQueue.cancelAll(TAG);
        }
    }

//    store downloaded data through Shared Preference
    private void storeDownloadedData(String SP_set_item, String data){
        SharedPreferences prefs = getSharedPreferences(SharedPref_set_data, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SP_set_item, data);
        editor.commit();
    }

//    find the buttons on activity and wire functions to them
    private void wireButtons() {
        ImageView currentLocationIcon = findViewById(R.id.gpsimage);
        currentLocationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                getDeviceLocation();
            }
        });
        ImageView restaurantListIcon = findViewById(R.id.listimage);
        restaurantListIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RestaurantList.class);
                startActivity(intent);
            }
        });

        TextView clearSearch = findViewById(R.id.clear_search);
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText keyword = findViewById(R.id.keyword);
                keyword.setText("");
                Spinner hazard = findViewById(R.id.spinner_hazardLevel);
                hazard.setSelection(0);
                Spinner criticalCom = findViewById(R.id.spinner_criticalVio);
                criticalCom.setSelection(0);
                EditText criticalNum = findViewById(R.id.criticalNum);
                criticalNum.setText("");
                Spinner favorite = findViewById(R.id.spinner_favorite);
                favorite.setSelection(0);
                restaurantManager.clear();
                mMap.clear();
                readRestaurantData();
                readInspectionData();
                readViolationLib();
                setUpClusters();
            }
        });
    }

//    set up cluster group
    private void setUpClusters() {
        clusterManager = new ClusterManager<>(this, mMap);

        addRestaurantsToMap();

        markerClusterRenderer = new MarkerClusterRenderer(this, mMap, clusterManager);
        mMap.setInfoWindowAdapter(clusterManager.getMarkerManager());
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);
        mMap.setOnInfoWindowClickListener(clusterManager);
        clusterManager.setRenderer(markerClusterRenderer);
        clusterManager.getMarkerCollection().setInfoWindowAdapter(new CustomInfoWindowAdapter(this));
        clusterManager.getMarkerCollection().setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(com.google.android.gms.maps.model.Marker marker) {
                LatLng restaurantLatLng = marker.getPosition();
                Intent intent = single_restaurant.makeIntent(MainActivity.this, restaurantLatLng, marker.getTitle());
                startActivity(intent);
            }
        });
    }

//    get restaurant data and add them to map
    private void addRestaurantsToMap() {
        for (Restaurant r: restaurantManager.getRestaurants()) {
            Marker offsetItem = new Marker(
                    r.getLatitude(),
                    r.getLongitude(),
                    r.getName(),
                    r.getPhysical_address());

            clusterManager.addItem(offsetItem);
            markerList.add(offsetItem);
        }
        this.mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(this));
    }

//    find user location
    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "My Location");
                            Log.d(TAG, "onComplete: location: lat: "+currentLocation.getLatitude()+", long: "+currentLocation.getLongitude());

                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                        }
                    }
                });
            }
        }catch(SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: "+e.getMessage());
        }
    }

//    move camera function
    private void moveCamera(LatLng inputLatLng, float inputZoomValue, String inputTitle) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(inputLatLng, inputZoomValue));

        if (!inputTitle.equals("My Location")) {
            MarkerOptions options = new MarkerOptions().position(inputLatLng).title(inputTitle);
            mMap.addMarker(options);
        }
    }

//    read data from Shared Preferences and import to restaurantManager
    private void readRestaurantData() {
        String downloadRestaurant = getDownloadedData(SP_data_restaurant);
        BufferedReader reader;
        if (have_not_download_data()){
            InputStream is = getResources().openRawResource(R.raw.restaurants_itr1);
            reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        } else{
            reader = new BufferedReader(new StringReader(downloadRestaurant));
        }
        String line = "";
        try {
            reader.readLine();
            while((line = reader.readLine()) != null){
                importRestaurantData(line);
            }
        } catch (IOException e) {
            Log.wtf("MyActivity", "Error reading data file from line" + line, e);
            e.printStackTrace();
        }
    }

//    read each line of local data String and import to restaurantManager
    private void importRestaurantData(String line) {
        if(line.length()==4){
            Log.d(TAG, "importRestaurantData: empty line ignored");
            return;
        }
        String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 7);
        Restaurant sample = new Restaurant();
        sample.setTracking_number(tokens[0]);
        sample.setName(tokens[1].replace("\"", ""));
        sample.setPhysical_address(tokens[2]);
        sample.setPhysicality(tokens[3]);
        sample.setFac_type(tokens[4]);
        if (tokens[5].length() > 0) {
            sample.setLatitude(Double.valueOf(tokens[5]));
        } else {
            sample.setLatitude(0.0);
        }
        if (tokens[6].length() > 0) {
            sample.setLongitude(Double.valueOf(tokens[6]));
        } else {
            sample.setLongitude(0.0);
        }
        sample.setLatLng(Double.valueOf(tokens[5]), Double.valueOf(tokens[6]));
        restaurantManager.addRestaurant(sample);
        restaurantManager.sortRestaurant();
        restaurant_num++;
    }

//    read data from Shared Preferences and import to restaurantManager
    private void readViolationLib() {
        InputStream is = getResources().openRawResource(R.raw.allviolations);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8)
        );
        VioLib violation_lib = VioLib.getInstance();
        String line = "";
        try {
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                importViolationData(violation_lib, line);
            }
        } catch (IOException e) {
            Log.wtf("MyActivity", "Error reading data file from line" + line, e);
            e.printStackTrace();
        }
    }

//    read each line of local data String and import to restaurantManager
    private void importViolationData(VioLib violation_lib, String line) {
        String[] tokens = line.split(",");
        Violation sample = new Violation();
        sample.setVio_num(Integer.parseInt(tokens[0]));
        sample.setCritical(tokens[1]);
        sample.setNature(tokens[2]);
        sample.setShort_des(tokens[3]);
        sample.setFull_des(tokens[4]);
        violation_lib.add(sample);
    }

//    read data from Shared Preferences and import to restaurantManager
    private void readInspectionData() {
        String downloadedInspection = getDownloadedData(SP_data_inspection);
        BufferedReader reader;
        if (have_not_download_data()){
            InputStream is = getResources().openRawResource(R.raw.inspectionreports_itr1);
            reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        } else{
            reader = new BufferedReader(new StringReader(downloadedInspection));
        }
        String line = "";
        String last_track = "";
        Restaurant r = new Restaurant();
        try {
            reader.readLine();
            while((line = reader.readLine()) != null){
                importInspectionData(r,line,last_track);
            }
        } catch (IOException e) {
            Log.wtf("MyActivity", "Error reading data file from line" + line, e);
            e.printStackTrace();
        }

    }

//    import inspection data to restaurantManager
    private void importInspectionData(Restaurant r, String line, String last_track) {
        if(line.length()==4 || line.length()==6){
            Log.d(TAG, "importInspectionData: empty line ignored");
            return;
        }
        String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
//        Create new empty inspection
        Inspection sample = new Inspection();
//        remove extra char in data
        for (int i=0; i<tokens.length; i++){
            tokens[i]=tokens[i].replaceAll("^\"|\"$", "");
        }

//        set tracking number
        if (tokens[0].length() > 0) {
            sample.setTracking_number(tokens[0]);
            if (! (tokens[0].equals(last_track))){
                r = restaurantManager.getRestaurantByTrackingNumber(tokens[0]);
            }
        } else {
            sample.setTracking_number("");
        }

//        set inspection date
        if (tokens[1].length() > 0) {
            sample.setInspection_date(Integer.parseInt(tokens[1]));
        } else {
            sample.setInspection_date(0);
        }

//        set inspection type
        sample.setInsp_type(tokens[2]);
//        set critical Num
        if (tokens[3].length() > 0) {
            sample.setNum_critical(Integer.parseInt(tokens[3]));
        } else {
            sample.setNum_critical(0);
        }

//        set non critical Num
        if (tokens[4].length() > 0) {
            sample.setNum_noncritical(Integer.parseInt(tokens[4]));
        } else {
            sample.setNum_noncritical(0);
        }

//        set vio lump
        String viol_lump;
        if (have_not_download_data()) {
//            local data is itr1 data
            sample.setHazard_rating(tokens[5]);
            if (tokens.length == 7) {
                viol_lump = tokens[6];
            } else {
                viol_lump = "";
            }
//            local data is server data
        }else{
            if (tokens.length == 7) {
                sample.setHazard_rating(tokens[6]);
                viol_lump = tokens[5];
            } else {
                sample.setHazard_rating(tokens[5]);
                viol_lump = "";
            }
        }

        addViolationToInspection(viol_lump, sample);
        addInspectionToRestaurant(r, sample);
        inspection_num++;
    }

//    add violation to inspection
    private void addViolationToInspection(String viol_lump, Inspection sample) {
        if (viol_lump.length() > 0){
            String[] violations = viol_lump.split("\\|");
//            extract code and add each violation
            for (String vio : violations){
                String[] nums = vio.split(",",2);
                int num = Integer.parseInt(nums[0].replace("\"",""));
                sample.addViolation(num);
            }
        }
    }

//    add inspection to restaurant
    private void addInspectionToRestaurant(Restaurant r, Inspection sample) {
        if (r != null){
            r.addInspection(sample);
        }
    }

//    get downloaded data from Shared Preference
    private String getDownloadedData(String SharedPref_ref){
        SharedPreferences prefs = getSharedPreferences(SharedPref_set_data, MODE_PRIVATE);
        String extractedText = prefs.getString(SharedPref_ref, default_data);
        return extractedText;
    }

//    check if any data has been downloaded
    private boolean have_not_download_data(){
        String last_restaurant_list = getDownloadedData(SP_data_restaurant);
        String last_inspection_list = getDownloadedData(SP_data_inspection);
        if (last_inspection_list.compareTo(default_data) == 0) {
            return true;
        } else return last_restaurant_list.compareTo(default_data) == 0;
    }

    private void getFavoriteRestaurantNameList(){
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
    }

    public static Intent makeIntent(Context inputContext) {

        return new Intent(inputContext, MainActivity.class);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    //    check the results of user's answer for permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted=false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted=false;
                            Log.d(TAG, "onRequestPermissionsResult: permission denied");
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    initMap();
                }
            }
        }
    }

    private com.google.android.gms.maps.model.Marker getMarkerFromLatLng(ClusterManager<Marker> inputClusterManager, LatLng inputLatLng, String inputTitle) {
        Collection<com.google.android.gms.maps.model.Marker> markers = inputClusterManager.getClusterMarkerCollection().getMarkers();
        Log.d("collection:", "" + markers.size());

        for (com.google.android.gms.maps.model.Marker marker : markers) {
            LatLng position = marker.getPosition();
            String title = marker.getTitle();
            if (title.equals(inputTitle)) {
                return marker;
            }
        }
        return null;
    }
    private Marker getMarkerObjByName(String name){
        for (Marker m :markerList){
            if (m.getTitle().equalsIgnoreCase(name)){
                Log.d("obj", "not null");
                return m;
            }
        }
        return null;
    }
    private com.google.android.gms.maps.model.Marker getMarkerFromObj(Marker obj){
        com.google.android.gms.maps.model.Marker m = markerClusterRenderer.getMarker(obj);
        //com.google.android.gms.maps.model.Marker m = markerClusterRenderer.getMarker(obj);
        if (m != null){
            Log.d("Marker:", "not null");
        }

        return m;
    }
}
