package com.example.asthana.airmuleschat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
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

    private GoogleMap mMap;
    private Context mContext;

    double latitude;
    double longitude;
    double planedir;
    String depart;
    String arrive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mContext = getBaseContext();
        latitude = this.getIntent().getDoubleExtra("Latitude",latitude);
        longitude = this.getIntent().getDoubleExtra("Latitude",longitude);
        planedir = this.getIntent().getDoubleExtra("Direction",planedir);
        depart = this.getIntent().getStringExtra("depTime");
        arrive = this.getIntent().getStringExtra("arrTime");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        planedir = planedir -45;
        // Add a marker in Sydney and move the camera
        LatLng mulepos = new LatLng(latitude,longitude);
        mMap.addMarker(new MarkerOptions()
                .position(mulepos)
                .title("Mule Position")
                .snippet("Departed at: " + depart + "\n"+ "Arrives at: " + arrive)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_plane))
                .rotation((float)(planedir)));
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
