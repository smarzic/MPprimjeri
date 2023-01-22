package com.example.mp_primjeri;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DatabaseActivity extends AppCompatActivity {

    private SQLiteDatabase db;
    private String LOGTAG = "DANTE-LOG-database";
    private TextView txtNum, txtData;
    private String strURL = "https://opentdb.com/api.php?type=multiple&amount=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        QuestionDbHelper dbHelper = new QuestionDbHelper(getApplicationContext());

        db = dbHelper.getWritableDatabase();

        txtData = findViewById(R.id.txtData);
        txtNum = findViewById(R.id.txtNum);

        /*ContentValues values = new ContentValues();
        values.put("name", "Category 1");
        long newRowId = dbW.insert("categories", null, values);
        Log.i(LOGTAG, "Category inserted! " + newRowId);*/


        String[] projection = {"count(*)"};

        Cursor cursor = db.query("categories", projection, null, null, null, null, null);
        cursor.moveToFirst();
        long r = cursor.getLong(0);
        Log.i(LOGTAG, "count = " + r);

        Button btnFetch = findViewById(R.id.btnFetch);
        btnFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new FetchTask().execute(strURL + txtNum.getText());
            }
        });

        Button btnRead = findViewById(R.id.btnRead);
        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readQuestions();
            }
        });

        Button btnStat = findViewById(R.id.btnStat);
        btnStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshData();
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

        refreshData();

    }

    private void refreshData() {
        if(db == null) {
            return;
        }
        String[] projection = {"count(*)"};
        Cursor c1 = db.query("categories", projection, null, null, null, null, null);
        Cursor c2 = db.query("questions", projection, null, null, null, null, null);
        Cursor c3 = db.query("answers", projection, null, null, null, null, null);

        c1.moveToFirst();
        c2.moveToFirst();
        c3.moveToFirst();

        String ret = "Br. kategorija: " + c1.getLong(0) + "\n";
        ret += "Br. pitanja: " + c2.getLong(0) + "\n";
        ret += "Br. odgovora: " + c3.getLong(0) + "\n";

        c1 = db.rawQuery("select c.name, count(*) from categories c, questions q where c.id = q.categoryId group by c.name order by 2 desc", null);
        ret += "\nBr. pitanja po kategoriji: \n";
        while (c1.moveToNext()) {
            ret += c1.getLong(1) + " - " + c1.getString(0) + "\n";
        }


        txtData.setText(ret);

    }

    private void readQuestions() {
        if(db == null) {
            return;
        }
        Cursor questions = db.rawQuery("select id, question from questions order by RANDOM() limit 10", null);
        String ret = "";
        int i = 1;
        while(questions.moveToNext()) {
            ret += i + ".  <b>" + questions.getString(1) + "</b><br />";
            Cursor answers = db.rawQuery("select answer, correct from answers where questionId=? order by RANDOM()", new String[]{questions.getString(0)});
            while(answers.moveToNext()) {
                ret += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + answers.getString(0) + " (" + answers.getString(1) + ")<br />";
            }
            ret += "<br />";
            i++;
        }
        txtData.setText(Html.fromHtml(ret, Html.FROM_HTML_MODE_COMPACT));
    }

    class QuestionDbHelper extends SQLiteOpenHelper {

        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "questions.db";

        public QuestionDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL("CREATE TABLE categories (id INTEGER PRIMARY KEY, name TEXT)");
            sqLiteDatabase.execSQL("CREATE TABLE questions (id INTEGER PRIMARY KEY, categoryId INTEGER, question TEXT, difficulty TEXT)");
            sqLiteDatabase.execSQL("CREATE TABLE answers (id INTEGER PRIMARY KEY, questionId INTEGER, answer TEXT, correct INTEGER)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            // ToDo
            onCreate(sqLiteDatabase);
        }
    }

    class FetchTask extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;
        private String LOGTAG = "DANTE-LOG-internet-task";

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(DatabaseActivity.this, "Dohvaćanje podataka", "Molimo pričekajte...");
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.setTitle("Obrada podataka");

            int numQuestions = 0, numInserted = 0;

            try {
                JSONObject json = new JSONObject(s);
                Log.i(LOGTAG, "JSON processed");
                JSONArray questions = json.getJSONArray("results");
                numQuestions = questions.length();
                for(int i = 0; i < questions.length(); i++) {
                    Log.i(LOGTAG, "Processing q=" + i);
                    //Log.i(LOGTAG, "Data=" + questions.getString(i));
                    JSONObject q = questions.getJSONObject(i);

                    if(!q.getString("type").equals("multiple")) {
                        continue;
                    }

                    String category = q.getString("category");
                    String question = q.getString("question");
                    String correct = q.getString("correct_answer");
                    //Log.i(LOGTAG, "Processing q=" + i + " F1");
                    String answer1 = q.getJSONArray("incorrect_answers").getString(0);
                    String answer2 = q.getJSONArray("incorrect_answers").getString(1);
                    String answer3 = q.getJSONArray("incorrect_answers").getString(2);
                    String difficulty = q.getString("difficulty");

                    //Log.i(LOGTAG, "Processing q=" + i + " F2");
                    category = Html.fromHtml(category, Html.FROM_HTML_MODE_COMPACT).toString();
                    question = Html.fromHtml(question, Html.FROM_HTML_MODE_COMPACT).toString();
                    correct = Html.fromHtml(correct, Html.FROM_HTML_MODE_COMPACT).toString();
                    answer1 = Html.fromHtml(answer1, Html.FROM_HTML_MODE_COMPACT).toString();
                    answer2 = Html.fromHtml(answer2, Html.FROM_HTML_MODE_COMPACT).toString();
                    answer3 = Html.fromHtml(answer3, Html.FROM_HTML_MODE_COMPACT).toString();
                    difficulty = Html.fromHtml(difficulty, Html.FROM_HTML_MODE_COMPACT).toString();

                    //Log.i(LOGTAG, "Processing q=" + i + " F3");
                    // check if category exists, insert if it doesnt
                    Cursor c = db.query("categories", new String[] {"id"}, "name=?", new String[] {category}, null, null, null, null);
                    long categoryId = -1;
                    if(c.moveToFirst()) {
                        categoryId = c.getLong(0);
                    } else {
                        ContentValues values = new ContentValues();
                        values.put("name", category);
                        categoryId = db.insert("categories", null, values);
                    }

                    //Log.i(LOGTAG, "Processing q=" + i + " F4");

                    // check if question is already in database, insert if it is not
                    c = db.query("questions", new String[] {"count(*)"}, "question = ?", new String[] {question}, null, null, null, null);
                    //Log.i(LOGTAG, "Processing q=" + i + " F4.1 (" + question + ")");
                    if(!c.moveToFirst()) {
                        //Log.i(LOGTAG, "Processing q=" + i + " F4.1a (" + question + ")");
                        // question already exists, skip
                        refreshData();
                        progressDialog.dismiss();
                        return;
                    }

                    //Log.i(LOGTAG, "Processing q=" + i + " F5");

                    ContentValues values = new ContentValues();
                    values.put("categoryId", categoryId);
                    values.put("question", question);
                    values.put("difficulty", difficulty);
                    long qId = db.insert("questions", null, values);

                    //Log.i(LOGTAG, "Processing q=" + i + " F6");

                    values = new ContentValues();
                    values.put("questionId", qId);
                    values.put("answer", correct);
                    values.put("correct", 1);
                    db.insert("answers", null, values);

                    //Log.i(LOGTAG, "Processing q=" + i + " F7");

                    values.put("answer", answer1);
                    values.put("correct", 0);
                    db.insert("answers", null, values);

                    values.put("answer", answer2);
                    db.insert("answers", null, values);

                    values.put("answer", answer3);
                    db.insert("answers", null, values);

                    numInserted++;

                }
            } catch(Exception e) {
                Log.e(LOGTAG, "Processing exception: " + e.toString());
            }
            Toast.makeText(getApplicationContext(), "Dohvaćeno pitanja: " + numQuestions + ", novih pitanja: " + numInserted, Toast.LENGTH_LONG).show();
            refreshData();
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