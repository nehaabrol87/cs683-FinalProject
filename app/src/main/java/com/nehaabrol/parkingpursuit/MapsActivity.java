package com.nehaabrol.parkingpursuit;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONObject;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.os.AsyncTask;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Paint.Align;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;

import java.util.HashMap;

import android.widget.ImageView;
import android.util.Log;
import android.app.AlertDialog;
import android.provider.Settings;
import android.content.DialogInterface;
import android.app.AlertDialog.Builder;
import android.content.Intent;

import com.google.android.gms.maps.MapFragment;


/**
 * Created by neha_.abrol on 11/7/15.
 */
public class MapsActivity implements  OnMarkerClickListener {

    private static GoogleMap  mMap;
    private  Context context;
    private View hiddenPanel;
    private Activity activity;
    private HashMap<String, JSONObject> markerInfoList = new HashMap<String, JSONObject>();

    public MapsActivity(final Context context, MapFragment mapFragment, Activity activity) {
        this.context = context;
        this.activity = activity;
        mMap = mapFragment.getMap();
    }


    //Method to add markers on map based on API resposne
    public  void addMapsOnMarker(JSONArray parking_listings , Double lat, Double lng) {
        JSONObject parking_data;
        double latitude = 0;
        double longitude = 0;
        Integer price = 0;
        String price_formatted = " ";
        String location_name = " ";
        String address = " ";
        LatLng latLng = new LatLng(lat,lng);
        String id;
        String city = " ";
        String state = " ";
        String zip = " ";
        String api_url = " ";
        String security = " ";
        String valet = " ";
        String attended = " ";
        String covered = " ";
        String restroom = " ";
        String mobile_pass = " ";
        String distance = " ";
        String spots = " ";
        String likes = " ";

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),R.drawable.my_marker_icon);
        icon = Bitmap.createScaledBitmap(icon, 45, 70, true);

        System.out.println("Parking Listings " +parking_listings);
        System.out.println("Latlng" +latLng);

        for (int i = 0; i < parking_listings.length(); i++) {
            parking_data = new JSONObject();
            try {
                latitude = parking_listings.getJSONObject(i).getDouble("lat");
                address = parking_listings.getJSONObject(i).getString("address");
                longitude =  parking_listings.getJSONObject(i).getDouble("lng");
                price_formatted = parking_listings.getJSONObject(i).getString("price_formatted");
                price = (int)Math.floor(parking_listings.getJSONObject(i).getDouble("price"));
                location_name = parking_listings.getJSONObject(i).getString("location_name");
                city = parking_listings.getJSONObject(i).getString("city");
                state = parking_listings.getJSONObject(i).getString("state");
                zip = parking_listings.getJSONObject(i).getString("zip");
                api_url = parking_listings.getJSONObject(i).getString("api_url");
                distance = parking_listings.getJSONObject(i).getString("distance");
                likes = parking_listings.getJSONObject(i).getString("recommendations");
                spots = parking_listings.getJSONObject(i).getString("available_spots");

                if(parking_listings.getJSONObject(i).getString("security") =="0"){
                    security = "No";
                } else {
                    security = "Yes";
                }

                if(parking_listings.getJSONObject(i).getString("valet") =="0"){
                    valet = "No";
                } else {
                    valet = "Yes";
                }

                if(parking_listings.getJSONObject(i).getString("attended") =="0"){
                    attended = "No";
                } else {
                    attended = "Yes";
                }

                if(parking_listings.getJSONObject(i).getString("indoor") =="0"){
                    covered = "No";
                } else {
                    covered = "Yes";
                }

                if(parking_listings.getJSONObject(i).getString("eticket") =="0"){
                    mobile_pass = "No";
                } else {
                    mobile_pass = "Yes";
                }

                if(parking_listings.getJSONObject(i).getString("restroom") =="0"){
                    restroom = "No";
                } else {
                    restroom = "Yes";
                }
                parking_data.put("lat",latitude);
                parking_data.put("address", address);
                parking_data.put("lng", longitude);
                parking_data.put("price_formatted", price_formatted);
                parking_data.put("price", price);
                parking_data.put("location_name", location_name);
                parking_data.put("city",city);
                parking_data.put("state",state);
                parking_data.put("zip",zip);
                parking_data.put("api_url",api_url);
                parking_data.put("security",security);
                parking_data.put("valet", valet);
                parking_data.put("attended",attended);
                parking_data.put("covered",covered);
                parking_data.put("mobile_pass",mobile_pass);
                parking_data.put("restroom",restroom);
                parking_data.put("likes",likes);
                parking_data.put("spots","No of Available Spots " + spots);
                parking_data.put("distance",distance + " miles away");

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),14.0f));
            id= mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude)).title("Name:" + location_name)
                            .icon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(icon, "$"+price)))).getId();
            markerInfoList.put(id, parking_data);
            mMap.setOnMarkerClickListener(this);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        JSONObject parking_data;
        double latitude = 0;
        double longitude = 0;
        String location_name = " ";
        String address = " ";
        String price_formatted = " ";
        String city = " ";
        String state = " ";
        String zip = " ";
        String api_url = " ";
        String security = " ";
        String valet = " ";
        String attended = " ";
        String covered = " ";
        String restroom = " ";
        String mobile_pass = " ";
        String distance = " ";
        String spots = " ";
        String likes = " ";
        parking_data= markerInfoList.get(marker.getId());
        try{
            api_url = parking_data.getString("api_url");
            latitude = parking_data.getDouble("lat");
            longitude = parking_data.getDouble("lng");
            location_name = parking_data.getString("location_name");
            price_formatted = parking_data.getString("price_formatted");
            address  = parking_data.getString("address");
            city  = parking_data.getString("city");
            state  = parking_data.getString("state");
            zip  = parking_data.getString("zip");
            security =   parking_data.getString("security");
            valet =   parking_data.getString("valet");
            covered =   parking_data.getString("covered");
            mobile_pass =   parking_data.getString("mobile_pass");
            attended = parking_data.getString("attended");
            restroom = parking_data.getString("restroom");
            distance =   parking_data.getString("distance");
            spots = parking_data.getString("spots");
            likes = parking_data.getString("likes");

            //Call to get API results
            GetDetailAPIResults apiResults = new GetDetailAPIResults(this.context,this.activity);
            apiResults.execute(api_url+"&key="+context.getResources().getString(R.string.park_whiz_key));
        }
         catch (JSONException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        }

        //Set Street View Image
        String url= "https://maps.googleapis.com/maps/api/streetview?size=200x200&location=" + latitude + "," + longitude + "&heading=0&pitch=0&key=AIzaSyABnXKSGwNY4LhUExDF48esvU4n9z_Cypc";
        new DownloadImageTask((ImageView) this.activity.findViewById(R.id.streetview))
                .execute(url);

        //Set Location Name
        TextView location_name_field = (TextView) this.activity.findViewById (R.id.locationName);
        location_name_field.setText(location_name);

        //Set Price
        TextView price_formatted_field = (TextView) this.activity.findViewById (R.id.price);
        price_formatted_field.setText(price_formatted);

        //Set address
        TextView address_field = (TextView) this.activity.findViewById (R.id.address);
        address_field.setText(address + " , " + city + " , " + state + " , " +zip);

        //Set security
        TextView security_field = (TextView) this.activity.findViewById (R.id.security_status);
        security_field.setText(security);

        //Set valet
        TextView valet_field = (TextView) this.activity.findViewById (R.id.valet_status);
        valet_field.setText(valet);

        //Set attended
        TextView attended_field = (TextView) this.activity.findViewById (R.id.attended_status);
        attended_field .setText(attended);

        //Set mobile pass
        TextView covered_field = (TextView) this.activity.findViewById (R.id.covered_status);
        covered_field.setText(covered);


        //Set covered
        TextView mobile_pass_field = (TextView) this.activity.findViewById (R.id.mobilepass_status);
        mobile_pass_field.setText(mobile_pass);

        //Set restroom
        TextView restroom_field = (TextView) this.activity.findViewById (R.id.restrooms_status);
        restroom_field.setText(restroom);

        //Set likes
        TextView likes_field = (TextView) this.activity.findViewById (R.id.likes_text);
        likes_field.setText(likes);

        //Set spots
        TextView spots_field = (TextView) this.activity.findViewById (R.id.spots);
        spots_field.setText(spots);


        hiddenPanel = this.activity.findViewById(R.id.hidden_panel);
        slideUpDown(hiddenPanel);
        return  true;
    }

    public void slideUpDown(View panel) {
        if (!isPanelShown()) {
            // Show the panel
            Animation bottomUp = AnimationUtils.loadAnimation(context,
                    R.anim.bottom_up);

            hiddenPanel.startAnimation(bottomUp);
            hiddenPanel.setVisibility(View.VISIBLE);
        }
        else {
            // Hide the Panel
            Animation bottomDown = AnimationUtils.loadAnimation(context,
                    R.anim.bottom_down);

            hiddenPanel.startAnimation(bottomDown);
            hiddenPanel.setVisibility(View.GONE);
        }
    }

    private boolean isPanelShown() {
        return hiddenPanel.getVisibility() == View.VISIBLE;
    }

    private Bitmap writeTextOnDrawable(Bitmap bm, String text) {

        android.graphics.Bitmap.Config bitmapConfig =   bm.getConfig();
        Typeface tf = Typeface.create("Times New Roman", Typeface.BOLD);

        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bm = bm.copy(bitmapConfig, true);

        Paint paint = new Paint();
        paint.setStyle(Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(tf);
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize(convertToPixels(context,18));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);
        Canvas canvas = new Canvas(bm);

        //If the text is bigger than the canvas , reduce the font size
        if(textRect.width() >= (canvas.getWidth() - 1))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(context, 8)); //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2) - 2;
        int yPos = (int) ((canvas.getHeight() / 2) - 10) ;
        canvas.drawText(text, xPos, yPos, paint);
        return  bm;
    }


    public static int convertToPixels(Context context, int nDP)
    {
        final float conversionScale = context.getResources().getDisplayMetrics().density;
        return (int) ((nDP * conversionScale) + 0.5f) ;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
