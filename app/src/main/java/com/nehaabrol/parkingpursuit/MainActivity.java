    package com.nehaabrol.parkingpursuit;


    import android.app.Activity;
    import android.app.AlertDialog;
    import android.app.DatePickerDialog;
    import android.app.TimePickerDialog;
    import android.content.DialogInterface;
    import android.location.Address;
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
    import android.graphics.PorterDuff;
    import android.widget.ProgressBar;
    import java.util.GregorianCalendar;
    import android.support.v4.view.ViewPager;


    public class MainActivity extends AppCompatActivity implements OnClickListener, OnItemClickListener, LocationListener {

        private String userSelectedDestination;
        private ActionBarDrawerToggle mDrawerToggle;
        private DrawerLayout mDrawerLayout;
        private String mActivityTitle;
        private ListView mDrawerList;
        private ArrayAdapter<String> mAdapter;
        public View hiddenPanel;
        public GoogleMap mMap;
        public static MapFragment mapFragment;
        private static MainActivity main = new MainActivity();
        private static final String LOG_TAG = "Google Places Autocomplete";
        private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
        private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
        private static final String OUT_JSON = "/json";
        private static final String API_KEY = "AIzaSyCjEZXOWU6WX09TxR5Nlb6f46wceV-MoJE";
        private Button btnCalendarStart, btnTimeStart, btnCalendarEnd, btnTimeEnd ,submitUpdate;
        private EditText txtDateStart, txtTimeStart, txtDateEnd, txtTimeEnd;
        // Variable for storing start current date and time
        private int startYear, startMonth, startDay, startHour, startMinute;
        private int endYear, endMonth, endDay, endHour, endMinute;
        protected static final int SUB_ACTIVITY_REQUEST_CODE = 100;
        //Objec for GetApiResults
        private GetAPIResults apiResults;
        private MapsActivity mapsActivity;
        private boolean saved;
        private String parking_listings;
        private Double latitude;
        private Double longitude;
        private boolean locationUpdated = false;
        private LocationManager locationManager;
        private AutoCompleteTextView autoCompView;
        private long userSelectedStartDateTime;
        private long userSelectedEndDateTime;
        private ProgressBar spinner;

        // Process to get Current Date
        private final Calendar c = Calendar.getInstance();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (!isGooglePlayServicesAvailable()) {
                finish();
            }

            //Create view
            setContentView(R.layout.activity_home_page);

            //Spinner
            spinner=(ProgressBar)findViewById(R.id.progressBar);
            spinner.bringToFront();
            spinner.setVisibility(View.GONE);

            //Add Maps
            mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
            mapsActivity = new MapsActivity(getBaseContext(),mapFragment, this);

            //Get Autocomplete textview
            autoCompView = (AutoCompleteTextView) findViewById(R.id.destination);
            autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
            autoCompView.setOnItemClickListener(this);

            //Add background to Done button
            submitUpdate =(Button)findViewById(R.id.submitUpdate);
            submitUpdate.getBackground().setColorFilter(0xFF000000, PorterDuff.Mode.MULTIPLY);

            //Set Date TimePicker
            setDateTimePicker();
            //Call the method to call the Maps activity
            //Set the drawer
            setupDrawer();
            addDrawerItems();
            //Get hidden Panel
            hiddenPanel = findViewById(R.id.hidden_panel);

            //Call to get API esults
            apiResults = new GetAPIResults(getBaseContext(),mapFragment, this);

            //Check if there is a savedInstance
            if(savedInstanceState != null && savedInstanceState.getString("parking_listings") != null ) {
                saved = true;
                JSONArray parking_listings_array = null;

                try {
                    latitude = savedInstanceState.getDouble("lat");
                    longitude = savedInstanceState.getDouble("lng");
                    parking_listings = savedInstanceState.getString("parking_listings");
                    parking_listings_array = new JSONArray(parking_listings);
                    mapsActivity.addMapsOnMarker(parking_listings_array, latitude, longitude);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                mMap = mapFragment.getMap();
                getCurrentLocationOfUser();
            }
        }

        public void setDateTimePicker() {
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
            setDefaultStartDateTime();
            setDefaultEndDateTime();


            btnCalendarStart.setOnClickListener(this);
            btnTimeStart.setOnClickListener(this);
            btnCalendarEnd.setOnClickListener(this);
            btnTimeEnd.setOnClickListener(this);
            submitUpdate.setOnClickListener(this);
        }


        public void getCurrentLocationOfUser(){
            try {
                //Can access User's location
                if (canGetLocation()) {
                    System.out.println("Location services on");
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                    if(location!= null) {
                        onLocationChanged(location);
                        locationManager.removeUpdates(MainActivity.this);
                    }
                } else {
                    //Display alert box to turn location services om
                    askUserToTurnLocationOn();
                }

            } catch (SecurityException e) {
                Log.e("PERMISSION_EXCEPTION","PERMISSION_NOT_GRANTED");
            }
        }

        public boolean canGetLocation() {
            LocationManager locationManager;
            boolean gpsEnabled = false;
            boolean networkEnabled = false;

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            // exceptions will be thrown if provider is not permitted.
            try {
                gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ex) {

            }
            try {
                networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception ex) {
            }

            return gpsEnabled || networkEnabled;
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
                                    d.dismiss();
                                    MainActivity.this.startActivity(new Intent(action));
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

        @Override
        public void onLocationChanged(Location location) {
            if (location != null && locationUpdated == false) {
                locationUpdated = true;
                System.out.println("locationChangedWasCalled" + location);
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

        public void setDefaultStartDateTime(){
            Date currentDate = new Date();
            long unixTime = roundToNearestHour(currentDate).getTime()/1000L;


            Date date = new Date(unixTime *1000L); // *1000 is to convert seconds to milliseconds
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm"); // the format of your date
            sdf.setTimeZone(TimeZone.getTimeZone("GMT-5")); // give a timezone reference for formating
            String formattedDate = sdf.format(date);

            String [] formattedDateSeparated = formattedDate.split(" ");
            String [] formatDate = formattedDateSeparated[0].split("/");
            String [] formatTime = formattedDateSeparated[1].split(":");


            startDay = Integer.parseInt(formatDate[0]);
            startMonth = Integer.parseInt(formatDate[1]) -1 ;
            startYear  = Integer.parseInt(formatDate[2]);
            startHour = Integer.parseInt(formatTime[0]);
            startMinute = Integer.parseInt(formatTime[1]);


            txtDateStart.setText(String.format("%02d",startDay) + "/" + String.format("%02d", (startMonth+1)) + "/" + startYear);
            txtTimeStart.setText(String.format("%02d", startHour) + ":" + String.format("%02d", startMinute));
        }

        public void setDefaultEndDateTime(){
            Date currentDate = new Date();
            long unixTime = roundToNearestHour(currentDate).getTime()/1000L;
            long after3Hours = (unixTime + 10800);

            Date date = new Date(after3Hours *1000L); // *1000 is to convert seconds to milliseconds
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm"); // the format of your date
            sdf.setTimeZone(TimeZone.getTimeZone("GMT-5")); // give a timezone reference for formating
            String formattedDate = sdf.format(date);

            String [] formattedDateSeparated = formattedDate.split(" ");
            String [] formatDate = formattedDateSeparated[0].split("/");
            String [] formatTime = formattedDateSeparated[1].split(":");

            endDay = Integer.parseInt(formatDate[0]);
            endMonth = Integer.parseInt(formatDate[1]) -1 ;
            endYear  = Integer.parseInt(formatDate[2]);
            endHour = Integer.parseInt(formatTime[0]);
            endMinute = Integer.parseInt(formatTime[1]);

            txtDateEnd.setText(String.format("%02d", endDay) + "/" + String.format("%02d", (endMonth + 1)) + "/" + endYear);
            txtTimeEnd.setText(String.format("%02d", endHour) + ":" + String.format("%02d", endMinute));
        }

        public Date roundToNearestHour(Date date) {
            Calendar c = new GregorianCalendar();
            c.setTime(date);
            c.add(Calendar.HOUR, 1);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            return c.getTime();
        }

        @Override
        public void onClick(final View v) {
            if (v == btnCalendarStart ) {
                // Launch Date Picker Dialog
                DatePickerDialog dpdStart = new DatePickerDialog(this,new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,int monthOfYear, int dayOfMonth) {
                            // Display Selected date in textbox
                            txtDateStart.setText(String.format("%02d", dayOfMonth) + "/" + String.format("%02d", (monthOfYear+1)) + "/" + year);
                            startYear = year;
                            startMonth = monthOfYear;
                            startDay = dayOfMonth;
                    }
                }, startYear, startMonth, startDay);
                dpdStart.show();
            }
            if (v==btnCalendarEnd) {
                // Launch End Date Picker Dialog
                DatePickerDialog dpdEnd = new DatePickerDialog(this,new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,int monthOfYear, int dayOfMonth) {
                            txtDateEnd.setText(String.format("%02d", dayOfMonth) + "/"+ String.format("%02d", (monthOfYear +1)) + "/" + year);
                            endYear = year;
                            endMonth = monthOfYear;
                            endDay = dayOfMonth;
                        }
                }, endYear, endMonth, endDay);
                dpdEnd.show();
            }
            if (v == btnTimeStart) {
                // Launch Time Picker Dialog
                TimePickerDialog tpdStart = new TimePickerDialog(this,new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        startHour = hourOfDay;
                        startMinute = minute;

                        if(startMinute !=0){
                            Date startDate = convertStringToDate(startDay, startMonth, startYear, startHour, startMinute);
                            Date startDateRoundedToNearestHour = roundToNearestHour(startDate);

                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm"); // the format of your date
                            sdf.setTimeZone(TimeZone.getTimeZone("GMT-5")); // give a timezone reference for formating
                            String formattedDate = sdf.format(startDateRoundedToNearestHour);

                            String [] formattedDateSeparated = formattedDate.split(" ");
                            String [] formatDate = formattedDateSeparated[0].split("/");
                            String [] formatTime = formattedDateSeparated[1].split(":");

                            startDay = Integer.parseInt(formatDate[0]);
                            startMonth = Integer.parseInt(formatDate[1]) -1 ;
                            startYear  = Integer.parseInt(formatDate[2]);
                            startHour = Integer.parseInt(formatTime[0]);
                            startMinute = Integer.parseInt(formatTime[1]);

                            txtDateStart.setText(String.format("%02d", startDay) + "/" + String.format("%02d", (startMonth+1)) + "/" + startYear);
                        }

                        txtTimeStart.setText(String.format("%02d", startHour)+ ":" + String.format("%02d", startMinute));
                    }
                }, startHour, startMinute, false);
                tpdStart.show();
            }

            if (v==btnTimeEnd) {
                // Launch Time Picker Dialog
                TimePickerDialog tpdEnd = new TimePickerDialog(this,new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        endHour = hourOfDay;
                        endMinute = minute;
                        if (endMinute!= 0) {
                            Date endDate = convertStringToDate(endDay, endMonth, endYear, endHour, endMinute);
                            Date endDateRoundedToNearestHour = roundToNearestHour(endDate);

                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm"); // the format of your date
                            sdf.setTimeZone(TimeZone.getTimeZone("GMT-5")); // give a timezone reference for formating
                            String formattedDate = sdf.format(endDateRoundedToNearestHour);

                            String [] formattedDateSeparated = formattedDate.split(" ");
                            String [] formatDate = formattedDateSeparated[0].split("/");
                            String [] formatTime = formattedDateSeparated[1].split(":");

                            endDay = Integer.parseInt(formatDate[0]);
                            endMonth = Integer.parseInt(formatDate[1]) -1 ;
                            endYear  = Integer.parseInt(formatDate[2]);
                            endHour = Integer.parseInt(formatTime[0]);
                            endMinute = Integer.parseInt(formatTime[1]);

                            txtDateEnd.setText(String.format("%02d", endDay) + "/"+ String.format("%02d", (endMonth +1)) + "/" + endYear);
                        }

                        txtTimeEnd.setText(String.format("%02d", endHour) + ":" + String.format("%02d", endMinute));
                    }
                }, endHour, endMinute, false);
                tpdEnd.show();
            }

            if (v==submitUpdate) {
                if(userSelectedDestination == null ){
                    if(apiResults.getListings() != null) {
                        mMap = mapFragment.getMap();
                        mMap.clear();
                    }
                    Toast.makeText(MainActivity.this, "Destination cannot be empty", Toast.LENGTH_SHORT).show();
                } else {
                    if(doDateTimeValidation()){
                        spinner.setVisibility(View.VISIBLE);
                        submitUpdate.setEnabled(false);
                        autoCompView.setFocusable(false);
                        autoCompView.setFocusableInTouchMode(true);
                        submitUpdate.getBackground().setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
                        String sorting ="rating";
                        int checked_restroom =0;
                        int checked_mobile =0;
                        int checked_indoor =0;
                        int checked_attended  =0;
                        int checked_security =0;
                        int checked_valet =0;
                        long epoch_start = userSelectedStartDateTime;
                        long epoch_end = userSelectedEndDateTime;
                        String urlString = "http://api.parkwhiz.com/search?destination="+userSelectedDestination+"&key="+getResources().getString(R.string.park_whiz_key)+"&start="+epoch_start+"&end="+epoch_end+"&sort="+sorting+"&restroom="+0+"&security="+0+"&valet="+0+"&indoor="+0+"&eticket="+0+"&attended="+0;

                        Toast.makeText(MainActivity.this, "Sending your request", Toast.LENGTH_SHORT).show();
                        mMap = mapFragment.getMap();
                        mMap.clear();
                        apiResults= new GetAPIResults(getBaseContext(),mapFragment, this);
                        apiResults.execute(urlString);
                        hideSoftKeyboard();
                    }
                }
            }
        }

        public Date convertStringToDate(int day,int month,int year,int hour,int minute) {
            Date date = null;
            String dateInString = String.format("%02d", day) + "-" + String.format("%02d", (month + 1)) + "-" + year + " "  + String.format("%02d", (hour)) + ":" + String.format("%02d", (minute));
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            try {
                date = format.parse(dateInString);
            } catch (Exception e) {
            }
            return date;
        }

        public boolean  doDateTimeValidation() {
            Date startDate = convertStringToDate(startDay,startMonth,startYear,startHour,startMinute);
            Date endDate = convertStringToDate(endDay,endMonth,endYear,endHour,endMinute);

            userSelectedStartDateTime = startDate.getTime()/1000L;
            userSelectedEndDateTime = endDate.getTime() /1000L;
            long unixTime = System.currentTimeMillis() / 1000L;

                if(userSelectedStartDateTime > userSelectedEndDateTime ) {
                    Toast.makeText(MainActivity.this, "Start Date time cant be more than end date time", Toast.LENGTH_SHORT).show();
                    if(apiResults.getListings() != null) {
                        mMap = mapFragment.getMap();
                        mMap.clear();
                    }
                    return false;
                }

                if(userSelectedStartDateTime < unixTime) {
                    Toast.makeText(MainActivity.this, "Start time must be greater than current time", Toast.LENGTH_SHORT).show();
                    if(apiResults.getListings() != null) {
                        mMap = mapFragment.getMap();
                        mMap.clear();
                    }
                    return false;
                }

             return true;
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
            try {
                userSelectedDestination = URLEncoder.encode(value,"UTF-8");
                autoCompView.setFocusable(false);
                autoCompView.setFocusableInTouchMode(true);
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
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

        @Override
        public void onSaveInstanceState(Bundle savedInstanceState) {
            // Save the user's current search
            if(apiResults.getListings() != null ) {
                saved = false;
                parking_listings = apiResults.getListings().toString();
                latitude = apiResults.getLat();
                longitude = apiResults.getLong();
                savedInstanceState.putString("parking_listings", parking_listings);
                savedInstanceState.putDouble("lat",latitude);
                savedInstanceState.putDouble("lng", longitude);
                super.onSaveInstanceState(savedInstanceState);
            }
            if(saved) {
                savedInstanceState.putString("parking_listings", parking_listings);
                savedInstanceState.putDouble("lat", latitude);
                savedInstanceState.putDouble("lng", longitude);
                super.onSaveInstanceState(savedInstanceState);
            }
        }

        public void hideSoftKeyboard() {
            //Hide Soft keyboard
            InputMethodManager imm = (InputMethodManager)
            getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(
                    autoCompView.getWindowToken(), 0);

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
        public void onResume() {
            super.onResume();  //  call the superclass method
            if(parking_listings == null){ //Means no results have been saved till now
                try {
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                    if(location!= null) {
                        onLocationChanged(location);
                        locationManager.removeUpdates(MainActivity.this);
                    }
                }
                catch (SecurityException e) {
                Log.e("PERMISSION_EXCEPTION","PERMISSION_NOT_GRANTED");
                }
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
            hideSoftKeyboard();
            autoCompView.setFocusable(false);
            autoCompView.setFocusableInTouchMode(true);
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


