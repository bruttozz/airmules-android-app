package com.example.asthana.airmuleschat;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MulesRadioAdapter extends ArrayAdapter<UserClass> {

    private int selectedIndex = -1;
    private UserClass selectedUser;
    private HashMap<UserClass, String> mulesToIDs;
    private String transactionID;

    public MulesRadioAdapter(Context context, int activity_radio_button, List<UserClass> availableMules,
                             HashMap<UserClass, String> mulesToIDs, String transactionID) {
        super(context, activity_radio_button, availableMules);
        this.mulesToIDs = mulesToIDs;
        this.transactionID = transactionID;
    }

    public void setSelectedIndex(int index) {
        selectedIndex = index;
    }

    public UserClass getSelectedItem() {
        return selectedUser;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.dialog_radio_button_item, null);
        }

        RadioButton rbSelect = (RadioButton) v.findViewById(R.id.radioButton);

        rbSelect.setChecked(position == selectedIndex);
        rbSelect.setTag(position);
        rbSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedIndex = (Integer) view.getTag();
                notifyDataSetChanged();
            }
        });

        UserClass mule = getItem(position);

        if (mule != null) {
            //Set the specific mules data
            TextView muleName = v.findViewById(R.id.dialogTxtMuleName);
            RatingBar muleRating = v.findViewById(R.id.dialogMuleRating);
            TextView txtNumRatings = v.findViewById(R.id.txtNumRatings);
            TextView txtPopLocation = v.findViewById(R.id.txtPopLocation);
            Button btnHistory = v.findViewById(R.id.btnHistory);
            Button btnDelete = v.findViewById(R.id.btnDelete);

            muleName.setText(mule.getName());
            muleRating.setRating(mule.getRating());
            txtNumRatings.setText(Integer.toString(mule.getNumRatings()));

            //Find the most popular location the mule has gone to
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("requests").getRef();
            // Attach a listener to read the data at our posts reference
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap<String, Integer> locationCounts = new HashMap<String, Integer>();

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Request req = postSnapshot.getValue(Request.class);
                        if (req == null || req.getTransactionID() == null) {
                            //req was deleted?
                            continue;
                        }

                        //Is the request completed by this mule?
                        if (req.getStatus().equals(Request.COMPLETE) && req.getMule() != null && req.getMule().equals(mulesToIDs.get(mule))) {
                            String depLocation = req.getDeparture().getLocationString();
                            Integer countD = locationCounts.get(depLocation);
                            if (countD == null) {
                                countD = 0;
                            }
                            countD++;
                            locationCounts.put(depLocation, countD);

                            String arrLocation = req.getArrival().getLocationString();
                            Integer countA = locationCounts.get(arrLocation);
                            if (countA == null) {
                                countA = 0;
                            }
                            countA++;
                            locationCounts.put(arrLocation, countA);
                        }
                    }

                    String maxLoc = "No Data";
                    int maxCount = 0;
                    for (Map.Entry<String, Integer> loc : locationCounts.entrySet()) {
                        if (loc.getValue() > maxCount) {
                            maxLoc = loc.getKey();
                            maxCount = loc.getValue();
                        }
                    }

                    txtPopLocation.setText(maxLoc);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("Mules List", "Cannot connect to Firebase");
                }
            });

            btnHistory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(MulesRadioAdapter.this.getContext(), "Stubbed Functionality...", Toast.LENGTH_SHORT).show();
                }
            });

            //Remove the mule from the list from the list of potential mules
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    remove(mule);

                    String muleID = mulesToIDs.get(mule);

                    String potentialMuleKey = transactionID + muleID;
                    FirebaseDatabase.getInstance().getReference().child("potentialMules").child(potentialMuleKey).removeValue();

                    DatabaseReference reqRef = FirebaseDatabase.getInstance().getReference().child("requests").child(transactionID).getRef();
                    reqRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Request req = dataSnapshot.getValue(Request.class);

                            if (req == null || req.getTransactionID() == null) {
                                return;
                            }

                            if (req.getMule() != null && req.getMule().equals(muleID)) {
                                FirebaseDatabase.getInstance().getReference().child("requests").child(transactionID).child("mule").removeValue();
                                FirebaseDatabase.getInstance().getReference().child("requests").child(transactionID).child("status").setValue(Request.NO_MULE);
                                FirebaseDatabase.getInstance().getReference().child("requests").child(transactionID).child("flightNumber").removeValue();
                                clearChat();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w("Error", databaseError.toString());
                        }
                    });
                }
            });

            //If this is the current mule, set a green border to the row
            View v_final = v;
            DatabaseReference reqRef = FirebaseDatabase.getInstance().getReference().child("requests").child(transactionID).getRef();
            reqRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Request req = dataSnapshot.getValue(Request.class);

                    if (req == null || req.getTransactionID() == null) {
                        return;
                    }

                    String muleID = mulesToIDs.get(mule);
                    if (req.getMule() != null && req.getMule().equals(muleID)) {
                        v_final.setBackground(getContext().getResources().getDrawable(R.drawable.green_border));
                    }
                    else{
                        v_final.setBackground(getContext().getResources().getDrawable(R.drawable.black_border));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w("Error", databaseError.toString());
                }
            });

            if (rbSelect.isChecked()) {
                selectedUser = mule;
            }
        }

        return v;
    }

    private void clearChat() {
        if (transactionID != null) {
            FirebaseDatabase.getInstance().getReference().
                    child(PersonalChat.PERSONAL_MESSAGES_CHILD).child(transactionID).removeValue();
        }
    }
}