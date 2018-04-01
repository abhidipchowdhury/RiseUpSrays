package com.abhidip.strays.riseupsrays;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button goToSignUpButn;
    Toolbar toolbar1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar1 = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar1);

        getSupportActionBar().setTitle("Rise Up...Strays");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar1.setElevation(10f);
        }

        // Locate the button in activity_main.xml
        goToSignUpButn = (Button) findViewById(R.id.goToSignUpButn);

        // Capture button clicks
        goToSignUpButn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                // Start NewActivity.class
                Intent myIntent = new Intent(MainActivity.this,
                        SignUpActivity.class);
                startActivity(myIntent);
            }
        });
            }
}
