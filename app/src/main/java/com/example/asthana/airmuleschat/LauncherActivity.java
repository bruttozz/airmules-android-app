package com.example.asthana.airmuleschat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LauncherActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        Button btnOpenChat = (Button) findViewById(R.id.openChatButton);
        btnOpenChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(LauncherActivity.this, MainActivity.class);

                LauncherActivity.this.startActivity(myIntent);
            }
        });
        Button btnOpenPayments = (Button) findViewById(R.id.btnOpenPayment);
        btnOpenPayments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(LauncherActivity.this, PaymentActivity.class);

                LauncherActivity.this.startActivity(myIntent);
            }
        });

        Button btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startTracking = new Intent(LauncherActivity.this, TrackingActivity.class);
                LauncherActivity.this.startActivity(startTracking);
            }
        });

        Button btnTransactions = (Button) findViewById(R.id.btnTransactions);
        btnTransactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewTransactions = new Intent(LauncherActivity.this, AllTransactionsActivity.class);
                LauncherActivity.this.startActivity(viewTransactions);
            }
        });

        Button btnFlightAPI = (Button) findViewById(R.id.btnFlightAPI);
        btnFlightAPI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent flights = new Intent(LauncherActivity.this, FlightActivity.class);
                LauncherActivity.this.startActivity(flights);
            }
        });
        Button btnAuth = (Button) findViewById(R.id.btnAuth);
        btnAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LauncherActivity.this, AuthActivity.class);
                LauncherActivity.this.startActivity(i);
            }
        });
    }
}
