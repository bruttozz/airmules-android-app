package com.example.asthana.airmuleschat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.os.Handler;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import org.w3c.dom.Text;


public class UserProfileActivity extends BaseMenuActivity {
    // User Profile main page

    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private ImageView userProfilePicture;
    private TextView userDisplayName;
    private TextView txtViewMoneyLeft;
    private RatingBar ratingAsMule;
    private RatingBar ratingAsCustomer;
    private TextView txtViewRatingAsMule;
    private TextView txtViewRatingAsCustimer;
    private Button btnAddMoney;
    private Button btnWithdrawMoney;

    private String userID;
    private static final String USERS = "users";
    private static final String MONEY = "money";

    String openId = "tZmNIobZL";
    String token = "77cd7a5ef528400aac865e2a001a6432";
    String appId = "6623341290717185";
    String channel = "xiaomi";

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri;
    private String path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        userProfilePicture = (ImageView) findViewById(R.id.imageViewUserProfile);
        path = "userProfile/" + mFirebaseAuth.getCurrentUser().getUid().toString() + ".jpg";

        StorageReference storageReference = storage.getReference().child(path);
        downloadProfileImage(storageReference);

        userProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });
        userDisplayName = (TextView) findViewById(R.id.txtViewUserName);
        txtViewMoneyLeft = (TextView) findViewById(R.id.txtViewMoneyLeft);
        txtViewRatingAsMule = (TextView) findViewById(R.id.txtViewRateAsMule);
        // todo display rating number according to database

        txtViewRatingAsCustimer = (TextView) findViewById(R.id.txtViewRateAsCustomer);
        // todo display rating number according to database


        ratingAsMule = (RatingBar) findViewById(R.id.ratingBarAsMule);
        // todo set up the stars according to rating as mule stored database
        ratingAsCustomer = (RatingBar) findViewById(R.id.ratingBarAsCustomer);
        // todo set up the start according to rating as customer database

        DatabaseReference ref = mDatabase.child(USERS).child(mFirebaseAuth.getCurrentUser().getUid()).getRef();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserClass me = dataSnapshot.getValue(UserClass.class);
                float myFunds = me.getMoney();
                txtViewMoneyLeft.setText(PaymentActivity.convertToMoneyFormatString(myFunds));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Error", databaseError.toString());
            }
        });

        userID = mFirebaseAuth.getCurrentUser().getUid().toString();
        Log.w("USERID", userID);

        userDisplayName.setText(mFirebaseAuth.getCurrentUser().getDisplayName().toString());

        //init Epay
        EPay.getInstance(UserProfileActivity.this).init(openId, token, appId, channel);

        //Config key
        EPay.getInstance(getApplicationContext()).loadConfig("KEY1", new ConfigResultListener() {
            @Override
            public void onSuccess(String value) {
                Log.e("e", value);
            }
        });



        btnAddMoney = (Button) findViewById(R.id.btnAddMoney);
        btnWithdrawMoney = (Button) findViewById(R.id.btnWithdrawMoney);

        btnAddMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String subject = "Airmules";
                String body = "In APP Payment";
                String orderId = UUID.randomUUID().toString().replace("-", "");
                String payUserId = orderId;
                String backPara = "";

                EPay.getInstance(UserProfileActivity.this).pay(subject, body, 1, orderId, payUserId, backPara, new PayResultListener() {
                    @Override
                    public void onFinish(Context context, Long payId, String orderId, String payUserId, EPayResult payResult, int payType, Integer amount) {
                        EPay.getInstance(context).closePayView();
                        if (payResult.getCode() == EPayResult.SUCCESS_CODE.getCode()) {
                            Toast.makeText(UserProfileActivity.this, "Payment Success", Toast.LENGTH_SHORT).show();
                            //Check the payment result
                            EPay.getInstance(context).queryOrder(payId, new QueryOrderListener() {
                                @Override
                                public void onFinish(boolean isSuccess, String msg, QueryOrderModel model) {
                                    if (isSuccess) {
                                        Toast.makeText(UserProfileActivity.this, "Payment Success", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(UserProfileActivity.this, "Payment Failed", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
//                            topupSuccess();
                            Toast.makeText(UserProfileActivity.this, "Payment Success", Toast.LENGTH_SHORT).show();
                        } else if (payResult.getCode() == EPayResult.FAIL_CODE.getCode()) {

                            Toast.makeText(UserProfileActivity.this, "Payment Failed", Toast.LENGTH_SHORT).show(); //payResult.getMsg()
                            topupSuccess();

                        }
                    }

                });

            }
        });

        btnWithdrawMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // todo withdraw money from account
//                Toast.makeText(getBaseContext(), "0.0 has been withdrawn from your account", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(UserProfileActivity.this, Deposit.class));
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
                inAppMoney = inAppMoney + 100;
                mDatabase.child("users").child(mFirebaseAuth.getCurrentUser().getUid()).child("money").setValue(inAppMoney);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Error", databaseError.toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null
                && data.getData() != null) {
            mImageUri = data.getData();
            userProfilePicture.setImageURI(mImageUri);

            StorageReference storageReference = storage.getReference(path);

            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setCustomMetadata("user", mFirebaseAuth.getCurrentUser().getUid().toString()).build();

            UploadTask uploadTask = storageReference.putFile(mImageUri);
            uploadTask.addOnSuccessListener(UserProfileActivity.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(UserProfileActivity.this, "image uploaded", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(UserProfileActivity.this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UserProfileActivity.this, e.toString(), Toast.LENGTH_SHORT).show();

                }
            });

        }


    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);

    }

    private void setImageViewWithByteArray(ImageView view, byte[] data) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        view.setImageBitmap(bitmap);
    }


    private void downloadProfileImage(StorageReference storageRef) {

        // [START download_to_memory]
        StorageReference islandRef = storageRef;

        final long ONE_MEGABYTE = 1024 * 1024 * 10;
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
                try {
                    setImageViewWithByteArray(userProfilePicture, bytes);
                } catch (Exception e) {
                    userProfilePicture.setImageResource(R.drawable.profileicon);
                    Toast.makeText(UserProfileActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }


}
