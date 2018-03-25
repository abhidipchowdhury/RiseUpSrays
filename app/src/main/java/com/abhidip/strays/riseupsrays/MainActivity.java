package com.abhidip.strays.riseupsrays;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button goToSignUpButn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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
