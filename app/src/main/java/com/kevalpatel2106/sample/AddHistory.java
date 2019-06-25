package com.kevalpatel2106.sample;

import androidx.annotation.NonNull;
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

public class AddHistory extends AppCompatActivity {


    String phone;

    EditText editTextHistory,editTextTime,getEditTextDesc;
    Button buttonAddItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_history);

        editTextHistory = (EditText)findViewById(R.id.history);
        editTextTime = (EditText)findViewById(R.id.cause);
        getEditTextDesc = (EditText) findViewById(R.id.Description);

        buttonAddItem = (Button)findViewById(R.id.add_hist_btn);
        buttonAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addtoFirebase();
            }
        });
    }

    private void   addtoFirebase() {

        final ProgressDialog loading = ProgressDialog.show(this,"Adding Item","Please wait");
        final String allergy = editTextHistory.getText().toString().trim();
        final String dessc = getEditTextDesc.getText().toString().trim();
        final String medicine = editTextTime.getText().toString().trim();

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


                    RootRef.child("Users").child(phone).child("History").child(allergy).updateChildren(userdataMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        Toast.makeText(AddHistory.this, "Your Medical History is registered", Toast.LENGTH_SHORT).show();
                                        loading.dismiss();

                                        Intent intent = new Intent(AddHistory.this, HistoryActivity.class);
                                        startActivity(intent);
                                    }
                                    else {
                                        loading.dismiss();
                                        Toast.makeText(AddHistory.this, "Network Error: Please try again", Toast.LENGTH_SHORT).show();
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
