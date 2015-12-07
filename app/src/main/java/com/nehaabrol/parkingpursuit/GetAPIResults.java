package com.nehaabrol.parkingpursuit;

import android.app.Dialog;
import android.graphics.PorterDuff;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;


/**
 * Created by neha_.abrol on 11/7/15.
 */
public class GetAPIResults extends  AsyncTask<String, Void, String>  {

    private Context context;
    private MapFragment mapFragment;
    private Activity activity;
    private  JSONArray parking_listings = null;
    private double lat = 0;
    private double lng = 0;
    private Button submitUpdate;
    private ProgressBar spinner;

    public GetAPIResults(Context context,MapFragment mapfragment,Activity activity) {
        this.context = context;
        this.mapFragment = mapfragment;
        this.activity = activity;
    }

    public static String GET(String urlString){
        String result = "";
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("User-Agent", "");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();

            InputStream inputStream = connection.getInputStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while ((line = rd.readLine()) != null) {
                result = result + line;
            }
        } catch (Exception e) {
            result = "Error calling ParkWhiz API"+ e;
        }
        return result;
    }

    @Override
    protected String doInBackground(String... urls) {
        return GET(urls[0]);
    }
    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {
        JSONObject json = null;
        submitUpdate =(Button)this.activity.findViewById(R.id.submitUpdate);
        spinner=(ProgressBar)this.activity.findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

        try {
            System.out.println("Parking listings from API" + parking_listings);
            json = new JSONObject(result);
            parking_listings = json.getJSONArray("parking_listings");
            lat = json.getDouble("lat");
            lng = json.getDouble("lng");
        } catch (JSONException e) {
            e.printStackTrace();
        }
            if (parking_listings != null) {
                Toast.makeText(context, "Success getting all listings", Toast.LENGTH_SHORT).show();
                MapsActivity maps = new MapsActivity(context,mapFragment,activity);
                maps.addMapsOnMarker(parking_listings, lat, lng);
                submitUpdate.setEnabled(true);
                submitUpdate.getBackground().setColorFilter(0xFF000000, PorterDuff.Mode.MULTIPLY);
            } else {
                Toast.makeText(context, "Sorry! No Parking Garages found", Toast.LENGTH_SHORT).show();
                GoogleMap mMap;
                mMap = mapFragment.getMap();
                mMap.clear();
                submitUpdate.setEnabled(true);
                submitUpdate.getBackground().setColorFilter(0xFF000000, PorterDuff.Mode.MULTIPLY);
            }
    }

    public  JSONArray getListings(){
        return parking_listings;
    }

    public  Double getLat(){
        return lat;
    }

    public  Double getLong(){
        return lng;
    }
  }
