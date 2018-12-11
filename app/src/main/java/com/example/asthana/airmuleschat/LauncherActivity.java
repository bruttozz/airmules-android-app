package com.example.asthana.airmuleschat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

        DatabaseReference q = FirebaseDatabase.getInstance().getReference().child("users").child(mFirebaseAuth.getCurrentUser().getUid())
                .child(GeographicalPreferences.DATABASE_TABLE_NAME).getRef();
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int geoPrefCount = 0;
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    geoPrefCount++;
                }
                if(geoPrefCount == 0){
                    Toast t = Toast.makeText(LauncherActivity.this, "No Geo. Preferences found, consider adding some through the main menu", Toast.LENGTH_LONG);
                    t.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 250);
                    t.show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("GeoPref", "Cannot connect to Firebase");
            }
        });
    }
}
