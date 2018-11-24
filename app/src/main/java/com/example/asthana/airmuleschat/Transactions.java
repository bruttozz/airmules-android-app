package com.example.asthana.airmuleschat;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class Transactions extends Fragment {
    private TransactionsListener TL;

    //Filter control
    private LinearLayout layoutButton;
    private Button btnFilter;
    private LinearLayout layoutFilter;
    private EditText editTextDepCity;
    private EditText editTextDepCountry;
    private EditText editTextDepDate;
    private EditText editTextArrCity;
    private EditText editTextArrCountry;
    private EditText editTextArrDate;
    private Button btnApply;
    private Button btnClear;

    //Database stuff
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabase;
    private RecyclerView listTransactions;
    private TransactionAdapter adapter;
    //private FirebaseRecyclerAdapter adapter;

    public Transactions() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        TL = (TransactionsListener)context;
    }

    public interface TransactionsListener {
        //TODO add methods to the parent activity "listener"
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragView = inflater.inflate(R.layout.fragment_transactions, container, false);

        layoutButton = (LinearLayout)fragView.findViewById(R.id.layoutButton);
        btnFilter = (Button)fragView.findViewById(R.id.btnFilter);
        layoutFilter = (LinearLayout)fragView.findViewById(R.id.layoutFilter);
        editTextDepCity = (EditText)fragView.findViewById(R.id.editTextDepCity);
        editTextDepCountry = (EditText)fragView.findViewById(R.id.editTextDepCountry);
        editTextDepDate = (EditText)fragView.findViewById(R.id.editTextDepDate);
        editTextArrCity = (EditText)fragView.findViewById(R.id.editTextArrCity);
        editTextArrCountry = (EditText)fragView.findViewById(R.id.editTextArrCountry);
        editTextArrDate = (EditText)fragView.findViewById(R.id.editTextArrDate);
        btnApply = (Button)fragView.findViewById(R.id.btnApply);
        btnClear = (Button)fragView.findViewById(R.id.btnClear);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Set-up the recycler view
        listTransactions = (RecyclerView)fragView.findViewById(R.id.listTransactions);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listTransactions.setLayoutManager(linearLayoutManager);

        createDatabaseQueryAdapter();
        listTransactions.setAdapter(adapter);

        return fragView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //add any listeners to the views (except the handler, which is assigned below)
        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnFilter.setVisibility(LinearLayout.GONE);
                layoutFilter.setVisibility(LinearLayout.VISIBLE);
            }
        });

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutFilter.setVisibility(LinearLayout.GONE);
                btnFilter.setVisibility(LinearLayout.VISIBLE);

               adapter.filterRequests();
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutFilter.setVisibility(LinearLayout.GONE);
                btnFilter.setVisibility(LinearLayout.VISIBLE);

                editTextDepCity.setText("");
                editTextDepCountry.setText("");
                editTextDepDate.setText("");
                editTextArrCity.setText("");
                editTextArrCountry.setText("");
                editTextArrDate.setText("");

                adapter.clearRequestFilters();
            }
        });


        //hide the filter layout until it is needed
        layoutFilter.setVisibility(LinearLayout.GONE);
    }

    private void createDatabaseQueryAdapter(){
        //TODO add custom queries depending on the data of interest (ex. requests from specific user)
        DatabaseReference q = mDatabase.child("requests").getRef();
        adapter = new TransactionAdapter(getContext(), q);
    }

    /*
    private void createDatabaseQueryAdapter2(){
        //based on https://github.com/firebase/FirebaseUI-Android/blob/master/database/README.md

        //TODO add custom queries depending on the data of interest
        Query q = mDatabase.child("requests").getRef();
        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(q, Request.class).build();

        adapter = new FirebaseRecyclerAdapter<Request, TransactionHolder>(options) {
            @Override
            public TransactionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.transaction_row, parent, false);
                return new TransactionHolder(parent.getContext(), view);
            }

            @Override
            protected void onBindViewHolder(TransactionHolder holder, int position, Request model) {
                holder.bindTransactionData(model.getTransactionID(),
                        model.getDeparture().getCity(), model.getDeparture().getCountry(),
                        model.getArrival().getCity(), model.getArrival().getCountry(),
                        model.getArrival().getDate(),
                        Float.toString(model.getReward()));
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
    */

    //Based on https://github.com/puf/firebase-stackoverflow-android/blob/master/app/src/main/java/com/firebasedemo/stackoverflow/Activity34962254.java
    private class TransactionAdapter extends RecyclerView.Adapter<TransactionHolder> {
        private Context mContext;
        private DatabaseReference myQuery;
        private ArrayList<Request> requestListAll;
        private ArrayList<Request> requestListToShow;

        public TransactionAdapter(Context mContext, DatabaseReference myQuery){
            this.mContext = mContext;
            this.myQuery = myQuery;

            requestListAll = new ArrayList<Request>();
            requestListToShow = new ArrayList<Request>();

            myQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    requestListAll.clear();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Request r = postSnapshot.getValue(Request.class);
                        requestListAll.add(r);
                    }

                    //TODO sort by date

                    clearRequestFilters();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The request read failed: " + databaseError.getMessage());
                }
            });
        }

        @NonNull
        @Override
        public TransactionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.transaction_row, parent, false);
            return new TransactionHolder(parent.getContext(), view);
        }

        @Override
        public void onBindViewHolder(@NonNull TransactionHolder holder, int position) {
            Request model = requestListToShow.get(position);
            holder.bindTransactionData(model.getTransactionID(),
                    model.getDeparture().getCity(), model.getDeparture().getCountry(),
                    model.getArrival().getCity(), model.getArrival().getCountry(),
                    model.getArrival().getDate(),
                    Float.toString(model.getReward()));
        }

        @Override
        public int getItemCount() {
            return requestListToShow.size();
        }

        private void filterRequests(){
            requestListToShow.clear();

            //apply the filers
            for(Request r : requestListAll){
                if(!dataMatches(editTextDepCity.getText().toString(), r.getDeparture().getCity())){
                    continue;
                }
                if(!dataMatches(editTextDepCountry.getText().toString(), r.getDeparture().getCountry())){
                    continue;
                }
                if(!dataMatches(editTextDepDate.getText().toString(), r.getDeparture().getDate())){
                    continue;
                }

                if(!dataMatches(editTextArrCity.getText().toString(), r.getArrival().getCity())){
                    continue;
                }
                if(!dataMatches(editTextArrCountry.getText().toString(), r.getArrival().getCountry())){
                    continue;
                }
                if(!dataMatches(editTextArrDate.getText().toString(), r.getArrival().getDate())) {
                    continue;
                }

                requestListToShow.add(r);
            }

            notifyDataSetChanged();
        }

        private boolean dataMatches(String guiData, String requestData){
            if(guiData == null || guiData.isEmpty()){
                //We don't care to sort on this field
                return true;
            }

            if(guiData.equals(requestData)){
                return true;
            }

            return false;
        }

        private void clearRequestFilters(){
            requestListToShow.clear();
            requestListToShow.addAll(requestListAll);

            notifyDataSetChanged();
        }
    }
}



