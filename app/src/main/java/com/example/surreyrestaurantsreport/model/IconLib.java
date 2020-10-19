package com.example.surreyrestaurantsreport.model;
/**
 * Singleton class for holding a list of restaurant names with unique icons
 * Format: remove all space and symbols, case sensitive
 */

public class IconLib {

    private static IconLib myLib;            // singleton
    private String[] library = {             // library of available icon res names
            "7Eleven",
            "DairyQueen",
            "AandW",
            "Starbucks",
            "TimHorton",
            "Barcelo",
            "BostonPizza",
            "PizzaHut",
            "Wendys",
            "Subway",
            "Freshslice"};

    public static IconLib getInstance(){
        if(myLib == null){
            myLib = new IconLib();
        }
        return myLib;
    }

    public String[] getLibrary(){
        return library;
    }

}
