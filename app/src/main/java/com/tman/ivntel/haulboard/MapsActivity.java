package com.tman.ivntel.haulboard;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import com.google.android.gms.ads.InterstitialAd;
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
import com.tman.ivntel.haulboard.objects.HaulData;

import java.util.ArrayList;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Firebase.setAndroidContext(this);
        firebaseRef = new Firebase(Constants.FIREBASE_URL);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        SharedPreferences sp1 = getSharedPreferences("fbID", Activity.MODE_PRIVATE);
        firebaseID = sp1.getString("FireBaseID", firebaseID);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.bottom_buttons_activity);

        BottomBar bottomBar = BottomBar.attach(this, savedInstanceState);
        //bottomBar.setItemsFromMenu(R.menu.bottom_buttons_menu, new OnMenuTabSelectedListener() {
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
                        break;
                }
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {
                switch (menuItemId) {
                    case R.id.post_haul:
                        Snackbar.make(coordinatorLayout, "Already Have An Active Post", Snackbar.LENGTH_LONG).show();
                        final String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

                        firebaseRef.addValueEventListener(new ValueEventListener() {
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
        //TRIAL
            firebaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    MapsActivity.myData.clear();

                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        //Getting the data from snapshot & where error is happening
                        HaulData hData = postSnapshot.getValue(HaulData.class);
                        Log.d("Haul Date: ", hData.getContact() + hData.getLat() + hData.getLng());
                        MapsActivity.myData.add(hData);
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    System.out.println("The read failed: " + firebaseError.getMessage());
                }
            });
        }
        //TRIAL
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
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (myData.size() == 0) {
        //if (MainActivity.firebaseID == null) {
            LatLng job = new LatLng(48.960585, -97.246712);
            mMap.addMarker(new MarkerOptions().position(job).title("JOB").snippet("Item: " + "No data" + "\n" + "Date: " + "No data" + "\n" + "Time: " + "No data" + "\n" + "Contact: " + "No data").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(job, 3));//zoom level = 16 goes up to 21
        } else {
            for(HaulData data: myData) {
                item = data.getItem();
                date = data.getDate();
                time = data.getTime();
                contact = data.getContact();
                lat = data.getLat();
                lng = data.getLng();
                deviceID = data.getDeviceID();
                Log.d("Haul Date Receive: ", "Values" + item + " " + date  + " " + time + " "+ contact + " " + lat + " " + lng);

                LatLng job = new LatLng(Double.valueOf(lat), Double.valueOf(lng));

                mMap.addMarker(new MarkerOptions().position(job).title("JOB").snippet("Item: " + item + "\n" + "Date: " + date + "\n" + "Time: " + time + "\n" + "Contact: " + contact).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(job, 7));//zoom level = 16 goes up to 21
            }
        }

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
        }

    /*public void buttonOnClickShare(View v) {
        //screenShot = getScreenShot(v);
        if (shouldAskPermission()){
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
        }
        else{
            captureScreen();
            //File file = saveBitmap(screenShot);
            //shareImage(file);
        }
    }*/

    /*public static Bitmap getScreenShot(View view) {
        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private void shareImage(File file){
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, "These are people around the area looking to get things hauled!");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Share Screenshot Using:"));
    }
    public File saveBitmap(Bitmap bitmap) {
        File imagePath = new File(Environment.getExternalStorageDirectory() + "/screenshot.png");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            Log.e("GREC", e.getMessage(), e);
        } catch (IOException e) {
            Log.e("GREC", e.getMessage(), e);
        }return imagePath;
    }
    private boolean shouldAskPermission(){

        return(Build.VERSION.SDK_INT> Build.VERSION_CODES.LOLLIPOP_MR1);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // save file
                captureScreen();
            } else {
                Toast.makeText(getApplicationContext(), "PERMISSION_DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void captureScreen() {
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                // TODO Auto-generated method stub
                Bitmap bitmap = snapshot;

                OutputStream fout = null;

                String filePath = System.currentTimeMillis() + ".jpeg";
                Log.d("captureDebug", "filePath is " + filePath);
                try {
                    fout = openFileOutput(filePath,
                            MODE_WORLD_READABLE);

                    // Write the string to the file
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
                    fout.flush();
                    fout.close();
                }
                catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    Log.d("ImageCapture", "FileNotFoundException");
                    Log.d("ImageCapture", e.getMessage());
                    filePath = "";
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.d("ImageCapture", "IOException");
                    Log.d("ImageCapture", e.getMessage());
                    filePath = "";
                }

                openShareImageDialog(filePath);
            }
        };

        mMap.snapshot(callback);
    }
    public void openShareImageDialog(String filePath) {
        File file = this.getFileStreamPath(filePath);

        if(!filePath.equals("")) {
            Log.d("shareDebug", "filePath is " + filePath);
            final ContentValues values = new ContentValues(2);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            final Uri contentUriFile = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("image/jpeg");
            intent.putExtra(android.content.Intent.EXTRA_STREAM, contentUriFile);
            startActivity(Intent.createChooser(intent, "Share Image"));
        } else {
            //This is a custom class I use to show dialogs...simply replace this with whatever you want to show an error message, Toast, etc.
            Toast.makeText(this, "Sharing Fail", Toast.LENGTH_LONG).show();
        }
    }*/
}