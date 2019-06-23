package com.kevalpatel2106.sample;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kevalpatel2106.sample.Prevelant.Prevalent;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

import io.paperdb.Paper;


public class Accelerometer_data extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private MediaPlayer player;

    Sensor acc;

    Date date;

    int i, n,ONGOING_NOTIFICATION_ID=1;

    double lat, lon;

    private FusedLocationProviderClient fusedLocationProviderClient;

    //SendData sendData = new SendData();

    FirebaseStorage storage;
    StorageReference storageReference;


    public Accelerometer_data() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        i = 0;
        n = 0;



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        /*Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification=new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                    .setContentText("Fall Detector")
                    .setContentIntent(pendingIntent).build();
            Log.d("XXX","Notif created");
            startForeground(ONGOING_NOTIFICATION_ID, notification);
        }*/




        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (acc != null) {
            sensorManager.registerListener((SensorEventListener) Accelerometer_data.this, acc, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Log.d("Sensor", "Gyro Error");
        }


        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        File baseDir = getFilesDir();
        String fileName = "AnalysisData" + n + ".csv";
        String filePath = baseDir + File.separator + fileName;
        File f = new File(filePath);
        CSVWriter writer = null;

        String x1, y1, z1;

        x1 = "a";
        y1 = "a";
        z1 = "a";

        Sensor sensor = event.sensor;

        if (sensor.getType() == sensor.TYPE_ACCELEROMETER) {
            Log.d("ACC", "" + event.values[0]);
            float x = event.values[0];
            x1 = Float.toString(x);

            float y = event.values[1];
            y1 = Float.toString(y);

            float z = event.values[2];
            z1 = Float.toString(z);
        }

        // File exist
        if (f.exists() && !f.isDirectory()) {

            FileWriter mFileWriter = null;
            try {
                mFileWriter = new FileWriter(filePath, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            writer = new CSVWriter(mFileWriter);
        } else {
            try {
                writer = new CSVWriter(new FileWriter(filePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String[] data = {x1, y1, z1};

        writer.writeNext(data);
        Log.d("DATA", "Written" + i);
        i++;

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (i == 500) {
            //sendData.sendData(x1,y1,z1,"https://script.google.com/macros/s/AKfycbxManrWNmgZZ2r6cttnfylpaAYL-FOyC7AZnV8c0edPeGCM-XQl/exec");

            //startUploadThread(filePath);

            player = MediaPlayer.create(this, Settings.System.DEFAULT_NOTIFICATION_URI);
            player.start();

            i = 0;
            n++;

            fetchLocation();

            Log.d("XXX", "" + lat + lon);


        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    //------------------------------------------------------------------------------------------------------------------------------------------------------


    public void startUploadThread(String dir) {
        Accelerometer_data.NewRunnable runnable = new Accelerometer_data.NewRunnable(dir);
        new Thread(runnable).start();
    }

    class NewRunnable implements Runnable {
        String dir;

        public NewRunnable(String dir) {
            this.dir = dir;
        }

        @Override
        public void run() {

            Uri uriSavedImage = Uri.fromFile(new File(dir));
            Log.d("Photo", "Image uri created");

            uploadImage(uriSavedImage);
            //Display the image to the image view;
        }
    }


    private void uploadImage(final Uri filePath) {

        if (filePath != null) {
            /*final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();*/

            Random generator = new Random();
            date = new Date();
            String date1 = date.toString();
            date1 = date1.replaceAll(" ", "_");
            Log.d("DATA1", date.toString().replaceAll(" ", "_"));

            StorageReference ref = storageReference.child("Data/" + "EXP_newcsv" + date1 + ".csv");
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //progressDialog.dismiss();
                            Toast.makeText(getBaseContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                            File fdelete = new File(filePath.getPath());
                            if (fdelete.exists()) {
                                if (fdelete.delete()) {
                                    System.out.println("file Deleted :" + filePath.getPath());
                                } else {
                                    System.out.println("file not Deleted :" + filePath.getPath());
                                }
                            }

                            player = MediaPlayer.create(Accelerometer_data.this, Settings.System.DEFAULT_RINGTONE_URI);
                            player.start();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //progressDialog.dismiss();
                            Toast.makeText(getBaseContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    /*.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });*/
        }
    }

    private void fetchLocation() {

        if ((ContextCompat.checkSelfPermission(Accelerometer_data.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(Accelerometer_data.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                lat = location.getLatitude();
                lon = location.getLongitude();
                Toast.makeText(Accelerometer_data.this, "" + location.getLatitude(), Toast.LENGTH_SHORT).show();
            }
        });

        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        String UserPhone = Paper.book().read(Prevalent.userPhone);

        RootRef.child("Users").child(UserPhone).child("Latitude").setValue(lat);
        RootRef.child("Users").child(UserPhone).child("Longitude").setValue(lon);

    }



}
