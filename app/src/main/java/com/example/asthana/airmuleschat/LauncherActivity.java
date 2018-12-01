package com.example.asthana.airmuleschat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LauncherActivity extends BaseMenuActivity {

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

        Button btnTransactions = (Button) findViewById(R.id.btnViewAllRequests);
        btnTransactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewTransactions = AllTransactionsActivity.createIntentForAllRequests(LauncherActivity.this);
                LauncherActivity.this.startActivity(viewTransactions);
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

        Button btnSeeRequest = findViewById(R.id.btnViewPersonalRequests);

        btnSeeRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = AllTransactionsActivity.createIntentForCustomerRequests(LauncherActivity.this);
                LauncherActivity.this.startActivity(i);
            }
        });

        Button btnSeeMuleJobs = findViewById(R.id.btnViewMuleRequests);

        btnSeeMuleJobs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = AllTransactionsActivity.createIntentForMuleRequests(LauncherActivity.this);
                LauncherActivity.this.startActivity(i);
            }
        });
    }
}
