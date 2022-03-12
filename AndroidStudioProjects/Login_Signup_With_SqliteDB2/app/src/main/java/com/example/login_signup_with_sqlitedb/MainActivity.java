package com.example.login_signup_with_sqlitedb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void register(View view) {
        startActivity(new Intent(getApplicationContext(),Signup.class));
    }

    public void login(View view) {
        startActivity(new Intent(getApplicationContext(),Login.class));
    }
}