package com.example.mp_primjeri;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class InternetActivity extends AppCompatActivity {

    private String LOGTAG = "DANTE-LOG-internet";
    private String strURL = "https://opentdb.com/api.php?amount=";
    private TextView txtData, txtNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet);

        txtData = findViewById(R.id.txtData);
        txtNum = findViewById(R.id.txtNum);

        Button btnFetch = findViewById(R.id.btnFetch);
        btnFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new FetchTask().execute(strURL + txtNum.getText());
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

    class FetchTask extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;
        private String LOGTAG = "DANTE-LOG-internet-task";

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(InternetActivity.this, "Dohvaćanje podataka", "Molimo pričekajte...");
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.setTitle("Obrada podataka");

            txtData.setText("");

            try {
                JSONObject json = new JSONObject(s);
                Log.i(LOGTAG, "JSON processed");
                JSONArray questions = json.getJSONArray("results");
                String res = "";
                for(int i = 0; i < questions.length(); i++) {
                    String question = questions.getJSONObject(i).getString("question");
                    question = Html.fromHtml(question, Html.FROM_HTML_MODE_COMPACT).toString();
                    res += (i+1) + ". " + question + "\n\n";

                }
                txtData.setText(res);
            } catch(Exception e) {
                Log.e(LOGTAG, "Processing exception: " + e.toString());
            }

            progressDialog.dismiss();
        }

        @Override
        protected String doInBackground(String... strings) {
            String strUrl = strings[0];
            Log.i(LOGTAG, "doInBackground called " + strUrl);

            String res = "";
            try {
                URL url = new URL(strUrl);
                StringBuilder builder = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
                String line = "";
                while((line = bufferedReader.readLine()) != null) {
                    builder.append(line);
                }
                res = builder.toString();
            } catch(Exception e) {
                Log.e(LOGTAG, "Exception: " + e.toString());
            }

            return res;
        }
    }
}