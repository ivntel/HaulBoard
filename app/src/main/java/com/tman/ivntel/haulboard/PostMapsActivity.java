package com.tman.ivntel.haulboard;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;
import com.roughike.bottombar.OnMenuTabSelectedListener;
import com.tman.ivntel.haulboard.MapsActivity;
import com.tman.ivntel.haulboard.objects.HaulData;

public class PostMapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener
{

    public static final String TAG = PostMapsActivity.class.getSimpleName();
    private String item;
    private CoordinatorLayout coordinatorLayout;
    private String date;
    private String time;
    private String contact;
    private double lat;
    private double lng;
    private InterstitialAd interstitial;

    // Variables for current location on google map
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    android.location.Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_maps);
        Firebase.setAndroidContext(this);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.post_bottom_buttons_activity);

        BottomBar bottomBar = BottomBar.attach(this, savedInstanceState);
        bottomBar.useFixedMode();
        bottomBar.setItems(R.menu.post_bottom_buttons_menu);

        bottomBar.setOnMenuTabClickListener(new OnMenuTabClickListener()
        {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId)
            {
                switch (menuItemId)
                {
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

                        if (interstitial.isLoaded())
                        {
                            interstitial.show();
                        }
                        else
                        {
                            saveLocationData();
                        }
                        break;
                }
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId)
            {
                switch (menuItemId)
                {
                    case R.id.post_home:
                        Snackbar.make(coordinatorLayout, "Home", Snackbar.LENGTH_LONG).show();
                        Intent i = new Intent(PostMapsActivity.this, MapsActivity.class);
                        startActivity(i);
                        finish();
                        break;
                }
            }
        });

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.postmap);
        mapFragment.getMapAsync(this);

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

        interstitial.setAdListener(new AdListener()
        {
            @Override
            public void onAdClosed()
            {
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
    public void onPause()
    {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else
        {
            buildGoogleApiClient();

            LatLng northAmerica = new LatLng(48.960585, -97.246712);
            mMap.addMarker(new MarkerOptions().position(northAmerica).draggable(true).title("Pick Up Location/Area"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(northAmerica, 3));//zoom level = 16 goes up to 21

            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener()
            {
                @Override
                public void onMarkerDragStart(Marker marker)
                {
                    // TODO Auto-generated method stub
                    // Here your code
                    Toast.makeText(PostMapsActivity.this, "Drag to haul location", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onMarkerDragEnd(Marker marker)
                {
                    LatLng position = marker.getPosition();
                    Toast.makeText(PostMapsActivity.this, "Haul location selected", Toast.LENGTH_LONG).show();
                    lat = position.latitude;
                    lng = position.longitude;
                }

                @Override
                public void onMarkerDrag(Marker marker)
                {
                    // TODO Auto-generated method stub
                    //Toast.makeText(PostMapsActivity.this, "Dragging", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    protected synchronized void buildGoogleApiClient()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(android.location.Location location)
    {
        mLastLocation = location;

        if (mCurrLocationMarker != null)
        {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.addMarker(new MarkerOptions().position(latLng).draggable(true).title("Pick Up Location/Area").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener()
        {
            @Override
            public void onMarkerDragStart(Marker marker)
            {
                // TODO Auto-generated method stub
                // Here your code
                Toast.makeText(PostMapsActivity.this, "Drag to haul location", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMarkerDragEnd(Marker marker)
            {
                LatLng position = marker.getPosition();
                Toast.makeText(PostMapsActivity.this, "Haul location selected", Toast.LENGTH_LONG).show();
                lat = position.latitude;
                lng = position.longitude;
            }

            @Override
            public void onMarkerDrag(Marker marker)
            {
                // TODO Auto-generated method stub
                //Toast.makeText(PostMapsActivity.this, "Dragging", Toast.LENGTH_SHORT).show();
            }
        });

        //stop location updates
        if (mGoogleApiClient != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION))
            {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            else
            {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case MY_PERMISSIONS_REQUEST_LOCATION:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        if (mGoogleApiClient == null)
                        {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else
                {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void saveLocationData()
    {
        String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, deviceID);
        HaulData hData = new HaulData(item, date, time, contact, String.valueOf(lat), String.valueOf(lng), deviceID);
        MapsActivity.firebaseRef.push().setValue(hData);

        MapsActivity.firebaseRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {
                MapsActivity.myData.clear();

                for (DataSnapshot postSnapshot : snapshot.getChildren())
                {
                    //Getting the data from snapshot
                    HaulData hData = postSnapshot.getValue(HaulData.class);
                    String tempFirebaseID = postSnapshot.getKey();
                    MapsActivity.firebaseID = tempFirebaseID;
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
            public void onCancelled(FirebaseError firebaseError)
            {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
        Intent i = new Intent(PostMapsActivity.this, MapsActivity.class);
        startActivity(i);
    }

    private void requestNewInterstitial()
    {
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitial.loadAd(adRequest);
    }
}