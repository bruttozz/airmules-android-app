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
import android.support.annotation.NonNull;

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

import com.payelves.sdk.EPay;
import com.payelves.sdk.bean.QueryOrderModel;
import com.payelves.sdk.enums.EPayResult;
import com.payelves.sdk.listener.ConfigResultListener;
import com.payelves.sdk.listener.PayResultListener;
import com.payelves.sdk.listener.QueryOrderListener;

import java.util.UUID;

public class Deposit extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private TextView moneyLeft;
    private EditText amountInput;
    private EditText accountInfo;
    private Button withdrawbtn;
    private Button thirdparty;

    private String userID;
    private static final String USERS = "users";
    private static final String MONEY = "money";

    String openId = "tZmNIobZL";
    String token = "77cd7a5ef528400aac865e2a001a6432";
    String appId = "6623341290717185";
    String channel = "xiaomi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit);
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
        thirdparty = (Button) findViewById(R.id.thirdpartypay_btn);

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
                depositSuccess(amount);
//                startActivity(new Intent(Withdraw.this, UserProfileActivity.class));
            }
        });

        //init Epay
        EPay.getInstance(Deposit.this).init(openId, token, appId, channel);

        //Config key
        EPay.getInstance(getApplicationContext()).loadConfig("KEY1", new ConfigResultListener() {
            @Override
            public void onSuccess(String value) {
                Log.e("e", value);
            }
        });

        thirdparty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subject = "Airmules";
                String body = "In APP Payment";
                String orderId = UUID.randomUUID().toString().replace("-", "");
                String payUserId = orderId;
                String backPara = "";

                EPay.getInstance(Deposit.this).pay(subject, body, 1, orderId, payUserId, backPara, new PayResultListener() {
                    @Override
                    public void onFinish(Context context, Long payId, String orderId, String payUserId, EPayResult payResult, int payType, Integer amount) {
                        EPay.getInstance(context).closePayView();
                        if (payResult.getCode() == EPayResult.SUCCESS_CODE.getCode()) {
                            Toast.makeText(Deposit.this, "Payment Success", Toast.LENGTH_SHORT).show();
                            //Check the payment result
                            EPay.getInstance(context).queryOrder(payId, new QueryOrderListener() {
                                @Override
                                public void onFinish(boolean isSuccess, String msg, QueryOrderModel model) {
                                    if (isSuccess) {
                                        Toast.makeText(Deposit.this, "Payment Success", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(Deposit.this, "Payment Failed", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                            topupSuccess();
                            Toast.makeText(Deposit.this, "Payment Success", Toast.LENGTH_SHORT).show();
                        } else if (payResult.getCode() == EPayResult.FAIL_CODE.getCode()) {

                            Toast.makeText(Deposit.this, "Payment Failed", Toast.LENGTH_SHORT).show(); //payResult.getMsg()
//                            topupSuccess();

                        }
                    }

                });
            }
        });
    }

    private void topupSuccess() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = mDatabase.child(USERS).child(mFirebaseAuth.getCurrentUser().getUid()).getRef();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserClass user = dataSnapshot.getValue(UserClass.class);
                float inAppMoney = user.getMoney();
                inAppMoney = inAppMoney + 1;
                mDatabase.child("users").child(mFirebaseAuth.getCurrentUser().getUid()).child("money").setValue(inAppMoney);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Error", databaseError.toString());
            }
        });
    }

    private void depositSuccess(float num) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = mDatabase.child(USERS).child(mFirebaseAuth.getCurrentUser().getUid()).getRef();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserClass user = dataSnapshot.getValue(UserClass.class);
                float inAppMoney = user.getMoney();
                inAppMoney = inAppMoney + num;
                mDatabase.child("users").child(mFirebaseAuth.getCurrentUser().getUid()).child("money").setValue(inAppMoney);

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
