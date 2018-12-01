package com.example.asthana.airmuleschat;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class FlightActivity extends BaseMenuActivity {

    EditText flightNum;
    TextView responseView;
    ProgressBar progressBar;
    //API key and call, hardcoded for now for testing purposes
    //API is using Aviation Edge
    static final String API_KEY = "782cbd-deb8af";
    static final String API_URL = "https://aviation-edge.com/v2/public/flights?key=782cbd-deb8af&flightIata=";
    //test API call (please don't, we only get so many free ones) https://aviation-edge.com/v2/public/flights?key=782cbd-deb8af&flightIata=HU482

    double latitude;
    double longitude;
    double planedir;
    String depart;
    String arrive;
    private String otherUser;
    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight);

        responseView = (TextView) findViewById(R.id.responseView);
        flightNum = (EditText) findViewById(R.id.flightnum);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uid = dataSnapshot.child("requests").child("CFEngFvb5HZJAVrB5HdHD14or3x21542479029577").child("mule").getValue().toString();
                otherUser = dataSnapshot.child("users").child(uid).child("name").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Error", databaseError.toString());
            }
        });
        Button queryButton = (Button) findViewById(R.id.queryButton);

        //on a button click, send the HTTP request (using the class getFlightTask)
        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(otherUser);

//                DialogFragment newFragment = new RatingFragment();
//                newFragment.show(getSupportFragmentManager());//, "missiles");
//                parseAirport(loadJSONFromAsset("flights.json"));
//                parseRealTime(loadJSONFromAsset("realtime.json"));
//                Intent myIntent = new Intent(FlightActivity.this, MapsActivity.class);
//                myIntent.putExtra("Latitude",latitude);
//                myIntent.putExtra("Longitude",longitude);
//                myIntent.putExtra("Direction",planedir);
//                myIntent.putExtra("arrTime",arrive);
//                myIntent.putExtra("depTime",depart);
//                FlightActivity.this.startActivity(myIntent);
                //new getFlightTask().execute();
            }
        });
    }

    void showDialog(String otherUser) {
        // Create the fragment and show it as a dialog.
        Bundle bundle = new Bundle();
        bundle.putString("otherUser", otherUser);
        DialogFragment newFragment = new RatingFragment();
        newFragment.setArguments(bundle);
        newFragment.show(getFragmentManager(), "ratings");
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
        progressBar.setVisibility(View.GONE);
        Log.i("INFO", response);
        responseView.setText(result);
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
        progressBar.setVisibility(View.GONE);
        Log.i("INFO", response);
        responseView.setText(result);
    }

    //Uses the HttpURLConnection and URL libraries to get the result of the request
    //Uses input/output buffers to read in the results and paste it to the TextView
    class getFlightTask extends AsyncTask<Void, Void, String> {

        private Exception exception;

        //toggles progress wheel while making the API call
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            responseView.setText("");
        }

        //runs the API call in the background
        protected String doInBackground(Void... urls) {
            //get the flight number from user input
            String flight = flightNum.getText().toString();

            try {
                //formats the URL containing the API key to add in the flight number (IATA)
                URL url = new URL(API_URL+flight);
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
            progressBar.setVisibility(View.GONE);
            Log.i("INFO", response);
            responseView.setText(response);
        }
    }
}