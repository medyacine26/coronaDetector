package com.example.coronadetector.formulaire;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coronadetector.R;

import java.util.Calendar;

public class MainActivityFormulaire extends AppCompatActivity {
    CheckBox male;
    CheckBox female;
    private EditText date;
    EditText pays;
    private Button skip;
    DatePickerDialog.OnDateSetListener dateSetListener1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_formulaire);

        pays=findViewById(R.id.editText3);
        male = findViewById(R.id.male);
        female = findViewById(R.id.female);
        skip = findViewById(R.id.next);
        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                female.setChecked(false);
            }
        });
        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                male.setChecked(false);
            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivityFormulaire.this, Main2Activity.class);
                int s=0;
                if(male.isChecked()||female.isChecked())s++;
                if(!pays.getText().toString().isEmpty())s++;
                if(s==2) {
                    startActivityForResult(i, 0);
                }
                else{
                    Toast toast=Toast.makeText(MainActivityFormulaire.this,"You have to fill all the fields",Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

    }
}
