package com.example.asthana.airmuleschat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Payu.Payu;
import com.payu.india.Payu.PayuConstants;
import com.payu.payuui.Activity.PayUBaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Iterator;

public class PaymentActivity extends BaseMenuActivity {
    private static final float SERVICE_FEE = .05f;

    private TextView txtMuleName;

    //Original price of transaction
    private TextView txtPrice;

    //Our service fee amount
    private TextView txtFee;

    //The total amount to pay
    private TextView txtTotalAmount;

    //Key for our app
    private String merchantKey;
    //Key for user using our app
    private String userCredentials;

    // These will hold all the payment parameters
    private PaymentParams mPaymentParams;

    // This sets the configuration
    private PayuConfig payuConfig;

    //Database stuff
    private String transactionID;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabase;

    public static String convertToMoneyFormatString(float money) {
        return convertToMoneyFormatString(money, true);
    }

    public static String convertToMoneyFormatString(float money, boolean addCommas) {
        String format = "%,.2f";
        if (!addCommas) {
            format.replace(",", "");
        }
        String moneyString = String.format(format, money);
        return moneyString;
    }

    public static float convertMoneyStringToFloat(String moneyString) {
        moneyString = moneyString.replace(",", "");
        return Float.parseFloat(moneyString);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Payu.setInstance(this);

        transactionID = getIntent().getStringExtra("transactionID").toString();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        txtMuleName = (TextView) findViewById(R.id.txtMuleName);
        txtMuleName.setText("");
        txtPrice = (TextView) findViewById(R.id.txtPrice);
        txtFee = (TextView) findViewById(R.id.txtFeeAmount);
        txtTotalAmount = (TextView) findViewById(R.id.txtTotalAmount);
        setUpPriceListeners();

        //Get the price of the transaction and the name of the mule
        DatabaseReference reqRef = mDatabase.child("requests").child(transactionID).getRef();
        reqRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Request req = dataSnapshot.getValue(Request.class);
                if (req == null || req.getTransactionID() == null) {
                    PaymentActivity.this.finish();
                    return;
                }
                if (req.getMule() == null) {
                    Toast.makeText(PaymentActivity.this, "The mule has unregistered.", Toast.LENGTH_LONG).show();
                    PaymentActivity.this.finish();
                    return;
                }

                //Get the name of the mule
                DatabaseReference userRef = mDatabase.child("users").child(req.getMule()).getRef();
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserClass mule = dataSnapshot.getValue(UserClass.class);
                        if (mule != null) {
                            txtMuleName.setText(mule.getName());
                        } else {
                            Log.e("Error", "Could not find mule.");
                            PaymentActivity.this.finish();
                            return;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("Error", databaseError.toString());
                    }
                });

                //Set the price
                txtPrice.setText(convertToMoneyFormatString(req.getReward()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Error", databaseError.toString());
            }
        });

        Button btnPayCredit = (Button) findViewById(R.id.btnPayCredit);
        btnPayCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToBaseActivity();
            }
        });

        Button btnPayInApp = (Button) findViewById(R.id.btnPayInApp);
        btnPayInApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String totalAmountString = txtTotalAmount.getText().toString();
                final float totalAmount = convertMoneyStringToFloat(totalAmountString);

                //Get the amount of money the user has in account and compare to total
                DatabaseReference ref = mDatabase.child("users").child(mFirebaseAuth.getCurrentUser().getUid()).getRef();
                // Attach a listener to read the data at our posts reference
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserClass user = dataSnapshot.getValue(UserClass.class);
                        float inAppMoney = user.getMoney();
                        String inAppMoneyString = convertToMoneyFormatString(inAppMoney);

                        if (inAppMoney < totalAmount) {
                            new AlertDialog.Builder(PaymentActivity.this)
                                    .setCancelable(false)
                                    .setMessage("Not enough funds, have only $" + inAppMoneyString)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                        } else {
                            inAppMoney = inAppMoney - totalAmount;
                            inAppMoneyString = convertToMoneyFormatString(inAppMoney);
                            //subtract money from user's account
                            mDatabase.child("users").child(mFirebaseAuth.getCurrentUser().getUid()).child("money").setValue(inAppMoney);
                            mDatabase.child("requests").child(transactionID).child("status").setValue(Request.PAID);
                            new AlertDialog.Builder(PaymentActivity.this)
                                    .setCancelable(false)
                                    .setMessage("Payment Confirmed! $" + inAppMoneyString + " remaining.")
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            dialog.dismiss();
                                            PaymentActivity.this.finish();
                                            return;
                                        }
                                    }).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("Payment", "Cannot connect to Firebase");
                        Toast.makeText(PaymentActivity.this, "Cannot connect, please try again later.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void setUpPriceListeners() {
        //We can no longer edit the price, so this is less important to be a listener, but still good to keep
        txtPrice.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                float price = 0;
                try {
                    price = convertMoneyStringToFloat(txtPrice.getText().toString());
                } catch (Exception e) {
                    txtPrice.setText(convertToMoneyFormatString(0));
                    return;
                }

                //Update the dependent text views: service fee and total
                float serviceFee = price * SERVICE_FEE;
                float total = price + serviceFee;
                txtFee.setText(convertToMoneyFormatString(serviceFee));
                txtTotalAmount.setText(convertToMoneyFormatString(total));
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });

    }

    /**
     * This method prepares all the payments params to be sent to PayuBaseActivity.java
     * Based on:
     * https://www.youtube.com/watch?v=gnQ0IF3XniM
     * https://github.com/payu-intrepos/Android-SDK-Sample-App/releases/
     * https://github.com/payu-intrepos/Documentations/wiki/6.-Android-SDK-Integration
     */
    public void navigateToBaseActivity() {
        merchantKey = "gtKFFx";
        String amount = txtTotalAmount.getText().toString().replace(",", "");
        String email = "test@gmail.com";

        //Environment for testing the API with the provided test card
        int environment = PayuConstants.STAGING_ENV;

        //User key includes user identifier
        userCredentials = merchantKey + ":" + email;

        //Collect the parameters for the payment
        //We can pass specific credit card information in the future too
        mPaymentParams = new PaymentParams();
        mPaymentParams.setKey(merchantKey);
        mPaymentParams.setAmount(amount);
        mPaymentParams.setProductInfo("product_info");
        mPaymentParams.setFirstName("firstname");
        mPaymentParams.setEmail(email);
        mPaymentParams.setPhone("");

        //Fill in the info of the test card
        mPaymentParams.setNameOnCard("UserName");
        mPaymentParams.setCardName("UserName");
        mPaymentParams.setCardNumber("512345678901346");
        mPaymentParams.setCvv("123");
        mPaymentParams.setExpiryMonth("5");
        mPaymentParams.setExpiryMonth("2020");


        /*
         * Transaction Id should be kept unique for each transaction.
         * */
        mPaymentParams.setTxnId("" + System.currentTimeMillis());

        /**
         * Surl --> Success url is where the transaction response is posted by PayU on successful transaction
         * Furl --> Failre url is where the transaction response is posted by PayU on failed transaction
         */
        mPaymentParams.setSurl(" https://payuresponse.firebaseapp.com/success");
        mPaymentParams.setFurl("https://payuresponse.firebaseapp.com/failure");
        mPaymentParams.setNotifyURL(mPaymentParams.getSurl());  //for lazy pay

        /*
         * udf1 to udf5 are options params where you can pass additional information related to transaction.
         * If you don't want to use it, then send them as empty string like, udf1=""
         * */
        mPaymentParams.setUdf1("udf1");
        mPaymentParams.setUdf2("udf2");
        mPaymentParams.setUdf3("udf3");
        mPaymentParams.setUdf4("udf4");
        mPaymentParams.setUdf5("udf5");

        mPaymentParams.setUserCredentials(userCredentials);

        payuConfig = new PayuConfig();
        //Set that we are using the TEST environment
        payuConfig.setEnvironment(environment);
        /*
        //Tried to pass along test credit card info but could not
        try {
            PostData postData = new PaymentPostParams(mPaymentParams, PayuConstants.CC).getPaymentPostParams();
            if (postData.getCode() == PayuErrors.NO_ERROR) {
                payuConfig.setData(postData.getResult());
            }
        } catch(Exception e){}
        */

        generateHashFromServer(mPaymentParams);
    }

    /**
     * This method generates hash from server.
     *
     * @param mPaymentParams payments params used for hash generation
     */
    public void generateHashFromServer(PaymentParams mPaymentParams) {
        //Create the post params
        StringBuffer postParamsBuffer = new StringBuffer();
        postParamsBuffer.append(concatParams(PayuConstants.KEY, mPaymentParams.getKey()));
        postParamsBuffer.append(concatParams(PayuConstants.AMOUNT, mPaymentParams.getAmount()));
        postParamsBuffer.append(concatParams(PayuConstants.TXNID, mPaymentParams.getTxnId()));
        postParamsBuffer.append(concatParams(PayuConstants.EMAIL, null == mPaymentParams.getEmail() ? "" : mPaymentParams.getEmail()));
        postParamsBuffer.append(concatParams(PayuConstants.PRODUCT_INFO, mPaymentParams.getProductInfo()));
        postParamsBuffer.append(concatParams(PayuConstants.FIRST_NAME, null == mPaymentParams.getFirstName() ? "" : mPaymentParams.getFirstName()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF1, mPaymentParams.getUdf1() == null ? "" : mPaymentParams.getUdf1()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF2, mPaymentParams.getUdf2() == null ? "" : mPaymentParams.getUdf2()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF3, mPaymentParams.getUdf3() == null ? "" : mPaymentParams.getUdf3()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF4, mPaymentParams.getUdf4() == null ? "" : mPaymentParams.getUdf4()));
        postParamsBuffer.append(concatParams(PayuConstants.UDF5, mPaymentParams.getUdf5() == null ? "" : mPaymentParams.getUdf5()));
        postParamsBuffer.append(concatParams(PayuConstants.USER_CREDENTIALS, mPaymentParams.getUserCredentials() == null ? PayuConstants.DEFAULT : mPaymentParams.getUserCredentials()));

        // for offer_key
        if (null != mPaymentParams.getOfferKey())
            postParamsBuffer.append(concatParams(PayuConstants.OFFER_KEY, mPaymentParams.getOfferKey()));

        String postParams = postParamsBuffer.charAt(postParamsBuffer.length() - 1) == '&' ? postParamsBuffer.substring(0, postParamsBuffer.length() - 1).toString() : postParamsBuffer.toString();

        //Create an API call to generate the Hash for the server
        //After the hash is generated (in onPostExecute) we will make an Intent call
        GetHashesFromServerTask getHashesFromServerTask = new GetHashesFromServerTask();
        getHashesFromServerTask.execute(postParams);
    }

    protected String concatParams(String key, String value) {
        return key + "=" + value + "&";
    }

    /**
     * This method adds the PayU hashes and other required params to intent and launches the PayuBaseActivity.java
     *
     * @param payuHashes it contains all the hashes generated from merchant server
     */
    public void launchSdkUI(PayuHashes payuHashes) {
        //Create the Intent object to start payment procedure
        Intent intent = new Intent(this, PayUBaseActivity.class);
        intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
        intent.putExtra(PayuConstants.PAYMENT_PARAMS, mPaymentParams);
        intent.putExtra(PayuConstants.PAYU_HASHES, payuHashes);

        startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            //We got the payment authentication status

            if (data != null) {
                //Get the response sent by PayU
                String resultJSON = data.getStringExtra("result");
                JSONObject json = null;
                String status = "failure";
                String amount = "0";
                String cardNumber = "???";
                try {
                    //Parse the JSON object to get the actual data
                    json = new JSONObject(resultJSON);
                    status = json.getString("status");
                    amount = json.getString("amount");
                    //Actually just show the total
                    amount = txtTotalAmount.getText().toString();
                    cardNumber = json.getString("cardnum");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String message;
                if (status.equals("success")) {
                    mDatabase.child("requests").child(transactionID).child("status").setValue(Request.PAID);
                    message = "Payment Confirmed! Charged $" + amount + " to card: " + cardNumber;
                } else {
                    message = "Payment Failed...";
                }
                final String statusFinal = status;

                //Display some basic information from the transaction
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                                if (statusFinal.equals("success")) {
                                    PaymentActivity.this.finish();
                                    return;
                                }
                            }
                        }).show();
            } else {
                Toast.makeText(this, getString(R.string.could_not_receive_data), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * This AsyncTask generates hash from server.
     */
    private class GetHashesFromServerTask extends AsyncTask<String, String, PayuHashes> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(PaymentActivity.this);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
        }

        @Override
        protected PayuHashes doInBackground(String... postParams) {
            PayuHashes payuHashes = new PayuHashes();
            try {
                //URL to generate hash for testing
                URL url = new URL("https://payu.herokuapp.com/get_hash");

                // get the payuConfig first
                String postParam = postParams[0];

                byte[] postParamsByte = postParam.getBytes("UTF-8");

                //Set up the connection to the hash generator URL
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(postParamsByte.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(postParamsByte);

                //Receive data from the hash generator URL
                InputStream responseInputStream = conn.getInputStream();

                //Parse the hashes
                StringBuffer responseStringBuffer = new StringBuffer();
                byte[] byteContainer = new byte[1024];
                for (int i; (i = responseInputStream.read(byteContainer)) != -1; ) {
                    responseStringBuffer.append(new String(byteContainer, 0, i));
                }

                JSONObject response = new JSONObject(responseStringBuffer.toString());

                //Collect the hashes to send in the Intent
                Iterator<String> payuHashIterator = response.keys();
                while (payuHashIterator.hasNext()) {
                    String key = payuHashIterator.next();
                    switch (key) {
                        case "payment_hash":
                            //Hash for payment (Mandatory)
                            payuHashes.setPaymentHash(response.getString(key));
                            break;
                        case "vas_for_mobile_sdk_hash":
                            //Other payment hash(Mandatory)
                            payuHashes.setVasForMobileSdkHash(response.getString(key));
                            break;
                        case "payment_related_details_for_mobile_sdk_hash":
                            //Other payment hash (Mandatory)
                            payuHashes.setPaymentRelatedDetailsForMobileSdkHash(response.getString(key));
                            break;
                        default:
                            break;
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return payuHashes;
        }

        @Override
        protected void onPostExecute(PayuHashes payuHashes) {
            super.onPostExecute(payuHashes);

            progressDialog.dismiss();
            //Finally we can launch the Intent!
            launchSdkUI(payuHashes);
        }
    }

}
