package com.example.mp_primjeri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class NotificationActivity extends AppCompatActivity {

    private String CHANNEL_ID = "CHANNEL-1";
    private String CHANNEL_ID2 = "CHANNEL-2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // registracija kanala obavijesti, važno je da se pozove prije slanja obaveijsti, ne škodi ako je pozvan više puta
        createNotificationChannel();

        // obična obavijest
        Button btnMakeNotification = findViewById(R.id.btnMakeNotification);
        btnMakeNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textTitle = "Naslov obavijesti";
                String textContent = "Tekst obavijesti";
                int notificationId = 10;

                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setSmallIcon(R.drawable.location_pin_solid)
                        .setContentTitle(textTitle)
                        .setContentText(textContent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                notificationManager.notify(notificationId, builder.build());
            }
        });

        // uklanjanje obavijetsti programski - dovoljno je znati ID obavijesti (prvi parametar funkcije notify)
        Button btnRemoveNotification = findViewById(R.id.btnRemoveNotification);
        btnRemoveNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int notificationId = 10;
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                notificationManager.cancel(notificationId);
            }
        });

        // slanje obavijesti iz pozadinske obrade
        Button btnNotificationLoading = findViewById(R.id.btnNotificationLoading);
        btnNotificationLoading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                LoadingTask task = new LoadingTask();
                task.execute();
            }
        });

        // gumb za povratak
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

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Važne obavijesti";
            String description = "Važne obavijesti testne aplikacije";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            name = "Obične obavijesti";
            description = "Obavijesti testne aplikacije";
            importance = NotificationManager.IMPORTANCE_LOW;
            channel = new NotificationChannel(CHANNEL_ID2, name, importance);
            channel.setDescription(description);
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private class LoadingTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {

            String textTitle = "Demo obrada";
            String textContent = "Obrada traje...";
            int notificationId = 11;

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID2)
                    .setSmallIcon(R.drawable.location_pin_solid)
                    .setContentTitle(textTitle)
                    .setContentText(textContent)
                    .setPriority(NotificationCompat.PRIORITY_LOW);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            builder.setProgress(100, 0, false);
            notificationManager.notify(notificationId, builder.build());

            for(int i = 0; i <= 100; i++) {
                try {
                    Thread.sleep(100);
                    builder.setContentText("Obrada " + i + " od 100").setProgress(100, i, false);
                    notificationManager.notify(notificationId, builder.build());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            builder.setContentText("Obrada završena").setProgress(0, 0, false);
            notificationManager.notify(notificationId, builder.build());

            return null;
        }
    }
}