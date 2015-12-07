package com.nehaabrol.parkingpursuit;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;


/**
 * Created by neha_.abrol on 11/7/15.
 */
public class GetDetailAPIResults extends  AsyncTask<String, Void, String>  {

    private Context context;
    private Activity activity;

    public GetDetailAPIResults(Context context,Activity activity) {
        this.context = context;
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
        ProgressBar spinner=(ProgressBar)this.activity.findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

        Toast.makeText(context, "Success getting Garage details", Toast.LENGTH_SHORT).show();
        JSONObject json = null;
        String description = " ";
        String directions = " ";
        String type = " ";

        System.out.println("Parking Details  " + result);
        try {
            json = new JSONObject(result);
            description =  json.getString("description");
            type =  json.getString("type");
            directions =  json.getString("directions");

            //Set description
            TextView description_field = (TextView) this.activity.findViewById (R.id.description);
            description_field.setText(description);

            //Set directions
            TextView directions_field = (TextView) this.activity.findViewById (R.id.directions);
            directions_field.setText(directions);

            //Set type
            TextView type_field = (TextView) this.activity.findViewById (R.id.type);
            type_field.setText(type);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
