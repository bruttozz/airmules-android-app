package com.example.asthana.airmuleschat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class AllTransactionsActivity extends BaseMenuActivity implements Transactions.TransactionsListener{
    private Transactions fragmentTransactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_transactions);

        fragmentTransactions = (Transactions) getSupportFragmentManager().findFragmentById(R.id.fragmentTransactions);
    }
}
