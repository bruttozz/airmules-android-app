package com.example.asthana.airmuleschat;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RequestDetailActivity extends BaseMenuActivity {
    static final String API_URL = "https://aviation-edge.com/v2/public/flights?key=782cbd-deb8af&flightIata=";
    private static final String REQUESTS = "requests";
    private static final String MULE = "mule";
    private static final String STATUS = "status";
    private TextView txtStatus;
    private TextView txtViewDeparture;
    private TextView txtViewArrival;
    private TextView txtViewDepartureDate;
    private TextView txtViewArrivalDate;
    private TextView txtViewItem;
    private TextView txtViewWeight;
    private TextView txtViewSize;
    private TextView txtViewMule;
    private TextView txtViewReward;
    private EditText flightNum;
    private Button btnChat;
    private Button btnCancel;
    private Button btnSignUpOrUnregister;
    private Button btnFlight;
    private Button btnPayOrConfirm;
    private Button btnViewMules;
    private String transactionID;
    private double latitude;
    private double longitude;
    private double planedir;
    private String depart;
    private String arrive;
    private UserClass mule;
    private String otherUser;
    private String uid;
    private float currentRating, numRatings;

    private RadioButton mCurrentlyCheckedRB;
    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);

        transactionID = getIntent().getStringExtra("transactionID").toString();

        txtStatus = findViewById(R.id.txtStatus);
        txtViewMule = findViewById(R.id.txtViewMule);
        txtViewDeparture = findViewById(R.id.txtViewDeparture);
        txtViewArrival = findViewById(R.id.textViewArrival);
        txtViewDepartureDate = (TextView) findViewById(R.id.textViewDepartureDate);
        txtViewArrivalDate = (TextView) findViewById(R.id.textViewArrivalDate);
        txtViewReward = (TextView) findViewById(R.id.txtViewReward);
        txtViewItem = (TextView) findViewById(R.id.textViewItem);
        txtViewWeight = (TextView) findViewById(R.id.textViewWeight);
        txtViewSize = (TextView) findViewById(R.id.textViewSize);
        flightNum = (EditText) findViewById(R.id.flightIata);


        btnChat = (Button) findViewById(R.id.btnChat);

        btnChat.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent i = new Intent(RequestDetailActivity.this, PersonalChat.class);
                i.putExtra("chatID", transactionID);
                startActivity(i);
            }
        });

        btnViewMules = (Button) findViewById(R.id.btnViewMules);

        btnCancel = (Button) findViewById(R.id.btnCancelRequest);
        btnSignUpOrUnregister = (Button) findViewById(R.id.btnSignUpOrUnregister);
        btnPayOrConfirm = (Button) findViewById(R.id.btnPayOrConfirm);

        btnFlight = (Button) findViewById(R.id.btnFlight);
        btnFlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new getFlightTask().execute();
