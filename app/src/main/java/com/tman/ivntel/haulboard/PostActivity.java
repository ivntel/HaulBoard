package com.tman.ivntel.haulboard;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ivntel on 2016-06-03.
 */
public class PostActivity extends ActionBarActivity {

    public static final double REQUEST_CODE = 123;
    public static final String LOCATION = "location";
    public static double location = 0;
    private EditText item;
    private EditText time;
    private EditText date;
    private EditText contact;
    private TextView itemText;
    private TextView dateText;
    private TextView timeText;
    private TextView contactText;
    private String mItem;
    private String mDate;
    private String mTime;
    private String mContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        item = (EditText) findViewById(R.id.item);
        date = (EditText) findViewById(R.id.date);
        time = (EditText) findViewById(R.id.time);
        contact = (EditText) findViewById(R.id.contact);
        itemText = (TextView) findViewById(R.id.itemText);
        dateText = (TextView) findViewById(R.id.dateText);
        timeText = (TextView) findViewById(R.id.timeText);
        contactText = (TextView) findViewById(R.id.contactText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_post_maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        switch(id){

            case R.id.how_to_post_maps:
                new AlertDialog.Builder(this)
                        .setTitle("How To Use Post A Haul")
                        .setMessage("Item: Enter a description of the item/s that you need hauled or are able to haul, and then click submit. \nExample: King size mattress" + "\n\nDate: Enter the date or date range that you need your item hauled or are available to haul, and then click submit. Example: May 21st 2016"+ "\n\nTime: Enter the time or time range that you need your item hauled or are available to haul, and then click submit. Example: 1-5pm"+ "\n\nContact: Enter your contact information, and then click submit. \nExample: John 604******* John@email.com" + "\n\nLast: Click 'Choose Pick Up Location', you will not be able to click this button until all of the haul information has been submitted." )
                        .setNegativeButton("Done", null)
                        .create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void buttonOnClickEnterItem(View v) {
        item = (EditText) findViewById(R.id.item);
        itemText = (TextView) findViewById(R.id.itemText);
        itemText.setText(item.getText());
        mItem = itemText.getText().toString();
    }

    public void buttonOnClickEnterDate(View v) {
        date = (EditText) findViewById(R.id.date);
        dateText = (TextView) findViewById(R.id.dateText);
        dateText.setText(date.getText());
        mDate = dateText.getText().toString();
    }

    public void buttonOnClickEnterTime(View v) {
        time = (EditText) findViewById(R.id.time);
        timeText = (TextView) findViewById(R.id.timeText);
        timeText.setText(time.getText());
        mTime = timeText.getText().toString();
    }

    public void buttonOnClickEnterContact(View v) {
        contact = (EditText) findViewById(R.id.contact);
        contactText = (TextView) findViewById(R.id.contactText);
        contactText.setText(contact.getText());
        mContact = contactText.getText().toString();
    }

    public void buttonOnClickLocation(View v) {
        if(mItem != null && mDate != null && mTime != null && mContact != null) {
            Intent intent = new Intent(this, PostMapsActivity.class);
            intent.putExtra("item",mItem);
            intent.putExtra("date",mDate);
            intent.putExtra("time",mTime);
            intent.putExtra("contact",mContact);
            startActivity(intent);
        }
        else{
            Toast.makeText(this, "Missing Information!", Toast.LENGTH_LONG).show();
        }
    }
}
