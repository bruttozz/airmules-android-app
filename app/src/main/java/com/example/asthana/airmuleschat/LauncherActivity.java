package com.example.asthana.airmuleschat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LauncherActivity extends BaseMenuActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        Button btnUserProfile = (Button) findViewById(R.id.btnSeeUserProfile);
        btnUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gotoUserProfile = new Intent(LauncherActivity.this, UserProfileActivity.class);
                LauncherActivity.this.startActivity(gotoUserProfile);
            }
        });



        Button btnOpenChat = (Button) findViewById(R.id.openChatButton);
        btnOpenChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(LauncherActivity.this, PersonalChat.class);
                myIntent.putExtra("chatID", "A2YQRPCTSoTKZCi18Clz1YE1JkJ3q4RP5s1MHaV5b72EvMpHW5b1GVh2");
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

        Button btnPostRequest = (Button) findViewById(R.id.btnPostRequest);
        btnPostRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gotoPostRequestActivity = new Intent(LauncherActivity.this, PostRequestActivity.class);
                LauncherActivity.this.startActivity(gotoPostRequestActivity);
            }
        });
    }
}
