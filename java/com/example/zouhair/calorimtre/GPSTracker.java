package com.example.zouhair.calorimtre;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


@SuppressLint("Registered")
public class GPSTracker extends Service implements LocationListener {

    private final Context mContext;

    //public static final Locale fr_FR;


    // Statut GPS
    boolean isGPSEnabled = false;

    // Statut Réseau
    boolean isNetworkEnabled = false;

    // Statut Localisation
    boolean canGetLocation = false;

    Location location; // localisation
    double latitude; // latitude
    double longitude; // longitude
    double altitude; //altitude
    float accu;

    // Distance minimum pour la maj en mètres
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // Temps minimum entre 2 maj en millisecondes
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Déclaration du locationManager
    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();
    }
    public boolean checkLocationPermission(){
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = mContext.checkCallingOrSelfPermission(permission);
        return (res==PackageManager.PERMISSION_GRANTED);
    }
    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            // récupération du statut GPS
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // récupération du statut réseau
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // pas de GPS ni de réseau !
            }
            else {
                this.canGetLocation = true;
                // Premièrement récupérer par le réseau
                if (isNetworkEnabled) {
                    if (!checkLocationPermission()){
                        ActivityCompat.requestPermissions((Activity)mContext,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},10);
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            //MINIMUN_TIME_BETWEEN_UPDATES, locationListener);
                            MIN_TIME_BW_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {

                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            altitude = location.getAltitude();
                        }
                    }
                }
                // Si le GPS est ok, récupérer lat/long grâce au GPS
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        //MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                altitude = location.getAltitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    /**
     * Arrêt du GPS listener
     * L'appel de cette fonction arrête l'utilisation du GPS dans l'appli
     * */
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Fonction pour récupérer la latitude
     * */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Fonction pour récupérer la longitude
     * */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }


    /**
     * Fonction pour vérifier le GPS ou le réseau Wifi
     * @return boolean
     * */
    public boolean canGetLocation() {

        return this.canGetLocation;

    }


    //Renvoie la distance en mètres entre la position de l'utilisateur et la target
    public float getDistance(double LatTarget, double LongTarget)
    {
        float distance=0;
        Location locationA = new Location("point A");

        locationA.setLatitude(location.getLatitude());
        locationA.setLongitude(location.getLongitude());

        Location locationB = new Location("point B");

        locationB.setLatitude(LatTarget);
        locationB.setLongitude(LongTarget);

        return distance = locationA.distanceTo(locationB);
    }


    @Nullable

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
