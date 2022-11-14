package com.example.appcalidadaire;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    ImageButton btnCalidadAire;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCalidadAire = findViewById(R.id.btnCalidadAire);

        btnCalidadAire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent activityCalidadAire = new Intent(getApplicationContext(),
                        CalidadAireActivity.class);
                startActivity(activityCalidadAire);
            }
        });
    }
}