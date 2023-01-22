package com.example.mp_primjeri;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;

public class AccelerometerActivity extends AppCompatActivity {

    // varijable na razini klase (atributi), dostupni u svim funkcijama (metodama) unutar klase
    private String LOGTAG = "DANTE-LOG-accelerometer";
    private MediaPlayer bell1, bell2;
    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;
    private float prevX, prevY, prevZ;
    private DecimalFormat df;
    TextView txtX, txtY, txtZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        // inicijalizacija - dohvaćanje svih resursa koje ćemo kasnije koristiti
        // da ih ne treba svaki put dohvaćati
        bell1 = MediaPlayer.create(this, R.raw.bell_123742);
        bell2 = MediaPlayer.create(this, R.raw.church_bell_5993);
        df = new DecimalFormat("+##0.00000;-##0.00000");
        df.setMinimumFractionDigits(5);
        df.setMaximumFractionDigits(5);
        txtX = findViewById(R.id.txtX);
        txtY = findViewById(R.id.txtY);
        txtZ = findViewById(R.id.txtZ);

        // gumb za povratak
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                // osim izlaska iz aktivonsti, ovdje još mičemo listener senzora (inače bi nastavio obrađivati očitanja sa senzora)
                sensorManager.unregisterListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
                finish();
            }
        });

        // akcelerometar - dobivanje varijable (objekta) za rad sa senzorom
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // definiranje listenera za senzor (što će se dogoditi na svakom očitanju)
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                // čitanje novih očitanja
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];

                // ispis očitanja u log + formatiranje da budu lakše čitljivi
                Log.i(LOGTAG, "onSensorChanged " + df.format(x) + ", " + df.format(y) + ", " + df.format(z));

                // obrada očitanja
                if(Math.abs(x - prevX) > 20) {
                    Log.i(LOGTAG, "small bell");
                    bell1.start();
                }
                if(Math.abs(z - prevZ) > 10) {
                    Log.i(LOGTAG, "big bell");
                    bell2.start();
                }

                // prikaz očitanja na ekranu
                txtX.setText(df.format(x));
                txtY.setText(df.format(y));
                txtZ.setText(df.format(z));

                // spremanje očitanja
                prevX = x;
                prevY = y;
                prevZ = z;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        // dodjeljivanje listenera senzoru
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);


    }
}