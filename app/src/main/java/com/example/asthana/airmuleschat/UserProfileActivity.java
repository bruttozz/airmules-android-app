package com.example.asthana.airmuleschat;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.payu.india.Model.StoredCard;


public class UserProfileActivity extends BaseMenuActivity {
    // User Profile main page

    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private ImageView userProfilePicture;
    private TextView userDisplayName;
    private TextView txtViewMoneyLeft;
    private Button btnSeeRequest;
    private Button btnSeeChat;

    private String userID;
    private static final String USERS = "users";
    private static final String MONEY = "money";

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
        btnSeeRequest = (Button) findViewById(R.id.btnSeeRequest);

        btnSeeChat = (Button) findViewById(R.id.btnSeeChat);


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String moneyLeft = dataSnapshot.child(USERS).child(mFirebaseAuth.getCurrentUser().getUid().toString())
                        .child(MONEY).getValue().toString();
                txtViewMoneyLeft.setText(moneyLeft);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Error", databaseError.toString());
            }
        });

        userID = mFirebaseAuth.getCurrentUser().getUid().toString();
        Log.w("USERID", userID);

        userDisplayName.setText(mFirebaseAuth.getCurrentUser().getDisplayName().toString());
        // TODO: set user profile image in imageView


        btnSeeRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(UserProfileActivity.this, SeeRequestActivity.class);
                UserProfileActivity.this.startActivity(i);
            }
        });


        btnSeeChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


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
                    ;
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
                    userProfilePicture.setImageResource(R.drawable.image1);
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
