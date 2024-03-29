// Based on https://github.com/firebase/friendlychat-android and Stack Overflow.
package com.example.asthana.airmuleschat;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import com.annimon.stream.Optional;
import com.example.asthana.airmuleschat.bean.WeChatInfo;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9999;
    private FirebaseAuth mFirebaseAuth;
    private SignInButton mSignInButton;
    private DatabaseReference mDatabase;
    private CircleImageView welcomeImage;


    private GoogleApiClient mGoogleApiClient;

    //for WeChat
    private Unbinder unbinder;
    private IWXAPI api;
    private Button launchBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        unbinder = ButterKnife.bind(this);
        api = WXAPIFactory.createWXAPI(this, WeChat.APP_ID, false);
        launchBtn = (Button) findViewById(R.id.wechat_login_btn);
        welcomeImage = (CircleImageView) findViewById(R.id.welcomeImage);
        Animation animation1 =
                AnimationUtils.loadAnimation(getApplicationContext(),
                        R.anim.rotate);
        welcomeImage.startAnimation(animation1);

        //Log in view WeChat
        launchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (api.openWXApp() == false) {
                    Toast.makeText(SignInActivity.this, "Please install WeChat APP first", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(SignInActivity.this, WeChatLoginActivity.class);
                startActivityForResult(intent, WeChat.WE_CHAT_LOGIN);
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

    /**
     * Log in via Google
     * @param v
     */
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

        if (requestCode == WeChat.WE_CHAT_LOGIN) {
            startActivity(new Intent(SignInActivity.this, LauncherActivity.class));
            Optional.ofNullable(data).ifPresent(intent -> {
                final String code = intent.getStringExtra(WeChat.WE_CHAT_AUTH_CODE);
                getAccessToken(code);
            });
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
                            completeSignInProcedureForUser();
                        }
                    }
                });
    }

    /**
     * Add the user to the user table if we don't have him or her and then start the main app
     */
    private void completeSignInProcedureForUser() {
        DatabaseReference ref = mDatabase.child("users").child(mFirebaseAuth.getCurrentUser().getUid()).getRef();
        // Attach a listener to read the data at our posts reference
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserClass user = dataSnapshot.getValue(UserClass.class);
                if (user == null || user.getName() == null) {
                    //We don't have this user yet, so add him/her
                    mDatabase.child("users").child(mFirebaseAuth.getCurrentUser().getUid())
                            .setValue(new UserClass(mFirebaseAuth.getCurrentUser().getDisplayName(), 0, 0, 0));
                }

                //start the app!
                startActivity(new Intent(SignInActivity.this, LauncherActivity.class));
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Sign In", "Cannot connect to Firebase");
            }
        });
    }

    private void createUserWithWeChat(String openid, final String name) {
        String token = openid + "@gmail.com";
        mFirebaseAuth.createUserWithEmailAndPassword(token, openid)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Successfully authenticated the new user
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            if (user != null) {
                                //Set the actual name for the user
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name).build();
                                user.updateProfile(profileUpdates);
                            }
                            //Add the user to the users table and start the app
                            completeSignInProcedureForUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void firebaseAuthWithWeChat(final String openid, final String name) {
        String token = openid + "@gmail.com";
        //try to first sign in with the email and password, which are both based on the openid
        mFirebaseAuth.signInWithEmailAndPassword(token, openid)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Have already authenticated this user?
                        if (task.isSuccessful()) {
                            // Sign in success, set the user and start the application
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            mDatabase.child("users").child(mFirebaseAuth.getCurrentUser().getUid()).child("name").setValue(name);
                            startActivity(new Intent(SignInActivity.this, LauncherActivity.class));
                            finish();
                        } else {
                            //We need to add the user as a new user
                            createUserWithWeChat(openid, name);
                        }
                    }
                });
    }

    private void getAccessToken(String code) {
        String http = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + WeChat.APP_ID + "&secret="
                        + WeChat.APP_SECRET + "&code=" + code + "&grant_type=authorization_code";
        OkHttpUtils.ResultCallback<String> resultCallback = new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                String access = null;
                String openId = null;
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    access = jsonObject.getString("access_token");
                    openId = jsonObject.getString("openid");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //We got the access token and the openid, so now we can get the user's information from the API
                String getUserInfo = "https://api.weixin.qq.com/sns/userinfo?access_token=" + access + "&openid=" + openId + "";
                final String opid = openId;     //unique ID for user to use in Firebase

                OkHttpUtils.ResultCallback<WeChatInfo> resultCallback = new OkHttpUtils.ResultCallback<WeChatInfo>() {
                    @Override
                    public void onSuccess(WeChatInfo response) {
                        //The WeChat user name
                        final String wxusername = response.toString();
                        Log.i("TAG", wxusername);
                        firebaseAuthWithWeChat(opid, wxusername);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(SignInActivity.this, "Auth Failed", Toast.LENGTH_SHORT).show();
                    }
                };

                OkHttpUtils.get(getUserInfo, resultCallback);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SignInActivity.this, "Auth Failed", Toast.LENGTH_SHORT).show();
            }
        };
        OkHttpUtils.get(http, resultCallback);
    }

    @Override
    protected void onDestroy() {
        Optional.ofNullable(unbinder).ifPresent(Unbinder::unbind);
        super.onDestroy();
    }
}
