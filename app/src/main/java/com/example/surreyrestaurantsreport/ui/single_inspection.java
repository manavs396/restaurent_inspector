package com.example.surreyrestaurantsreport.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.surreyrestaurantsreport.R;
import com.example.surreyrestaurantsreport.model.Inspection;
import com.example.surreyrestaurantsreport.model.ResList;
import com.example.surreyrestaurantsreport.model.Restaurant;
import com.example.surreyrestaurantsreport.model.Violation;

import java.util.List;

/**
 * Displays information about a single inspection, including a list of all violations
 */

public class single_inspection extends AppCompatActivity {
    private static int selectedInsIndex;
    private static int selectedResIndex;
    private Restaurant r;
    private ResList mList;
    private Inspection i;
    private List<Violation> violations;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_inspection);
        Toolbar toolbar = findViewById(R.id.ins_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getDrawable(R.drawable.ic_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mList = ResList.getInstance();
        r = mList.get(selectedResIndex);
        i = r.getInspection(selectedInsIndex);
        violations = i.getViolations();

        setInspectionInfo();
        populateListView();

    }


    private void setInspectionInfo(){
        String s_type = i.getInsp_type();
        String s_date = i.getFullDate();
        String s_rate = i.getHazard_rating();
        int num_critical = i.getNum_critical();
        int num_noncritical = i.getNum_noncritical();
        String s_hazard = i.getHazard_rating();
        String display;

        ImageView ins_icon = findViewById(R.id.single_ins_rate);
        if(s_rate.equalsIgnoreCase(getResources().getString(R.string.restaurant_hazardLevelHigh_value))){
            ins_icon.setImageResource(R.drawable.high_risk);
        }
        else if(s_rate.equalsIgnoreCase(getResources().getString(R.string.restaurant_hazardLevelModerate_value))){
            ins_icon.setImageResource(R.drawable.mid_risk);
        }
        else{
            ins_icon.setImageResource(R.drawable.low_risk);
        }

        TextView date_txt = findViewById(R.id.single_ins_date);
        display = String.format(
                getResources().getString(R.string.inspection_info), s_date);
        date_txt.setText(display);

        TextView type_txt = findViewById(R.id.single_ins_type);
        if (s_type.equalsIgnoreCase("Routine")){
            s_type = getResources().getString(R.string.inspection_type_routine);
        }
        else {
            s_type = getResources().getString(R.string.inspection_type_followup);
        }

        display = String.format(
                getResources().getString(R.string.inspection_type), s_type);
        type_txt.setText(display);

        TextView hazard_txt = findViewById(R.id.single_ins_hazard);
        if (s_hazard.equalsIgnoreCase("High")){
            s_hazard = getResources().getString(R.string.restaurant_hazardLevelHigh_value);
        }
        else if (s_hazard.equalsIgnoreCase("Moderate")){
            s_hazard = getResources().getString(R.string.restaurant_hazardLevelModerate_value);
        }
        else {
            s_hazard = getResources().getString(R.string.restaurant_hazardLevelLow_value);
        }

        display = String.format(
                getResources().getString(R.string.restaurant_hazardLevel), s_hazard);
        hazard_txt.setText(display);

        TextView noncritical_txt = findViewById(R.id.single_ins_noncritical);
        display = String.format(
                getResources().getString(R.string.inspection_num_noncritical), num_noncritical);
        noncritical_txt.setText(display);

        TextView critical_txt = findViewById(R.id.single_ins_critical);
        display = String.format(
                getResources().getString(R.string.inspection_num_critical), num_critical);
        critical_txt.setText(display);

    }

    private void populateListView(){
        ArrayAdapter<Violation> adapter = new MyListAdapter();
        ListView list = findViewById(R.id.vioList);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int index = i.getViolation(position).getVio_id();
                String full_des = getResources().getStringArray(R.array.violation_long)[index];
                Toast toast = Toast.makeText(single_inspection.this, full_des, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    private class MyListAdapter extends ArrayAdapter<Violation>{
        public MyListAdapter() {
            super(single_inspection.this, R.layout.listview_each_violation, violations);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View resView = convertView;
            if (resView == null){
                resView = getLayoutInflater().inflate(R.layout.listview_each_violation, parent,false);
            }

//            find the inspection to work with
            Violation curr = violations.get(position);
            int code = curr.getVio_num();
            int id = curr.getVio_id();
            String nature = getResources().getStringArray(R.array.violation_nature)[id];
            String critical = getResources().getStringArray(R.array.violation_critical)[id];
            String short_des = getResources().getStringArray(R.array.violation_short)[id];
            /*
            String nature = curr.getNature();
            String critical = curr.getCritical();
            String short_des = curr.getShort_des();

             */

//            set icon to its hazard level
            ImageView critical_icon = resView.findViewById(R.id.vio_critical_icon);
            if (critical.equalsIgnoreCase(getResources().getString(R.string.violation_critical) )){
                critical_icon.setImageResource(R.drawable.high_risk);
                resView.setBackground(getDrawable(R.drawable.high_back));
            }
            else{
                critical_icon.setImageResource(R.drawable.mid_risk);
                resView.setBackground(getDrawable(R.drawable.mid_back));
            }

            ImageView nature_icon = resView.findViewById(R.id.vio_nature_icon);
            if (nature.equalsIgnoreCase(getResources().getString(R.string.violation_animal))){
                nature_icon.setImageResource(R.drawable.animal);
            }
            else if (nature.equalsIgnoreCase(getResources().getString(R.string.violation_pest))){
                nature_icon.setImageResource(R.drawable.pest_blk);
            }
            else if (nature.equalsIgnoreCase(getResources().getString(R.string.violation_training))){
                nature_icon.setImageResource(R.drawable.training);
            }
            else if (nature.equalsIgnoreCase(getResources().getString(R.string.violation_employee))){
                nature_icon.setImageResource(R.drawable.cook);
            }
            else if (nature.equalsIgnoreCase(getResources().getString(R.string.violation_premises))){
                nature_icon.setImageResource(R.drawable.restaurant);
            }
            else if (nature.equalsIgnoreCase(getResources().getString(R.string.violation_food))){
                nature_icon.setImageResource(R.drawable.food);
            }
            else if (nature.equalsIgnoreCase(getResources().getString(R.string.violation_chemical))){
                nature_icon.setImageResource(R.drawable.chemical);
            }
            else if (nature.equalsIgnoreCase(getResources().getString(R.string.violation_permit))){
                nature_icon.setImageResource(R.drawable.permit);
            }
            else {
                nature_icon.setImageResource(R.drawable.equipment);
            }
            
//            hazard level
            TextView vio_info = resView.findViewById(R.id.vio_info);
            String info = String.format(getResources().getString(R.string.violation_info),
                    code, nature, critical);
            vio_info.setText(info);

            TextView vio_short = resView.findViewById(R.id.vio_short);
            String s_short = String.format(getResources().getString(R.string.violation_short), short_des);
            vio_short.setText(s_short);
            return resView;
        }
    }

    public static Intent makeIntent(Context inputContext, int resIndex, int insIndex) {
        Intent intent = new Intent(inputContext, single_inspection.class);
        selectedResIndex = resIndex;
        selectedInsIndex = insIndex;
        return intent;
    }
}