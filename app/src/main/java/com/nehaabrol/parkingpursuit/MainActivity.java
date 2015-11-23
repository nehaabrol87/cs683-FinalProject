package com.nehaabrol.parkingpursuit;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.ConnectionResult;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View;
import android.widget.AdapterView;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.ProgressDialog;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.View.OnClickListener;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.DateFormat;

public class MainActivity extends Activity   implements OnClickListener,OnItemClickListener {

    private String updatedValue;
    public View hiddenPanel;
    public static   MapFragment mapFragment;
    private static MainActivity main = new MainActivity();
    private static final String LOG_TAG = "Google Places Autocomplete";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyABnXKSGwNY4LhUExDF48esvU4n9z_Cypc";
    private Button btnCalendarStart, btnTimeStart,btnCalendarEnd,btnTimeEnd;
    private EditText txtDateStart, txtTimeStart ,txtDateEnd,txtTimeEnd;
    // Variable for storing current date and time
    private int mYear, mMonth, mDay, mHour, mMinute;
    // Process to get Current Date
    private final Calendar c = Calendar.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        setContentView(R.layout.activity_home_page);

        //Set time
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        // Process to get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);


        //Call google Maps API
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        new MapsActivity(getBaseContext(), mapFragment,this);

        //Get hidden Panel
        hiddenPanel = findViewById(R.id.hidden_panel);

        //Get date Time picker
        btnCalendarStart = (Button)findViewById(R.id.btnCalendarStart);
        btnTimeStart = (Button)findViewById(R.id.btnTimeStart);
        btnCalendarEnd = (Button)findViewById(R.id.btnCalendarEnd);
        btnTimeEnd = (Button)findViewById(R.id.btnTimeEnd);

        txtDateStart = (EditText)findViewById(R.id.txtDateStart);
        txtTimeStart = (EditText)findViewById(R.id.txtTimeStart);
        txtDateEnd = (EditText)findViewById(R.id.txtDateEnd);
        txtTimeEnd = (EditText)findViewById(R.id.txtTimeEnd);

        //Set current date/Time
        setCurrentDateTime();

        btnCalendarStart.setOnClickListener(this);
        btnTimeStart.setOnClickListener(this);
        btnCalendarEnd.setOnClickListener(this);
        btnTimeEnd.setOnClickListener(this);

        //Get Autocomplete textview
        AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.destination);

        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
        autoCompView.setOnItemClickListener(this);

    }

    public void setCurrentDateTime() {
        long unixTime = System.currentTimeMillis() / 1000L;
        long after3Hours = (unixTime + 10800);

        txtDateStart.setText(mDay + "/" + (mMonth + 1) + "/" + mYear);
        txtTimeStart.setText(mHour + ":" + mMinute);

        Date date = new Date(after3Hours *1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm"); // the format of your date
        sdf.setTimeZone(TimeZone.getTimeZone("GMT-5")); // give a timezone reference for formating
        String formattedDate = sdf.format(date);

        String [] formattedDateSeparated = formattedDate.split(" ");

        txtDateEnd.setText(formattedDateSeparated[0]);
        txtTimeEnd.setText(formattedDateSeparated[1]);
    }

    @Override
    public void onClick(final View v) {
        if (v == btnCalendarStart || v==btnCalendarEnd) {
            // Launch Date Picker Dialog
            DatePickerDialog dpd = new DatePickerDialog(this,new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year,int monthOfYear, int dayOfMonth) {
                // Display Selected date in textbox
                    if(v ==btnCalendarStart){
                        txtDateStart.setText(dayOfMonth + "/"+ (monthOfYear + 1) + "/" + year);
                    } else {
                        txtDateEnd.setText(dayOfMonth + "/"+ (monthOfYear + 1) + "/" + year);
                    }
                }
            }, mYear, mMonth, mDay);
            dpd.show();
        }
        if (v == btnTimeStart || v==btnTimeEnd) {
            // Launch Time Picker Dialog
            TimePickerDialog tpd = new TimePickerDialog(this,new TimePickerDialog.OnTimeSetListener() {

                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                // Display Selected time in textbox
                    if(v == btnTimeStart){
                        txtTimeStart.setText(hourOfDay + ":" + minute);
                    } else {
                        txtTimeEnd.setText(hourOfDay + ":" + minute);
                    }
                }
            }, mHour, mMinute, false);
            tpd.show();
        }
    }

    public void slideUpDown(View panel) {
        if (!isPanelShown()) {
            // Show the panel
            Animation bottomUp = AnimationUtils.loadAnimation(this,
                    R.anim.bottom_up);
            hiddenPanel.startAnimation(bottomUp);
            hiddenPanel.setVisibility(View.VISIBLE);
        }
        else {
            // Hide the Panel
            Animation bottomDown = AnimationUtils.loadAnimation(this,
                    R.anim.bottom_down);
            hiddenPanel.startAnimation(bottomDown);
            hiddenPanel.setVisibility(View.GONE);
        }
    }

    private boolean isPanelShown() {
        return hiddenPanel.getVisibility() == View.VISIBLE;
    }

    public void onItemClick(AdapterView adapterView, View view, int position, long id) {
        String value = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
        String sorting ="rating";
        int checked_restroom =0;
        int checked_mobile =0;
        int checked_indoor =0;
        int checked_attended  =0;
        int checked_security =0;
        int checked_valet =0;
        long epoch_start = System.currentTimeMillis() /1000L; //current time
        long epoch_end = (System.currentTimeMillis() + 10800000)/1000L; //3 hours from now

        try {
            updatedValue = URLEncoder.encode(value,"UTF-8");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }

        String urlString = "http://api.parkwhiz.com/search?destination="+updatedValue+"&key="+getResources().getString(R.string.park_whiz_key)+"&start="+epoch_start+"&end="+epoch_end+"&sort="+sorting+"&restroom="+0+"&security="+0+"&valet="+0+"&indoor="+0+"&eticket="+0+"&attended="+0;

           //Call to get API results
           GetAPIResults apiResults = new GetAPIResults(getBaseContext(),mapFragment,this);
           apiResults.execute(urlString);
    }
    public static  ArrayList autocomplete(String input) {
        ArrayList resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&components=country:us");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
             Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
             Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            System.out.println(jsonResults);
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                System.out.println("============================================================");
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
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


    class GooglePlacesAutocompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> resultList;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }
}
