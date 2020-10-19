package com.example.surreyrestaurantsreport.model;

import androidx.annotation.NonNull;

/**
 * Class that holds information of a specific violation
 */

public class Violation {
    private int vio_num;
    private int vio_id;
    private String critical;
    private String nature;
    private String short_des;   // short description
    private String full_des;    // full description

    public Violation() {
        this.vio_num = 0;
        this.critical = "";
        this.nature = "";
        this.short_des = "";
        this.full_des = "";
    }

    public void setVio_id(int i){
        vio_id = i;
    }

    public int getVio_id(){
        return vio_id;
    }
    public int getVio_num() {
        return vio_num;
    }

    public String getCritical() {
        return critical;
    }

    public String getNature(){
        return nature;
    }

    public String getShort_des() {
        return short_des;
    }

    public String getFull_des() {
        return full_des;
    }

    public void setVio_num(int vio_num) {
        this.vio_num = vio_num;
    }

    public void setCritical(String critical) {
        this.critical = critical;
    }

    public void setNature(String nature) {
        this.nature = nature;
    }

    public void setShort_des(String short_des) {
        this.short_des = short_des;
    }

    public void setFull_des(String full_des) {
        this.full_des = full_des;
    }

    @Override
    @NonNull
    public String toString() {
        return "Violation{" +
                "Code='" + vio_num+ '\'' +
                ", Nature =" + nature +
                ", Critical level='" + critical + '\'' +
                ", Short description=" + short_des +
                ", Full description=" + full_des +
                '}';
    }
}