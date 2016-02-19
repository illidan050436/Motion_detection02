package com.test02.illid.motion_detection02;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import static java.lang.Math.*;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private boolean isdetecting = false;
    private SensorManager sManager = null;
    private Sensor aSensor = null;
    private float xvalue = 0;
    private float yvalue = 0;
    private float zvalue = 0;
    private float xcal = 0;
    private float ycal = 0;
    private float zcal = 0;
    private ArrayList<Float> xcal_final = new ArrayList<>();
    private ArrayList<Float> ycal_final = new ArrayList<>();
    private ArrayList<Float> zcal_final = new ArrayList<>();
    private int value_count = 0;
    private String state_lr = "hold";
    private String state_ud = "hold";
    private String state_fb = "hold";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button cal = (Button) findViewById(R.id.button1);
        final Button start = (Button) findViewById(R.id.button2);
        sManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        aSensor = sManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        // calibration
        cal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (value_count >= 100) {
                    Float sum_x = 0.00f;
                    Float sum_y = 0.00f;
                    Float sum_z = 0.00f;
                    for (int i = 0; i < xcal_final.size(); i++) {
                        sum_x += xcal_final.get(i);
                        sum_y += ycal_final.get(i);
                        sum_z += zcal_final.get(i);
                    }
                    xcal = sum_x / xcal_final.size();
                    ycal = sum_y / ycal_final.size();
                    zcal = sum_z / zcal_final.size();
                    xcal_final.clear();
                    ycal_final.clear();
                    zcal_final.clear();
                    Log.e("calibration", " sum_x = " + sum_x);
                    Log.e("calibration", " sum_y = " + sum_y);
                    Log.e("calibration", " sum_z = " + sum_z);
                    Log.e("calibration", " x = " + xcal);
                    Log.e("calibration", " y = " + ycal);
                    Log.e("calibration", " z = " + zcal);
                    value_count = 0;
                }
            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isdetecting) {
                    start.setBackgroundColor(getResources().getColor(R.color.cyan));
                    start.setText(getResources().getText(R.string.stop));
                    isdetecting = false;
                } else {
                    start.setBackgroundColor(getResources().getColor(R.color.red));
                    start.setText(getResources().getText(R.string.start));
                    isdetecting = true;
                }
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        sManager.registerListener(this, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected void onPause() {
        super.onPause();
        sManager.unregisterListener(this);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            TextView xText = (TextView) findViewById(R.id.textView5);
            TextView yText = (TextView) findViewById(R.id.textView7);
            TextView zText = (TextView) findViewById(R.id.textView9);
            TextView pos_lr = (TextView) findViewById(R.id.textView1);
            TextView pos_ud = (TextView) findViewById(R.id.textView11);
            TextView pos_fb = (TextView) findViewById(R.id.textView10);
            float xtemp;
            float ytemp;
            float ztemp;
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                xvalue = event.values[0];
                yvalue = event.values[1];
                zvalue = event.values[2];
                if (value_count <= 100){
                    xcal_final.add(abs(xvalue));
                    ycal_final.add(abs(yvalue));
                    zcal_final.add(abs(zvalue));
                    value_count++;
                }
            }
            if (xvalue > 0.00f) {
                xtemp = xvalue - xcal;
            }else {
                xtemp = xvalue + xcal;
            }
            if (yvalue > 0.00f) {
                ytemp = yvalue - ycal;
            }else {
                ytemp = yvalue + ycal;
            }
            if (zvalue > 0.00f) {
                ztemp = zvalue - zcal;
            }else {
                ztemp = zvalue + zcal;
            }
            xText.setText(String.format("%f", xtemp));
            yText.setText(String.format("%f", ytemp));
            zText.setText(String.format("%f", ztemp));

            switch (state_lr) {
                case "left":
                    pos_lr.setText(getResources().getText(R.string.left));
                    break;
                case "right":
                    pos_lr.setText(getResources().getText(R.string.right));
                    break;
                default:
                    pos_lr.setText(getResources().getText(R.string.hold));
            }
            switch (state_fb) {
                case "forward":
                    pos_fb.setText(getResources().getText(R.string.forward));
                    break;
                case "back":
                    pos_fb.setText(getResources().getText(R.string.back));
                    break;
                default:
                    pos_fb.setText(getResources().getText(R.string.hold));
            }
            switch (state_ud) {
                case "up":
                    pos_ud.setText(getResources().getText(R.string.up));
                    break;
                case "down":
                    pos_ud.setText(getResources().getText(R.string.down));
                    break;
                default:
                    pos_ud.setText(getResources().getText(R.string.hold));
                    break;
            }

            switch (state_lr) {
                case "hold":
                    if (xtemp > 1)
                        state_lr = "right";
                    else if (xtemp < -1)
                        state_lr = "left";
                    break;
                case "left":
                    if (xtemp > 0.2)
                        state_lr = "hold";
                    break;
                case "right":
                    if (xtemp < -0.2)
                        state_lr = "hold";
                    break;
                default:
                    break;
            }
            switch (state_fb) {
                case "hold":
                    if (ytemp > 1)
                        state_fb = "forward";
                    else if (ytemp < -1)
                        state_fb = "back";
                    break;
                case "forward":
                    if (ytemp < -0.2)
                        state_fb = "hold";
                    break;
                case "back":
                    if (ytemp > 0.2)
                        state_fb = "hold";
                    break;
                default:
                    break;
            }
            switch (state_ud) {
                case "hold":
                    if (ztemp > 1)
                        state_ud = "up";
                    else if (ztemp < -1)
                        state_ud = "down";
                    break;
                case "up":
                    if (ztemp < -0.2)
                        state_ud = "hold";
                    break;
                case "down":
                    if (ztemp > 0.2)
                        state_ud = "hold";
                    break;
                default:
                    break;
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
