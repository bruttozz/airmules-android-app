package com.example.asthana.airmuleschat;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class Transactions extends Fragment {
    //Should match database:
    public static final String TO_LOCATION = "TO_LOCATION";
    public static final String FROM_LOCATION = "FROM_LOCATION";
    public static final String PRICE = "PRICE";

    private TransactionsListener TL;

    private RecyclerView listTransactions;

    public Transactions() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        TL = (TransactionsListener)context;
    }

    public interface TransactionsListener {
        //TODO add methods to the listener
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragView = inflater.inflate(R.layout.fragment_transactions, container, false);

        //Set-up the recycler view
        listTransactions = (RecyclerView)fragView.findViewById(R.id.listTransactions);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listTransactions.setLayoutManager(linearLayoutManager);
        //Get the data
        //Transaction ID => perimeter => value
        HashMap<String, HashMap<String, String>> transactionData = getTransactionData();
        //Link the adapter
        TransactionsAdapter adapter = new TransactionsAdapter(transactionData);
        listTransactions.setAdapter(adapter);

        return fragView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //TODO: Add listeners to the recycler view
    }

    private HashMap<String, HashMap<String, String>> getTransactionData(){
        //TODO get from database...
        HashMap<String, HashMap<String, String>> transactionData = new HashMap<String, HashMap<String, String>>();

        HashMap<String, String> data = new HashMap<String, String>();
        transactionData.put("abc", data);
        data.put(TO_LOCATION, "Boston");
        data.put(FROM_LOCATION, "London");
        data.put(PRICE, "50");

        data = new HashMap<String, String>();
        transactionData.put("qwe", data);
        data.put(TO_LOCATION, "Beijing");
        data.put(FROM_LOCATION, "New York City");
        data.put(PRICE, "100");

        data = new HashMap<String, String>();
        transactionData.put("rty", data);
        data.put(TO_LOCATION, "Denver");
        data.put(FROM_LOCATION, "Houston");
        data.put(PRICE, "20");

        return transactionData;
    }
}

class TransactionsAdapter extends RecyclerView.Adapter<TransactionHolder> {
    private HashMap<String, HashMap<String, String>> transactionData;

    public TransactionsAdapter(HashMap<String, HashMap<String, String>> transactionData){
        this.transactionData = transactionData;
    }

    @NonNull
    @Override
    public TransactionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transaction_row, parent, false);
        return new TransactionHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionHolder holder, int position) {
        //This is a hack for now
        HashMap<String, String> rowData = null;
        int index = 0;
        for(HashMap<String, String> rd : transactionData.values()){
            if(index == position){
                rowData = rd;
                break;
            }
            index++;
        }
        holder.bindTransactionData(rowData.get(Transactions.TO_LOCATION),
                rowData.get(Transactions.FROM_LOCATION),
                rowData.get(Transactions.PRICE));
    }

    @Override
    public int getItemCount() {
        return transactionData.size();
    }
}

class TransactionHolder extends RecyclerView.ViewHolder{
    private TextView txtFromLocation;
    private TextView txtToLocation;
    private TextView txtPostedPrice;

    public TransactionHolder(View itemView){
        super(itemView);

        txtFromLocation = (TextView) itemView.findViewById(R.id.txtFromLocation);
        txtToLocation = (TextView) itemView.findViewById(R.id.txtToLocation);
        txtPostedPrice = (TextView) itemView.findViewById(R.id.txtPostedPrice);
    }

    public void bindTransactionData(String from, String to, String price) {
        txtFromLocation.setText(from);
        txtToLocation.setText(to);
        txtPostedPrice.setText("$" + price);
    }
}
