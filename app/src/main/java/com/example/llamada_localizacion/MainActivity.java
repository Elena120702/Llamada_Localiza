package com.example.llamada_localizacion;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {
    EditText tvtel;
    Button btnentra;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvtel = findViewById(R.id.tvtel);
        btnentra = findViewById(R.id.btnentra);


        sharedPreferences= getSharedPreferences("Telefono",MODE_PRIVATE);

        btnentra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String numero = tvtel.getText().toString();
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putString("numero", numero);
                editor.apply();

                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                startActivity(intent);
            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        String numero=sharedPreferences.getString("numero", "");
        if (!numero.isEmpty()) {
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            startActivity(intent);
        }
    }
}