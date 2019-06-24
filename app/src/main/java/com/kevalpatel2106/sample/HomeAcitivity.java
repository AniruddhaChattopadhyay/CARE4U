package com.kevalpatel2106.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeAcitivity extends AppCompatActivity {

    TextView[] textViews ;
    int[] ids;
    int NUMBER_OF_FEATURES = 6;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_acitivity);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, HH:mm");
        Date date = new Date();
        String dt = simpleDateFormat.format(date);

        ids = new int[]{R.id.date_medicine, R.id.date_allergy, R.id.date_history, R.id.eat, R.id.contact, R.id.sos};
        textViews = new TextView[NUMBER_OF_FEATURES];
        for (int i=0;i<NUMBER_OF_FEATURES;i++){
            textViews[i] = (TextView) findViewById(ids[i]);
            textViews[i].setText(dt);
        }
    }
}
