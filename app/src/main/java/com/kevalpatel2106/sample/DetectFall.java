package com.kevalpatel2106.sample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseLocalModel;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.common.modeldownload.FirebaseRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;
import com.kevalpatel2106.sample.Prevelant.Prevalent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import io.paperdb.Paper;

public class DetectFall extends AppCompatActivity {

    float acc_x[][] = new float[1][1500];
    float acc_y[][] = new float[1][1500];
    float acc_z[][] = new float[1][1500];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void startmodel(File file) {
        float result=-2.0f;
        configureHostedModelSource();
        configureLocalModelSource();
        System.out.println("Models configured ****************************************************************************************************");
        try {
            System.out.println("Out to begin **************************************************************************************************");
            runInference(file);
        }
        catch (Exception e) {
            System.out.println(e);

        }

    }

    private void configureHostedModelSource() {
        // [START mlkit_cloud_model_source]
        FirebaseModelDownloadConditions.Builder conditionsBuilder =
                new FirebaseModelDownloadConditions.Builder().requireWifi();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Enable advanced conditions on Android Nougat and newer.
            conditionsBuilder = conditionsBuilder
                    .requireCharging()
                    .requireDeviceIdle();
        }
        FirebaseModelDownloadConditions conditions = conditionsBuilder.build();

        // Build a remote model source object by specifying the name you assigned the model
        // when you uploaded it in the Firebase console.
        FirebaseRemoteModel cloudSource = new FirebaseRemoteModel.Builder("detector")
                .enableModelUpdates(true)
                .setInitialDownloadConditions(conditions)
                .setUpdatesDownloadConditions(conditions)
                .build();
        FirebaseModelManager.getInstance().registerRemoteModel(cloudSource);
        // [END mlkit_cloud_model_source]
    }

    private void configureLocalModelSource() {
        // [START mlkit_local_model_source]
        FirebaseLocalModel localSource =
                new FirebaseLocalModel.Builder("detector")  // Assign a name to this model
                        .setAssetFilePath("model.tflite")
                        .build();
        FirebaseModelManager.getInstance().registerLocalModel(localSource);
        // [END mlkit_local_model_source]
    }

    private FirebaseModelInterpreter createInterpreter() throws FirebaseMLException {
        // [START mlkit_create_interpreter]
        FirebaseModelOptions options = new FirebaseModelOptions.Builder()
                .setRemoteModelName("detector")
                .setLocalModelName("detector")
                .build();
        FirebaseModelInterpreter firebaseInterpreter =
                FirebaseModelInterpreter.getInstance(options);
        // [END mlkit_create_interpreter]

        return firebaseInterpreter;
    }

    private FirebaseModelInputOutputOptions createInputOutputOptions() throws FirebaseMLException {
        // [START mlkit_create_io_options]
        FirebaseModelInputOutputOptions inputOutputOptions =
                new FirebaseModelInputOutputOptions.Builder()
                        .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1,1500})
                        .setInputFormat(1, FirebaseModelDataType.FLOAT32, new int[]{1,1500})
                        .setInputFormat(2, FirebaseModelDataType.FLOAT32, new int[]{1,1500})
                        .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 2})
                        .build();
        // [END mlkit_create_io_options]

        return inputOutputOptions;
    }

    private float[][][] getInput( File f)
    {
        int c=0;
        float acc[][][] = new float[3][1][1500];
        try
        {
            Scanner scanner = new Scanner(f);
            while(scanner.hasNextLine()) {
                String str = scanner.nextLine().replaceAll("\"","");
                //Log.d("ACCKan",str);
                String arr[] = str.split(",");
                acc[0][0][c]= Float.parseFloat(arr[0]);
                acc[1][0][c]= Float.parseFloat(arr[1]);
                acc[2][0][c]= Float.parseFloat(arr[2]);
                Log.d("ACCKan",""+acc[0][0][c]+"  "+acc[1][0][c]+"  "+acc[2][0][c] + "  "+c);
                c++;
                if (c==1500)
                    break;

            }
            scanner.close();
        }

        catch (Exception e)
        {
            Log.d("MODEL","Kanishka" + e.getMessage());
        }
        //for(int i=0;i<1500;i++)
         //   System.out.println(acc[0][0][i]+"  "+acc[1][0][i]+"  "+acc[2][0][i]);
        return acc;
    }

    private void runInference(File f) throws FirebaseMLException {
        FirebaseModelInterpreter firebaseInterpreter = createInterpreter();
        float acc[][][] = getInput(f);
        FirebaseModelInputOutputOptions inputOutputOptions = createInputOutputOptions();

        // [START mlkit_run_inference]
        FirebaseModelInputs inputs = new FirebaseModelInputs.Builder()
                .add(acc[0]).add(acc[1]).add(acc[2])  // add() as many input arrays as your model requires
                .build();
        System.out.println("Inputs added ***************************************************************************************");
        firebaseInterpreter.run(inputs, inputOutputOptions)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseModelOutputs>() {
                            @Override
                            public void onSuccess(FirebaseModelOutputs result) {
                                // [START_EXCLUDE]
                                // [START mlkit_read_result]
                                float[][] output = result.getOutput(0);
                                System.out.println(output[0][0]+"   "+output[0][1]+"  *************************************************************");
                                //float[] probabilities = output[0];
                                // [END mlkit_read_result]
                                // [END_EXCLUDE]
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                System.out.println(e.getMessage());
                                System.out.println("*****************************************************************************************");
                                // ...
                            }
                        });
        // [END mlkit_run_inference]
    }

    private void makecall(View view){
        Log.d("MAKECALL","before");
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:+919830749515"));
        startActivity(intent);
        Log.d("MAKECALL","after");
    }

    private void paper(float out){
        Paper.init(DetectFall.this);
        Paper.book().write(Prevalent.userfalldata,out );
    }

}
