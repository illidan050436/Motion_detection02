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
    public boolean isdetecting = false;
    public SensorManager sManager = null;
    public Sensor aSensor = null;
    public float xvalue = 0;
    public float yvalue = 0;
    public float zvalue = 0;
    public float xcal = 0;
    public float ycal = 0;
    public float zcal = 0;
    public ArrayList<Float> xcal_final = new ArrayList<>();
    public ArrayList<Float> ycal_final = new ArrayList<>();
    public ArrayList<Float> zcal_final = new ArrayList<>();
    public int value_count = 0;
    public String state = "hold";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button cal = (Button) findViewById(R.id.button1);
        final Button start = (Button) findViewById(R.id.button2);
        final TextView pos = (TextView) findViewById(R.id.textView1);
        sManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        aSensor = sManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        // calibration
        cal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (value_count >= 50) {
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
            TextView pos = (TextView) findViewById(R.id.textView1);
            float xtemp;
            float ytemp;
            float ztemp;
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                xvalue = event.values[0];
                yvalue = event.values[1];
                zvalue = event.values[2];
                if (value_count <= 50){
                    xcal_final.add(xvalue);
                    ycal_final.add(yvalue);
                    zcal_final.add(zvalue);
                    value_count++;
                }
            }
            xtemp = xvalue - xcal;
            ytemp = yvalue - ycal;
            ztemp = zvalue - zcal;
            xText.setText(String.format("%f", xtemp));
            yText.setText(String.format("%f", ytemp));
            zText.setText(String.format("%f", ztemp));

            switch (state){
                case "left":
                    pos.setText(getResources().getText(R.string.left));
                    break;
                case "right":
                    pos.setText(getResources().getText(R.string.right));
                    break;
                case "forward":
                    pos.setText(getResources().getText(R.string.forward));
                    break;
                case "back":
                    pos.setText(getResources().getText(R.string.back));
                    break;
                case "up":
                    pos.setText(getResources().getText(R.string.up));
                    break;
                case "down":
                    pos.setText(getResources().getText(R.string.down));
                    break;
                default:
                    pos.setText(getResources().getText(R.string.hold));
                    break;
            }

            switch (state){
                case "hold":
                    float max;
                    max = max(xtemp, ytemp);
                    max = max(max, ztemp);
                    if (max == xtemp) {
                        if (xtemp > 0.8)
                            state = "right";
                        else if (xtemp < -0.8)
                            state = "left";
                    }else if (max == ytemp) {
                        if (ytemp > 0.8)
                            state = "forward";
                        else if (ytemp < -0.8)
                            state = "back";
                    }else if (max == ztemp){
                        if (ztemp > 0.8)
                            state = "up";
                        else if (ztemp < -0.8)
                            state = "down";
                    }
                    break;
                case "left":
                    if (xtemp > 0.2)
                        state = "hold";
                    break;
                case "right":
                    if (xtemp < -0.2)
                        state = "hold";
                    break;
                case "forward":
                    if (ytemp < -0.2)
                        state = "hold";
                    break;
                case "back":
                    if (ytemp > 0.2)
                        state = "hold";
                    break;
                case "up":
                    if (ztemp < -0.2)
                        state = "hold";
                    break;
                case "down":
                    if (ztemp > 0.2)
                        state = "hold";
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
