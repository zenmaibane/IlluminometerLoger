package com.example.tokei.checkpsylliumlx;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    SensorManager sensorManager;
    boolean isWorking = false;
    String fileName = null;
    TimerLog timerLog = new TimerLog();
    Timer timer = new Timer();
    int dTime = 5000; //Timerの繰り返す時間(ms)
    long measuringTime; // 計測時間(m)
    long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.sensorSwitch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isWorking) {
                    stopMonitoring();
                } else {
                    EditText fileNameText = (EditText) findViewById(R.id.saveFileName);
                    EditText measuringTimeText = (EditText) findViewById(R.id.measuringTime);
                    fileName = fileNameText.getText().toString().trim();
                    if (isNullorBlank(fileName)) {
                        Toast.makeText(MainActivity.this, "ファイル名を入力してください", Toast.LENGTH_LONG).show();
                    }else if (isNullorBlank(measuringTimeText.getText().toString())){
                        Toast.makeText(MainActivity.this, "計測時間を入力してください", Toast.LENGTH_LONG).show();
                    }
                    else {
                        measuringTime = Long.parseLong(measuringTimeText.getText().toString()) * 60 * 1000;
                        timerLog.setFileName(fileName);
                        startMonitoring();
                    }
                }
            }
        });
    }

    protected boolean isNullorBlank(String str){
        return (str == null || str.equals(""));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        TextView textView = (TextView) findViewById(R.id.sensorResult);
        String message = "モニタが開始されていません";
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            timerLog.setContent(String.valueOf(event.values[0]));
            if (isWorking) {
                message = "照度:" + event.values[0];
                if (System.currentTimeMillis() - startTime >= measuringTime) {
                    stopMonitoring();
                    Toast.makeText(MainActivity.this, "計測が終了しました．", Toast.LENGTH_LONG).show();
                }
            }
        }
        textView.setText(message);
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
    protected void onResume() {
        super.onResume();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_LIGHT);
        if (0 < sensors.size()) {
            sensorManager.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_UI);
        }
    }

    private void startMonitoring() {
        TextView textView = (TextView) findViewById(R.id.sensorSwitch);
        isWorking = true;
        textView.setText("モニター停止");
        timer.schedule(timerLog, 0, dTime);
        startTime = System.currentTimeMillis();
    }

    private void stopMonitoring() {
        TextView textView = (TextView) findViewById(R.id.sensorSwitch);
        isWorking = false;
        textView.setText("モニター開始");
        timer.cancel();
        startTime = 0;
    }

    private void sampleFileOutput(String filename, String content) {
        try {
            FileOutputStream file = openFileOutput(filename + ".csv", MODE_APPEND);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(file));
            out.write(content);
            out.newLine();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class TimerLog extends TimerTask {
        String fileName;
        String content;
        int time = 0;

        public void setContent(String content) {
            this.content = content;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public void run() {
            if (fileName != null && content != null) {
                content = String.valueOf(time) + "," + content;
                sampleFileOutput(this.fileName, this.content);
            }
            time += dTime / 1000;
        }
    }
}

