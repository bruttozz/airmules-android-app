package com.example.asthana.airmuleschat;

import android.icu.text.UnicodeSetSpanner;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class RequestDetailActivity extends BaseMenuActivity {

    private TextView txtViewDeparture;
    private TextView txtViewArrival;
    private TextView txtViewDate;
    private TextView txtViewItem;
    private TextView txtViewWeight;
    private TextView txtViewSize;

    private Button btnChat;
    private Button btnCancel;
    private Button btnSignUpForMule;
    private Button btnFlight;
    private Button btnPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);

        txtViewDeparture = (TextView) findViewById(R.id.textViewDeparture);
        txtViewArrival = (TextView) findViewById(R.id.textViewArrival);
        txtViewDate = (TextView) findViewById(R.id.textViewDate);
        txtViewItem = (TextView) findViewById(R.id.textViewItem);
        txtViewWeight = (TextView) findViewById(R.id.textViewWeight);
        txtViewSize = (TextView) findViewById(R.id.textViewSize);

        btnChat = (Button) findViewById(R.id.btnChat);
        btnCancel = (Button) findViewById(R.id.btnCancelRequest);
        btnSignUpForMule = (Button) findViewById(R.id.btnSignUpForMule);
        btnFlight = (Button) findViewById(R.id.btnFlight);
        btnPay = (Button) findViewById(R.id.btnPay);

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go to chat activity
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeThisRequestFromDatabase();
            }
        });

        btnSignUpForMule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpForMuleToThisRequest();
            }
        });




    }
    private void removeThisRequestFromDatabase() {

    }
    private void signUpForMuleToThisRequest() {
        try {
            // TO_DO: link current userid as mule id in database

            Toast.makeText(this, "Successfully signed up!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed signed up!!", Toast.LENGTH_LONG).show();
        }
    }
}
