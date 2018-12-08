package com.example.asthana.airmuleschat;

import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;
import android.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Withdraw extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private TextView moneyLeft;
    private EditText amountInput;
    private EditText accountInfo;
    private Button withdrawbtn;

    private String userID;
    private static final String USERS = "users";
    private static final String MONEY = "money";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Spinner spinner = findViewById(R.id.choice);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.methods, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
//        spinner.setOnItemSelectedListener(this);

        moneyLeft = (TextView) findViewById(R.id.textView2);

        amountInput = (EditText) findViewById(R.id.amount_input);
        accountInfo = (EditText) findViewById(R.id.account_info);

        withdrawbtn = (Button) findViewById(R.id.submit_btn);

        DatabaseReference ref = mDatabase.child(USERS).child(mFirebaseAuth.getCurrentUser().getUid()).getRef();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserClass me = dataSnapshot.getValue(UserClass.class);
                float myFunds = me.getMoney();
                moneyLeft.setText(PaymentActivity.convertToMoneyFormatString(myFunds));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Error", databaseError.toString());
            }
        });

        withdrawbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float amount = Float.parseFloat(amountInput.getText().toString());
                String account = accountInfo.getText().toString();
//                accountInfo.setText(amt);
                withdrawSuccess(amount);
//                startActivity(new Intent(Withdraw.this, UserProfileActivity.class));
            }
        });
    }

    private void withdrawSuccess(float num) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = mDatabase.child(USERS).child(mFirebaseAuth.getCurrentUser().getUid()).getRef();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserClass user = dataSnapshot.getValue(UserClass.class);
                float inAppMoney = user.getMoney();
                String inAppMoneyString = convertToMoneyFormatString(inAppMoney);

                if(inAppMoney < num){
                    new AlertDialog.Builder(Withdraw.this)
                            .setCancelable(false)
                            .setMessage("Not enough funds, have only $" + inAppMoneyString)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            }).show();
                }
                else{
                    inAppMoney = inAppMoney - num;
                    mDatabase.child("users").child(mFirebaseAuth.getCurrentUser().getUid()).child("money").setValue(inAppMoney);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Error", databaseError.toString());
            }
        });
    }

    public static String convertToMoneyFormatString(float money){
        return convertToMoneyFormatString(money, true);
    }

    public static String convertToMoneyFormatString(float money, boolean addCommas){
        String format = "%,.2f";
        if(!addCommas){
            format.replace(",", "");
        }
        String moneyString = String.format(format, money);
        return moneyString;
    }

}
