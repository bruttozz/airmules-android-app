package com.example.asthana.airmuleschat;


import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


// see reference: https://www.androidauthority.com/create-a-gps-tracking-application-with-firebase-realtime-databse-844343/
public class TrackingService extends Service {

    private static final String TAG = TrackingService.class.getSimpleName();
    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

//Unregister the BroadcastReceiver when the notification is tapped//

            unregisterReceiver(stopReceiver);

//Stop the Service//

            stopSelf();
            stopForeground(true);
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    };
    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

//Create the persistent notification//

    @Override
    public void onCreate() {
        super.onCreate();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();

        //Don't create a notification for later android versions (was causing issues)
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            buildNotification();
//        loginToFirebase();
        //Start listening to location updates
        requestLocationUpdates();
    }

    private void buildNotification() {

        String stop = "stop";
        registerReceiver(stopReceiver, new IntentFilter(stop));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);
        // Create the persistent notification
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.tracking_enabled_notif))

//Make this notification ongoing so it can’t be dismissed by the user//

                .setOngoing(true)
                .setContentIntent(broadcastIntent)
                .setSmallIcon(R.drawable.tracking_enabled);
        startForeground(1, builder.build());
    }


//Initiate the request to track the device's location//

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();

//Specify how often your app should request the device’s location//

        request.setInterval(10000);

//Get the most accurate location data available//

        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
//        final String path = getString(R.string.firebase_path);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

//If the app currently has access to the location permission...//

        if (permission == PackageManager.PERMISSION_GRANTED) {

//...then request location updates//

            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

//Get a reference to the database, so your app can perform read and write operations//

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                    Location location = locationResult.getLastLocation();
                    if (location != null) {

//Save the location data to the database//

//                        mDatabase.child("users")
//                                .child(mFirebaseAuth.getCurrentUser().getUid())
//                                .child("location")
//                                .child("latitude").setValue(location.getLatitude());
//                        mDatabase.child("users")
//                                .child(mFirebaseAuth.getCurrentUser().getUid())
//                                .child("location")
//                                .child("longitude").setValue(location.getLongitude());

                        if (mFirebaseAuth.getCurrentUser() == null) {
                            return;
                        }
                        mDatabase.child("users").child(mFirebaseAuth.getCurrentUser().getUid())
                                .child("latitude").setValue(location.getLatitude());
                        mDatabase.child("users").child(mFirebaseAuth.getCurrentUser().getUid())
                                .child("longitude").setValue(location.getLongitude());

                    }
                }
            }, null);
        }
    }
}
