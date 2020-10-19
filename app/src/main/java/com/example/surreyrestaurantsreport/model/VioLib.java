package com.example.surreyrestaurantsreport.model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A library of all violations, including a hashmap for easy searching
 */

public class VioLib {
    private static VioLib myLib;                // singleton
    private List<Violation> library;            // library of all violation
    private HashMap<Integer, Integer> map;      // hashmap for mapping violation num to list index
    private int size;                           // total num of violations

    public static VioLib getInstance(){
        if(myLib == null){
            myLib = new VioLib();
        }
        return myLib;
    }

    private VioLib(){
        library = new ArrayList<>();
        map = new HashMap<>();
        size = 0;
    }

    public Violation get(int vio_num){
        Integer id = map.get(vio_num);
        if (id != null){
            return library.get(id);
        }
        else{
            return null;
        }
    }

    public Integer get_id(int vio_num){
        return map.get(vio_num);
    }

    public void add(Violation v){
        // add to library
        library.add(v);
        // update mapping
        int vio_num = v.getVio_num();
        map.put(vio_num, size);
        // update size
        v.setVio_id(size);
        size ++;
    }
}