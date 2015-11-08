package com.nehaabrol.parkingpursuit;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.ConnectionResult;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import android.app.ProgressDialog;

public class MainActivity extends Activity  {

    private AutoCompleteTextView destination;
    private GooglePlacesAutoComplete googlePlacesAutoComplete;
    private  String updatedValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        setContentView(R.layout.activity_home_page);

        //Call google Maps API
        MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        new MapsActivity(getBaseContext(),mapFragment);

        //Call google places autocomplete API
        destination = (AutoCompleteTextView) findViewById(R.id.destination);
        destination.setThreshold(1);


        destination.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                googlePlacesAutoComplete = new GooglePlacesAutoComplete(getBaseContext(), destination);
                googlePlacesAutoComplete.execute(destination.getText().toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

        destination.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String sorting ="rating";
                int checked_restroom =0;
                int checked_mobile =0;
                int checked_indoor =0;
                int checked_attended  =0;
                int checked_security =0;
                int checked_valet =0;
                long epoch_start = System.currentTimeMillis() /1000L; //current time
                long epoch_end = (System.currentTimeMillis() + 10800000)/1000L; //3 hours from now

                String value = ((TextView)view).getText().toString();

                try {
                  updatedValue = URLEncoder.encode(value,"UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                }

                String urlString = "http://api.parkwhiz.com/search?destination="+updatedValue+"&key=296ce162e9bb823e5c66bd85280aa208&start="+epoch_start+"&end="+epoch_end+"&sort="+sorting+"&restroom="+0+"&security="+0+"&valet="+0+"&indoor="+0+"&eticket="+0+"&attended="+0;

                //String urlString= "http://api.parkwhiz.com/search?destination="+updatedValue+"&key=296ce162e9bb823e5c66bd85280aa208&start="+epoch_start+"&end="+epoch_end;
                GetAPIResults apiResults = new GetAPIResults(getBaseContext());
                apiResults.execute(urlString);
            }
        });
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }
  }