class TransactionHolder extends RecyclerView.ViewHolder {
    private Context mContext;
    private TextView txtFromLocation;
    private TextView txtToLocation;
    private TextView txtArrivalDate;
    private TextView txtPostedPrice;

    private String transactionID;

    public TransactionHolder(Context context, View itemView){
        super(itemView);
        mContext = context;

        txtFromLocation = (TextView) itemView.findViewById(R.id.txtFromLocation);
        txtToLocation = (TextView) itemView.findViewById(R.id.txtToLocation);
        txtArrivalDate = (TextView) itemView.findViewById(R.id.txtArrivalDate);
        txtPostedPrice = (TextView) itemView.findViewById(R.id.txtPostedPrice);

        //With the Firebase Adapter, for some reason, we set the listener directly on the view
        //instead of on the view holder
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO make an intent to start the request details activity
                Toast.makeText(mContext, transactionID, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void bindTransactionData(String transactionID,
                                    String fromzCity, String fromCountry,
                                    String toCity, String toCountry,
                                    String arrivalDate, String price) {
        this.transactionID = transactionID;
        txtFromLocation.setText(fromzCity + ", " + fromCountry);
        txtToLocation.setText(toCity + ", " + toCountry);
        txtArrivalDate.setText(arrivalDate);
        txtPostedPrice.setText("$" + price);
    }
}
