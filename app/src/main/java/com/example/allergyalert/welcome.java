package com.example.allergyalert;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

public class welcome extends AppCompatActivity {

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        progressBar=findViewById(R.id.logo_pb);

        Thread thread= new Thread(){
            @Override
            public void run() {
                try {
                    sleep(3000);
                    startActivity( new Intent(welcome.this, MainActivity.class));
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();


    }
}
