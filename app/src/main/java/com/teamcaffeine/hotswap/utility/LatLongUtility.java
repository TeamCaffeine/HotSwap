package com.teamcaffeine.hotswap.utility;

import android.util.Log;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

import java.io.IOException;

public class LatLongUtility {

    private static String TAG = "LatLongUtility";
    private static String KEY = "AIzaSyCdD6V_pMev1dl8LAsoJ6PLG5JLnR-OiUc";
    public static LatLng getLatLongForAddress(String address) {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(KEY)
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
}
