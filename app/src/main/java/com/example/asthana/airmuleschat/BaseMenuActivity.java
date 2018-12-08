package com.example.asthana.airmuleschat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public abstract class BaseMenuActivity extends AppCompatActivity
        implements  GoogleApiClient.OnConnectionFailedListener{

    protected FirebaseAuth mFirebaseAuth;
    protected FirebaseUser mFirebaseUser;
    protected GoogleApiClient mGoogleApiClient;
    protected String mUsername;
    protected String mPhotoUrl;
    public static final String ANONYMOUS = "Unknown Person";

    private HashMap<String, TreeSet<String>> geoLocationsDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_menu);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set default username to anonymous.
        mUsername = ANONYMOUS;

        // Firebase Auth!
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        readInWorldLocationData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                return true;
            case R.id.geo_pref_menu:
                startActivity(new Intent(this, GeographicalPreferences.class));
                return true;
            case R.id.start_tracking_menu:
                startActivity(new Intent(this, TrackingActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Everything has gone wrong.
        Log.e("Menu Stuff", "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private void readInWorldLocationData(){
        try {
            if(geoLocationsDatabase == null || geoLocationsDatabase.isEmpty()) {
                geoLocationsDatabase = new HashMap<String, TreeSet<String>>();

                AssetManager assetManager = getApplicationContext().getAssets();
                InputStream inputStream = assetManager.open("world_cities" + File.separator + "world-cities.csv");
                InputStreamReader inputreader = new InputStreamReader(inputStream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;
                boolean header = true;
                int cityIndex = 0;
                int countryIndex = 1;
                while ((line = buffreader.readLine()) != null) {
                    if(header){
                        //skip the header
                        header = false;
                        continue;
                    }

                    //Based on:
                    //https://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes
                    //Get rid of commas in quotes (ex. "Washington, D.C.")
                    StringBuilder builder = new StringBuilder(line);
                    boolean inQuotes = false;
                    for (int currentIndex = 0; currentIndex < builder.length(); currentIndex++) {
                        char currentChar = builder.charAt(currentIndex);
                        if (currentChar == '\"') inQuotes = !inQuotes; // toggle state
                        if (currentChar == ',' && inQuotes) {
                            builder.deleteCharAt(currentIndex);
                        }
                    }
                    //Get rid of quotes
                    String cleanedUpLine = builder.toString();
                    cleanedUpLine = cleanedUpLine.replace("\"", "");

                    String[] rowData = cleanedUpLine.split(",");
                    String city = rowData[cityIndex];
                    String country = rowData[countryIndex];

                    TreeSet<String> countryData = geoLocationsDatabase.get(country);
                    if(countryData == null){
                        countryData = new TreeSet<String>();
                        geoLocationsDatabase.put(country, countryData);
                    }
                    countryData.add(city);
                }
            }
        }catch (Exception e){
            //dictionary = null;
            Toast.makeText(getApplicationContext(), "Unable to load locations!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * User must fill in the country data before the city data
     * Each one of them are validated with the geoLocationsDatabase
     *
     * @param cityET
     * @param countryET
     */
    public void syncUpCityAndCountry(Context parentContext,
                                     TextView cityLabel, AutoCompleteTextView cityET,
                                     AutoCompleteTextView countryET){
        int currentCityColor = cityLabel.getCurrentTextColor();

        cityET.setEnabled(false);
        cityLabel.setTextColor(Color.LTGRAY);
        cityET.setThreshold(2);
        cityET.setValidator(new AutoCompleteTextView.Validator(){
            @Override
            public boolean isValid(CharSequence text) {
                String textString = text.toString();

                if(textString.equals("")){
                    return true;
                }
                if(!geoLocationsDatabase.get(countryET.getText().toString()).contains(textString)){
                    //Entered not a real city in the country
                    return false;
                }
                return true;
            }

            @Override
            public CharSequence fixText(CharSequence invalidText) {
                return "";
            }
        });

        countryET.setAdapter(new ArrayAdapter<String>(parentContext, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>(geoLocationsDatabase.keySet())));
        countryET.setThreshold(3);
        countryET.setValidator(new AutoCompleteTextView.Validator(){
            @Override
            public boolean isValid(CharSequence text) {
                String textString = text.toString();

                if(textString.equals("")){
                    return true;
                }
                if(geoLocationsDatabase.get(textString) == null){
                    //Entered not a real country
                    return false;
                }

                //User can now interact with the city
                cityET.setEnabled(true);
                cityLabel.setTextColor(currentCityColor);
                cityET.setAdapter(new ArrayAdapter<String>(parentContext, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>(geoLocationsDatabase.get(textString))));
                return true;
            }

            @Override
            public CharSequence fixText(CharSequence invalidText) {
                cityET.setText("");     //reset the city too
                cityET.setEnabled(false);
                cityLabel.setTextColor(Color.GRAY);
                return "";
            }
        });
        countryET.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                //Validate right away, so the user can click on the city field
                countryET.performValidation();
            }
        });
    }
}
