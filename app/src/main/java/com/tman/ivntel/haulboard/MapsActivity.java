package com.tman.ivntel.haulboard;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;

import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
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
import com.tman.ivntel.haulboard.objects.HaulData;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private CoordinatorLayout coordinatorLayout;
    private static final int REQUEST_CODE = 0x11;
    String[] permissions = {"android.permission.WRITE_EXTERNAL_STORAGE"};
    Bitmap screenShot;
    public String item;
    public String time;
    public String date;
    public String contact;
    public String lat;
    public String lng;
    public String deviceID;
    public static ArrayList<HaulData> myData = new ArrayList<HaulData>();
    public static String firebaseID = null;
    public static Firebase firebaseRef;
    private boolean deviceMatchBool;
    private ProgressDialog dialog;
    MarshMallowPermission marshMallowPermission;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Firebase.setAndroidContext(this);
        firebaseRef = new Firebase(Constants.FIREBASE_URL);

        marshMallowPermission = new MarshMallowPermission(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SharedPreferences sp1 = getSharedPreferences("fbID", Activity.MODE_PRIVATE);
        firebaseID = sp1.getString("FireBaseID", firebaseID);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.bottom_buttons_activity);

        BottomBar bottomBar = BottomBar.attach(this, savedInstanceState);
        bottomBar.useFixedMode();
        bottomBar.setItems(R.menu.bottom_buttons_menu);
        bottomBar.setOnMenuTabClickListener(new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                switch (menuItemId) {
                    case R.id.how_to_item:
                        Snackbar.make(coordinatorLayout, "How To", Snackbar.LENGTH_LONG).show();
                        new AlertDialog.Builder(MapsActivity.this)
                                .setTitle("How To Use Haul Postings")
                                .setMessage("Blue marker indicates the location or area of a haul job or a hauler" + "\n\nTap on the blue marker to get the information of that haul job or hauler")
                                .setNegativeButton("Done", null)
                                .create().show();
                        break;
                    case R.id.share_item:
                        Snackbar.make(coordinatorLayout, "Share", Snackbar.LENGTH_LONG).show();

                        if (!marshMallowPermission.checkPermissionForExternalStorage())
                        {
                            marshMallowPermission.requestPermissionForExternalStorage();
                        }
                        else
                        {
                            GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback()
                            {
                                @Override
                                public void onSnapshotReady(Bitmap snapshot)
                                {
                                    try
                                    {
                                        View mView = findViewById(android.R.id.content).getRootView();
                                        mView.setDrawingCacheEnabled(true);
                                        Bitmap backBitmap = mView.getDrawingCache();
                                        Bitmap bmOverlay = Bitmap.createBitmap(
                                                backBitmap.getWidth(), backBitmap.getHeight(),
                                                Bitmap.Config.ARGB_8888);
                                        Canvas canvas = new Canvas(bmOverlay);
                                        canvas.drawBitmap(backBitmap, 0, 0, null);
                                        canvas.drawBitmap(snapshot, 0, 160, null);
                                        /*FileOutputStream out = new FileOutputStream(
                                                Environment.getExternalStorageDirectory()
                                                        + "/MapScreenShot"
                                                        + System.currentTimeMillis() + ".png");*/
                                        File sampleDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/MapScreenShots");

                                        Log.i("MyFile",""+sampleDir);

                                        // Created directory if not exist
                                        if(!sampleDir.exists())
                                        {
                                            sampleDir.mkdirs();
                                        }
                                        Date d = new Date();
                                        File fn = new File(sampleDir+"/"+"Map"+d.getTime()+".png");
                                        FileOutputStream out = new FileOutputStream(fn);
                                        bmOverlay.compress(Bitmap.CompressFormat.PNG, 100, out);

                                        Intent share = new Intent(Intent.ACTION_SEND);
                                        share.setType("image/*");

                                        File imageFileToShare = new File(fn.getAbsolutePath());

                                        Uri uri = Uri.fromFile(imageFileToShare);
                                        share.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
                                        share.putExtra(android.content.Intent.EXTRA_TEXT, "These are people around the area looking to get things hauled or looking to haul!");
                                        share.putExtra(Intent.EXTRA_STREAM, uri);
                                        startActivity(Intent.createChooser(share, "Share Screenshot Using:"));
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            };

                            mMap.snapshot(callback);
                        }
                        break;
                }
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {
                switch (menuItemId) {
                    case R.id.post_haul:
                        Snackbar.make(coordinatorLayout, "Post A Haul", Snackbar.LENGTH_LONG).show();
                        final String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

                        firebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                MapsActivity.myData.clear();

                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                    //Getting the data from snapshot & where error is happening
                                    HaulData hData = postSnapshot.getValue(HaulData.class);
                                    Log.d("Haul Date: ", hData.getContact() + hData.getLat() + hData.getLng());
                                    if (deviceID.equals(hData.getDeviceID())) {
                                        deviceMatchBool = true;
                                    }
                                }
                                if (deviceMatchBool){
                                    Toast.makeText(MapsActivity.this, "Already have a post", Toast.LENGTH_SHORT).show();
                                }else{
                                    Intent i = new Intent(MapsActivity.this, PostActivity.class);
                                    startActivity(i);
                                }
                            }
                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                                System.out.println("The read failed: " + firebaseError.getMessage());
                            }
                        });
                        break;
                }
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        switch(id){
            case R.id.about:
                new AlertDialog.Builder(this)
                        .setTitle("About/How To Use HaulBoard")
                        .setMessage("About:\n\nDon't have a vehicle or one large enough to haul certain items, post on this app and find someone to help you haul your item quick and easy. If you have a vehicle and are willing to help others with a haul job post your availability on here as well.\n\n"+ "How To Use:\n\nSee Haul Postings: See what haul jobs or who is available for a haul job in your area" + "\n\nPost A Haul: Post a haul job that you need done or post your availbility as a hauler, you are only able to have one active posting at a time" + "\n\nDelete Posting: Deletes your current posting when your haul job has been completed or is no longer available")
                        .setNegativeButton("Done", null)
                        .create().show();
                break;
            case R.id.delete_post:
                if(firebaseID == null){
                    Toast.makeText(this, "No Post To Delete", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "Post Deleted", Toast.LENGTH_SHORT).show();
                    String tempID = firebaseID;
                    firebaseID = null;
                    firebaseRef.child(tempID).removeValue();
                    getApplicationContext().getSharedPreferences("fbID", 0).edit().clear().commit();
                    deviceMatchBool = false;
                    Intent i = new Intent(MapsActivity.this, MapsActivity.class);
                    startActivity(i);
                }
                break;
            case R.id.refresh:
                Toast.makeText(this, "Postings Refreshed", Toast.LENGTH_SHORT).show();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mMap!=null){
            reloadMap(mMap);
        }
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        firebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() == true) {
                    reloadMap(mMap);

                    mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                        @Override
                        public View getInfoWindow(Marker arg0) {
                            return null;
                        }

                        @Override
                        public View getInfoContents(Marker marker) {

                            Context mContext = getApplicationContext();

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
                } else {
                    LatLng northAmerica = new LatLng(48.960585, -97.246712);
                    mMap.addMarker(new MarkerOptions().position(northAmerica).draggable(true).title("No Active Posts"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(northAmerica, 3));//zoom level = 16 goes up to 21
                }
            }
            @Override
            public void onCancelled(FirebaseError arg0) {
            }
        });
    }
    public void reloadMap(GoogleMap googleMap) {
        mMap = googleMap;
        showProgress(true);
        firebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                MapsActivity.myData.clear();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    //Getting the data from snapshot & where error is happening
                    HaulData hData = postSnapshot.getValue(HaulData.class);
                    Log.d("Haul Date: ", hData.getContact() + hData.getLat() + hData.getLng());
                    MapsActivity.myData.add(hData);

                    showProgress(false);

                }

                for (HaulData data : myData) {
                    item = data.getItem();
                    date = data.getDate();
                    time = data.getTime();
                    contact = data.getContact();
                    lat = data.getLat();
                    lng = data.getLng();
                    deviceID = data.getDeviceID();
                    Log.d("Haul Date Receive: ", "Values" + item + " " + date + " " + time + " " + contact + " " + lat + " " + lng);

                    LatLng job = new LatLng(Double.valueOf(lat), Double.valueOf(lng));

                    mMap.addMarker(new MarkerOptions().position(job).title("JOB").snippet("Item: " + item + "\n" + "Date: " + date + "\n" + "Time: " + time + "\n" + "Contact: " + contact).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(job, 2));//zoom level = 16 goes up to 21
                    }
                }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

    public void showProgress(boolean showProgress) {
        if (showProgress) {
            dialog = new ProgressDialog(MapsActivity.this);
            dialog.setMessage("Please wait...");
            dialog.setCancelable(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            if(dialog!=null)
                dialog.show();
        } else {
            if(dialog!=null)
                dialog.hide();
        }
    }
}