package com.kevalpatel2106.sample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevalpatel2106.sample.Model.Allergy;
import com.kevalpatel2106.sample.Prevelant.Prevalent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import io.paperdb.Paper;
import me.xdrop.fuzzywuzzy.FuzzySearch;

public class MedicinesActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    ListView listView;
    ListAdapter adapter;
    ProgressDialog loading;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicines);

        btn = (Button)findViewById(R.id.btn_med);
        assert btn != null;
        System.out.println("***************************************************************");
        System.out.println(btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MedicinesActivity.this,AddMedicine.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.btn_cam_med).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        listView = (ListView) findViewById(R.id.med_items);

        getItems();

    }


    private void getItems() {

        loading =  ProgressDialog.show(this,"Loading","please wait . . .",false,true);

        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        String phone = Paper.book().read(Prevalent.userPhone);

        final List<Allergy> allergyList = new ArrayList<>();
        RootRef.child("Users").child(phone).child("Medicine")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                            allergyList.add(snapshot.getValue(Allergy.class));
                            Allergy x = allergyList.get(0);
                            Log.d("ZZZ",""+x.getCause());
                        }
                        parseItems(allergyList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }


    private void parseItems(List<Allergy> allergyList) {

        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        try {

            for (Allergy i : allergyList) {


                HashMap<String, String> item = new HashMap<>();
                item.put("Name", i.getName());
                item.put("Cause", i.getCause());
                item.put("Medicines", i.getMedicines());

                list.add(item);
                Toast.makeText(this, "item loaded", Toast.LENGTH_SHORT).show();


            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        adapter = new SimpleAdapter(this,list,R.layout.activity_list_item_row,
                new String[]{"Name","Cause","Medicines"},new int[]{R.id.Main_head,R.id.Sub_head,R.id.sub_sub});


        listView.setAdapter(adapter);
        loading.dismiss();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private int matchWords(String wrd, String medicine){
        wrd = wrd.toLowerCase();
        medicine = medicine.toLowerCase();
        return FuzzySearch.ratio(medicine,wrd);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
            if(!textRecognizer.isOperational()){
                System.out.println("Non operational *******************************");
            }
            else {
                System.out.println("Operational ******************************");
                assert imageBitmap != null;
                Frame frame = new Frame.Builder().setBitmap(imageBitmap).build();
                SparseArray<TextBlock> items = textRecognizer.detect(frame);
                StringBuilder sb = new StringBuilder();
                for (int i =0; i<items.size(); i++){
                    TextBlock myitems = items.valueAt(i);
                    sb.append(myitems.getValue());
                    sb.append(" ");
                }
                System.out.println("STRING ****************************************");
                System.out.println(sb.toString());
                String text = sb.toString();
                if(items.size()==0){
                    System.out.println("Sorry, No text detected");
                    text = "No text found";
                }
                ArrayList<String> medicines= new ArrayList<>();
                try {
                    Scanner sc = new Scanner(getAssets().open("medicines.txt"));
                    while (sc.hasNextLine()) {
                        String ln = sc.nextLine();
                        System.out.println(ln);
                        System.out.println("***************************************************");
                        medicines.add(ln);
                    }
                }catch (Exception e){
                    System.out.println(e.getMessage());
                    System.out.println("***************************************************");
                }
                String[] foundwrds = text.split(" ");
                int max = -1;
                for (String foundwrd : foundwrds) {
                    for (int j = 0; j < medicines.size(); j++) {
                        int score = matchWords(foundwrd, medicines.get(j));
                        if (score > max) {
                            max = score;
                            text = medicines.get(j);
                        }
                    }
                }
                System.out.println(max);
                System.out.println(text);
                System.out.println("**************************Aadi**********************************");

                Intent intent = new Intent(MedicinesActivity.this,AddMedicine.class);
                if(max>=90){
                intent.putExtra("Med_Name",text);}
                else {
                    Toast.makeText(this, "Sorry we didn't get the name.", Toast.LENGTH_SHORT).show();
                }
                startActivity(intent);

            }
        }
    }
}
