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

    // varijable na razini klase (atributi), dostupni u svim funkcijama (metodama) unutar klase
    private SQLiteDatabase db;
    private String LOGTAG = "DANTE-LOG-database";
    private TextView txtNum, txtData;
    private String strURL = "https://opentdb.com/api.php?type=multiple&amount=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        // inicijalizacija baze podataka (po potrebi kreiranje, update)
        QuestionDbHelper dbHelper = new QuestionDbHelper(getApplicationContext());
        // objekt za rad s bazom
        db = dbHelper.getWritableDatabase();

        txtData = findViewById(R.id.txtData);
        txtNum = findViewById(R.id.txtNum);

        // dohvaćanje novih pitanja
        Button btnFetch = findViewById(R.id.btnFetch);
        btnFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // pokretanje zadatka za dohvaćanje, šaljemo mu URL + broj iz polja txtNum
                // vrijednost se šalje API-ju bez provjere, ako je neispravna neka je on kontrolira
                new FetchTask().execute(strURL + txtNum.getText());
            }
        });

        // prikaz 10 nasumičnih pitanja iz baze
        Button btnRead = findViewById(R.id.btnRead);
        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readQuestions();
                //readAllQuestions();
            }
        });

        // prikaz statistike baze (broj kategorija, pitanja, odgovora, pitanja po kategoriji)
        Button btnStat = findViewById(R.id.btnStat);
        btnStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshData();
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

        // inicijalno se prikazuje statistika baze
        refreshData();

    }

    // prikaz statistike baze podataka
    private void refreshData() {
        if(db == null) {
            return;
        }
        // upiti -> dohvaćanje broja kategorija, pitanja i odgovora
        // db.query prima 7 parametara
        //      tablica
        //      polja koja se dohvaćaju
        //      izraz za uvjete (npr. question=? and categoryId=?)
        //      vrijednosti za uvjete (ako postoje) (npr. 10, "Art")
        //      polja za grupiranje upita (ako ih ima i ako se koriste funkcije kao što su sum, count i sl (aggregate functions))
        //      polja za having dio upita (filter po agregiranim funkcijama)
        //      polja za sortiranje podataka
        String[] projection = {"count(*)"};
        Cursor c1 = db.query("categories", projection, null, null, null, null, null);
        Cursor c2 = db.query("questions", projection, null, null, null, null, null);
        Cursor c3 = db.query("answers", projection, null, null, null, null, null);

        // očekujemo točno jedan red pa je dovoljno postaviti rezultate na prvi red
        c1.moveToFirst();
        c2.moveToFirst();
        c3.moveToFirst();

        String ret = "Br. kategorija: " + c1.getLong(0) + "\n";
        ret += "Br. pitanja: " + c2.getLong(0) + "\n";
        ret += "Br. odgovora: " + c3.getLong(0) + "\n";

        // čitanje podataka iz više tablica -> nije moguće koristiti query metodu jer ona čita jednu tablicu nego pišemo cijeli upit sa rawQuery
        // drugi parametar je popis varijabli upita, ako ih ima (sada ih nema jer čitamo sve)
        c1 = db.rawQuery("select c.name, count(*) from categories c, questions q where c.id = q.categoryId group by c.name order by 2 desc", null);
        ret += "\nBr. pitanja po kategoriji: \n";
        while (c1.moveToNext()) {
            ret += c1.getLong(1) + " - " + c1.getString(0) + "\n";
        }

        txtData.setText(ret);

    }

    // dohvaćanje 10 nasumičnih pitanja iz baze podataka
    private void readQuestions() {
        if(db == null) {
            return;
        }

        // upit za dohvaćanje 10 pitanja
        //      -> RANDOM() je SQLite funkcija
        //      -> limit 10 -> vraća prvih 10 rezultata upita
        Cursor questions = db.rawQuery("select id, question from questions order by RANDOM() limit 10", null);
        String ret = "";
        int i = 1;
        while(questions.moveToNext()) {
            // rezultat zapisujemo u HTML formatu (npr. <b></b> za bold, <br /> za novi red, &nbsp; za više razmaka)
            ret += i + ".  <b>" + questions.getString(1) + "</b><br />";
            Cursor answers = db.rawQuery("select answer, correct from answers where questionId=? order by RANDOM()", new String[]{questions.getString(0)});
            while(answers.moveToNext()) {
                ret += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + answers.getString(0) + " (" + answers.getString(1) + ")<br />";
            }
            ret += "<br />";
            i++;
        }
        // pretvaranje HTML-a u tekst
        txtData.setText(Html.fromHtml(ret, Html.FROM_HTML_MODE_COMPACT));
    }

    // dohvćanje svih pitanja iz baze
    private void readAllQuestions() {
        if(db == null) {
            return;
        }
        Log.i(LOGTAG, "readAllQuestions");
        Cursor questions = db.rawQuery("select id, question from questions order by question", null);
        Log.i(LOGTAG, "after query");
        String ret = "";
        int i = 1;
        while(questions.moveToNext()) {
            ret += i + ".  <b>" + questions.getString(1) + "</b><br />";
            /*Cursor answers = db.rawQuery("select answer, correct from answers where questionId=? order by RANDOM()", new String[]{questions.getString(0)});
            while(answers.moveToNext()) {
                ret += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + answers.getString(0) + " (" + answers.getString(1) + ")<br />";
            }
            ret += "<br />";*/
            i++;
        }
        Log.i(LOGTAG, "setText");
        txtData.setText(Html.fromHtml(ret, Html.FROM_HTML_MODE_COMPACT));
        Log.i(LOGTAG, "end");
    }

    // DB helper - nužno dodati za rad sa SQLite bazom
    class QuestionDbHelper extends SQLiteOpenHelper {

        // konstante, verzija i naziv baze
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "questions.db";

        // konstruktor, mora postojati
        // važno mu je poslati verziju baze i naziv baze, na temelju toga određuje što će napraviti
        // on pamti koja baza je već stvorena i s kojom verzijom, ako baza ne postoji pozvat će onCreate, ako postoji, ali druga verzija pozvat će onUpgrade
        public QuestionDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // stvaranje baze podataka -> stvaranje tablica, punjenje inicijalnih podataka i slično
        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL("CREATE TABLE categories (id INTEGER PRIMARY KEY, name TEXT)");
            sqLiteDatabase.execSQL("CREATE TABLE questions (id INTEGER PRIMARY KEY, categoryId INTEGER, question TEXT, difficulty TEXT)");
            sqLiteDatabase.execSQL("CREATE TABLE answers (id INTEGER PRIMARY KEY, questionId INTEGER, answer TEXT, correct INTEGER)");
        }

        // onUpgrade -> npr. kada napravimo drugu verziju aplikacije, možda se struktura tablica treba promijeniti -> ovdje ubaciti kôd za to
        // najjednostavnije je izbrisati sve i ponovno kreirati prazne tablice, ali obično ne želimo resetirati aplikaciju ako smo dodali polje
        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            // ToDo
            onCreate(sqLiteDatabase);
        }
    }

    // isti način asinkronog dohvaćanja podataka s interneta kao i kod InternetActivity, s razlikom da se ovdje podaci spremaju u bazu podataka
    class FetchTask extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;
        private String LOGTAG = "DANTE-LOG-internet-task";

        @Override
        protected void onPreExecute() {
            // prije početka izvođenja -> prikaz okvira s informacijom da učitavanje traje
            progressDialog = ProgressDialog.show(DatabaseActivity.this, "Dohvaćanje podataka", "Molimo pričekajte...");
        }

        @Override
        protected void onPostExecute(String s) {
            // nakon završetka dohvaćanja -> obrada dohvaćenih podataka
            progressDialog.setTitle("Obrada podataka");

            // broj dohvaćenih i unesenih pitanja, za prikaz poruke na kraju
            int numQuestions = 0, numInserted = 0;

            // obrada podataka:
            //      pročitati podatke iz JSON-a
            //      pronaći ID kategorije, ako ne postoji unijeti je
            //      provjeriti postoji li pitanje već u bazi
            //          ako postoji, preskoči
            //          ako ne postoji -> unesi pitanje i odgovore
            try {
                JSONObject json = new JSONObject(s);
                Log.i(LOGTAG, "JSON processed");
                JSONArray questions = json.getJSONArray("results");
                numQuestions = questions.length();
                for(int i = 0; i < questions.length(); i++) {
                    Log.i(LOGTAG, "Processing q=" + i);
                    JSONObject q = questions.getJSONObject(i);

                    // zasada učitavamo samo pitanja s više odgovora (postoje još da-ne pitanja)
                    if(!q.getString("type").equals("multiple")) {
                        continue;
                    }

                    // čitanje podataka o pitanju
                    String category = q.getString("category");
                    String question = q.getString("question");
                    String difficulty = q.getString("difficulty");
                    String correct = q.getString("correct_answer");

                    String answer1 = q.getJSONArray("incorrect_answers").getString(0);
                    String answer2 = q.getJSONArray("incorrect_answers").getString(1);
                    String answer3 = q.getJSONArray("incorrect_answers").getString(2);

                    // obrada znakova koji su kodirani po HTML pravilima (npr. pretvara &quot; u navodnik)
                    category = Html.fromHtml(category, Html.FROM_HTML_MODE_COMPACT).toString();
                    question = Html.fromHtml(question, Html.FROM_HTML_MODE_COMPACT).toString();
                    difficulty = Html.fromHtml(difficulty, Html.FROM_HTML_MODE_COMPACT).toString();
                    correct = Html.fromHtml(correct, Html.FROM_HTML_MODE_COMPACT).toString();
                    answer1 = Html.fromHtml(answer1, Html.FROM_HTML_MODE_COMPACT).toString();
                    answer2 = Html.fromHtml(answer2, Html.FROM_HTML_MODE_COMPACT).toString();
                    answer3 = Html.fromHtml(answer3, Html.FROM_HTML_MODE_COMPACT).toString();

                    // provjera postoji li kategorija -> iz baze se dohvaća ID kategorije dobivenim nazivom
                    Cursor c = db.query("categories", new String[] {"id"}, "name=?", new String[] {category}, null, null, null, null);
                    long categoryId = -1;

                    if(c.moveToFirst()) {
                        // ako postoji prvi red u rezultatima -> kategorija postoji
                        categoryId = c.getLong(0);
                    } else {
                        // nema prvog rezultata -> nema rezultata -> unos kategorije
                        ContentValues values = new ContentValues();
                        values.put("name", category);
                        // insert vraća ID unesenog reda, to je ID kategorije koji nam treba za dalje
                        categoryId = db.insert("categories", null, values);
                    }

                    // provjera postoji li pitanje već u bazi (po tekstu pitanja), isto kao i za kategoriju
                    c = db.query("questions", new String[] {"id"}, "question = ?", new String[] {question}, null, null, null, null);

                    if(c.moveToFirst()) {
                        // pitanje već postoji, obrada ovog pitanja je gotova
                        continue;
                    }
                    // ne treba nam ELSE blok, continue se vraća na početak petlje (nije krivo ako ga stavimo)

                    // upisivnaje pitanja u bazu
                    ContentValues values = new ContentValues();
                    values.put("categoryId", categoryId);
                    values.put("question", question);
                    values.put("difficulty", difficulty);
                    long qId = db.insert("questions", null, values);

                    // upisivanje odgovora u bazu
                    values = new ContentValues();
                    values.put("questionId", qId);
                    values.put("answer", correct);
                    values.put("correct", 1);
                    db.insert("answers", null, values);

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