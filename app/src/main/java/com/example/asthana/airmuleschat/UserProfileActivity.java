package com.example.asthana.airmuleschat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class UserProfileActivity extends BaseMenuActivity {
    // User Profile main page

    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;

    private ImageView userProfilePicture;
    private TextView userDisplayName;
    private TextView moneyLeft;
    private Button btnSeeRequest;
    private Button btnSeeTravel;
    private Button btnSeeChat;
    private Button btnSignOut;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        userProfilePicture = (ImageView) findViewById(R.id.imageViewUserProfile);
        userDisplayName = (TextView) findViewById(R.id.txtViewUserName);
        moneyLeft = (TextView) findViewById(R.id.txtViewMoneyLeft);
        btnSeeRequest = (Button) findViewById(R.id.btnSeeRequest);
        btnSeeTravel = (Button) findViewById(R.id.btnSeeTravel);
        btnSeeChat = (Button) findViewById(R.id.btnSeeChat);
        btnSignOut = (Button) findViewById(R.id.btnSignOut);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();

        userDisplayName.setText(mFirebaseAuth.getCurrentUser().getDisplayName().toString());
        // TO_DO: set user profile image in imageView

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Google sign out

                // Wechat sign out

                // update UI after signed out
            }
        });

        btnSeeRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(UserProfileActivity.this, SeeRequestActivity.class);
                UserProfileActivity.this.startActivity(i);
            }
        });

        btnSeeTravel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(UserProfileActivity.this, SeeTravelActivity.class);
                UserProfileActivity.this.startActivity(i);
            }
        });

        btnSeeChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(UserProfileActivity.this, SeeChatActivity.class);
                UserProfileActivity.this.startActivity(i);
            }
        });


    }
}
