package com.nehaabrol.parkingpursuit;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
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
import java.io.ByteArrayOutputStream;


/**
 * Created by neha_.abrol on 11/7/15.
 */
public class MapsActivity implements LocationListener {

    private static GoogleMap  mMap;
    private Location location;
    private Context context;
    private MapFragment mapFragment;
    private  LocationManager locationManager;

    public MapsActivity(Context context,MapFragment mapFragment) {
        this.context = context;

        mMap = mapFragment.getMap();
        mMap.setMyLocationEnabled(true);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //Check permissions (Required for API>23
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            //TODO  Ask User for permissions
        }

        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0, this);

        if(provider!=null && !provider.equals("")){
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(location!=null) {
                onLocationChanged(location);
                locationManager.removeUpdates(this);
            }
            else
                Toast.makeText(context, "Location can't be retrieved", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "No Provider Found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location)  {
        String address = " ";
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        if(Geocoder.isPresent()){
            Geocoder gcd = new Geocoder(context , Locale.getDefault());
            try {
                List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1);
                address = addresses.get(0).getAddressLine(0);
            } catch (IOException e) { e.printStackTrace(); }
        }
        mMap.addMarker(new MarkerOptions()
                .position(latLng).title("Current Location:" + address));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
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

    //Method to add markers on map based on API resposne
    public  void addMapsOnMarker(JSONArray parking_listings , Double lat, Double lng) {
        double latitude = 0;
        double longitude = 0;
        String price = " ";
        LatLng latLng = new LatLng(lat,lng);

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.my_marker_icon);
        icon = Bitmap.createScaledBitmap(icon, 45, 70, true);

        for (int i = 0; i < parking_listings.length(); i++) {

            try{
                price = " ";
                System.out.println("Listing" + parking_listings.getJSONObject(i));
                latitude = parking_listings.getJSONObject(i).getDouble("lat");
                longitude = parking_listings.getJSONObject(i).getDouble("lng");
                price = parking_listings.getJSONObject(i).getString("price_formatted");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            mMap.addMarker(new MarkerOptions()
                   .position(new LatLng(latitude,longitude))
                   .icon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(icon,price)))
                   );
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }
      //System.out.println(parking_listings);
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
}
