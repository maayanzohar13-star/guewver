package com.example.ideaformaayanzohar.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationHelper {

    public interface OnLocationResultListener {
        void onLocationReceived(double latitude, double longitude);
        void onError(String errorMsg);
    }

    private final FusedLocationProviderClient fusedLocationClient;
    private final Context context;

    public LocationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.context);
    }

    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void getCurrentLocation(OnLocationResultListener listener) {
        if (!hasLocationPermission()) {
            if (listener != null) listener.onError("אין הרשאת מיקום");
            return;
        }

        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null && listener != null) {
                    listener.onLocationReceived(location.getLatitude(), location.getLongitude());
                } else if (listener != null) {
                    // Fallback default (תל אביב / מרכז)
                    listener.onLocationReceived(32.0853, 34.7818);
                }
            }).addOnFailureListener(e -> {
                if (listener != null) {
                    listener.onLocationReceived(32.0853, 34.7818);
                }
            });
        } catch (SecurityException e) {
            if (listener != null) listener.onError(e.getMessage());
        }
    }

    /**
     * מחשב מרחק במטרים בין שני מיקומים
     */
    public static float calculateDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }
}
