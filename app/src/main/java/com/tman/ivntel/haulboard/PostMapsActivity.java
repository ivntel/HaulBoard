package com.tman.ivntel.haulboard;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;
import com.roughike.bottombar.OnMenuTabSelectedListener;
import com.tman.ivntel.haulboard.objects.HaulData;

public class PostMapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    public static final String TAG = PostMapsActivity.class.getSimpleName();
    private String item;
    private CoordinatorLayout coordinatorLayout;
    private String date;
    private String time;
    private String contact;
    private double lat;
    private double lng;
    private InterstitialAd interstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_maps);
        Firebase.setAndroidContext(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.postmap);
        mapFragment.getMapAsync(this);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.post_bottom_buttons_activity);

        BottomBar bottomBar = BottomBar.attach(this, savedInstanceState);
        bottomBar.useFixedMode();
        bottomBar.setItems(R.menu.post_bottom_buttons_menu);
        bottomBar.setOnMenuTabClickListener(new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                switch (menuItemId) {
                    case R.id.post_how_to_item:
                        Snackbar.make(coordinatorLayout, "How To", Snackbar.LENGTH_LONG).show();
                        new AlertDialog.Builder(PostMapsActivity.this)
                                .setTitle("How To Use Haul Postings")
                                .setMessage("Tap and hold down on the red marker while you drag it to your desired haul job or haul services location" + "\n\nOnce you have chosen your location hit the 'Submit' button to submit")
                                .setNegativeButton("Done", null)
                                .create().show();
                        break;
                    case R.id.submit_item:
                        Snackbar.make(coordinatorLayout, "Submit", Snackbar.LENGTH_LONG).show();
                        if (MapsActivity.firebaseID == null) {
                            if (interstitial.isLoaded()) {
                                interstitial.show();
                            } else {
                                saveLocationData();
                            }
                            break;
                        } else {
                            Intent i = new Intent(PostMapsActivity.this, MapsActivity.class);
                            startActivity(i);
                        }
                }
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {
                switch (menuItemId) {
                    case R.id.post_home:
                        Snackbar.make(coordinatorLayout, "Home", Snackbar.LENGTH_LONG).show();
                        Intent i = new Intent(PostMapsActivity.this, MapsActivity.class);
                        startActivity(i);
                        break;
                }
            }
        });

        item = getIntent().getStringExtra("item");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");
        contact = getIntent().getStringExtra("contact");

        AdRequest adRequest = new AdRequest.Builder().build();
        // Prepare the Interstitial Ad
        interstitial = new InterstitialAd(PostMapsActivity.this);
        // Insert the Ad Unit ID
        interstitial.setAdUnitId(getString(R.string.admob_interstitial_id));
        interstitial.loadAd(adRequest);

        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                saveLocationData();
            }
        });
        requestNewInterstitial();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final LatLng northAmerica = new LatLng(48.960585, -97.246712);
        //mMap.setMyLocationEnabled(true);
        mMap.addMarker(new MarkerOptions().position(northAmerica).draggable(true).title("Pick Up Location/Area"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(dallas));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(northAmerica, 3));//zoom level = 16 goes up to 21

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart(Marker marker) {
                // TODO Auto-generated method stub
                // Here your code
                Toast.makeText(PostMapsActivity.this, "Drag to haul location", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng position = marker.getPosition();
                Toast.makeText(PostMapsActivity.this, "Haul location selected", Toast.LENGTH_LONG).show();
                lat = position.latitude;
                lng = position.longitude;
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                // TODO Auto-generated method stub
                //Toast.makeText(PostMapsActivity.this, "Dragging", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveLocationData() {
        String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, deviceID);
        HaulData hData = new HaulData(item, date, time, contact, String.valueOf(lat), String.valueOf(lng), deviceID);
        MapsActivity.firebaseRef.push().setValue(hData);

        MapsActivity.firebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                MapsActivity.myData.clear();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    //Getting the data from snapshot
                    HaulData hData = postSnapshot.getValue(HaulData.class);
                    String tempFirebaseID = postSnapshot.getKey();
                    Log.d("Firebase key: ", tempFirebaseID);
                    Log.d("Haul Date: ", hData.getContact() + hData.getLat() + hData.getLng());

                    SharedPreferences sp = getSharedPreferences("fbID", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("FireBaseID", tempFirebaseID);
                    editor.commit();

                    MapsActivity.myData.add(hData);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
        Intent i = new Intent(PostMapsActivity.this, MapsActivity.class);
        startActivity(i);
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitial.loadAd(adRequest);
    }
}