package com.example.asthana.airmuleschat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
                finish();
            }
        });
    }

    private  void saveRequestToDatabase() {
        // add exception check in here!!!!!!
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
    }
}
