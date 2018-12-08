package com.example.asthana.airmuleschat;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class PostRequestActivity extends BaseMenuActivity {
    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;

    private EditText editTextName;
    private EditText editTextWeight;
    private EditText editTextHeight;
    private EditText editTextLength;
    private EditText editTextWidth;

    private EditText editTextReward;

    private AutoCompleteTextView editTextDepCity;
    private AutoCompleteTextView editTextDepCountry;
    private TextView txtDepDate;

    private AutoCompleteTextView editTextArrCity;
    private AutoCompleteTextView editTextArrCountry;
    private TextView txtArrDate;

    private Calendar myCalendar;
    private DatePickerDialog.OnDateSetListener dateDep;
    private DatePickerDialog.OnDateSetListener dateArr;

    private ArrayList<TextView> allEditTexts;

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

        editTextDepCity = (AutoCompleteTextView) findViewById(R.id.editTextDepCity);
        editTextDepCountry = (AutoCompleteTextView) findViewById(R.id.editTextDepCountry);
        txtDepDate = (TextView) findViewById(R.id.txtDepDate);

        editTextArrCity = (AutoCompleteTextView) findViewById(R.id.editTextArrCity);
        editTextArrCountry = (AutoCompleteTextView) findViewById(R.id.editTextArrCountry);
        txtArrDate = (TextView) findViewById(R.id.txtArrDate);

        allEditTexts = new ArrayList<TextView>();
        allEditTexts.add(editTextName);
        allEditTexts.add(editTextWeight);
        allEditTexts.add(editTextHeight);
        allEditTexts.add(editTextLength);
        allEditTexts.add(editTextWidth);

        allEditTexts.add(editTextReward);

        allEditTexts.add(editTextDepCity);
        allEditTexts.add(editTextDepCountry);
        allEditTexts.add(txtDepDate);

        allEditTexts.add(editTextArrCity);
        allEditTexts.add(editTextArrCountry);
        allEditTexts.add(txtArrDate);

        //Setup the calendar date pop-ups
        myCalendar = Calendar.getInstance();
        txtDepDate.setClickable(true);
        dateDep = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel("dep");
            }

        };
        txtDepDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(PostRequestActivity.this, dateDep, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        txtArrDate.setClickable(true);
        dateArr = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel("arr");
            }
        };
        txtArrDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(PostRequestActivity.this, dateArr, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        //Clear the edit text data
        for(TextView et : allEditTexts){
            et.setText("");
        }

        syncUpCityAndCountry(getBaseContext(),
                findViewById(R.id.depCityLabel),
                editTextDepCity, editTextDepCountry);
        syncUpCityAndCountry(getBaseContext(),
                findViewById(R.id.arrCityLabel),
                editTextArrCity, editTextArrCountry);
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
        //Perform a final check of the city/country data
        editTextDepCity.performValidation();
        editTextDepCountry.performValidation();
        editTextArrCity.performValidation();
        editTextArrCountry.performValidation();

        //Is there data set?
        for(TextView et : allEditTexts){
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
                txtDepDate.getText().toString());
        req.setDeparture(departure);

        Request.LocationInfo arrival = new Request.LocationInfo(editTextArrCity.getText().toString(),
                editTextArrCountry.getText().toString(),
                txtArrDate.getText().toString());
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

    private void updateLabel(String type) {
        String myFormat = "dd" + Request.LocationInfo.DATE_DELIMITER + "MM" + Request.LocationInfo.DATE_DELIMITER + "yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        if (type.equals("arr"))
            txtArrDate.setText(sdf.format(myCalendar.getTime()));
        else
            txtDepDate.setText(sdf.format(myCalendar.getTime()));
    }
}
