package com.kevalpatel2106.sample;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevalpatel2106.sample.Model.Users;
import com.kevalpatel2106.sample.Prevelant.Prevalent;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {

    private Button Login_Btn;
    private EditText Input_Phone,Input_Psswd;
    private ProgressDialog loadingbar;
    private String parentDbName = "Users";

    private CheckBox chkb_remember_me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Login_Btn = (Button) findViewById(R.id.login_btn);
        Input_Phone = (EditText) findViewById(R.id.login_phone);
        Input_Psswd = (EditText) findViewById(R.id.login_password);
        loadingbar = new ProgressDialog(this);
        chkb_remember_me = (CheckBox) findViewById(R.id.remember_me_chkb);

        Paper.init(this);


        Login_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Login_Function();
            }
        });
    }

    private void Login_Function()
    {
        String phone = Input_Phone.getText().toString();
        String password = Input_Psswd.getText().toString();

        if(TextUtils.isEmpty(phone))
        {
            Toast.makeText(LoginActivity.this, "Please enter your Phone Number", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password))
        {
            Toast.makeText(LoginActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingbar.setTitle("Logging in");
            loadingbar.setMessage("Verifying Credentials");
            loadingbar.setCanceledOnTouchOutside(false);
            loadingbar.show();

            Account_Acess(phone,password);
        }


    }

    private void Account_Acess(final String phone, final String password)
    {
        if (chkb_remember_me.isChecked())
        {
            Paper.book().write(Prevalent.userPhone,phone);
            Paper.book().write(Prevalent.userPassword,password);
        }

        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.child(parentDbName).child(phone).exists())
                {
                    Users userData = dataSnapshot.child(parentDbName).child(phone).getValue(Users.class);

                    if(userData.getPhone().equals(phone))
                    {
                        if (userData.getPassword().equals(password))
                        {
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            loadingbar.dismiss();

                            Intent intent1 = new Intent(LoginActivity.this, CameraService.class);
                            startService(intent1);

                            Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        else
                        {
                            Toast.makeText(LoginActivity.this, "Password is incorrect.", Toast.LENGTH_SHORT).show();
                            loadingbar.dismiss();
                            Toast.makeText(LoginActivity.this, "Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "Account with this Phnoe Number is not Registered.", Toast.LENGTH_SHORT).show();
                    loadingbar.dismiss();
                    Toast.makeText(LoginActivity.this, "Please retry valid Login Credentials.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
