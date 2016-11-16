package com.example.tokei.checkpsylliumlx;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
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
    Timer timer;
    TimerLog timerLog;
    int dTime = 5000; //Timerの繰り返す時間(ms)
    long measuringTime; // 計測時間(m)
    long startTime;
    private Handler mHandler = new Handler();
    private Runnable updateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialization();
        setContentView(R.layout.activity_main);
        TextView systemMessage = (TextView) findViewById(R.id.systemMessage);
        systemMessage.setText("モニタを開始してください．");
        findViewById(R.id.sensorSwitch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isWorking) {
                    stopMonitoring();
                } else {
                    EditText fileNameText = (EditText) findViewById(R.id.saveFileName);
                    EditText measuringTimeText = (EditText) findViewById(R.id.measuringTime);
                    fileName = fileNameText.getText().toString().trim();
                    if (isNullOrBlank(fileName)) {
                        Toast.makeText(MainActivity.this, "ファイル名を入力してください", Toast.LENGTH_LONG).show();
                    }else if (isNullOrBlank(measuringTimeText.getText().toString())){
                        Toast.makeText(MainActivity.this, "計測時間を入力してください", Toast.LENGTH_LONG).show();
                    }
                    else {
                        measuringTime = Long.parseLong(measuringTimeText.getText().toString()) * 60 * 1000;
                        initialization();
                        timerLog.setFileName(fileName);
                        startMonitoring();
                    }
                }
            }
        });
    }

    protected boolean isNullOrBlank(String str){
        return (str == null || str.equals(""));
    }
    protected void initialization(){
        timer = new Timer();
        timerLog = new TimerLog();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            timerLog.setContent(String.valueOf(event.values[0]));
            if (isWorking) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                TextView systemMessage = (TextView) findViewById(R.id.systemMessage);
                systemMessage.setText("計測中(経過時間"+ elapsedTime/1000+ "秒)\t照度:" + event.values[0]);
                if (elapsedTime >= measuringTime) {
                    stopMonitoring();
                }
            }
        }
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

        int countDown = 5;
        Toast.makeText(MainActivity.this, String.valueOf(countDown) + "秒後に開始します", Toast.LENGTH_LONG).show();
        updateText = new Runnable() {
            public void run() {
                TextView sensorSwitch = (TextView) findViewById(R.id.sensorSwitch);
                isWorking = true;
                sensorSwitch.setText("モニター停止");
                timer.schedule(timerLog, 0, dTime);
                startTime = System.currentTimeMillis();
            }
        };
        mHandler.postDelayed(updateText, countDown * 1000);
    }

    private void stopMonitoring() {
        TextView sensorSwitch = (TextView) findViewById(R.id.sensorSwitch);
        TextView systemMessage = (TextView) findViewById(R.id.systemMessage);
        isWorking = false;
        sensorSwitch.setText("モニター開始");
        systemMessage.setText("計測が終了しました");
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
                sampleFileOutput(this.fileName, String.valueOf(time) + "," + content);
            }
            time += dTime / 1000;
        }
    }
}

