package com.kevalpatel2106.sample;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

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

import io.paperdb.Paper;

public class ActivityEating extends AppCompatActivity {

    ListView listView;
    ListAdapter adapter;
    ProgressDialog loading;
    Button btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eating);

        btn = (Button)findViewById(R.id.btn_eating);
        assert btn != null;
        System.out.println("***************************************************************");
        System.out.println(btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityEating.this,AddEating.class);
                startActivity(intent);
            }
        });

        listView = (ListView) findViewById(R.id.lv_items);

        getItems();


    }

    private void getItems() {

        loading =  ProgressDialog.show(this,"Loading","please wait . . .",false,true);

        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        String phone = Paper.book().read(Prevalent.userPhone);

        final List<Allergy> allergyList = new ArrayList<>();
        RootRef.child("Users").child(phone).child("Eating")
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
}
