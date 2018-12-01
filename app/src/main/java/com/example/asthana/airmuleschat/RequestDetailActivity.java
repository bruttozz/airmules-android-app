package com.example.asthana.airmuleschat;

import android.content.Intent;
import android.os.AsyncTask;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RequestDetailActivity extends BaseMenuActivity {
    private TextView txtStatus;
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
    private Button btnPayOrConfirm;
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
    private static final String STATUS = "status";
    static final String API_URL = "https://aviation-edge.com/v2/public/flights?key=782cbd-deb8af&flightIata=";
    private double latitude;
    private double longitude;
    private double planedir;
    private String depart;
    private String arrive;

    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);

        transactionID = getIntent().getStringExtra("transactionID").toString();

        txtStatus = (TextView) findViewById(R.id.txtStatus);
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
        btnPayOrConfirm = (Button) findViewById(R.id.btnPayOrConfirm);

        btnFlight = (Button) findViewById(R.id.btnFlight);
        btnFlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parseAirport(loadJSONFromAsset("flights.json"));
                parseRealTime(loadJSONFromAsset("realtime.json"));
                Intent myIntent = new Intent(RequestDetailActivity.this, MapsActivity.class);
                myIntent.putExtra("Latitude",latitude);
                myIntent.putExtra("Longitude",longitude);
                myIntent.putExtra("Direction",planedir);
                myIntent.putExtra("arrTime",arrive);
                myIntent.putExtra("depTime",depart);
                RequestDetailActivity.this.startActivity(myIntent);
                //new getFlightTask().execute();
            }
        });

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reqRef = mDatabase.child("requests").child(transactionID).getRef();
        reqRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Request req = dataSnapshot.getValue(Request.class);
                if (req == null || req.getTransactionID() == null) {
                    RequestDetailActivity.this.finish();
                    return;
                }

                //Make sure no one else has already signed up to be the mule
                if(!req.getCustomer().equals(mFirebaseAuth.getCurrentUser().getUid())
                        && req.getMule() != null && !req.getMule().equals(mFirebaseAuth.getCurrentUser().getUid())){
                    RequestDetailActivity.this.finish();
                    return;
                }

                setTextAndButton(req);
                addButtonFunctions(req);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Error", databaseError.toString());
            }
        });

    }

    private void setTextAndButton(Request myReq) {
        txtStatus.setText(myReq.getStatus());
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
            btnPayOrConfirm.setVisibility(View.GONE);
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

    private void addButtonFunctions(final Request myReq) {
        final String status = myReq.getStatus();

        if(status.equals(Request.PAID) || status.equals(Request.COMPLETE)){
            btnPayOrConfirm.setText("CONFIRM");
        }
        if(status.equals(Request.NO_MULE) || status.equals(Request.COMPLETE)){
            btnPayOrConfirm.setEnabled(false);
        }
        else{
            btnPayOrConfirm.setEnabled(true);
        }
        btnPayOrConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payOrConfirmButtonAction(status);
            }
        });

        if(status.equals(Request.COMPLETE)){
            btnCancel.setEnabled(false);
        }
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeThisRequestFromDatabase();
            }
        });

        if(status.equals(Request.COMPLETE)){
            btnSignUpOrUnregister.setEnabled(false);
        }
        btnSignUpOrUnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpOrUnregisterForMuleToThisRequest(myReq);
            }
        });
    }

    private void payOrConfirmButtonAction(String status){
        if(status.equals(Request.PAID)){
            //TODO Deliver money to mule
            //TODO Rate the mule
            //complete the transaction
            mDatabase.child(REQUESTS).child(transactionID).child(STATUS).setValue(Request.COMPLETE);
        }
        else{
            //TODO start payment activity instead
            mDatabase.child(REQUESTS).child(transactionID).child(STATUS).setValue(Request.PAID);
        }
    }

    private void removeThisRequestFromDatabase() {
        try {
            mDatabase.child(REQUESTS).child(transactionID).removeValue();
            RequestDetailActivity.this.finish();
        } catch (Exception e) {
            Log.e("Error", e.toString());
        }
    }

    private void signUpOrUnregisterForMuleToThisRequest(Request myReq) {
        if (btnSignUpOrUnregister.getText().toString().equals("unregister")) {
            try {
                mDatabase.child(REQUESTS).child(transactionID).child(MULE).removeValue();
                if(myReq.getStatus().equals(Request.PAID)) {
                    //TODO refund payment
                }
                mDatabase.child(REQUESTS).child(transactionID).child(STATUS).setValue(Request.NO_MULE);
                Toast.makeText(this, "Unregistered!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("Error", e.toString());
            }
        } else {
            if (myReq.getMule() != null) {
                Toast.makeText(this, "Sorry, someone already signed up", Toast.LENGTH_SHORT).show();

            } else {
                mDatabase.child(REQUESTS).child(transactionID).child(MULE).setValue
                        (mFirebaseAuth.getCurrentUser().getUid().toString());
                mDatabase.child(REQUESTS).child(transactionID).child(STATUS).setValue(Request.NO_PAYMENT);
                Toast.makeText(this, "Successfully signed up!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String loadJSONFromAsset(String jsonobject) {
        String json = null;
        try {
            InputStream is = getAssets().open(jsonobject);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    protected void parseAirport(String response) {
        String result= "";
        if(response == null) {
            response = "Error with processing the request";
        }
        try{
            JSONObject obj = new JSONObject(response);
            JSONObject departure = new JSONObject(obj.getString("departure"));
            JSONObject arrival = new JSONObject(obj.getString("arrival"));
            result = departure.getString("scheduledTime").substring(11,16) + " to " + arrival.getString("scheduledTime").substring(11,16);
            depart = departure.getString("scheduledTime");
            arrive = arrival.getString("scheduledTime");
        }
        catch (JSONException e){
            result = e.getMessage();
        }
        //progressBar.setVisibility(View.GONE);
        Log.i("INFO", response);
        //responseView.setText(result);
    }

    protected void parseRealTime(String response) {
        String result = "";
        if(response == null) {
            response = "Error with processing the request";
        }
        try{
            JSONObject obj = new JSONObject(response);
            JSONObject geography = new JSONObject(obj.getString("geography"));
            latitude = Double.parseDouble(geography.getString("latitude"));
            longitude = Double.parseDouble(geography.getString("longitude"));
            planedir = Double.parseDouble(geography.getString("direction"));
            result = geography.getString("longitude");
        }
        catch (JSONException e){
            result = e.getMessage();
        }
        //progressBar.setVisibility(View.GONE);
        Log.i("INFO", response);
        //responseView.setText(result);
    }

    //Uses the HttpURLConnection and URL libraries to get the result of the request
    //Uses input/output buffers to read in the results and paste it to the TextView
    class getFlightTask extends AsyncTask<Void, Void, String> {

        private Exception exception;

        //toggles progress wheel while making the API call
        protected void onPreExecute() {
            //progressBar.setVisibility(View.VISIBLE);
            //responseView.setText("");
        }

        //runs the API call in the background
        protected String doInBackground(Void... urls) {
            //get the flight number from user input
            //UNCOMMENT HERE WHEN TESTING API need to get this from user data
            //String flight = flightNum.getText().toString();

            try {
                //formats the URL containing the API key to add in the flight number (IATA)
                //UNCOMMENT HERE WHEN TESTING API
                URL url = new URL(API_URL);//+flight);
                //open the connection
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    //read in the HTML
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                finally{
                    //close the connection
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        //returns with errors if couldn't process the http request or returned with nothing and toggles the progress bar
        //sends a Log with the response
        protected void onPostExecute(String response) {
            if(response == null) {
                response = "Error with processing the request";
            }
            //progressBar.setVisibility(View.GONE);
            Log.i("INFO", response);
            //responseView.setText(response);
        }
    }

}
