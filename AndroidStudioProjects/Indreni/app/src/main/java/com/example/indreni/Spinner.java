package com.example.indreni;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class Spinner extends AppCompatActivity {

    Spinner spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spinner);

        spinner=findViewById(R.id.spinner);

        ArrayList<String> arrayList=new ArrayList<String>();
        arrayList.add("Rahul");
        arrayList.add("Rohan sir");
        arrayList.add("Rupak sir");
        arrayList.add("Binod Sir");
        arrayList.add("Sunny Sir");
        arrayList.add("Asmit Sir");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,                         android.R.layout.simple_spinner_item, arrayList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);




    }
}