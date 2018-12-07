package com.example.asthana.airmuleschat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class GeographicalPreferences extends BaseMenuActivity {
    protected static final String DATABASE_TABLE_NAME = "geoPrefs";

    private FloatingActionButton buttonAdd;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabase;
    private RecyclerView listPreferences;
    private FirebaseRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geographical_preferences);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        buttonAdd = (FloatingActionButton) findViewById(R.id.buttonAdd);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAddDialog();
            }
        });

        listPreferences = (RecyclerView)findViewById(R.id.listPreferences);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listPreferences.setLayoutManager(linearLayoutManager);

        createDatabaseQueryAdapter();
        listPreferences.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.geo_pref_menu);
        item.setVisible(false);
        return true;
    }

    private void createDatabaseQueryAdapter(){
        //based on https://github.com/firebase/FirebaseUI-Android/blob/master/database/README.md

        Query q = mDatabase.child("users").child(mFirebaseAuth.getCurrentUser().getUid()).child(DATABASE_TABLE_NAME).getRef();
        FirebaseRecyclerOptions<GeoPref> options = new FirebaseRecyclerOptions.Builder<GeoPref>()
                .setQuery(q, GeoPref.class).build();

        adapter = new FirebaseRecyclerAdapter<GeoPref, GeoPrefHolder>(options) {
            @Override
            public GeoPrefHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.geo_pref_row, parent, false);
                return new GeoPrefHolder(view);
            }

            @Override
            protected void onBindViewHolder(GeoPrefHolder holder, int position, GeoPref model) {
                holder.bindTransactionData(model);
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void createAddDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getResources().getString(R.string.addGeoPrefTitle));

        View content = getLayoutInflater().inflate(R.layout.add_geo_pref, null);
        final AutoCompleteTextView edtTxtCity = (AutoCompleteTextView) content.findViewById(R.id.edtTxtCity);
        final AutoCompleteTextView edtTxtCountry = (AutoCompleteTextView) content.findViewById(R.id.edtTxtCountry);
        syncUpCityAndCountry(getBaseContext(),
                content.findViewById(R.id.cityLabel),
                edtTxtCity, edtTxtCountry);
        builder.setView(content);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //We want to do error handling, which stops the dialog from always closing,
                //so we can only do that after the dialog is made
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();

        //Base on https://stackoverflow.com/questions/40261250/validation-on-edittext-in-alertdialog
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Perform a final check of the city/country data
                edtTxtCity.performValidation();
                edtTxtCountry.performValidation();

                if(edtTxtCountry.getText().toString().isEmpty()){
                    Toast toast = Toast.makeText(getApplicationContext(),"Country is required", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                //We will allow the city to be unspecified

                String city = null;
                if(!edtTxtCity.getText().toString().isEmpty()){
                    city = edtTxtCity.getText().toString();
                }
                final String city_final = city;
                final String country = edtTxtCountry.getText().toString();
                final String key = GeoPref.createKey(city_final, country);

                DatabaseReference q = mDatabase.child("users").child(mFirebaseAuth.getCurrentUser().getUid()).child(DATABASE_TABLE_NAME)
                        .child(key).getRef();
                q.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            //We already have this entry
                            Toast toast = Toast.makeText(getApplicationContext(),"Preference already exists", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        else{
                            //New entry, so let's add it
                            mDatabase.child("users").child(mFirebaseAuth.getCurrentUser().getUid()).child(DATABASE_TABLE_NAME)
                                    .child(key).setValue(new GeoPref(city_final, country));
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("GeoPref", "Cannot connect to Firebase");
                    }
                });
            }
        });
    }
}

class GeoPref{
    private String city;
    private String country;

    public GeoPref(){
    }

    public GeoPref(String city, String country){
        this.city = city;
        this.country = country;
    }

    public static String createKey(String city, String country){
        if(city == null){
            return country;
        }
        return city + ", " + country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLocationString(){
        return createKey(getCity(), getCountry());
    }

    public boolean prefMatches(String cityToCheck, String countryToCheck){
        if(city != null){
            return getLocationString().equals(createKey(cityToCheck, countryToCheck));
        }
        else{
            return country.equals(countryToCheck);
        }
    }
}

class GeoPrefHolder extends RecyclerView.ViewHolder {
    private GeoPref geoPref;
    private TextView txtLocation;
    private Button btnDelete;

    public GeoPrefHolder(View itemView) {
        super(itemView);

        txtLocation = (TextView) itemView.findViewById(R.id.txtLocation);
        btnDelete = (Button) itemView.findViewById(R.id.btnDelete);
    }

    public void bindTransactionData(GeoPref gP) {
        geoPref = gP;

        txtLocation.setText(geoPref.getLocationString());
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(GeographicalPreferences.DATABASE_TABLE_NAME).child(geoPref.getLocationString()).removeValue();
            }
        });
    }
}
