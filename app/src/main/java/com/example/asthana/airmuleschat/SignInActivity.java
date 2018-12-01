// Based on https://github.com/firebase/friendlychat-android and Stack Overflow.


package com.example.asthana.airmuleschat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.annimon.stream.Optional;
import com.example.asthana.airmuleschat.wxapi.WeChatLoginActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9999;
    private FirebaseAuth mFirebaseAuth;
    private SignInButton mSignInButton;
    private DatabaseReference mDatabase;


    private GoogleApiClient mGoogleApiClient;

    //for WeChat
    private Unbinder unbinder;
    private IWXAPI api;
    private Button launchBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        unbinder = ButterKnife.bind(this);
        api = WXAPIFactory.createWXAPI(this, WeChat.APP_ID, false);
        launchBtn = (Button) findViewById(R.id.wechat_login_btn);
        launchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(SignInActivity.this, LauncherActivity.class));
                if(api.openWXApp() == false){
                    Toast.makeText(SignInActivity.this, "您还未安装微信客户端", Toast.LENGTH_LONG).show();
                }
                Toast.makeText(SignInActivity.this, "success", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(SignInActivity.this, WeChatLoginActivity.class);
                startActivityForResult(intent, ActivityReqCode.WE_CHAT_LOGIN);
                //SignInActivity.this.startActivity(new Intent(WeChatLoginActivity.class, LauncherActivity.class)));
            }
        });


        // Assign fields
        mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);

        // Set click listeners
        mSignInButton.setOnClickListener(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();

        //  Instantiate DB
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the GoogleSignInApi.getSignInIntent(...) Intent;
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign-In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign-In failed
                Log.e(TAG, "Google Sign-In failed.");
            }
        }

        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == ActivityReqCode.WE_CHAT_LOGIN) {
            startActivity(new Intent(SignInActivity.this, LauncherActivity.class));
            Optional.ofNullable(data).ifPresent(intent -> {

                final String code = intent.getStringExtra(IntentKey.WE_CHAT_AUTH_CODE);
                Toast.makeText(this, code, Toast.LENGTH_SHORT).show();

            });
            //startActivity(new Intent(SignInActivity.this, LauncherActivity.class));
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

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            mDatabase.child("users").child(mFirebaseAuth.getCurrentUser().getUid())
                                    .setValue(new UserClass(mFirebaseAuth.getCurrentUser().getDisplayName(), 100,0,0));
                            startActivity(new Intent(SignInActivity.this, LauncherActivity.class));
                            finish();
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        Optional.ofNullable(unbinder).ifPresent(Unbinder::unbind);
        super.onDestroy();
    }
}
