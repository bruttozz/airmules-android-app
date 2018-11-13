package com.example.asthana.airmuleschat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;


public class PickPersonChat extends AppCompatActivity{
    private DatabaseReference mDatabase;
    private ChildEventListener mChildEventListener;
    private RecyclerView userList;
    private String TAG="Pick Person";
    ArrayAdapter<String> mAdapter;
    private Vector <String> spinnerArray;
    private HashMap<Integer,String> spinnerMap;
    private Button button;
    private FirebaseUser mFirebaseUser;
    private String mUniqueChatID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_person_chat);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        spinnerMap = new HashMap<Integer, String>();

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Spinner userSpinner = (Spinner) findViewById(R.id.spinner);
                String selectedID = spinnerMap.get(userSpinner.getSelectedItemPosition());
                Log.e(TAG, "Selected :" + selectedID);

                if (mFirebaseUser.getUid().compareTo(selectedID) <= 0)
                {
                    mUniqueChatID = mFirebaseUser.getUid() + selectedID;
                }
                else
                {
                    mUniqueChatID = selectedID + mFirebaseUser.getUid();
                }
                Intent i = new Intent(PickPersonChat.this, PersonalChat.class);
                i.putExtra("chatID", mUniqueChatID);
                startActivity(i);
            }
        });

        Query query = mDatabase.child("users").orderByChild("id");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    spinnerArray = new Vector<String>();
                    int i = 0;
                    for (DataSnapshot user : dataSnapshot.getChildren()) {
                        if (!(user.getKey().equals(mFirebaseUser.getUid()))){

                            Log.e(TAG, "" + user.getKey() + " : " + user.getValue());
                            spinnerArray.add(user.getValue().toString());
                            spinnerMap.put(i, user.getKey());
                            i++;
                        }
                    }

                    Spinner userSpinner = (Spinner) findViewById(R.id.spinner);
                    ArrayAdapter<String> areasAdapter = new ArrayAdapter<String>(PickPersonChat.this,
                            android.R.layout.simple_spinner_item, spinnerArray);
                    areasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    userSpinner.setAdapter(areasAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
