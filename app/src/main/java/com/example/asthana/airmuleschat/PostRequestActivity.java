package com.example.asthana.airmuleschat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PostRequestActivity extends BaseMenuActivity {
    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;

    private EditText editTextName;
    private EditText editTextWeight;
    private EditText editTextHeight;
    private EditText editTextLength;
    private EditText editTextWidth;

    private EditText editTextReward;

    private EditText editTextDepCity;
    private EditText editTextDepCountry;
    private EditText editTextDepDate;

    private EditText editTextArrCity;
    private EditText editTextArrCountry;
    private EditText editTextArrDate;

    private ArrayList<EditText> allEditTexts;

    private Button btnSubmit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_request);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();

        findAllEditTextAndClear();

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean success = saveRequestToDatabase();
                if(success) {
                    finish();
                }
            }
        });
    }

    private void findAllEditTextAndClear(){
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextWeight = (EditText) findViewById(R.id.editTextWeight);
        editTextHeight = (EditText) findViewById(R.id.editTextHeight);
        editTextLength = (EditText) findViewById(R.id.editTextLength);
        editTextWidth = (EditText) findViewById(R.id.editTextWidth);

        editTextReward = (EditText) findViewById(R.id.editTextReward);

        editTextDepCity = (EditText) findViewById(R.id.editTextDepCity);
        editTextDepCountry = (EditText) findViewById(R.id.editTextDepCountry);
        editTextDepDate = (EditText) findViewById(R.id.editTextDepDate);

        editTextArrCity = (EditText) findViewById(R.id.editTextArrCity);
        editTextArrCountry = (EditText) findViewById(R.id.editTextArrCountry);
        editTextArrDate = (EditText) findViewById(R.id.editTextArrDate);

        allEditTexts = new ArrayList<EditText>();
        allEditTexts.add(editTextName);
        allEditTexts.add(editTextWeight);
        allEditTexts.add(editTextHeight);
        allEditTexts.add(editTextLength);
        allEditTexts.add(editTextWidth);

        allEditTexts.add(editTextReward);

        allEditTexts.add(editTextDepCity);
        allEditTexts.add(editTextDepCountry);
        allEditTexts.add(editTextDepDate);

        allEditTexts.add(editTextArrCity);
        allEditTexts.add(editTextArrCountry);
        allEditTexts.add(editTextArrDate);

        //Clear the edit text data
        for(EditText et : allEditTexts){
            et.setText("");
        }
    }

    private boolean saveRequestToDatabase() {
        Request req = readReqDataFromGUI();
        //Request req = makeFakeRequest();

        if(req != null){
            mDatabase.child("requests").child(req.getTransactionID()).setValue(req);
            return true;
        }
        else{
            return false;
        }

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
    }

    private Request createEmptyRequestObject(){
        String customerID = mFirebaseAuth.getCurrentUser().getUid();
        String reqID = customerID + System.currentTimeMillis();     //makes it unique
        Request req = new Request(reqID, customerID);
        return req;
    }

    private Request readReqDataFromGUI(){
        //Is there data set?
        for(EditText et : allEditTexts){
            if(et.getText().toString().isEmpty()){
                Toast.makeText(getBaseContext(), "Missing Data...", Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        //TODO add more checks

        Request req = createEmptyRequestObject();

        Request.ItemData itemData = new Request.ItemData(editTextName.getText().toString(),
                Integer.parseInt(editTextWeight.getText().toString()),
                Float.parseFloat(editTextHeight.getText().toString()),
                Float.parseFloat(editTextLength.getText().toString()),
                Float.parseFloat(editTextWidth.getText().toString()));
        req.setItemData(itemData);

        req.setReward(Float.parseFloat(editTextReward.getText().toString()));

        Request.LocationInfo departure = new Request.LocationInfo(editTextDepCity.getText().toString(),
                editTextDepCountry.getText().toString(),
                editTextDepDate.getText().toString());
        req.setDeparture(departure);

        Request.LocationInfo arrival = new Request.LocationInfo(editTextArrCity.getText().toString(),
                editTextArrCountry.getText().toString(),
                editTextArrDate.getText().toString());
        req.setArrival(arrival);

        return req;
    }

    private Request makeFakeRequest(){
        Request req = createEmptyRequestObject();

        Request.ItemData itemData = new Request.ItemData("cheese", 2, 1.1f, 2.1f, 3);
        req.setItemData(itemData);

        req.setReward(123);

        Request.LocationInfo departure = new Request.LocationInfo("Shanghai", "China", "16-11-2018");
        req.setDeparture(departure);

        Request.LocationInfo arrival = new Request.LocationInfo("Boston", "USA", "17-11-2018");
        req.setArrival(arrival);

        return req;
    }
}
