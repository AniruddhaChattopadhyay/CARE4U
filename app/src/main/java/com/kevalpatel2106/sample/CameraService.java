package com.kevalpatel2106.sample;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import android.provider.Settings;
import android.util.SparseArray;
import android.widget.Toast;

import com.androidhiddencamera.CameraConfig;
import com.androidhiddencamera.CameraError;
import com.androidhiddencamera.HiddenCameraService;
import com.androidhiddencamera.HiddenCameraUtils;
import com.androidhiddencamera.config.CameraFacing;
import com.androidhiddencamera.config.CameraFocus;
import com.androidhiddencamera.config.CameraImageFormat;
import com.androidhiddencamera.config.CameraResolution;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;

/**
 * Created by Keval on 11-Nov-16.
 *
 * @author {@link 'https://github.com/kevalpatel2106'}
 */

public class CameraService extends HiddenCameraService {

    private CameraConfig mCameraConfig;
    private FaceDetector detector;
    private float smile_prob;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //MediaPlayer player1 = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
        //player1.start();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            if (HiddenCameraUtils.canOverDrawOtherApps(this)) {
                CameraConfig cameraConfig = new CameraConfig()
                        .getBuilder(this)
                        .setCameraFacing(CameraFacing.FRONT_FACING_CAMERA)
                        .setCameraResolution(CameraResolution.HIGH_RESOLUTION)
                        .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                        .setCameraFocus(CameraFocus.AUTO)
                        .build();

                startCamera(cameraConfig);



                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraService.this,
                        "Capturing image.", Toast.LENGTH_SHORT).show();

                        takePicture();
                    }
                }, 20L);
            } else {

                //Open settings to grant permission for "Draw other apps".
                HiddenCameraUtils.openDrawOverPermissionSetting(this);
            }
        } else {

            //TODO Ask your parent activity for providing runtime permission
            Toast.makeText(this, "Camera permission not available", Toast.LENGTH_SHORT).show();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onImageCapture(@NonNull File imageFile) {
        Toast.makeText(this, "Image Taken", Toast.LENGTH_SHORT).show();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
        Toast.makeText(this, "Image Created", Toast.LENGTH_SHORT).show();
        smile_prob = checkSmile(bitmap);

        MediaPlayer player = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
        player.start();

        Toast.makeText(this,
                "Captured image size is : " + imageFile.length(),
                Toast.LENGTH_SHORT)
                .show();

        // Do something with the image...

        stopSelf();
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

        stopSelf();
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
        SparseArray<Face> faces = detector.detect(frame);

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
