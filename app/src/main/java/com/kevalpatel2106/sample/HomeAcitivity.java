package com.kevalpatel2106.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.kevalpatel2106.sample.Prevelant.Prevalent;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.paperdb.Paper;

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
        Paper.init(this);

        ids = new int[]{R.id.date_medicine, R.id.date_allergy, R.id.date_history, R.id.eat, R.id.contact, R.id.sos};
        textViews = new TextView[NUMBER_OF_FEATURES];
        for (int i=0;i<NUMBER_OF_FEATURES;i++){
            textViews[i] = (TextView) findViewById(ids[i]);
            textViews[i].setText(dt);
        }

        //-----------------------------------------------------------SOS---------------------------------------------------------------------
        findViewById(R.id.Sos_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        call();
                    }
                }, 20L);

            }
        });


        //---------------------------------------Allergy----------------------------------------------------------------------------------
        findViewById(ids[1]).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeAcitivity.this,AllergyActivity.class));
            }
        });
    }
    private void call(){
        String number = Paper.book().read(Prevalent.careGiver);
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel",number,null));
        startActivity(intent);
    }
}
