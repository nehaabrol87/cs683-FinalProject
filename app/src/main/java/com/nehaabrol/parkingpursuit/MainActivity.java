    package com.nehaabrol.parkingpursuit;


    import android.app.Activity;
    import android.app.AlertDialog;
    import android.app.DatePickerDialog;
    import android.app.TimePickerDialog;
    import android.content.DialogInterface;
    import android.location.Address;
    import android.location.Criteria;
    import android.location.Geocoder;
    import android.location.Location;
    import android.location.LocationListener;
    import android.location.LocationManager;
    import android.os.Bundle;
    import android.support.v7.app.AppCompatActivity;
    import android.widget.AutoCompleteTextView;

    import com.google.android.gms.maps.CameraUpdateFactory;
    import com.google.android.gms.maps.MapFragment;
    import com.google.android.gms.common.GooglePlayServicesUtil;
    import com.google.android.gms.common.ConnectionResult;
    import com.google.android.gms.maps.model.LatLng;
    import com.google.android.gms.maps.model.MarkerOptions;
    import com.google.android.gms.maps.model.Marker;

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

    import java.util.List;
    import java.util.Locale;
    import java.util.TimeZone;
    import java.text.SimpleDateFormat;

    import android.view.Menu;
    import android.view.MenuItem;

    import java.util.Date;

    import android.widget.ListView;
    import android.support.v4.widget.DrawerLayout;
    import android.support.v7.app.ActionBarDrawerToggle;
    import android.content.res.Configuration;
    import android.content.Intent;

    import com.google.android.gms.maps.GoogleMap;

    import android.provider.Settings;
    import android.view.inputmethod.InputMethodManager;

    import android.app.Dialog;;

    public class MainActivity extends AppCompatActivity implements OnClickListener, OnItemClickListener, LocationListener {

        private String updatedValue;
        private ActionBarDrawerToggle mDrawerToggle;
        private DrawerLayout mDrawerLayout;
        private String mActivityTitle;
        private ListView mDrawerList;
        private ArrayAdapter<String> mAdapter;
        public View hiddenPanel;
        public GoogleMap mMap;
        private Marker marker;
        public static MapFragment mapFragment;
        private static MainActivity main = new MainActivity();
        private static final String LOG_TAG = "Google Places Autocomplete";
        private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
        private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
        private static final String OUT_JSON = "/json";
        private static final String API_KEY = "AIzaSyABnXKSGwNY4LhUExDF48esvU4n9z_Cypc";
        private Button btnCalendarStart, btnTimeStart, btnCalendarEnd, btnTimeEnd;
        private EditText txtDateStart, txtTimeStart, txtDateEnd, txtTimeEnd;
        // Variable for storing current date and time
        private int mYear, mMonth, mDay, mHour, mMinute;
        protected static final int SUB_ACTIVITY_REQUEST_CODE = 100;
        private LocationManager locationManager;
        private Location location;

        // Process to get Current Date
        private final Calendar c = Calendar.getInstance();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (!isGooglePlayServicesAvailable()) {
                finish();
            }
            setContentView(R.layout.activity_home_page);

            //Set Date TimePicker
            setDateTimePicker();

            //Call the method to call the Maps activity
            addMaps();

            //Set the drawer
            setupDrawer();
            addDrawerItems();

            //Get hidden Panel
            hiddenPanel = findViewById(R.id.hidden_panel);

            //Get Autocomplete textview
            AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.destination);
            autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
            autoCompView.setOnItemClickListener(this);

        }

        public void setDateTimePicker() {
            //Set time
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            // Process to get Current Time
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

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
        }

        public void addMaps() {

            // Getting Google Play availability status
            int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

            // Showing status
            if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available
                int requestCode = 10;
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
                dialog.show();

            } else {
               // Getting reference to the SupportMapFragment of activity_main.xml
                  mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
                // Getting GoogleMap object from the fragment
                mMap = mapFragment.getMap();

                // Getting LocationManager object from System Service LOCATION_SERVICE
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                // Creating a criteria object to retrieve provider
                Criteria criteria = new Criteria();
                // Getting the name of the best provider
                String provider = locationManager.getBestProvider(criteria, true);

                try {
                // Getting Current Location
                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0, this);
                    //locationManager.requestLocationUpdates(provider, 20000, 0, this);
                    if (location != null) {
                        onLocationChanged(location);
                    }
                    //Ask user for permission if GPS is off
                    if(provider!=null && !provider.equals("")){
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                          if(location==null) {
                              askUserToTurnLocationOn();
                          }
                        } else {
                             Toast.makeText(getBaseContext(), "No Provider Found", Toast.LENGTH_SHORT).show();
                           }
                    locationManager.removeUpdates(MainActivity.this);
                } catch (SecurityException e) {
                    Log.e("PERMISSION_EXCEPTION","PERMISSION_NOT_GRANTED");
                }
            }
        }

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                System.out.println("On location changed" + location);
                String address = " ";
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LatLng latLng = new LatLng(latitude, longitude);
                if (Geocoder.isPresent()) {
                    Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
                    try {
                        List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1);
                        address = addresses.get(0).getAddressLine(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //Show the current location on Google Map
                mMap.addMarker(new MarkerOptions().position(latLng).title("Current Location:" + address));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }

        private void askUserToTurnLocationOn(){
            final AlertDialog.Builder builder =
                    new AlertDialog.Builder(MainActivity.this);
            final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
            final String message = "Enable either GPS or any other location"
                    + " service to find current location.  Click OK to go to"
                    + " location services settings to let you do so.";

            builder.setMessage(message)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    MainActivity.this.startActivity(new Intent(action));
                                    d.dismiss();
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.cancel();
                                }
                            });
            builder.create().show();
        }

        private void setupDrawer() {
            mDrawerList = (ListView) findViewById(R.id.navList);
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mActivityTitle = getTitle().toString();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

                /** Called when a drawer has settled in a completely open state. */
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    getSupportActionBar().setTitle("Navigation!");
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }

                /** Called when a drawer has settled in a completely closed state. */
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                    getSupportActionBar().setTitle(mActivityTitle);
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }
            };

            mDrawerToggle.setDrawerIndicatorEnabled(true);
            mDrawerLayout.setDrawerListener(mDrawerToggle);
        }

        private void addDrawerItems() {
            String[] osArray = { "Android", "iOS", "Windows", "OS X", "Linux" };
            mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
            mDrawerList.setAdapter(mAdapter);

            mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(MainActivity.this, "Time for an upgrade!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void setCurrentDateTime() {
            long unixTime = System.currentTimeMillis() / 1000L;
            long after3Hours = (unixTime + 10800);

            txtDateStart.setText(String.format("%02d",mDay) + "/" + String.format("%02d", (mMonth+1)) + "/" + mYear);
            txtTimeStart.setText(String.format("%02d", mHour) + ":" + String.format("%02d", mMinute));

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
                            txtDateStart.setText(String.format("%02d", dayOfMonth)+ "/"+ String.format("%02d", (monthOfYear+1)) + "/" + year);
                        } else {
                            txtDateEnd.setText(String.format("%02d", dayOfMonth) + "/"+ String.format("%02d", (monthOfYear+1)) + "/" + year);
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
                            txtTimeStart.setText(String.format("%02d", hourOfDay)+ ":" + String.format("%02d", minute));
                        } else {
                            txtTimeEnd.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
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
           // Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
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

               //Call to get APxresults
               GetAPIResults apiResults = new GetAPIResults(getBaseContext(),mapFragment, this);
               apiResults.execute(urlString);

            ((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE))
                    .toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
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
                System.out.println("Places API "+jsonResults);
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

        @Override
        protected void onPostCreate(Bundle savedInstanceState) {
            super.onPostCreate(savedInstanceState);
            // Sync the toggle state after onRestoreInstanceState has occurred.
            mDrawerToggle.syncState();
        }

        @Override
        public void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            mDrawerToggle.onConfigurationChanged(newConfig);
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                return true;
            }

            // Activate the navigation drawer toggle
            if (mDrawerToggle.onOptionsItemSelected(item)) {
                return true;
            }

            return super.onOptionsItemSelected(item);
        }
    }
