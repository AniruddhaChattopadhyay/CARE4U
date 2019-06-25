package com.kevalpatel2106.sample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevalpatel2106.sample.Prevelant.Prevalent;

import java.util.HashMap;

import io.paperdb.Paper;

public class AddMedicine extends AppCompatActivity {
    private String text="";
    String phone;

    private EditText medicine,desc;
    private TimePicker time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        try {
            text = getIntent().getStringExtra("Med_Name");
        }catch (Exception e){
            e.printStackTrace();
        }

        if (text!="")
            ((EditText)findViewById(R.id.medicine_text)).setText(text);

        medicine = (EditText) findViewById(R.id.medicine_text);
        time = (TimePicker) findViewById(R.id.medicine_time);
        desc = (EditText) findViewById(R.id.medicine_desc);

        findViewById(R.id.add_med_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addtoFirebase();
            }
        });

    }

    private void   addtoFirebase() {

        final ProgressDialog loading = ProgressDialog.show(this,"Adding Item","Please wait");
        final String medicineString = medicine.getText().toString().trim();
        final String timeString = time.getHour() + ":" + time.getMinute();
        Log.d("TIME",timeString);
        final String  descString = desc.getText().toString().trim();

        phone = Paper.book().read(Prevalent.userPhone);


        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if((dataSnapshot.child("Users").child(phone).exists()))
                {
                    HashMap<String, Object> userdataMap = new HashMap<>();
                    userdataMap.put("Name",medicineString);
                    userdataMap.put("Cause",timeString);
                    userdataMap.put("Medicines",descString);


                    RootRef.child("Users").child(phone).child("Medicine").child(medicineString).updateChildren(userdataMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        Toast.makeText(AddMedicine.this, "Your Medicine is registered", Toast.LENGTH_SHORT).show();
                                        loading.dismiss();

                                        Intent intent = new Intent(AddMedicine.this, MedicinesActivity.class);
                                        startActivity(intent);
                                    }
                                    else {
                                        loading.dismiss();
                                        Toast.makeText(AddMedicine.this, "Network Error: Please try again", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }
}
