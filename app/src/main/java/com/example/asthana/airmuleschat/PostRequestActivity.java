package com.example.asthana.airmuleschat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PostRequestActivity extends AppCompatActivity {

    private Button btnSubmit;
    private EditText editTextDepartureCity;
    private EditText editTextArrivalCity;
    private EditText editTextEndDate;
    private EditText itemEditText;
    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_request);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        editTextDepartureCity = (EditText) findViewById(R.id.editTextDepartureCity);
        editTextArrivalCity = (EditText) findViewById(R.id.editTextArrivalCity);
        editTextEndDate = (EditText) findViewById(R.id.editTextEndDate);
        itemEditText = (EditText) findViewById(R.id.itemTextView);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRequestToDatabase();
                //finish();
            }
        });
    }

    private  void saveRequestToDatabase() {
        // add exception check in here!!!!!!

        Request req = makeFakeRequest();
        String reqID = req.getCustomer() + System.currentTimeMillis();     //makes it unique
        mDatabase.child("requests").child(reqID).setValue(req);


        /*
        SAMPLE CODE TO RETRIEVE A REQUEST OBJECT

        DatabaseReference ref = mDatabase.child("requests").child("1").getRef();
        // Attach a listener to read the data at our posts reference
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Request req = dataSnapshot.getValue(Request.class);
                req.getCustomer();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        */

        //TODO add and read all properties from GUI

        /*
        mDatabase.child("users")
                .child(mFirebaseAuth.getCurrentUser().getUid())
                .child("Request")
                .child("DepartureCity").setValue(editTextDepartureCity.getText().toString());
        mDatabase.child("users")
                .child(mFirebaseAuth.getCurrentUser().getUid())
                .child("Request")
                .child("ArrivalCity").setValue(editTextArrivalCity.getText().toString());
        mDatabase.child("users")
                .child(mFirebaseAuth.getCurrentUser().getUid())
                .child("Request")
                .child("EndDate").setValue(editTextEndDate.getText().toString());
        mDatabase.child("users")
                .child(mFirebaseAuth.getCurrentUser().getUid())
                .child("Request")
                .child("Item").setValue(itemEditText.getText().toString());
                */
    }

    private Request makeFakeRequest(){
        String customerID = mFirebaseAuth.getCurrentUser().getUid();
        Request req = new Request(customerID);
        req.setReward(123);

        Request.ItemData itemData = new Request.ItemData("cheese", 2, 1.1f, 2.1f, 3);
        req.setItemData(itemData);

        Request.LocationInfo departure = new Request.LocationInfo("Shanghai", "China", "2018-11-16");
        req.setDeparture(departure);

        Request.LocationInfo arrival = new Request.LocationInfo("Boston", "USA", "2018-11-17");
        req.setArrival(arrival);

        return req;
    }
}
