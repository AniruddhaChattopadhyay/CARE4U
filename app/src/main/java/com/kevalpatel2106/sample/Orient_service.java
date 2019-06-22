package com.kevalpatel2106.sample;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

public class Orient_service extends Service implements SensorEventListener {

    SensorManager sensorManager;
    Sensor orient;

    SendData sendData = new SendData();
    postData post_data = new postData();



    public Orient_service() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        orient = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (orient != null) {
            sensorManager.registerListener((SensorEventListener) Orient_service.this, orient, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.d("Sensor", "Gyro Error");
        }

        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        String g1, g2, g3;

        g1 = "a";
        g2 = "a";
        g3 = "a";

        Sensor sensor = event.sensor;

        if (sensor.getType() == sensor.TYPE_ORIENTATION) {
            Log.d("ACC", "" + event.values[0]);
            float xx = event.values[0];
            g1 = Float.toString(xx);

            float yy = event.values[1];
            g2 = Float.toString(yy);

            float zz = event.values[2];
            g3 = Float.toString(zz);
        }

        sendData.sendData(g1, g2, g3, "https://script.google.com/macros/s/AKfycbxxufbgx3DN8plqIhaK453ZVK3J0XgBC2ymfUxEbdEHmQQ99UA/exec");
        //post_data.post(g1,g2,g3,"Orient");

    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
