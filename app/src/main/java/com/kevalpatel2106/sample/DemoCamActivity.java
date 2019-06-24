/*
 * Copyright 2016 Keval Patel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kevalpatel2106.sample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;



import com.androidhiddencamera.CameraConfig;
import com.androidhiddencamera.CameraError;
import com.androidhiddencamera.HiddenCameraActivity;
import com.androidhiddencamera.HiddenCameraUtils;
import com.androidhiddencamera.config.CameraFacing;
import com.androidhiddencamera.config.CameraFocus;
import com.androidhiddencamera.config.CameraImageFormat;
import com.androidhiddencamera.config.CameraResolution;
import com.androidhiddencamera.config.CameraRotation;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

public class DemoCamActivity extends HiddenCameraActivity {
    private static final int REQ_CODE_CAMERA_PERMISSION = 1253;

    private CameraConfig mCameraConfig;
    private FaceDetector detector;

    FirebaseStorage storage;
    StorageReference storageReference;

    private FusedLocationProviderClient fusedLocationProviderClient;

    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS=1;

    float smile_prob;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hidden_cam);

        fetchPermission();


        //storage = FirebaseStorage.getInstance();
        //storageReference = storage.getReference();


        mCameraConfig = new CameraConfig()
                .getBuilder(this)
                .setCameraFacing(CameraFacing.FRONT_FACING_CAMERA)
                .setCameraResolution(CameraResolution.HIGH_RESOLUTION)
                .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                .setImageRotation(CameraRotation.ROTATION_270)
                .setCameraFocus(CameraFocus.AUTO)
                .build();


        //Check for the camera permission for the runtime
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            //Start camera preview
            startCamera(mCameraConfig);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQ_CODE_CAMERA_PERMISSION);
        }


        //Take a picture
        findViewById(R.id.capture_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Take picture using the camera without preview.
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                Toast.makeText(DemoCamActivity.this, "Started takepicture", Toast.LENGTH_SHORT).show();
                takePicture();


            }
        });

    }

    //---------------------------------------------Start Thread-----------------------------------------------------------------------------------------------------------





    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQ_CODE_CAMERA_PERMISSION) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera(mCameraConfig);
            } else {
                Toast.makeText(this, R.string.error_camera_permission_denied, Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        if(requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS){
            if (grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                //abc
            } else {
                //abc
            }
        }
    }

    @Override
    public void onImageCapture(@NonNull File imageFile) {

        // Convert file to bitmap.
        // Do something.
        Toast.makeText(this, "Image Taken", Toast.LENGTH_SHORT).show();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
        Toast.makeText(this, "Image Created", Toast.LENGTH_SHORT).show();

        Log.d("Photo", "Image created");


        smile_prob = checkSmile(bitmap);

        MediaPlayer player = MediaPlayer.create(this,Settings.System.DEFAULT_RINGTONE_URI);
        player.start();

        //Intent intent = new Intent(DemoCamActivity.this, MainActivity.class);
        //startActivity(intent);

        /*String dir = saveImage(bitmap);
        Log.d("Photo", "Image dir: "  + dir);*/

        /*Intent intent = new Intent(this, Upload.class);
        intent.putExtra("DIRECTORY",dir);
        startService(intent);*/

        //((ImageView) findViewById(R.id.cam_prev)).setImageBitmap(bitmap);
        new Thread(){
            @Override
            public void run() {
                try {
                    sleep(100);
                    startService(new Intent(DemoCamActivity.this, Accelerometer_data.class));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(100);
                    Intent intent = new Intent(DemoCamActivity.this, HomeAcitivity.class);
                    startActivity(intent);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();





    }

    @Override
    public void onCameraError(@CameraError.CameraErrorCodes int errorCode) {
        switch (errorCode) {
            case CameraError.ERROR_CAMERA_OPEN_FAILED:
                //Camera open failed. Probably because another application
                //is using the camera
                Toast.makeText(this, R.string.error_cannot_open, Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_IMAGE_WRITE_FAILED:
                //Image write failed. Please check if you have provided WRITE_EXTERNAL_STORAGE permission
                Toast.makeText(this, R.string.error_cannot_write, Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE:
                //camera permission is not available
                //Ask for the camera permission before initializing it.
                Toast.makeText(this, R.string.error_cannot_get_permission, Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION:
                //Display information dialog to the user with steps to grant "Draw over other app"
                //permission for the app.
                HiddenCameraUtils.openDrawOverPermissionSetting(this);
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA:
                Toast.makeText(this, R.string.error_not_having_camera, Toast.LENGTH_LONG).show();
                break;
        }
    }


    private void fetchPermission() {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(DemoCamActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(DemoCamActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(DemoCamActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(DemoCamActivity.this,
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(DemoCamActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("Permission")
                        .setMessage("Some Text")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(DemoCamActivity.this,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CAMERA,Manifest.permission.CALL_PHONE},
                                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(DemoCamActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CAMERA,Manifest.permission.CAMERA,Manifest.permission.CALL_PHONE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted

        }

    }


    private String saveImage(Bitmap bm){
        File dir = new File( Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File int_dir = getFilesDir();


        Log.d("Photo",int_dir.toString());
        if (int_dir.exists()){
            Toast.makeText(getBaseContext(), " exist!", Toast.LENGTH_SHORT).show();
        }
        File myDir = new File(int_dir + "/reqimages");
        boolean success = true;
        if (!myDir.exists()) {
            success = myDir.mkdir();
            Toast.makeText(getBaseContext(), "Test_Directory", Toast.LENGTH_SHORT).show();
        }
        if (success) {
            // Do something on success
            Toast.makeText(getBaseContext(), "Directory Created", Toast.LENGTH_SHORT).show();
        } else {
            // Do something else on failure
            Toast.makeText(getBaseContext(), "Error Directory", Toast.LENGTH_SHORT).show();
        }
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "AAA-" + n + ".jpeg";
        File file = new File(myDir, fname);
        Log.d("Photo", "" + file);


        if (file.exists()) {
            success = file.delete();
        }
        if (success) {
            // Do something on success
            Toast.makeText(getBaseContext(), "File Deleted", Toast.LENGTH_SHORT).show();
        } else {
            // Do something else on failure
            Toast.makeText(getBaseContext(), "Error Directory", Toast.LENGTH_SHORT).show();
        }
        /*if (file.exists())
            file.delete();*/

        try {
            FileOutputStream out = new FileOutputStream(file);
        bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Photo", "Caught Eception:" + e.toString());
        }
        return myDir + "/" +  fname;
    }

    private float checkSmile (Bitmap bitmap){

        detector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_CLASSIFICATIONS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        float smile=-1;
        float size=0;

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<com.google.android.gms.vision.face.Face> faces = detector.detect(frame);

        for (int index = 0; index < faces.size(); ++index) {
            Face face = faces.valueAt(index);

            if (face.getHeight()*face.getWidth() > size) {
                size = face.getHeight()*face.getWidth();
                smile = face.getIsSmilingProbability();
            }

            Toast.makeText(this, ""+ smile , Toast.LENGTH_LONG).show();
        }

        return smile;
    }




}
