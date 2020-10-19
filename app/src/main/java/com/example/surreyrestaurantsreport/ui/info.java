package com.example.surreyrestaurantsreport.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.surreyrestaurantsreport.R;

/**
 * Author information and resource attributes
 */

public class info extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attribute_info);
        Toolbar toolbar = findViewById(R.id.info_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(getDrawable(R.drawable.ic_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }



    public static Intent makeIntent(Context context) {
        return new Intent(context, info.class);
    }
}