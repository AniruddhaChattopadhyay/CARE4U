package com.kevalpatel2106.sample;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Accelerometer_data extends Service implements SensorEventListener{

    private SensorManager sensorManager;
    Sensor acc;
    Sensor pos;

    SendData sendData = new SendData();


    public Accelerometer_data() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(acc!=null) {
            sensorManager.registerListener((SensorEventListener) Accelerometer_data.this, acc, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            Log.d("Sensor","Gyro Error");
        }

        pos = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if(pos!=null) {
            sensorManager.registerListener((SensorEventListener) Accelerometer_data.this, pos, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            Log.d("Sensor","Position Error");
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

        String x1,y1,z1,g1,g2,g3,o1,o2,o3;

        x1="a";
        y1="a";
        z1="a";
        g1="a";
        g2="a";
        g3="a";
        o1="a";
        o2="a";
        o3="a";

        Sensor sensor = event.sensor;

        if(sensor.getType() == sensor.TYPE_ACCELEROMETER) {
            Log.d("ACC", "" + event.values[0]);
            float x = event.values[0];
            x1 = Float.toString(x);

            float y = event.values[1];
            y1 = Float.toString(y);

            float z = event.values[2];
            z1 = Float.toString(z);
        }
        if(sensor.getType() == sensor.TYPE_GYROSCOPE) {
            Log.d("ACC", "" + event.values[0]);
            float xx = event.values[0];
            g1 = Float.toString(xx);

            float yy = event.values[1];
            g2 = Float.toString(yy);

            float zz = event.values[2];
            g3 = Float.toString(zz);
        }
        if(sensor.getType() == sensor.TYPE_ORIENTATION) {
            Log.d("ACC", "" + event.values[0]);
            float xxx = event.values[0];
            o1 = Float.toString(xxx);

            float yyy = event.values[1];
            o2 = Float.toString(yyy);

            float zzz = event.values[2];
            o3 = Float.toString(zzz);
        }
        sendData.sendData(x1,y1,z1,"https://script.google.com/macros/s/AKfycbxManrWNmgZZ2r6cttnfylpaAYL-FOyC7AZnV8c0edPeGCM-XQl/exec");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}
