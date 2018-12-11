package com.example.asthana.airmuleschat;


import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tencent.mm.opensdk.modelmsg.SendAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;


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
    private AutoCompleteTextView editTextDepCity;
    private AutoCompleteTextView editTextDepCountry;
    private TextView editTextDepDate;
    private AutoCompleteTextView editTextArrCity;
    private AutoCompleteTextView editTextArrCountry;
    private TextView editTextArrDate;
    private Button btnApply;
    private Button btnClear;
    private SwipeRefreshLayout swipeRefreshLayout;

    //Database stuff
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabase;
    private RecyclerView listTransactions;
    private TransactionAdapter adapter;
    //private FirebaseRecyclerAdapter adapter;

    // calendar
    private Calendar myCalendar;
    private DatePickerDialog.OnDateSetListener dateDep;
    private DatePickerDialog.OnDateSetListener dateArr;

    public Transactions() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        TL = (TransactionsListener) context;
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
        editTextDepCity = (AutoCompleteTextView) fragView.findViewById(R.id.editTextDepCity);
        editTextDepCountry = (AutoCompleteTextView) fragView.findViewById(R.id.editTextDepCountry);
        editTextDepDate = (TextView) fragView.findViewById(R.id.editTextDepDate);
        editTextArrCity = (AutoCompleteTextView) fragView.findViewById(R.id.editTextArrCity);
        editTextArrCountry = (AutoCompleteTextView) fragView.findViewById(R.id.editTextArrCountry);
        editTextArrDate = (TextView) fragView.findViewById(R.id.editTextArrDate);
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

        //add pop-up date picker
        myCalendar = Calendar.getInstance();
        editTextDepDate.setClickable(true);
        dateDep = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel("dep");
            }

        };
        editTextDepDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(getContext(), dateDep, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        editTextArrDate.setClickable(true);
        dateArr = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel("arr");
            }
        };
        editTextArrDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(getContext(), dateArr, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        swipeRefreshLayout = fragView.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.updateDataFromFirebase();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 2500);
            }
        });

        return fragView;
    }

    private void updateLabel(String type) {
        String myFormat = "dd" + Request.LocationInfo.DATE_DELIMITER + "MM" + Request.LocationInfo.DATE_DELIMITER + "yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        if (type.equals("arr"))
            editTextArrDate.setText(sdf.format(myCalendar.getTime()));
        else
            editTextDepDate.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //add any listeners to the views (except the handler, which is assigned below)

        ((BaseMenuActivity) TL).syncUpCityAndCountry(getContext(),
                view.findViewById(R.id.depCityLabel),
                editTextDepCity, editTextDepCountry);
        ((BaseMenuActivity) TL).syncUpCityAndCountry(getContext(),
                view.findViewById(R.id.arrCityLabel),
                editTextArrCity, editTextArrCountry);

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

    @Override
    public void onResume() {
        super.onResume();

        //Every time we resume the activity make sure we pull the latest data
        adapter.updateDataFromFirebase();
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

    public interface TransactionsListener {
        //TODO add methods to the parent activity "listener"
    }

    //Based on https://github.com/puf/firebase-stackoverflow-android/blob/master/app/src/main/java/com/firebasedemo/stackoverflow/Activity34962254.java
    private class TransactionAdapter extends RecyclerView.Adapter<TransactionHolder> {
        private Context mContext;
        private DatabaseReference myQuery;
        private ArrayList<Request> requestListAll;
        private ArrayList<Request> requestListToShow;

        public TransactionAdapter(Context mContext, DatabaseReference myQuery) {
            this.mContext = mContext;
            this.myQuery = myQuery;
        }

        protected void updateDataFromFirebase() {
            requestListAll = new ArrayList<Request>();
            requestListToShow = new ArrayList<Request>();

            myQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<GeoPref> userGeoPrefs = new ArrayList<GeoPref>();
                    if (myType.equals(TYPE_ALL)) {
                        //Apply the users geo prefs
                        Query myGeoPrefsQuery = mDatabase.child("users").child(mFirebaseAuth.getCurrentUser().getUid())
                                .child(GeographicalPreferences.DATABASE_TABLE_NAME).getRef();
                        myGeoPrefsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot2) {
                                for (DataSnapshot postSnapshot : dataSnapshot2.getChildren()) {
                                    GeoPref geoPref = postSnapshot.getValue(GeoPref.class);
                                    userGeoPrefs.add(geoPref);
                                }

                                setUpDataForAdapter(dataSnapshot, userGeoPrefs);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e("Transactions_GeoPrefs", "The Geo Prefs read failed: " + databaseError.getMessage());
                            }
                        });
                    } else {
                        setUpDataForAdapter(dataSnapshot, userGeoPrefs);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("Transactions", "The request read failed: " + databaseError.getMessage());
                }
            });
        }

        private void setUpDataForAdapter(DataSnapshot dataSnapshot, ArrayList<GeoPref> userGeoPrefs) {
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
                    if (r.getMule() != null || r.getCustomer().equals(mFirebaseAuth.getCurrentUser().getUid())) {
                        //There is already a mule or there is no mule but I am the customer (can't sign up for own activity)
                        continue;
                    }

                    if (!userGeoPrefs.isEmpty()) {
                        boolean geoPrefMatch = false;
                        for (GeoPref geoPref : userGeoPrefs) {
                            if (geoPref.prefMatches(r.getDeparture().getCity(), r.getDeparture().getCountry())
                                    || geoPref.prefMatches(r.getArrival().getCity(), r.getArrival().getCountry())) {
                                //The request matches a geo preference
                                geoPrefMatch = true;
                                break;
                            }
                        }
                        if (!geoPrefMatch) {
                            //Did not match a geo pref, so skip the request
                            continue;
                        }
                    }
                }

                //Request is good to add
                requestListAll.add(r);
            }

            //Sort by arrive date
            Collections.sort(requestListAll, new Comparator<Request>() {
                @Override
                public int compare(Request r1, Request r2) {
                    return Request.LocationInfo.compareDates(r1.getArrival().getDate(),
                            r2.getArrival().getDate(),
                            true);
                }
            });

            clearRequestFilters();
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
            if (model.getStatus().equals(Request.NO_PAYMENT)) {

                holder.itemView.setBackgroundColor(Color.parseColor("#F5DEB3")); // no payment: yellow

            } else if (model.getStatus().equals(Request.PAID)) {

                holder.itemView.setBackgroundColor(Color.parseColor("#8FBC8F"));// paid: green

            } else if (model.getStatus().equals(Request.COMPLETE)) {

                holder.itemView.setBackgroundColor(Color.parseColor("#ADD8E6")); // complete: blue

            }
            holder.bindTransactionData(model.getTransactionID(),
                    model.getDeparture().getCity(), model.getDeparture().getCountry(),
                    model.getArrival().getCity(), model.getArrival().getCountry(),
                    model.getArrival().getDate(),
                    PaymentActivity.convertToMoneyFormatString(model.getReward()));
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
