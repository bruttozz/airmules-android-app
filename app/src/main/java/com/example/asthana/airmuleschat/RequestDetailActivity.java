package com.example.asthana.airmuleschat;

import android.content.Intent;
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
    private String chatID;
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

        btnChat.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent i = new Intent(RequestDetailActivity.this, PersonalChat.class);
                i.putExtra("chatID", transactionID);
                startActivity(i);
            }
        });
        btnCancel = (Button) findViewById(R.id.btnCancelRequest);


        btnSignUpOrUnregister = (Button) findViewById(R.id.btnSignUpOrUnregister);
        btnFlight = (Button) findViewById(R.id.btnFlight);
        // TODO: add function to this button

        btnPay = (Button) findViewById(R.id.btnPay);
        // TODO: add function to this button



        transactionID = getIntent().getStringExtra("transactionID").toString();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reqRef = mDatabase.child("requests").child(transactionID).getRef();
        reqRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Request req = dataSnapshot.getValue(Request.class);
                setTextAndButton(req);
                addButtonFunctions(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Error", databaseError.toString());
            }
        });

    }

    private void setTextAndButton(Request myReq) {
        if (myReq.getTransactionID() == null) {
            RequestDetailActivity.this.finish();
            return;
        }

        txtViewDeparture.setText(myReq.getDeparture().getCity() + ", " + myReq.getDeparture().getCountry());
        txtViewArrival.setText(myReq.getArrival().getCity() + ", " + myReq.getArrival().getCountry());
        txtViewDate.setText(myReq.getDeparture().getDate());

        txtViewItem.setText(myReq.getItemData().getName());
        txtViewSize.setText(Float.toString(myReq.getItemData().getLength())
                + " x " + Float.toString(myReq.getItemData().getWidth())
                + " x " + Float.toString(myReq.getItemData().getHeight()));
        txtViewWeight.setText(Float.toString(myReq.getItemData().getWeight()));

        if (mFirebaseAuth.getCurrentUser().getUid().equals(myReq.getCustomer())) {
            btnCancel.setVisibility(View.VISIBLE);
            btnSignUpOrUnregister.setVisibility(View.GONE);
        } else {
            btnCancel.setVisibility(View.GONE);
            btnSignUpOrUnregister.setVisibility(View.GONE);
        }

        if (myReq.getMule() == null) {
            if (!myReq.getCustomer().equals(mFirebaseAuth.getCurrentUser().getUid())) {
                //I am not the customer, so I can sign up to be the mule
                btnSignUpOrUnregister.setText("sign up for mule");
                btnSignUpOrUnregister.setVisibility(View.VISIBLE);
            }
        } else if (mFirebaseAuth.getCurrentUser().getUid().equals(myReq.getMule())){
            //I am the mule, so I can unregister if I want to
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
