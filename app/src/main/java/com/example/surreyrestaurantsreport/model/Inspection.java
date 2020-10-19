package com.example.surreyrestaurantsreport.model;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.surreyrestaurantsreport.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Class that holds information of a single inspection
 */
public class Inspection implements Comparable<Inspection>{

    private String tracking_number;
    private int inspection_date;
    private String insp_type;
    private int num_critical;
    private int num_noncritical;
    private String hazard_rating;
    private List<Violation> violations;

    public Inspection() {
        violations = new ArrayList<>();
    }

    public String getTracking_number() {
        return tracking_number;
    }

    public void setTracking_number(String tracking_number) {
        this.tracking_number = tracking_number;
    }

    public int getInspection_date() {
        return inspection_date;
    }

    public void setInspection_date(int inspection_date) {
        this.inspection_date = inspection_date;
    }

    public String getInsp_type() {
        return insp_type;
    }

    public void setInsp_type(String insp_type) {
        this.insp_type = insp_type;
    }

    public int getNum_critical() {
        return num_critical;
    }

    public void setNum_critical(int num_critical) {
        this.num_critical = num_critical;
    }

    public int getNum_noncritical() {
        return num_noncritical;
    }

    public void setNum_noncritical(int num_noncritical) {
        this.num_noncritical = num_noncritical;
    }

    public String getHazard_rating() {
        return hazard_rating;
    }

    public void setHazard_rating(String hazard_rating) {
        this.hazard_rating = hazard_rating;
    }

    public List<Violation> getViolations() {
        return violations;
    }

    public Violation getViolation(int id){
        if(violations.size() == 0){
            return null;
        }
        else{
            return violations.get(id);
        }
    }

    public void addViolation(int vio_num){
        // get violation from library
        VioLib lib = VioLib.getInstance();
        Violation origin = lib.get(vio_num);
        Violation v = new Violation();
        v.setVio_num(origin.getVio_num());
        v.setCritical(origin.getCritical());
        v.setNature(origin.getNature());
        v.setShort_des(origin.getShort_des());
        v.setFull_des(origin.getFull_des());
        v.setVio_id(origin.getVio_id());
        violations.add(v);
    }

    public String getFormattedDate(Context context) {

        String inspectionTime = String.valueOf(inspection_date);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date inspectionDate = null;
        try {
            inspectionDate = format.parse(inspectionTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date currentDate = new Date();
        long diff = currentDate.getTime() - inspectionDate.getTime();
        diff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        String formattedDate;
        if (diff <= 30) {
            formattedDate = String.format(
                    context.getResources().getString(R.string.inspection_date),
                    diff);
        } else if (diff < 366) {
            SimpleDateFormat newFormat = new SimpleDateFormat("MMMM dd");
            formattedDate = newFormat.format(inspectionDate);
        } else {
            SimpleDateFormat newFormat = new SimpleDateFormat("MMMM yyyy");
            formattedDate = newFormat.format(inspectionDate);
        }
        return formattedDate;
    }

    public String getFullDate() {

        String inspectionTime = String.valueOf(inspection_date);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date inspectionDate = null;
        try {
            inspectionDate = format.parse(inspectionTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat newFormat = new SimpleDateFormat("MMMM dd, yyyy");
        String formattedDate = newFormat.format(inspectionDate);
        return formattedDate;
    }

    @Override
    public int compareTo(Inspection o) {
        return this.getInspection_date() - (o.getInspection_date());
    }

    @Override
    @NonNull
    public String toString() {
        return "Inspection{" +
                "tracking_number='" + tracking_number + '\'' +
                ", inspection_date=" + inspection_date +
                ", insp_type='" + insp_type + '\'' +
                ", num_critical=" + num_critical +
                ", num_noncritical=" + num_noncritical +
                ", hazard_rating='" + hazard_rating + '\'' +
                ", viol_lump='" + violations + '\'' +
                '}';
    }
}
