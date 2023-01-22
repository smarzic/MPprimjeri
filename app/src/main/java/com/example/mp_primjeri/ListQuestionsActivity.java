package com.example.mp_primjeri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListQuestionsActivity extends AppCompatActivity {

    SQLiteDatabase db;
    RecyclerView recyclerView;
    CustomAdapter mAdapter;
    String LOGTAG = "DANTE-LOG-recycler";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_questions);

        // dohvaćanje baze podataka, čitanje pitanja iz baze
        DatabaseActivity.QuestionDbHelper dbHelper = new DatabaseActivity.QuestionDbHelper(getApplicationContext());
        db = dbHelper.getReadableDatabase();

        Cursor questions = db.rawQuery("select q.id, c.name, question, difficulty " +
                "from questions q, categories c " +
                "where q.categoryId = c.id " +
                "order by question", null);

        // postavljanje RecyclerViewa
        recyclerView = findViewById(R.id.recyclerQuestions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // postavljanje adaptera - klasa koju sami moramo pripremiti za rad s podacima u RecyclerViewu
        mAdapter = new CustomAdapter(this, questions);
        recyclerView.setAdapter(mAdapter);
        // crta ispod svakog pitanja
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

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
    // Adapter za RecyclerView
    class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        private LayoutInflater layoutInflater;
        Cursor localDataSet;
        public CustomAdapter(Context context, Cursor data) {
            layoutInflater = LayoutInflater.from(context);
            localDataSet = data;
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.question_layout, parent, false);
            return new ViewHolder(view);
        }

        // metoda za postavljanje konkretnog pitanja (red RecyclerViewa)
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Log.i(LOGTAG, "onBindViewHolder " + position);

            // dohvaćanje odgovarajućeg pitanja i čitanje odgovora iz baze
            localDataSet.moveToPosition(position);
            String id = localDataSet.getString(0);
            Cursor answers = db.rawQuery("select answer, correct from answers where questionId = ? order by RANDOM()", new String[] {id});

            // pisanje sadržaja u kontrole
            holder.txtCategory.setText(localDataSet.getString(1) + " (" + localDataSet.getString(3) + ")");
            holder.txtQuestion.setText(localDataSet.getString(2));
            String correct = "";
            answers.moveToPosition(0);
            holder.txtAns1.setText(answers.getString(0));
            if(answers.getLong(1) == 1) {
                correct = answers.getString(0);
            }
            answers.moveToPosition(1);
            holder.txtAns2.setText(answers.getString(0));
            if(answers.getLong(1) == 1) {
                correct = answers.getString(0);
            }
            answers.moveToPosition(2);
            holder.txtAns3.setText(answers.getString(0));
            if(answers.getLong(1) == 1) {
                correct = answers.getString(0);
            }
            answers.moveToPosition(3);
            holder.txtAns4.setText(answers.getString(0));
            if(answers.getLong(1) == 1) {
                correct = answers.getString(0);
            }
            final String c = correct;

            // reset kontrola u listi (na klik se točni odgovor bolda, a netočni precrtaju)
            holder.txtAns1.setTypeface(null, Typeface.NORMAL);
            holder.txtAns2.setTypeface(null, Typeface.NORMAL);
            holder.txtAns3.setTypeface(null, Typeface.NORMAL);
            holder.txtAns4.setTypeface(null, Typeface.NORMAL);
            holder.txtAns1.setPaintFlags(holder.txtAns1.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.txtAns2.setPaintFlags(holder.txtAns1.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.txtAns3.setPaintFlags(holder.txtAns1.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.txtAns4.setPaintFlags(holder.txtAns1.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);

            // postavljanje pozadine za parna/neparna pitanja
            if(position % 2 == 0) {
                holder.layout.setBackgroundColor(Color.argb(10, 0,0,0));
            } else {
                holder.layout.setBackgroundColor(Color.argb(0, 0,0,0));
            }

            // funkcija za klik na odgovor, uspoređuje se točan odgovor s tekstom koji piše na kontroli
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView txtView = (TextView) view;
                    Log.i(LOGTAG, "Txt: " + txtView.getText() + ", c: " + c);
                    if(txtView.getText().equals(c)) {
                        txtView.setTypeface(txtView.getTypeface(), Typeface.BOLD);
                    } else {
                        txtView.setPaintFlags(txtView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                }
            };
            holder.txtAns1.setOnClickListener(onClickListener);
            holder.txtAns2.setOnClickListener(onClickListener);
            holder.txtAns3.setOnClickListener(onClickListener);
            holder.txtAns4.setOnClickListener(onClickListener);
        }

        // metoda za dohvaćanje broja redova, koristi se interno, mora vratiti točnu vrijednost
        @Override
        public int getItemCount() {
            return localDataSet.getCount();
        }

        // kontrola za upravljanje elementom RecyclerViewa, sadrži reference na kontrole instance
        public class ViewHolder extends RecyclerView.ViewHolder {
            public final TextView txtCategory, txtQuestion, txtAns1, txtAns2, txtAns3, txtAns4;
            public final LinearLayout layout;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                layout = itemView.findViewById(R.id.layoutQuestion);
                txtCategory = itemView.findViewById(R.id.txtCategory);
                txtQuestion = itemView.findViewById(R.id.txtQuestion);
                txtAns1 = itemView.findViewById(R.id.txtAns1);
                txtAns2 = itemView.findViewById(R.id.txtAns2);
                txtAns3 = itemView.findViewById(R.id.txtAns3);
                txtAns4 = itemView.findViewById(R.id.txtAns4);
            }
        }
    }
}

