package com.kevalpatel2106.sample;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class AddAllergy extends AppCompatActivity implements View.OnClickListener {

    String phone;

    EditText editTextAllergy,editTextDesc,editTextMedicine;
    Button buttonAddItem;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_allergy);


        editTextAllergy = (EditText)findViewById(R.id.allergy);
        editTextDesc = (EditText)findViewById(R.id.cause);
        editTextMedicine = (EditText) findViewById(R.id.medicinee);

        buttonAddItem = (Button)findViewById(R.id.add_med_btn);
        buttonAddItem.setOnClickListener(this);


    }

    //This is the part where data is transafeered from Your Android phone to Sheet by using HTTP Rest API calls

    private void   addtoFirebase() {

        final ProgressDialog loading = ProgressDialog.show(this,"Adding Item","Please wait");
        final String allergy = editTextAllergy.getText().toString().trim();
        final String dessc = editTextDesc.getText().toString().trim();
        final String medicine = editTextMedicine.getText().toString().trim();

        phone = Paper.book().read(Prevalent.userPhone);


        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if((dataSnapshot.child("Users").child(phone).exists()))
                {
                    HashMap<String, Object> userdataMap = new HashMap<>();
                    userdataMap.put("Name",allergy);
                    userdataMap.put("Cause",dessc);
                    userdataMap.put("Medicines",medicine);


                    RootRef.child("Users").child(phone).child("Allergy").child(allergy).updateChildren(userdataMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        Toast.makeText(AddAllergy.this, "Your allergy is registered", Toast.LENGTH_SHORT).show();
                                        loading.dismiss();

                                        Intent intent = new Intent(AddAllergy.this, AllergyActivity.class);
                                        startActivity(intent);
                                    }
                                    else {
                                        loading.dismiss();
                                        Toast.makeText(AddAllergy.this, "Network Error: Please try again", Toast.LENGTH_SHORT).show();
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




    @Override
    public void onClick(View v) {

        if(v==buttonAddItem){
            addtoFirebase();

            //Define what to do when button is clicked
        }
    }
}
