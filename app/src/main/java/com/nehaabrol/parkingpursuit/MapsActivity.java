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
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by neha_.abrol on 11/7/15.
 */
public class MapsActivity  implements LocationListener {

    private GoogleMap mMap;
    private Location location;
    private Context context;

    public MapsActivity(Context context,MapFragment mapFragment) {
        this.context = context;

        System.out.println(mapFragment+"Maps");
        mMap = mapFragment.getMap();
        mMap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //Check permissions (Required for API>23
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            //TODO  Ask User for permissions
        }

        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0, this);

        if(provider!=null && !provider.equals("")){
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(location!=null)
                onLocationChanged(location);
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
        mMap.addMarker(new MarkerOptions().position(latLng).title("Current Location:" + address));
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
}
