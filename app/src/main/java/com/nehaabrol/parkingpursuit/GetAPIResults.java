package com.nehaabrol.parkingpursuit;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by neha_.abrol on 11/7/15.
 */
public class GetAPIResults extends  AsyncTask<String, Void, String>  {

    private Context context;
    private MapFragment mapFragment;

    public GetAPIResults(Context context,MapFragment mapfragment) {
        this.context = context;
        this.mapFragment = mapfragment;
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
        Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
        JSONObject json = null;
        JSONArray parking_listings = null;
        double lat = 0;
        double lng = 0;

        try {
            json = new JSONObject(result);
            parking_listings = json.getJSONArray("parking_listings");
            lat = json.getDouble("lat");
            lng = json.getDouble("lng");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        MapsActivity maps = new MapsActivity(context,mapFragment);
        maps.addMapsOnMarker(parking_listings, lat, lng);
    }
  }
