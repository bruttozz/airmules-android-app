package com.example.asthana.airmuleschat;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class FlightActivity extends AppCompatActivity {

    EditText flightNum;
    TextView responseView;
    ProgressBar progressBar;
    //API key and call, hardcoded for now for testing purposes
    //API is using Aviation Edge
    static final String API_KEY = "782cbd-deb8af";
    static final String API_URL = "https://aviation-edge.com/v2/public/flights?key=782cbd-deb8af&flightIata=";
    //test API call (please don't, we only get so many free ones) https://aviation-edge.com/v2/public/flights?key=782cbd-deb8af&flightIata=HU482
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight);

        responseView = (TextView) findViewById(R.id.responseView);
        flightNum = (EditText) findViewById(R.id.flightnum);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        Button queryButton = (Button) findViewById(R.id.queryButton);

        //on a button click, send the HTTP request (using the class getFlightTask)
        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                staticJson(loadJSONFromAsset());
                //new getFlightTask().execute();
            }
        });
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("flights.json");
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

    protected void staticJson(String response) {
        if(response == null) {
            response = "Error with processing the request";
        }
        progressBar.setVisibility(View.GONE);
        Log.i("INFO", response);
        responseView.setText(response);
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
