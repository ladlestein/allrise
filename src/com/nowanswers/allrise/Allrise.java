package com.nowanswers.allrise;

import static java.lang.Math.PI ;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;

public class Allrise extends Activity {

    private enum Posture {
        horizontal, vertical, neither
    }

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnemometer;

    private float[] valuesMagnet = new float[3];
    private float[] valuesAccel = new float[3];
    private float[] valuesOrientation = new float[3];

    private int[] barsOrientation = new int[3];

    private float[] rotationMatrix = new float[9];

    private Posture lastPosture;

    final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("x", "Starting Allrise");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnemometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(eventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(eventListener, magnemometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private final SensorEventListener eventListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            // Handle the events for which we registered
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    System.arraycopy(event.values, 0, valuesAccel, 0, 3);
                    break;

                case Sensor.TYPE_MAGNETIC_FIELD:
                    System.arraycopy(event.values, 0, valuesMagnet, 0, 3);
                    break;
            }

            SensorManager.getRotationMatrix(rotationMatrix, null, valuesAccel, valuesMagnet);
            SensorManager.getOrientation(rotationMatrix, valuesOrientation);
//            Log.i("OrientationTestActivity", String.format("Orientation: %f, %f, %f",
//                    valuesOrientation[0], valuesOrientation[1], valuesOrientation[2]));



            for (int i = 0; i < 3; i++) {
                barsOrientation[i] = (int)Math.floor((valuesOrientation[i] + PI) * 10.0 / (2.0 * PI));
            }


           // Log.i("OrientationTestActivity", stars(barsOrientation[0]) + stars(barsOrientation[1]) + stars(barsOrientation[2]));

            boolean vertical = isVertical(valuesOrientation[1]) || isVertical(valuesOrientation[2]);
            boolean horizontal = isHorizontal(valuesOrientation[1]) && isHorizontal(valuesOrientation[2]);

            Posture posture = Posture.neither;
            if (vertical) {
                posture = Posture.vertical;
            }
            if (horizontal) {
                posture = Posture.horizontal;
            }

            if (posture != Posture.neither && posture != lastPosture) {
                tg.startTone(posture == Posture.vertical ? ToneGenerator.TONE_PROP_BEEP : ToneGenerator.TONE_PROP_NACK);
                lastPosture = posture;

                Log.i("Orientation", posture.name());
            }


        }

    };

    private static boolean isVertical(float value) {
        return Math.abs(Math.abs(value) - (PI / 2.0)) < 0.1;
    }

    private static boolean isHorizontal(float value) {
        return Math.abs(Math.abs(value) - (PI)) < 0.1 || Math.abs(value) < 0.1;
    }


    private static String stars(int n) {
        return "**********".substring(0, n) + "___________".substring(n);
    }
}