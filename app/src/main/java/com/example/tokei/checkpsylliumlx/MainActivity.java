package com.example.tokei.checkpsylliumlx;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity  implements SensorEventListener{
    SensorManager sensorManager;
    boolean isWorking = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.sensorSwitch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(MainActivity.this, "Hello Android!!",Toast.LENGTH_LONG).show();
                if (isWorking) {
                    stopMonitoring();
                } else {
                    startMonitoring();
                }
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        TextView textView = (TextView)findViewById(R.id.sensorResult);
        String str = "ボタンを押してください";
        if (isWorking && event.sensor.getType() == Sensor.TYPE_LIGHT){
            str = "照度:" + event.values[0];
        }
        textView.setText(str);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_LIGHT);
        if (0 < sensors.size()){
            sensorManager.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_UI);
        }
    }

    private void startMonitoring(){
        TextView textView = (TextView) findViewById(R.id.sensorSwitch);
        textView.setText("モニター停止");
        isWorking = true;
    }
    private void stopMonitoring(){
        TextView textView = (TextView) findViewById(R.id.sensorSwitch);
        textView.setText("モニター開始");
        isWorking = false;
    }
}

