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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GeolocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    // varijable na razini klase (atributi), dostupni u svim funkcijama (metodama) unutar klase
    private ProgressBar progressBar;
    private TextView txtLat, txtLon, txtTime, txtSpeed, txtPrecision;
    private final int REQ_LOCATION = 1;
    private final String LOGTAG = "DANTE-LOG-geolocation";
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
    private MapView mapView;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geolocation);

        // inicijalizacija - dohvaćanje svih resursa koje ćemo kasnije koristiti
        // da ih ne treba svaki put dohvaćati
        progressBar = findViewById(R.id.progressBar);
        txtLat = findViewById(R.id.txtLat);
        txtLon = findViewById(R.id.txtLon);
        txtTime = findViewById(R.id.txtTime);
        txtSpeed = findViewById(R.id.txtSpeed);
        txtPrecision = findViewById(R.id.txtPrecision);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // inicijalizacija Google karte
        // uz sve ovo, u manifest je potrebno upisati API ključ za kartu kako bi karta radila
        // video upute -> https://www.youtube.com/watch?v=uINleRduCWM
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(null);
        mapView.getMapAsync(this);

        // dohvaćanje lokacije na klik gumba
        Button btnLocate = findViewById(R.id.btnLocate);
        // spremamo this u varijablu koja će biti dostupna u donjoj funkciji, u ovoj točki "this" je aktivnost, a niže je "this" onClickListener
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

        // povratak na početni ekran
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

    // provjera ima li aplikacija pravo pristupa lokaciji
    private boolean checkLocationPermission() {
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    // ugrađena funkcija koja se poziva kao odgovor na requestPermissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQ_LOCATION) {
            Log.i(LOGTAG, "onRequestPermissionsResult - REQ_LOCATION");
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // ako je dozvoljen pristup -> čitanje lokacije
                Log.i(LOGTAG,  "PERMISSION_GRANTED");
                accessLocation();
            } else {
                // ako nije dozvoljen pristup -> ispis na ekranu da nema dozvole
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
        // čitanje podataka o lokaciji
        Log.i(LOGTAG, "accessLocation called");
        if(!checkLocationPermission()) {
            Log.i(LOGTAG, "NO PERMISSION");
            return;
        }
        Log.i(LOGTAG, "permission OK");

        // tražimo trenutnu lokaciju (getCurrentLocation), postoji još getLastKnownLocation
        Task<Location> task = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null);

        // funkcija koja će se izvršiti kada je lokacija dobivena
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

                // objekt za formatiranje vrijednosti (uvijek ispisujemo 6 decimala, ne više i ne manje)
                DecimalFormat df = new DecimalFormat();
                df.setMinimumFractionDigits(6);
                df.setMaximumFractionDigits(6);
                float accuracy = location.getAccuracy(),
                        speed = location.getSpeed();
                double latitude = location.getLatitude(),
                        longitude = location.getLongitude();
                long time = location.getTime();

                // prikaz formatiranih vrijednosti
                txtLat.setText(df.format(latitude));
                txtLon.setText(df.format(longitude));

                // ako je karta inicijalizirana (mMap se postavlja na inicijalizaciji karte) - prikaz markera na karti
                if(mMap != null) {
                    Log.i(LOGTAG, "Placing marker");
                    // uvijek je jedan marker na karti
                    // ako ga nema -> postavlja se novi marker i sprema se u varijablu da ga drugi put pomaknemo
                    // ako je već postavljen -> postojećem markeru se pomiče pozicija na karti
                    // ako želimo svaki put novi marker, svaki put bi izvršili prvi slučaj (addMarker)
                    if(marker == null) {
                        Log.i(LOGTAG, "Adding marker (" + latitude + ", " + longitude + ")");
                        marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Last position"));
                    } else {
                        Log.i(LOGTAG, "Moving marker (" + latitude + ", " + longitude + ")");
                        marker.setPosition(new LatLng(latitude, longitude));
                    }
                }

                // promjena postavki formatiranja brojeva za ostale brojeve
                df.setMinimumFractionDigits(2);
                df.setMaximumFractionDigits(2);

                // prikaz brzine i preciznosti, preračunavanja
                txtSpeed.setText(df.format(speed) + " m/s (" + df.format(speed * 3.6) + " km/h)");
                txtPrecision.setText(df.format(accuracy) + " m");

                Date date = new Date(time);
                long now = System.currentTimeMillis();

                // format prikaza datuma i vremena
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy. H:mm:ss");

                df.setMinimumFractionDigits(1);
                df.setMaximumFractionDigits(1);

                // prikaz datuma i vremena očitanja, starosti očitanja
                txtTime.setText(simpleDateFormat.format(date) + "\n(prije " + df.format((now - time) / 1000) + " sec)");
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // metoda za inicijalizaciju karte, poziva se kada je karta spremna primiti naredbe
        Log.i(LOGTAG, "onMapReady");
        // spremamo objekt karte za kasnije korištenje (dodavanje markera i slično)
        mMap = googleMap;
        // postavljanje pozicije karte i zoom faktora
        LatLng rijeka = new LatLng(45.33573, 14.41609);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(rijeka, 11f));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }
}