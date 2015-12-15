    package com.nehaabrol.parkingpursuit;

    import android.content.Context;
    import android.graphics.PorterDuff;
    import android.location.Address;
    import android.location.Geocoder;
    import android.support.v4.widget.DrawerLayout;
    import android.support.v7.app.ActionBarDrawerToggle;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.content.Intent;
    import android.view.animation.Animation;
    import android.view.animation.AnimationUtils;
    import android.view.inputmethod.InputMethodManager;
    import android.widget.AdapterView;
    import android.widget.ArrayAdapter;
    import android.widget.AutoCompleteTextView;
    import android.widget.Filter;
    import android.widget.Filterable;
    import android.widget.ListView;
    import android.widget.ProgressBar;
    import android.widget.TextView;
    import android.view.MenuItem;
    import android.view.View;
    import java.io.BufferedReader;
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.InputStreamReader;
    import java.net.HttpURLConnection;
    import java.net.MalformedURLException;
    import java.net.URL;
    import java.net.URLEncoder;
    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;

    import org.json.JSONArray;
    import org.json.JSONException;
    import org.json.JSONObject;

    import android.graphics.Color;
    import android.os.AsyncTask;
    import android.util.Log;
    import android.view.Menu;

    import com.google.android.gms.maps.CameraUpdateFactory;
    import com.google.android.gms.maps.GoogleMap;
    import com.google.android.gms.maps.MapFragment;
    import com.google.android.gms.maps.model.BitmapDescriptorFactory;
    import com.google.android.gms.maps.model.LatLng;
    import com.google.android.gms.maps.model.LatLngBounds;
    import com.google.android.gms.maps.model.MarkerOptions;
    import com.google.android.gms.maps.model.PolylineOptions;
    import android.widget.Button;
    import android.view.View.OnClickListener;
    import android.widget.Toast;
    import android.support.v4.app.NavUtils;
    import android.widget.TableLayout;
    import android.widget.TableRow;
    import android.view.Gravity;
    import android.text.Html;
    import  android.view.ViewGroup.LayoutParams;


    /**
     * Created by neha_.abrol on 12/7/15.
     */
    public class GetDirectionsActivity extends AppCompatActivity implements OnClickListener,AdapterView.OnItemClickListener {
        GoogleMap mGoogleMap;
        private String startAddress = " ";
        private String endAddress = " ";
        double endtLatitude;
        double endLongitude;
        private ActionBarDrawerToggle mDrawerToggle;
        private DrawerLayout mDrawerLayout;
        private ListView mDrawerList;
        private ArrayAdapter<String> mAdapter;
        private String mActivityTitle;
        private MapFragment mapFragment;
        private LatLng origin;
        private AutoCompleteTextView startAutocomplete,endAutocomplete;
        private LatLng dest;
        private static final String LOG_TAG = "Google Places Autocomplete";
        private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
        private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
        private static final String OUT_JSON = "/json";
        private static final String API_KEY = "AIzaSyALXjBvhqwy3xqlDY4QWzIElp3So132dtw";
        private ProgressBar spinner;
        private Button getDirections;
        private Button getDetailedDirections;
        public View hiddenPanel;
        private  ArrayList<String> routeDirections = new ArrayList<String>();
        private  ArrayList<String> routeDuration = new ArrayList<String>();
        private  ArrayList<String> routeDistance = new ArrayList<String>();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.directions);

            // Get the message from the intent
            Intent intent = getIntent();
            String jsonString = intent.getStringExtra(MapsActivity.DIRECTIONS);

            try {
                JSONObject parking_data = new JSONObject(jsonString);
                endtLatitude = parking_data.getDouble("lat");
                endLongitude = parking_data.getDouble("lng");
                endAddress = parking_data.getString("address");
            }
            catch(JSONException e) {

            }

            //Map defaults
            mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapDirections);
            mGoogleMap = mapFragment.getMap();
            mGoogleMap.clear();
            //getCurrentLocationOfUser();
            setUpMapDefaults();

            setupDrawer();

            //Get hidden Panel
            hiddenPanel = findViewById(R.id.hidden_panel2);


            //Spinner
            spinner=(ProgressBar)findViewById(R.id.progressBar);
            spinner.bringToFront();
            spinner.setVisibility(View.GONE);

            //Set on click listener on done button
            getDirections = (Button) findViewById(R.id.getDirections);
            getDirections.getBackground().setColorFilter(0xFF000000, PorterDuff.Mode.MULTIPLY);
            getDirections.setOnClickListener(this);

            getDetailedDirections = (Button) findViewById(R.id.getDetailedDirections);
            getDetailedDirections.setOnClickListener(this);

            }

        public void setUpMapDefaults() {
            // Create the text view
            TextView endLocation = (TextView) findViewById(R.id.endLocation);
            endLocation.setText(endAddress);

            dest = new LatLng(endtLatitude, endLongitude);
            drawMarker(dest,"dest");
            //Get Autocomplete textview
            startAutocomplete = (AutoCompleteTextView) findViewById(R.id.startLocation);
            startAutocomplete.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item1));
            startAutocomplete.setOnItemClickListener(this);

            //Get Autocomplete textview
            endAutocomplete = (AutoCompleteTextView) findViewById(R.id.endLocation);
            endAutocomplete.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item2));
            endAutocomplete.setOnItemClickListener(this);


        }
        @Override
        public void onClick(final View v){
            if(v == getDirections) {

                startAddress = startAutocomplete.getText().toString();
                endAddress = endAutocomplete.getText().toString();
                origin = getLocationFromAddress(this, startAddress);
                dest = getLocationFromAddress(this,endAddress);

                if (startAddress.length() == 0 || endAddress.length() == 0) {
                    Toast.makeText(GetDirectionsActivity.this, "Start or end location cannot be empty", Toast.LENGTH_SHORT).show();
                }if (origin == null || dest == null) {
                    Toast.makeText(GetDirectionsActivity.this, "There was a problem getting directions.Please try with nearby location", Toast.LENGTH_SHORT).show();
                }
                else {

                    System.out.println("Origin after change" + origin);
                    System.out.println("Dest after change" + dest);
                    mGoogleMap = mapFragment.getMap();
                    mGoogleMap.clear();
                    drawMarker(origin, "origin");
                    drawMarker(dest, "dest");
                    spinner.setVisibility(View.VISIBLE);
                    getDirections.setEnabled(false);

                    startAutocomplete.setFocusable(false);
                    startAutocomplete.setFocusableInTouchMode(true);
                    endAutocomplete.setFocusable(false);
                    endAutocomplete.setFocusableInTouchMode(true);

                    getDirections.getBackground().setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
                    hideSoftKeyboard();
                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(origin, dest);
                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.execute(url);
                }
            } else {
                slideUpDownDirections(hiddenPanel);
            }
        }


        public LatLng getLocationFromAddress(Context context,String strAddress) {

            Geocoder coder = new Geocoder(context);
            List<Address> address;
            LatLng p1 = null;

            try {
                address = coder.getFromLocationName(strAddress, 5);
                if (address == null) {
                    return null;
                }
                Address location = address.get(0);
                location.getLatitude();
                location.getLongitude();

                p1 = new LatLng(location.getLatitude(), location.getLongitude() );

            } catch (Exception ex) {

                ex.printStackTrace();
            }

            System.out.println("New Lat long " + p1);

            return p1;
        }

        public void hideSoftKeyboard() {
            //Hide Soft keyboard
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(
                    startAutocomplete.getWindowToken(), 0);

        }

        private void setupDrawer() {
            mDrawerList = (ListView) findViewById(R.id.navList);
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mActivityTitle = getTitle().toString();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

                /**
                 * Called when a drawer has settled in a completely open state.
                 */
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    getSupportActionBar().setTitle("Navigation!");
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }

                /**
                 * Called when a drawer has settled in a completely closed state.
                 */
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                    getSupportActionBar().setTitle(mActivityTitle);
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }
            };

            mDrawerToggle.setDrawerIndicatorEnabled(true);
            mDrawerLayout.setDrawerListener(mDrawerToggle);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    if (getParentActivityIntent() == null) {
                        onBackPressed();
                    } else {
                        NavUtils.navigateUpFromSameTask(this);
                    }
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        @Override
        public void onBackPressed() {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

        private String getDirectionsUrl(LatLng origin,LatLng dest){

            // Origin of route
            String str_origin = "origin="+origin.latitude+","+origin.longitude;

            // Destination of route
            String str_dest = "destination="+dest.latitude+","+dest.longitude;

            // Sensor enabled
            String sensor = "sensor=false";

            // Building the parameters to the web service
            String parameters = str_origin+"&"+str_dest+"&"+sensor;

            // Output format
            String output = "json";

            // Building the url to the web service
            String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

            return url;
        }

        /** A method to download json data from url */
        private String downloadUrl(String strUrl) throws IOException{
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try{
                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb  = new StringBuffer();

                String line = "";
                while( ( line = br.readLine())  != null){
                    sb.append(line);
                }

                data = sb.toString();

                br.close();

            }catch(Exception e){
                Log.d("Exception while downloading url", e.toString());
            }finally{
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }



    /** A class to download data from Google Directions URL */
    private class DownloadTask extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /** A class to parse the Google Directions in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
               // DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.RED);
            }
            // Drawing polyline in the Google Map for the i-th route
            animateCamera();
            spinner.setVisibility(View.GONE);
            getDirections.setEnabled(true);
            getDetailedDirections.setEnabled(true);
            getDirections.getBackground().setColorFilter(0xFF000000, PorterDuff.Mode.MULTIPLY);
            getDetailedDirections.getBackground().setColorFilter(0xFF000000, PorterDuff.Mode.MULTIPLY);
            mGoogleMap.addPolyline(lineOptions);
        }
    }
        private void animateCamera(){
            if(origin !=null && dest !=null){
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(origin);
                builder.include(dest);
                LatLngBounds bounds = builder.build();
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20));
            }
        }

        private void drawMarker(LatLng point, String source){
            // Creating MarkerOptions
            if(source == "dest"){
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(point)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .title("End Location:" + endAddress));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
            } else {
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(point)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .title("Start Location:" + startAddress));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
            }

        }

        public void onItemClick(AdapterView adapterView, View view, int position, long id) {

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

        public void slideUpDownDirections(View panel) {
            if (!isPanelShown()) {
                // Show the panel
                Animation bottomUp = AnimationUtils.loadAnimation(this,
                        R.anim.bottom_up);
                hiddenPanel.startAnimation(bottomUp);
                hiddenPanel.setVisibility(View.VISIBLE);
            }
            else {
                // Hide the Panel
                NavUtils.navigateUpFromSameTask(this);
            }
        }

        private boolean isPanelShown() {
            return hiddenPanel.getVisibility() == View.VISIBLE;
        }

        public List <List<HashMap<String,String>>> parse(JSONObject jObject){

            List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>() ;
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONArray jSteps = null;

            try {

                jRoutes = jObject.getJSONArray("routes");


                /** Traversing all routes */
                for(int i=0;i<jRoutes.length();i++) {
                    System.out.println("jRoutes" + jRoutes);
                    jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                    List path = new ArrayList<HashMap<String, String>>();

                    /** Traversing all legs */
                    for(int j=0;j<jLegs.length();j++){
                        jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");

                        /** Traversing all steps */
                        for(int k=0;k<jSteps.length();k++){
                            String polyline = "";
                            String instructions =" ";
                            String distance ="";
                            String duration ="";
                            polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                            instructions = (String)(((JSONObject)jSteps.get(k)).get("html_instructions"));
                            distance = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("distance")).get("text");
                            duration = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("duration")).get("text");
                            List<LatLng> list = decodePoly(polyline);

                            routeDirections.add(instructions);
                            routeDistance.add(distance);
                            routeDuration.add(duration);


                            /** Traversing all points */
                            for(int l=0;l<list.size();l++){
                                HashMap<String, String> hm = new HashMap<String, String>();
                                hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
                                hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                        setDetailedDirections(routeDirections,routeDistance,routeDuration);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }catch (Exception e){
            }

            System.out.println("routes"+routes);

            return routes;
        }


        /**
         * Method to decode polyline points
        **/
        private List<LatLng> decodePoly(String encoded) {

            List<LatLng> poly = new ArrayList<LatLng>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }

            return poly;
        }

        public void setDetailedDirections(ArrayList routeDirections,ArrayList routeDistance,ArrayList routeDuration){
                TableLayout stk = (TableLayout) findViewById(R.id.table_main);
                for (int i = 0; i < routeDirections.size(); i++) {
                    TableRow tbrow = new TableRow(this);
                    TextView t1v = new TextView(this);
                    if(i%2 ==0) {
                        t1v.setBackgroundColor(0xFFD3D3D3);
                    }

                    t1v.setTextColor(Color.BLACK);
                    t1v.setText("" + Html.fromHtml(routeDirections.get(i).toString()) + " Distance : " +
                            routeDistance.get(i) + " " + "Duration : " + routeDuration.get(i));
                    t1v.setPadding(5, 5, 5, 5);
                    t1v.setTextSize(15);
                    t1v.setGravity(Gravity.CENTER);
                    t1v.setLayoutParams(
                            new TableRow.LayoutParams(
                                    LayoutParams.MATCH_PARENT,
                                    LayoutParams.MATCH_PARENT));
                    tbrow.addView(t1v);
                    stk.addView(tbrow);
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

