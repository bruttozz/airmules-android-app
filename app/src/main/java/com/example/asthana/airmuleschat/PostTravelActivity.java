package com.example.asthana.airmuleschat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class PostTravelActivity extends AppCompatActivity {
    private Button btnSubmit;
    private EditText editTextDepartureCity;
    private EditText editTextArrivalCity;
    private EditText editTextDepartureDate;
    private EditText editTextArrivalDate;
    private EditText editTextFlightNumber;
    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_travel);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        editTextDepartureCity = (EditText) findViewById(R.id.editTextDepartureCity);
        editTextArrivalCity = (EditText) findViewById(R.id.editTextArrivalCity);
        editTextDepartureDate = (EditText) findViewById(R.id.editTextDepartureDate);
        editTextArrivalDate = (EditText) findViewById(R.id.editTextArrivalDate);
        editTextFlightNumber = (EditText) findViewById(R.id.editTextFlightNumber);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDataInDatabase();

            }
        });

    }

    private void saveDataInDatabase() {
//        mDatabase.child("users").child(mFirebaseAuth.getCurrentUser().getUid()).child("name")
//                .setValue(mFirebaseAuth.getCurrentUser().getDisplayName());
        mDatabase.child("users")
                .child(mFirebaseAuth.getCurrentUser().getUid())
                .child("Travel")
                .child("DepartureCity").setValue(editTextDepartureCity.getText().toString());
        mDatabase.child("users")
                .child(mFirebaseAuth.getCurrentUser().getUid())
                .child("Travel")
                .child("ArrivalCity").setValue(editTextArrivalCity.getText().toString());
        mDatabase.child("users")
                .child(mFirebaseAuth.getCurrentUser().getUid())
                .child("Travel")
                .child("DepartureDate").setValue(editTextDepartureDate.getText().toString());
        mDatabase.child("users")
                .child(mFirebaseAuth.getCurrentUser().getUid())
                .child("Travel")
                .child("ArrivalDate").setValue(editTextArrivalDate.getText().toString());
        mDatabase.child("users")
                .child(mFirebaseAuth.getCurrentUser().getUid())
                .child("Travel")
                .child("FlightNumber").setValue(editTextFlightNumber.getText().toString());

    }
}
