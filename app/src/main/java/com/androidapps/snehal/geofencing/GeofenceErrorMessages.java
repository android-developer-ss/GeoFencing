package com.androidapps.snehal.geofencing;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.location.GeofenceStatusCodes;

/**
 * Created by sneha on 11/19/2015.
 */
public class GeofenceErrorMessages {
    public static String getErrorString(Context context, int errorCode){
        Resources mResources = context.getResources();
        switch (errorCode){
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "Geo fence service is not available now";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return  "Your app has registered too many geofences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "You have provided too many Pending Intents to the addGeo..";
            default:
                return "Unknown error: the Geofence service is not available now";
        }
    }
}
