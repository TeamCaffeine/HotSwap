package com.teamcaffeine.hotswap.utility;

import android.location.Location;
import android.util.Log;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

import java.io.IOException;

public class LatLongUtility {

    private static String TAG = "LatLongUtility";
    public static LatLng getLatLongForAddress(String address, String key) {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(key)
                .build();
        GeocodingResult[] results = new GeocodingResult[0];

        try {
            results = GeocodingApi.geocode(context, address).await();
        } catch (ApiException | InterruptedException | IOException e) {
            Log.e(TAG, "Error getting lat long for address: " + address, e);
            return null;
        }

        return results[0].geometry.location;
    }

    public static float getDistanceToAddress(String address, Location myLocation, String key) {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(key)
                .build();
        GeocodingResult[] results = new GeocodingResult[0];

        try {
            results = GeocodingApi.geocode(context, address).await();
        } catch (ApiException | InterruptedException | IOException e) {
            Log.e(TAG, "Error getting lat long for address: " + address, e);
            return Float.parseFloat(null);
        }

        Location dest = new Location("dest");
        dest.setLatitude(results[0].geometry.location.lat);
        dest.setLongitude(results[0].geometry.location.lng);
        return myLocation.distanceTo(dest);
    }
}
