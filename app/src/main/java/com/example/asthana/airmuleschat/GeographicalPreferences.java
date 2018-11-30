package com.example.asthana.airmuleschat;

import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GeographicalPreferences extends AppCompatActivity {
    private static final String DATABASE_TABLE_NAME = "geoPrefs";

    private FloatingActionButton buttonAdd;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabase;
    private RecyclerView listPreferences;

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
    }

    private void createAddDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getResources().getString(R.string.addGeoPrefTitle));

        View content = getLayoutInflater().inflate(R.layout.add_geo_pref, null);
        final EditText edtTxtCity = (EditText) content.findViewById(R.id.edtTxtCity);
        final EditText edtTxtCountry = (EditText) content.findViewById(R.id.edtTxtCountry);
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
        dialog.show();

        //Base on https://stackoverflow.com/questions/40261250/validation-on-edittext-in-alertdialog
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edtTxtCountry.getText().toString().isEmpty()){
                    Toast toast = Toast.makeText(getApplicationContext(),"Country is required", Toast.LENGTH_SHORT);
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
}
