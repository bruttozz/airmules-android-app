package com.example.asthana.airmuleschat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class AllTransactionsActivity extends BaseMenuActivity implements Transactions.TransactionsListener {
    private TextView txtRequestTypes;
    private Transactions fragmentReqTransactions;

    public static Intent createIntentForAllRequests(Context originalActivity) {
        Intent viewTransactions = new Intent(originalActivity, AllTransactionsActivity.class);
        viewTransactions.putExtra(Transactions.INFO_TYPE, Transactions.TYPE_ALL);
        return viewTransactions;
    }

    /**
     * Requests where current user is the customer
     */
    public static Intent createIntentForCustomerRequests(Context originalActivity) {
        Intent viewTransactions = new Intent(originalActivity, AllTransactionsActivity.class);
        viewTransactions.putExtra(Transactions.INFO_TYPE, Transactions.TYPE_CUSTOMER);
        return viewTransactions;
    }

    /**
     * Requests where current user is the mule
     */
    public static Intent createIntentForMuleRequests(Context originalActivity) {
        Intent viewTransactions = new Intent(originalActivity, AllTransactionsActivity.class);
        viewTransactions.putExtra(Transactions.INFO_TYPE, Transactions.TYPE_MULE);
        return viewTransactions;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_transactions);

        Intent intent = getIntent();
        String myType = intent.getStringExtra(Transactions.INFO_TYPE);
        if (myType == null) {
            myType = Transactions.TYPE_ALL;     //Default request display
        }

        txtRequestTypes = findViewById(R.id.txtRequestTypes);
        setTextByType(myType);

        //Create the fragment on the fly so that we can pass it the type of requests it should show
        fragmentReqTransactions = new Transactions();
        Bundle bundle = new Bundle();
        bundle.putString(Transactions.INFO_TYPE, myType);
        fragmentReqTransactions.setArguments(bundle);
        android.support.v4.app.FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_layout, fragmentReqTransactions);
        fragmentTransaction.commit();
    }

    private void setTextByType(String myType) {
        String text;
        if (myType.equals(Transactions.TYPE_CUSTOMER)) {
            text = getResources().getString(R.string.customer_transactions);
        } else if (myType.equals(Transactions.TYPE_MULE)) {
            text = getResources().getString(R.string.mule_transactions);
        } else {
            text = getResources().getString(R.string.all_transactions);
        }
        txtRequestTypes.setText(text);
    }
}