//                parseAirport(loadJSONFromAsset("flights.json"));
//                parseRealTime(loadJSONFromAsset("realtime.json"));
//                Log.i("INFO", latitude+", "+longitude);
//                Intent myIntent = new Intent(RequestDetailActivity.this, MapsActivity.class);
//                myIntent.putExtra("Latitude", latitude);
//                myIntent.putExtra("Longitude", longitude);
//                myIntent.putExtra("Direction", planedir);
//                myIntent.putExtra("arrTime", arrive);
//                myIntent.putExtra("depTime", depart);
//                RequestDetailActivity.this.startActivity(myIntent);
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
                if (!req.getCustomer().equals(mFirebaseAuth.getCurrentUser().getUid())
                        && req.getMule() != null && !req.getMule().equals(mFirebaseAuth.getCurrentUser().getUid())) {
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

        DatabaseReference potentialMulesReference = mDatabase.child("potentialMules").getRef();
        potentialMulesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer number = 0;
                btnSignUpOrUnregister.setText("sign up");
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PotentialMule eachOne = snapshot.getValue(PotentialMule.class);
                    if (eachOne.getRequestID().equals(transactionID)) {
                        number++;
                        if (eachOne.getMuleID().equals(mFirebaseAuth.getCurrentUser().getUid())) {
                            // the current user has registered to be mule for this request
                            btnSignUpOrUnregister.setText("unregister");
                            // todo : how to set the chat button??
                        }
                    }
                }
                btnViewMules.setText("View Candidate Mules: " + number.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Error", databaseError.toString());
            }
        });

    }

    public void showAlertDialogButtonClicked(View view) {

        // create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Available Mules");

        // set the custom layout
        final View ViewMulesDialog = getLayoutInflater().inflate(R.layout.dialog_mules, null);
        builder.setView(ViewMulesDialog);

        // Initialize contacts
        List<UserClass> mules = new ArrayList<UserClass>();
        /*
        mules.add(new UserClass("food", 5, 5, 0));
        mules.add(new UserClass("Bar", 5, 2, 0));
        mules.add(new UserClass("troll", 5, 1, 0));
        mules.add(new UserClass("firer", 5, 3, 0));
        */


        HashMap<UserClass, String> mulesToIDs = new HashMap<UserClass, String>();
        final MulesRadioAdapter adapter = new MulesRadioAdapter(this, R.layout.dialog_mules, mules, mulesToIDs,
                transactionID);
        ListView listView = (ListView) ViewMulesDialog.findViewById(R.id.viewMulesDialogRecycler);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSelectedIndex(position);  // set selected position and notify the adapter
                adapter.notifyDataSetChanged();
            }
        });

        // add a button
        builder.setPositiveButton("Select Mule", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //We can't implement this here, because we want to stop the dialog from closing without a mule selected
            }
        });
        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
        getPotentialMules(adapter, mulesToIDs);
        dialog.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserClass myMule = adapter.getSelectedItem();
                if (myMule == null) {
                    Toast.makeText(RequestDetailActivity.this, "Please select a mule", Toast.LENGTH_LONG).show();
                } else {
                    String muleID = mulesToIDs.get(myMule);
                    mDatabase.child(REQUESTS).child(transactionID).child(MULE).setValue(muleID);
                    mDatabase.child(REQUESTS).child(transactionID).child(STATUS).setValue(Request.NO_PAYMENT);
                    dialog.dismiss();
                }
            }
        });
    }

    private void getPotentialMules(MulesRadioAdapter adapter, HashMap<UserClass, String> mulesToIDs) {
        DatabaseReference ref = mDatabase.child("potentialMules").getRef();
        // Attach a listener to read the data at our posts reference
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapter.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    PotentialMule pMule = postSnapshot.getValue(PotentialMule.class);
                    if (pMule == null || pMule.getRequestID() == null) {
                        //mule unregistered?
                        return;
                    }

                    if (RequestDetailActivity.this.transactionID.equals(pMule.getRequestID())) {
                        DatabaseReference ref2 = mDatabase.child("users").child(pMule.getMuleID()).getRef();
                        ref2.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot2) {
                                UserClass mule = dataSnapshot2.getValue(UserClass.class);
                                if (mule == null) {
                                    //user was deleted?
                                    return;
                                }

                                synchronized (adapter) {
                                    mulesToIDs.put(mule, pMule.getMuleID());
                                    adapter.add(mule);
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e("Potential Mules", "Cannot connect to Firebase");
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Potential Mules", "Cannot connect to Firebase");
            }
        });
    }

    private void setTextAndButton(Request myReq) {

        if (myReq.getMule() != null) {
            DatabaseReference userRef = mDatabase.child("users").child(myReq.getMule()).getRef();
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mule = dataSnapshot.getValue(UserClass.class);
                    if (mule != null) {
                        txtViewMule.setText(mule.getName());
                    } else {
                        txtViewMule.setText("No Mule");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w("Error", databaseError.toString());
                }
            });
        } else {
            txtViewMule.setText("No Mule");
        }

        txtStatus.setText(myReq.getStatus());
        txtViewDeparture.setText(myReq.getDeparture().getCity() + ", " + myReq.getDeparture().getCountry());
        txtViewArrival.setText(myReq.getArrival().getCity() + ", " + myReq.getArrival().getCountry());
        txtViewDepartureDate.setText(myReq.getDeparture().getDate());
        txtViewArrivalDate.setText(myReq.getArrival().getDate());
        txtViewReward.setText(PaymentActivity.convertToMoneyFormatString(myReq.getReward()));
        txtViewItem.setText(myReq.getItemData().getName());
        txtViewSize.setText(Float.toString(myReq.getItemData().getLength())
                + " x " + Float.toString(myReq.getItemData().getWidth())
                + " x " + Float.toString(myReq.getItemData().getHeight()));
        txtViewWeight.setText(Float.toString(myReq.getItemData().getWeight()));


        if (mFirebaseAuth.getCurrentUser().getUid().equals(myReq.getCustomer())) {
            // the current user is the customer
            btnCancel.setVisibility(View.VISIBLE);
            btnViewMules.setVisibility(View.VISIBLE);
            btnSignUpOrUnregister.setVisibility(View.GONE);
            btnPayOrConfirm.setVisibility(View.VISIBLE);
            btnChat.setVisibility(View.VISIBLE);
        } else {
            // the current user is not the customer
            btnCancel.setVisibility(View.GONE);
            btnViewMules.setVisibility(View.GONE);
            btnSignUpOrUnregister.setVisibility(View.VISIBLE);
            btnPayOrConfirm.setVisibility(View.GONE);
            if (mFirebaseAuth.getCurrentUser().getUid().equals(myReq.getMule())) {
                btnChat.setVisibility(View.VISIBLE);
            } else {
                btnChat.setVisibility(View.GONE);
            }
        }

    }

    private void addButtonFunctions(final Request myReq) {
        final String status = myReq.getStatus();

        if (status.equals(Request.PAID) || status.equals(Request.COMPLETE)) {
            btnPayOrConfirm.setText("CONFIRM");
        } else {
            btnPayOrConfirm.setText("PAY");
        }
        if (status.equals(Request.NO_MULE) || status.equals(Request.COMPLETE)) {
            btnPayOrConfirm.setEnabled(false);
        } else {
            btnPayOrConfirm.setEnabled(true);
        }
        btnPayOrConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payOrConfirmButtonAction(myReq);
            }
        });

        if (status.equals(Request.PAID) || status.equals(Request.COMPLETE)) {
            btnCancel.setEnabled(false);
        } else {
            btnCancel.setEnabled(true);
        }
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(RequestDetailActivity.this)
                        .setMessage("Are you sure you want to cancel this request?")
                        .setCancelable(true)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                                removeThisRequestFromDatabase();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        if (status.equals(Request.COMPLETE)) {
            btnSignUpOrUnregister.setEnabled(false);
        } else {
            btnSignUpOrUnregister.setEnabled(true);
        }
        btnSignUpOrUnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpOrUnregisterForMuleToThisRequest(myReq);
            }
        });

        if (status.equals(Request.COMPLETE)) {
            this.btnViewMules.setEnabled(false);
        }
    }

    private void payOrConfirmButtonAction(final Request myReq) {
        if (myReq.getStatus().equals(Request.PAID)) {
            //Deliver money to mule
            DatabaseReference ref = mDatabase.child("users").child(myReq.getMule()).getRef();
            // Attach a listener to read the data at our posts reference
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    UserClass user = dataSnapshot.getValue(UserClass.class);
                    if (user == null || user.getName() == null) {
                        //User was deleted?
                        return;
                    }

                    float inAppMoney = user.getMoney();
                    inAppMoney = inAppMoney + myReq.getReward();
                    mDatabase.child("users").child(myReq.getMule()).child("money").setValue(inAppMoney);

                    uid = myReq.getMule();
                    otherUser = user.getName();
                    currentRating = user.getRating();
                    numRatings = user.getNumRatings();
                    mDatabase.child("users").child(myReq.getMule()).child("money").setValue(inAppMoney);
                    showRatingDialog(otherUser);
                    //TODO notify the mule of payment
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("Payment", "Cannot connect to Firebase");
                }
            });

            //TODO Rate the mule

            //complete the transaction
            mDatabase.child(REQUESTS).child(transactionID).child(STATUS).setValue(Request.COMPLETE);
        } else {
            Intent payIntent = new Intent(this, PaymentActivity.class);
            payIntent.putExtra("transactionID", transactionID);
            this.startActivity(payIntent);
        }
    }

    void showRatingDialog(String otherUser) {
        // Create the fragment and show it as a dialog.
        Bundle bundle = new Bundle();
        bundle.putString("otherUser", otherUser);
        DialogFragment newFragment = new RatingFragment();
        newFragment.setArguments(bundle);
        newFragment.show(getFragmentManager(), "ratings");
    }

    void updateRating(float newRating) {
        float rating = (currentRating + newRating) / ((numRatings + 1));
        mDatabase.child("users").child(uid).child("rating").setValue(rating);
        mDatabase.child("users").child(uid).child("numRatings").setValue(numRatings + 1);
        Toast.makeText(this, "Rated " + Float.toString(rating), Toast.LENGTH_SHORT).show();
    }

    private void removeThisRequestFromDatabase() {
        try {
            clearChat();
            mDatabase.child(REQUESTS).child(transactionID).removeValue();
            DatabaseReference ref = mDatabase.child("potentialMules").getRef();
            // Attach a listener to read the data at our posts reference
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        PotentialMule pMule = postSnapshot.getValue(PotentialMule.class);
                        if (pMule == null || pMule.getRequestID() == null) {
                            //mule unregistered?
                            return;
                        }

                        if (RequestDetailActivity.this.transactionID.equals(pMule.getRequestID())) {
                            postSnapshot.getRef().removeValue();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("Potential Mules", "Cannot connect to Firebase");
                }
            });
            RequestDetailActivity.this.finish();
        } catch (Exception e) {
            Log.e("Error", e.toString());
        }
    }

    private void signUpOrUnregisterForMuleToThisRequest(final Request myReq) {
        if (btnSignUpOrUnregister.getText().toString().equals("unregister")) {
            try {
                clearChat();
                String potentialMuleKey = transactionID + mFirebaseAuth.getCurrentUser().getUid();
                mDatabase.child("potentialMules").child(potentialMuleKey).removeValue();
                mDatabase.child(REQUESTS).child(transactionID).child(MULE).removeValue();
                if (myReq.getStatus().equals(Request.PAID)) {
                    //refund payment
                    DatabaseReference ref = mDatabase.child("users").child(myReq.getCustomer()).getRef();
                    // Attach a listener to read the data at our posts reference
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            UserClass user = dataSnapshot.getValue(UserClass.class);
                            if (user == null || user.getName() == null) {
                                //User was deleted?
                                return;
                            }

                            float inAppMoney = user.getMoney();
                            inAppMoney = inAppMoney + myReq.getReward();
                            mDatabase.child("users").child(myReq.getCustomer()).child("money").setValue(inAppMoney);

                            //TODO notify the customer of mule cancellation and payment return
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("Payment", "Cannot connect to Firebase");
                        }
                    });
                }
                mDatabase.child(REQUESTS).child(transactionID).child(STATUS).setValue(Request.NO_MULE);
                Toast.makeText(this, "Unregistered!", Toast.LENGTH_SHORT).show();
                btnSignUpOrUnregister.setText("sign up");
            } catch (Exception e) {
                Log.e("Error", e.toString());
            }
        } else {
            if (myReq.getMule() != null) {
                Toast.makeText(this, "Sorry, someone already signed up", Toast.LENGTH_SHORT).show();

            } else {
                String key = transactionID + mFirebaseAuth.getCurrentUser().getUid();
                mDatabase.child("potentialMules").child(key)
                        .setValue(new PotentialMule(transactionID, mFirebaseAuth.getCurrentUser().getUid()));

                Toast.makeText(this, "Successfully signed up!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void clearChat() {
        if (transactionID != null) {
            mDatabase.child(PersonalChat.PERSONAL_MESSAGES_CHILD).child(transactionID).removeValue();
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
        String result = "";
        if (response == null) {
            response = "Error with processing the request";
        }
        try {
            JSONObject obj = new JSONObject(response);
            JSONObject departure = new JSONObject(obj.getString("departure"));
            JSONObject arrival = new JSONObject(obj.getString("arrival"));
            result = departure.getString("scheduledTime").substring(11, 16) + " to " + arrival.getString("scheduledTime").substring(11, 16);
            depart = departure.getString("scheduledTime");
            arrive = arrival.getString("scheduledTime");
        } catch (JSONException e) {
            result = e.getMessage();
        }
        //progressBar.setVisibility(View.GONE);
        Log.i("INFO", response);
        //responseView.setText(result);
    }

    protected void parseRealTime(String response) {
        if (response == null) {
            response = "Error with processing the request";
        }
        try {
            JSONObject obj = new JSONObject(response);
            JSONObject geography = new JSONObject(obj.getString("geography"));
            latitude = Double.parseDouble(geography.getString("latitude"));
            longitude = Double.parseDouble(geography.getString("longitude"));
            planedir = Double.parseDouble(geography.getString("direction"));
        } catch (JSONException e) {

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
            String flight = flightNum.getText().toString();

            try {
                //formats the URL containing the API key to add in the flight number (IATA)
                //UNCOMMENT HERE WHEN TESTING API
                URL url = new URL(API_URL + flight);
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
                } finally {
                    //close the connection
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        //returns with errors if couldn't process the http request or returned with nothing and toggles the progress bar
        //sends a Log with the response
        protected void onPostExecute(String response) {
            String flight = flightNum.getText().toString();
            if (response == null) {
                response = "Error with processing the request";
            }
            //progressBar.setVisibility(View.GONE);
            Log.i("INFO", response);
            String formatted_response = response.substring(1, response.length() - 1);
            parseRealTime(formatted_response);
            Intent myIntent = new Intent(RequestDetailActivity.this, MapsActivity.class);
            myIntent.putExtra("Flightnum", flight);
            myIntent.putExtra("Latitude", latitude);
            myIntent.putExtra("Longitude", longitude);
            myIntent.putExtra("Direction", planedir);
            myIntent.putExtra("arrTime", arrive);
            myIntent.putExtra("depTime", depart);
            RequestDetailActivity.this.startActivity(myIntent);
            //responseView.setText(response);
        }
    }
}
