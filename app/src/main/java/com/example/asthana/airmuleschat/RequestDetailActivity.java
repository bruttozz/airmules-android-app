package com.example.asthana.airmuleschat;

import android.icu.text.UnicodeSetSpanner;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.lang.ref.ReferenceQueue;

public class RequestDetailActivity extends BaseMenuActivity {

    private TextView txtViewDeparture;
    private TextView txtViewArrival;
    private TextView txtViewDate;
    private TextView txtViewItem;
    private TextView txtViewWeight;
    private TextView txtViewSize;

    private Button btnChat;
    private Button btnCancel;
    private Button btnSignUpOrUnregister;
    private Button btnFlight;
    private Button btnPay;
    private String transactionID;
    private static final String REQUESTS = "requests";
    private static final String DEPARTURE = "departure";
    private static final String ARRIVAL = "arrival";
    private static final String CITY = "city";
    private static final String COUNTRY = "country";
    private static final String DATE = "date";
    private static final String ITEMDATA = "itemData";
    private static final String ITEMNAME = "name";
    private static final String REWARD = "reward";
    private static final String LENGTH = "length";
    private static final String WEIGHT = "weight";
    private static final String HEIGHT = "height";
    private static final String WIDTH = "width";
    private static final String CUSTOMER = "customer";
    private static final String MULE = "mule";

    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;

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


        btnSignUpOrUnregister = (Button) findViewById(R.id.btnSignUpOrUnregister);
        btnFlight = (Button) findViewById(R.id.btnFlight);
        // TODO: add function to this button

        btnPay = (Button) findViewById(R.id.btnPay);
        // TODO: add function to this button



        transactionID = getIntent().getStringExtra("transactionID").toString();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                 setTextAndButton(dataSnapshot);
                 addButtonFunctions(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Error", databaseError.toString());
            }
        });

    }

    private void setTextAndButton(DataSnapshot dataSnapshot) {
        if (dataSnapshot.child(REQUESTS).child(transactionID).getValue() == null) {
            RequestDetailActivity.this.finish();
            return;
        }
        txtViewDeparture.setText(dataSnapshot.child(REQUESTS).child(transactionID).child(DEPARTURE)
                .child(CITY).getValue().toString() + ", " + dataSnapshot.child(REQUESTS)
                .child(transactionID).child(DEPARTURE).child(COUNTRY).getValue().toString());
        txtViewArrival.setText(dataSnapshot.child(REQUESTS).child(transactionID).child(ARRIVAL)
                .child(CITY).getValue().toString() + ", " + dataSnapshot.child(REQUESTS)
                .child(transactionID).child(ARRIVAL).child(COUNTRY).getValue().toString());
        txtViewDate.setText(dataSnapshot.child(REQUESTS).child(transactionID).child(DEPARTURE)
                .child(DATE).getValue().toString());
        txtViewItem.setText(dataSnapshot.child(REQUESTS).child(transactionID).child(ITEMDATA)
                .child(ITEMNAME).getValue().toString());
        txtViewSize.setText(dataSnapshot.child(REQUESTS).child(transactionID).child(ITEMDATA)
                .child(LENGTH).getValue().toString().substring(0, 1) + " x " +
                dataSnapshot.child(REQUESTS).child(transactionID).child(ITEMDATA).child(WIDTH).getValue()
                        .toString().substring(0, 1) + " x " + dataSnapshot.child(REQUESTS)
                .child(transactionID).child(ITEMDATA).child(HEIGHT).getValue().toString()
                .substring(0, 1));
        txtViewWeight.setText(dataSnapshot.child(REQUESTS).child(transactionID).child(ITEMDATA)
                .child(WEIGHT).getValue().toString().substring(0, 1));

        if (mFirebaseAuth.getCurrentUser().getUid().equals(dataSnapshot.child(REQUESTS).child(transactionID)
                .child(CUSTOMER).getValue().toString())) {
            btnCancel.setVisibility(View.VISIBLE);
            btnSignUpOrUnregister.setVisibility(View.GONE);
        } else {
            btnCancel.setVisibility(View.GONE);
            btnSignUpOrUnregister.setVisibility(View.GONE);
        }

        if (dataSnapshot.child(REQUESTS).child(transactionID).child(MULE).getValue() == null) {
            if (!dataSnapshot.child(REQUESTS).child(transactionID).child(CUSTOMER).getValue().toString()
                    .equals(mFirebaseAuth.getCurrentUser().getUid().toString())) {
                btnSignUpOrUnregister.setText("sign up for mule");
                btnSignUpOrUnregister.setVisibility(View.VISIBLE);
            } else {
                btnSignUpOrUnregister.setVisibility(View.GONE);
            }
        } else if (mFirebaseAuth.getCurrentUser().getUid().toString().equals
                (dataSnapshot.child(REQUESTS).child(transactionID).child(MULE).getValue().toString())){
            btnSignUpOrUnregister.setText("unregister");
            btnSignUpOrUnregister.setVisibility(View.VISIBLE);

        }
    }

    private void addButtonFunctions(final DataSnapshot dataSnapshot) {

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeThisRequestFromDatabase(dataSnapshot);
            }
        });

        btnSignUpOrUnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpOrUnregisterForMuleToThisRequest(dataSnapshot);
            }
        });




    }

    private void removeThisRequestFromDatabase(DataSnapshot dataSnapshot) {
        // TODO: test this function
        try {
            mDatabase.child(REQUESTS).child(transactionID).removeValue();
        } catch (Exception e) {
            Log.e("Error", e.toString());
        }

    }

    private void signUpOrUnregisterForMuleToThisRequest(DataSnapshot dataSnapshot) {
        // TODO: link current userid as mule id in database
        if (btnSignUpOrUnregister.getText().toString().equals("unregister")) {
            try {
                mDatabase.child(REQUESTS).child(transactionID).child(MULE).removeValue();
                Toast.makeText(this, "Unregistered!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("Error", e.toString());
            }
        } else {
            if (dataSnapshot.child(REQUESTS).child(transactionID).child(MULE).getValue() != null) {
                Toast.makeText(this, "Sorry, someone already signed up", Toast.LENGTH_SHORT).show();

            } else {
                mDatabase.child(REQUESTS).child(transactionID).child(MULE).setValue
                        (mFirebaseAuth.getCurrentUser().getUid().toString());
                Toast.makeText(this, "Successfully signed up!", Toast.LENGTH_SHORT).show();
            }
        }

    }


}
