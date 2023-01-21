package com.example.mp_primjeri;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnAkcelerometar = findViewById(R.id.btnAkcelerometar);
        btnAkcelerometar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), AccelerometerActivity.class);
                startActivity(i);
            }
        });

        Button btnGeolocation = findViewById(R.id.btnGeolocation);
        btnGeolocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), GeolocationActivity.class);
                startActivity(i);
            }
        });

        Button btnInternet = findViewById(R.id.btnInternet);
        btnInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), InternetActivity.class);
                startActivity(i);
            }
        });

        Button btnDatabase = findViewById(R.id.btnDatabase);
        btnDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), DatabaseActivity.class);
                startActivity(i);
            }
        });
    }
}