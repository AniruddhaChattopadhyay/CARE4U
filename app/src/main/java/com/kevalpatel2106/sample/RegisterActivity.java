package com.kevalpatel2106.sample;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.text.TextUtils;
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

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {



    private Button CreateAccountBtn;
    private EditText Input_name,Input_phone, Input_psswd;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        CreateAccountBtn = (Button) findViewById(R.id.resgister_btn);
        Input_name = (EditText) findViewById(R.id.register_username);
        Input_phone = (EditText) findViewById(R.id.register_phone);
        Input_psswd = (EditText) findViewById(R.id.register_password);
        loadingBar = new ProgressDialog(this);

        CreateAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateAccount_func();
            }
        });
    }

    private void CreateAccount_func()
    {
        String name = Input_name.getText().toString();
        String phone = Input_phone.getText().toString();
        String psswd = Input_psswd.getText().toString();
        String care = ((EditText) findViewById(R.id.register_caregiver)).getText().toString();

        if (TextUtils.isEmpty(name)){
            Toast.makeText(RegisterActivity.this,"Please enter your name/username",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(phone)){
            Toast.makeText(RegisterActivity.this,"Please enter your Phone Number",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(psswd)){
            Toast.makeText(RegisterActivity.this,"Please enter your password",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(care)){
            Toast.makeText(RegisterActivity.this,"Please enter your Caregiver's Phone Number",Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Create Account");
            loadingBar.setMessage("Please wait while we validate");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            ValidatephoneNumber(name,phone,psswd,care);
        }
    }

    private void ValidatephoneNumber(final String name, final String phone, final String psswd, final String care){

        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!(dataSnapshot.child("Users").child(phone).exists()))
                {
                    HashMap<String, Object> userdataMap = new HashMap<>();
                    userdataMap.put("phone",phone);
                    userdataMap.put("password",psswd);
                    userdataMap.put("name",name);
                    userdataMap.put("Latitude","0.0");
                    userdataMap.put("Longitude","0.0");
                    userdataMap.put("Caregiver",care);

                    RootRef.child("Users").child(phone).updateChildren(userdataMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        Toast.makeText(RegisterActivity.this, "Your account was created successfully", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();

                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                    }
                                    else {
                                        loadingBar.dismiss();
                                        Toast.makeText(RegisterActivity.this, "Network Error: Please try again", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                }
                else
                {
                    Toast.makeText(RegisterActivity.this, "This Number already exists", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    Toast.makeText(RegisterActivity.this, "Please try again with another Number", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
