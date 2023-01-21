package com.example.mp_primjeri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GeolocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ProgressBar progressBar;
    private TextView txtLat, txtLon, txtTime, txtSpeed, txtPrecision;
    private int REQ_LOCATION = 1;
    private String LOGTAG = "DANTE-LOG-geolocation";
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
    private MapView mapView;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geolocation);

        progressBar = findViewById(R.id.progressBar);
        txtLat = findViewById(R.id.txtLat);
        txtLon = findViewById(R.id.txtLon);
        txtTime = findViewById(R.id.txtTime);
        txtSpeed = findViewById(R.id.txtSpeed);
        txtPrecision = findViewById(R.id.txtPrecision);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // inicijalizacija karte
        mapView =findViewById(R.id.mapView);
        mapView.onCreate(null);
        mapView.getMapAsync(this);

        Button btnLocate = findViewById(R.id.btnLocate);
        Activity thisActivity = this;
        btnLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                if(!checkLocationPermission()) {
                    String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
                    ActivityCompat.requestPermissions(thisActivity, permissions, REQ_LOCATION);
                } else {
                    accessLocation();
                }
            }
        });

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    private boolean checkLocationPermission() {
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQ_LOCATION) {
            Log.i(LOGTAG, "onRequestPermissionsResult - REQ_LOCATION");
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(LOGTAG,  "PERMISSION_GRANTED");
                accessLocation();
            } else {
                Log.i(LOGTAG,  "PERMISSION_DENIED");
                txtLat.setText("DENIED");
                txtLon.setText("DENIED");
                txtSpeed.setText("DENIED");
                txtPrecision.setText("DENIED");
                txtTime.setText("DENIED");
                progressBar.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void accessLocation() {
        Log.i(LOGTAG, "accessLocation called");
        if(!checkLocationPermission()) {
            Log.i(LOGTAG, "NO PERMISSION");
            return;
        }
        Log.i(LOGTAG, "permission OK");
        Task<Location> task = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null);
        task.addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Log.i(LOGTAG, "onSuccess");
                if(location == null) {
                    Log.i(LOGTAG, "location is NULL");
                    txtLat.setText("UNKNOWN");
                    txtLon.setText("UNKNOWN");
                    txtSpeed.setText("UNKNOWN");
                    txtPrecision.setText("UNKNOWN");
                    txtTime.setText("UNKNOWN");
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }
                DecimalFormat df = new DecimalFormat();
                df.setMinimumFractionDigits(6);
                df.setMaximumFractionDigits(6);
                float accuracy = location.getAccuracy(),
                        speed = location.getSpeed();
                double latitude = location.getLatitude(),
                        longitude = location.getLongitude();
                long time = location.getTime();


                txtLat.setText(df.format(latitude));
                txtLon.setText(df.format(longitude));

                if(mMap != null) {
                    Log.i(LOGTAG, "Placing marker");
                    if(marker == null) {
                        Log.i(LOGTAG, "Adding marker (" + + latitude + ", " + longitude + ")");
                        marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Last position"));
                    } else {
                        Log.i(LOGTAG, "Moving marker (" + + latitude + ", " + longitude + ")");
                        marker.setPosition(new LatLng(latitude, longitude));
                    }
                }

                df.setMinimumFractionDigits(2);
                df.setMaximumFractionDigits(2);

                txtSpeed.setText(df.format(speed) + " m/s (" + df.format(speed * 3.6) + " km/h)");
                txtPrecision.setText(df.format(accuracy) + " m");

                Date date = new Date(time);
                long now = System.currentTimeMillis();

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.YYYY H:mm:ss");

                df.setMinimumFractionDigits(1);
                df.setMaximumFractionDigits(1);

                txtTime.setText(simpleDateFormat.format(date) + "\n(prije " + df.format((now - time) / 1000) + " sec)");
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.i(LOGTAG, "onMapReady");
        mMap = googleMap;
        LatLng rijeka = new LatLng(45.33573, 14.41609);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(rijeka, 11f));
        CameraPosition cp = googleMap.getCameraPosition();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }
}