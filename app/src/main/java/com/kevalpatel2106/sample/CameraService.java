package com.kevalpatel2106.sample;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

/*
Reference: http://stackoverflow.com/questions/28003186/capture-picture-without-preview-using-camera2-api
Problem
   1.  BufferQueue has been abandoned  from ImageCapture
 */
public class CameraService extends Service {
    protected static final String TAG = "VideoProcessing";
    protected static final int CAMERACHOICE = CameraCharacteristics.LENS_FACING_FRONT;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession session;
    protected ImageReader imageReader;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    protected CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.i(TAG, "CameraDevice.StateCallback onOpened");
            cameraDevice = camera;
            actOnReadyCameraDevice();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.w(TAG, "CameraDevice.StateCallback onDisconnected");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "CameraDevice.StateCallback onError " + error);
        }
    };

    protected CameraCaptureSession.StateCallback sessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.i(TAG, "CameraCaptureSession.StateCallback onConfigured");
            CameraService.this.session = session;
            try {
                session.setRepeatingRequest(createCaptureRequest(), null, null);
            } catch (CameraAccessException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
        }
    };

    protected ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.i(TAG, "onImageAvailable");
            Image img = reader.acquireLatestImage();
            if (img != null) {
                processImage(img);
                img.close();
            }
        }
    };

    public void readyCamera() {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String pickedCamera = getCamera(manager);
            if (ContextCompat.checkSelfPermission(CameraService.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
            manager.openCamera(pickedCamera, cameraStateCallback, null);
            imageReader = ImageReader.newInstance(320, 240, ImageFormat.JPEG, 5 /* images buffered */);
            imageReader.setOnImageAvailableListener(onImageAvailableListener, null);
            Log.i(TAG, "imageReader created");
        } catch (CameraAccessException e){
            Log.e(TAG, e.getMessage());
        }
    }


    /**
     *  Return the Camera Id which matches the field CAMERACHOICE.
     */
    public String getCamera(CameraManager manager){
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation == CAMERACHOICE) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("CAMERA", "onStartCommand flags " + flags + " startId " + startId);

        FirebaseApp.initializeApp(this);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();



        readyCamera();

        return super.onStartCommand(intent, flags, startId);
    }

    public void actOnReadyCameraDevice()
    {
        try {
            cameraDevice.createCaptureSession(Arrays.asList(imageReader.getSurface()), sessionStateCallback, null);
        } catch (CameraAccessException e){
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        try {
            session.abortCaptures();
        } catch (CameraAccessException e){
            Log.e(TAG, e.getMessage());
        }
        session.close();
    }

    /**
     *  Process image data as desired.
     */
    private void processImage(Image image){
        //Process image data
        Log.d("HEIGHT", ""+image.getHeight());
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

        Log.d("CAMERA", ""+options.outHeight + "   " + options.outWidth);
        if (bitmapImage!=null)
            smile_prob = checkSmile(bitmapImage);
            String dir =  saveImage(bitmapImage);
            Uri uriSavedImage = Uri.fromFile(new File(dir));
            uploadImage(uriSavedImage);
    }

    protected CaptureRequest createCaptureRequest() {
        try {
            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            builder.addTarget(imageReader.getSurface());
            return builder.build();
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private FaceDetector detector;
    private float smile_prob;

    private float checkSmile (Bitmap bitmap){

        detector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_CLASSIFICATIONS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        float smile=-1;
        float size=0;

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = detector.detect(frame);

        for (int index = 0; index < faces.size(); ++index) {
            Face face = faces.valueAt(index);

            if (face.getHeight()*face.getWidth() > size) {
                size = face.getHeight()*face.getWidth();
                smile = face.getIsSmilingProbability();
            }


        }
        Toast.makeText(this, ""+ smile , Toast.LENGTH_LONG).show();

        return smile;
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
        String fname = "AAA-" + n ;
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

            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpeg").build();


            Random generator = new Random();

            StorageReference ref = storageReference.child("images/"+ "imageJPEG" + generator.nextInt(1000) );
            ref.putFile(filePath,metadata)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //progressDialog.dismiss();


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
