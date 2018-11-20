package com.example.asthana.airmuleschat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// reference: https://firebase.google.com/docs/auth/android/google-signin
public class AuthActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener{
    private SignInButton signInButton;
    private Button signOutButton;
    private TextView statusTextView;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFirebaseAuth;
    private final String TAG = "AuthActivity";
    private static int RC_SIGN_IN = 9001;
    private Button requestButton;
    private Button travelButton;
    private Button startTrackingButton;
    private DataSnapshot mSnapShot;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        requestButton = (Button) findViewById(R.id.requestButton);
        travelButton = (Button) findViewById(R.id.travelButton);
        startTrackingButton = (Button) findViewById(R.id.startTrackingButton);
        startTrackingButton.setVisibility(View.GONE);
        startTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startTracking = new Intent(AuthActivity.this, TrackingActivity.class);
                AuthActivity.this.startActivity(startTracking);
            }
        });

        requestButton.setVisibility(View.GONE);
        travelButton.setVisibility(View.GONE);
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gotoPostRequest  = new Intent(AuthActivity.this, PostRequestActivity.class);
                AuthActivity.this.startActivity(gotoPostRequest);
            }
        });
        travelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gotoPostTravel = new Intent(AuthActivity.this, PostTravelActivity.class);
                AuthActivity.this.startActivity(gotoPostTravel);
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();

        // Read from the database
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                String value = dataSnapshot.getValue(String.class);
                mSnapShot = dataSnapshot;
                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        statusTextView = (TextView) findViewById(R.id.statusTextView);
        signInButton = (SignInButton) findViewById(R.id.googleSignInButton);
        signOutButton = (Button) findViewById(R.id.signOutButton);

        signInButton.setOnClickListener(this);
        signOutButton.setOnClickListener(this);


    }
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign-In was successful, authenticate with Firebase
                GoogleSignInAccount acct = result.getSignInAccount();
                statusTextView.setText("Hello, " + acct.getDisplayName());
                requestButton.setVisibility(View.VISIBLE);
                travelButton.setVisibility(View.VISIBLE);
                startTrackingButton.setVisibility(View.VISIBLE);
                firebaseAuthWithGoogle(acct);

            } else {
                // Google Sign-In failed
                Log.e(TAG, "Google Sign-In failed.");
            }
        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                        } else {
                            mDatabase.child("users").child(mFirebaseAuth.getCurrentUser().getUid()).child("name")
                                    .setValue(mFirebaseAuth.getCurrentUser().getDisplayName());
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.googleSignInButton) {
            signIn();
        } else if (view.getId() == R.id.signOutButton) {
            signOut();
        }

    }
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                statusTextView.setText("Signed Out");
            }
        });
        requestButton.setVisibility(View.GONE);
        travelButton.setVisibility(View.GONE);
        startTrackingButton.setVisibility(View.GONE);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "connection failed!!!!!!!!!!!!!" + connectionResult);

    }
}
