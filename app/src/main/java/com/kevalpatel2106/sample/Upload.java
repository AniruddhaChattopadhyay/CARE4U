package com.kevalpatel2106.sample;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

public class Upload extends Service {

    private static String id = "DIRECTORY";

    FirebaseStorage storage;
    StorageReference storageReference;



    public Upload() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        String dir = (String) intent.getExtras().get(id);

        startUploadThread(dir);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    public void startUploadThread (String dir) {
        NewRunnable runnable = new NewRunnable(dir);
        new Thread(runnable).start();
    }

    class NewRunnable implements Runnable{
        String  dir;

        public NewRunnable(String  dir) {
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

    private void uploadImage(Uri filePath) {

        if(filePath != null)
        {
            /*final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();*/

            Random generator = new Random();

            StorageReference ref = storageReference.child("images/"+ "imageJPEG" + generator.nextInt(1000) + ".jpeg");
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //progressDialog.dismiss();
                            Toast.makeText(getBaseContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //progressDialog.dismiss();
                            Toast.makeText(getBaseContext(), "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
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
}
