package com.androidapps.snehal.geofencing;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sneha on 11/19/2015.
 */
public class GeofenceTransitionsIntentService extends IntentService {
    protected static final String TAG = "gfservice";
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * //@param name Used to name the worker thread, important only for debugging.
     */
    public GeofenceTransitionsIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if(geofencingEvent.hasError()){
            String errorMessage = GeofenceErrorMessages.getErrorString(this, geofencingEvent.getErrorCode());
            Log.e(TAG,errorMessage);
            return;
        }
        //Get the transition type
        int geofenceTransition =  geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if((geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER )||
                (geofenceTransition== Geofence.GEOFENCE_TRANSITION_EXIT) ||
                (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL)){

            //Get the geofences that were triggered. A single event can trigger multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            //Get the transition details as a String
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            //Send notification and log the transition etials
            sendNotification(geofenceTransitionDetails);
            Log.i(TAG,getString(R.string.geofence_transition_invalid_type,geofenceTransition));
        }
    }

    public String getGeofenceTransitionDetails( Context context, int geofenceTransition, List<Geofence> triggeringGeofences){
        String geofenceTransitionString = getTransitionString(geofenceTransition);

        //get the Ids of each geofence that was triggered
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for(Geofence geofence: triggeringGeofences){
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(",", triggeringGeofencesIdsList);
        return geofenceTransitionString + ":" + triggeringGeofencesIdsString;
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return getString(R.string.geofence_transition_dwelling);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void sendNotification(String notificationDetails){
        //Create an explicit Intent that starts the main Activity
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        //Construct a task stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        //Add the main Activity to the task stack as the parent
        stackBuilder.addParentStack(MainActivity.class);

        //Push the content Intent on to the stack
        stackBuilder.addNextIntent(notificationIntent);

        //Get a Pending Intent containing the entire back stack
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

        //Get a notification builder thats compatible with platform version >=4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        //Define the notification setting.
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText("Click notification to return to the app")
                .setContentIntent(notificationPendingIntent);

        //Dismiss notification once the user touches it
        builder.setAutoCancel(true);

        //Get an instance of the notification Manager
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }
}
