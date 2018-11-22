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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

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
    private FirebaseRecyclerAdapter adapter;

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

                //TODO apply the filters
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
            }
        });


        //hide the filter layout until it is needed
        layoutFilter.setVisibility(LinearLayout.GONE);
    }

    private void createDatabaseQueryAdapter(){
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
