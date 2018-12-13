package com.example.asthana.airmuleschat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    public static final String USER_LOCATION = "USER_LOCATION";

    double latitude;
    double longitude;
    double planedir;
    String depart;
    String arrive;
    String flightnum;
    private GoogleMap mMap;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mContext = getBaseContext();
        flightnum = this.getIntent().getStringExtra("Flightnum");
        latitude = this.getIntent().getDoubleExtra("Latitude", 0.0);
        longitude = this.getIntent().getDoubleExtra("Longitude", 0.0);
        if(!flightnum.equals(USER_LOCATION)) {
            //We are displaying flight information, instead of just the user's location
            planedir = this.getIntent().getDoubleExtra("Direction", 0.0);
            depart = this.getIntent().getStringExtra("depTime");
            arrive = this.getIntent().getStringExtra("arrTime");
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng mulepos = new LatLng(latitude, longitude);
        String snippet;
        if(!flightnum.equals(USER_LOCATION)){
            snippet = flightnum + "\n" + "Latitude: " + latitude + "\n" + "Longitude: " + longitude + "\n" + "Direction: " + (planedir);
        } else{
            snippet = "Last tracked location:\n" + "Latitude: " + latitude + "\n" + "Longitude: " + longitude;
        }
        mMap.addMarker(new MarkerOptions()
                .position(mulepos)
                .title("Mule Position")
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mule_image)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mulepos));
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(mContext);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(mContext);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(mContext);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });
    }
}
