package com.example.asthana.airmuleschat;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.phonepe.intent.sdk.ui.TransactionActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class Transactions extends Fragment {
    public static final String INFO_TYPE = "TYPE";
    public static final String TYPE_ALL = "ALL";    //no mule attached
    public static final String TYPE_CUSTOMER = "CUSTOMER";  //I am the customer
    public static final String TYPE_MULE = "MULE";  //I am the mule
    private String myType;

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
        TL = (TransactionsListener) context;
    }

    public interface TransactionsListener {
        //TODO add methods to the parent activity "listener"
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //What kind of request data do we want to show?
        try {
            Bundle args = getArguments();
            myType = args.getString(INFO_TYPE, TYPE_ALL);
        } catch (Exception e) {
            //Data was not set, so default to all requests
            myType = TYPE_ALL;
        }

        // Inflate the layout for this fragment
        View fragView = inflater.inflate(R.layout.fragment_transactions, container, false);

        layoutButton = (LinearLayout) fragView.findViewById(R.id.layoutButton);
        btnFilter = (Button) fragView.findViewById(R.id.btnFilter);
        layoutFilter = (LinearLayout) fragView.findViewById(R.id.layoutFilter);
        editTextDepCity = (EditText) fragView.findViewById(R.id.editTextDepCity);
        editTextDepCountry = (EditText) fragView.findViewById(R.id.editTextDepCountry);
        editTextDepDate = (EditText) fragView.findViewById(R.id.editTextDepDate);
        editTextArrCity = (EditText) fragView.findViewById(R.id.editTextArrCity);
        editTextArrCountry = (EditText) fragView.findViewById(R.id.editTextArrCountry);
        editTextArrDate = (EditText) fragView.findViewById(R.id.editTextArrDate);
        btnApply = (Button) fragView.findViewById(R.id.btnApply);
        btnClear = (Button) fragView.findViewById(R.id.btnClear);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Set-up the recycler view
        listTransactions = (RecyclerView) fragView.findViewById(R.id.listTransactions);
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

    private void createDatabaseQueryAdapter() {
        /*
        //This doesn't pay attention to equalTo for some reason, so we will handle it on the client side
        DatabaseReference q;
        if(myType.equals(TYPE_CUSTOMER)){
            q = mDatabase.child("requests").orderByChild("customer").equalTo(mFirebaseAuth.getCurrentUser().getUid()).getRef();
        }else if(myType.equals(TYPE_MULE)){
            q = mDatabase.child("requests").orderByChild("mule").equalTo(mFirebaseAuth.getCurrentUser().getUid()).getRef();
        }else{
            q = mDatabase.child("requests").orderByChild("mule").equalTo(null).getRef();
        }
        */

        DatabaseReference q = mDatabase.child("requests").getRef();
        adapter = new TransactionAdapter(getContext(), q);
    }

    /*
    The Firebase adapter does not give us enough control for filtering the data
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

        public TransactionAdapter(Context mContext, DatabaseReference myQuery) {
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

                        if (myType.equals(TYPE_CUSTOMER)) {
                            if (!r.getCustomer().equals(mFirebaseAuth.getCurrentUser().getUid())) {
                                continue;
                            }
                        } else if (myType.equals(TYPE_MULE)) {
                            if (r.getMule() == null || !r.getMule().equals(mFirebaseAuth.getCurrentUser().getUid())) {
                                continue;
                            }
                        } else {
                            if (r.getMule() != null) {
                                continue;
                            }
                        }

                        requestListAll.add(r);
                    }

                    //Sort by arrive date
                    final int preferLatestFirst = -1;
                    Collections.sort(requestListAll, new Comparator<Request>() {
                        @Override
                        public int compare(Request r1, Request r2) {
                            String arrDate1 = r1.getArrival().getDate();
                            String[] arrDate1Data = arrDate1.split(Request.LocationInfo.DATE_DELIMITER);
                            String arrDate2 = r2.getArrival().getDate();
                            String[] arrDate2Data = arrDate2.split(Request.LocationInfo.DATE_DELIMITER);

                            int compare;
                            //year
                            compare = arrDate1Data[Request.LocationInfo.YEAR_INDEX].compareTo(arrDate2Data[Request.LocationInfo.YEAR_INDEX]);
                            if (compare != 0) {
                                return preferLatestFirst * compare;
                            }
                            //month
                            compare = arrDate1Data[Request.LocationInfo.MONTH_INDEX].compareTo(arrDate2Data[Request.LocationInfo.MONTH_INDEX]);
                            if (compare != 0) {
                                return preferLatestFirst * compare;
                            }
                            //day
                            compare = arrDate1Data[Request.LocationInfo.DAY_INDEX].compareTo(arrDate2Data[Request.LocationInfo.DAY_INDEX]);
                            return preferLatestFirst * compare;
                        }
                    });

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

        private void filterRequests() {
            requestListToShow.clear();

            //apply the filers
            for (Request r : requestListAll) {
                if (!dataMatches(editTextDepCity.getText().toString(), r.getDeparture().getCity())) {
                    continue;
                }
                if (!dataMatches(editTextDepCountry.getText().toString(), r.getDeparture().getCountry())) {
                    continue;
                }
                if (!dataMatches(editTextDepDate.getText().toString(), r.getDeparture().getDate())) {
                    continue;
                }

                if (!dataMatches(editTextArrCity.getText().toString(), r.getArrival().getCity())) {
                    continue;
                }
                if (!dataMatches(editTextArrCountry.getText().toString(), r.getArrival().getCountry())) {
                    continue;
                }
                if (!dataMatches(editTextArrDate.getText().toString(), r.getArrival().getDate())) {
                    continue;
                }

                requestListToShow.add(r);
            }

            notifyDataSetChanged();
        }

        private boolean dataMatches(String guiData, String requestData) {
            if (guiData == null || guiData.isEmpty()) {
                //We don't care to sort on this field
                return true;
            }

            if (guiData.equals(requestData)) {
                return true;
            }

            return false;
        }

        private void clearRequestFilters() {
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

    public TransactionHolder(Context context, View itemView) {
        super(itemView);
        mContext = context;

        txtFromLocation = (TextView) itemView.findViewById(R.id.txtFromLocation);
        txtToLocation = (TextView) itemView.findViewById(R.id.txtToLocation);
        txtArrivalDate = (TextView) itemView.findViewById(R.id.txtArrivalDate);
        txtPostedPrice = (TextView) itemView.findViewById(R.id.txtPostedPrice);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent seeRequestDetail = new Intent(mContext, RequestDetailActivity.class);
                seeRequestDetail.putExtra("transactionID", transactionID);
                mContext.startActivity(seeRequestDetail);
            }
        });
    }

    public void bindTransactionData(String transactionID,
                                    String fromCity, String fromCountry,
                                    String toCity, String toCountry,
                                    String arrivalDate, String price) {
        this.transactionID = transactionID;
        txtFromLocation.setText(fromCity + ", " + fromCountry);
        txtToLocation.setText(toCity + ", " + toCountry);
        txtArrivalDate.setText(arrivalDate);
        txtPostedPrice.setText("$" + price);
    }
}
