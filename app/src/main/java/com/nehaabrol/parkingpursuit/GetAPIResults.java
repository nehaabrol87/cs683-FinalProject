package com.nehaabrol.parkingpursuit;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.content.Context;
import android.widget.Toast;

/**
 * Created by neha_.abrol on 11/7/15.
 */
public class GetAPIResults extends  AsyncTask<String, Void, String>  {

    private Context context;

    public GetAPIResults(Context context) {
        this.context = context;
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
        System.out.println(result);
    }
  }
