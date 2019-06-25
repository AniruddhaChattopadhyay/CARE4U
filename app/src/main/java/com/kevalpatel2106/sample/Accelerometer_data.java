package com.kevalpatel2106.sample;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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
import java.util.Scanner;

import io.paperdb.Paper;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;
import static com.kevalpatel2106.sample.DemoCamActivity.MY_PERMISSIONS_REQUEST_READ_CONTACTS;


public class Accelerometer_data extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private MediaPlayer player;
    private String number;

    Sensor acc;

    Date date;

    int i, n,ID_SERVICE=188989898;

    double lat, lon;

    private String UserPhone;


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


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Fall Detector")
                .setContentText("We got your back!")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(PRIORITY_MIN)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();

        startForeground(ID_SERVICE, notification);



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


        Paper.init(this);

        UserPhone = Paper.book().read(Prevalent.userPhone);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        number = Paper.book().read(Prevalent.careGiver);

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

        if (i == 1500) {
            //sendData.sendData(x1,y1,z1,"https://script.google.com/macros/s/AKfycbxManrWNmgZZ2r6cttnfylpaAYL-FOyC7AZnV8c0edPeGCM-XQl/exec");

            //startUploadThread(filePath);

            try {

                player = MediaPlayer.create(this, Settings.System.DEFAULT_NOTIFICATION_URI);
                player.start();

                startmodel(f);



            }catch (Exception e){
                Log.d("MODEL",e.getMessage());
            }

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
            // for Activity#requestPermissions for more details
            Log.d("XXX" , "Location Permission Error");
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


        if (!(lat<0.0001 || lon <0.00001)) {
            RootRef.child("Users").child(UserPhone).child("Latitude").setValue(lat);
            RootRef.child("Users").child(UserPhone).child("Longitude").setValue(lon);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager){
        String channelId = "my_service_channelid";
        String channelName = "My Foreground Service";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        // omitted the LED color
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
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
                                if (output[0][0]>0.5)
                                    call(output[0][0]);
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

    private void call(float out){
        Log.d("YYY",""+out);
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel",number,null));
        startActivity(intent);
    }

}
