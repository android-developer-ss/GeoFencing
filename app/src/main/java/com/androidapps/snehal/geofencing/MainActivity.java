package com.androidapps.snehal.geofencing;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status> {

    //----------------------------------------------------------------------------------------------
    // Member variable Declarations
    protected GoogleApiClient mGoogleApiClient;
    protected static final String TAG = "activity";
    ArrayList<Geofence> mGeofenceList = new ArrayList<>();
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        poulateGeofenceList();
        buildGoogleApiClient();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        //Unregister the broadcast receiver that was registeres during onResume
        // LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.BROADCAST_ACTION));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /***********************************************************************************************
     * Build Google API Client
     **********************************************************************************************/
    protected synchronized void buildGoogleApiClient() {
        // GoogleApiClient.Builder addApi - Specify which Apis are requested by your app.
        // GoogleApiClient.Builder addConnectionCallbacks- Registers a listener to receive connection
        // events from this GoogleApiClient.
        // GoogleApiClient.Builder addOnConnectionFailedListener - Adds a listener to register to
        // receive connection failed events from this GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    /***********************************************************************************************
     * Populate Geofence List
     **********************************************************************************************/
    public void poulateGeofenceList(){
        for(HashMap.Entry<String,LatLng> entry :Constants.MY_LANDMARKS.entrySet()){
            mGeofenceList.add(new Geofence.Builder()
                            .setRequestId(entry.getKey())
                            .setCircularRegion(entry.getValue().latitude,
                                    entry.getValue().longitude,
                                    Constants.GEOFENCE_RADIUS_IN_METERS)
                            .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                            .setLoiteringDelay(1000)
                            .build()
            );
        }
    }

    /***********************************************************************************************
     * Get Geofencing Request
     **********************************************************************************************/
    private GeofencingRequest getGeofencingRequest(){
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return  builder.build();
    }

    /***********************************************************************************************
     * Pending Intent
     **********************************************************************************************/
    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeoFences()
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /***********************************************************************************************
     * Methods to be implemented for GoogleApiClient.ConnectionCallbacks Interface.
     * Provides callbacks that are called when the client is connected or disconnected from the
     * service. Most applications implement onConnected(Bundle) to start making requests.
     **********************************************************************************************/
    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();

    }

    /***********************************************************************************************
     * Methods to be implemented for GoogleApiClient.OnConnectionFailedListener Interface.
     * Provides callbacks for scenarios that result in a failed attempt to connect the client to the
     * service.
     * A ConnectionResult that can be used for resolving the error, and deciding what sort of error
     * occurred. To resolve the error, the resolution must be started from an activity with a
     * non-negative requestCode passed to startResolutionForResult(Activity, int). Applications
     * should implement onActivityResult in their Activity to call connect() again if the user has
     * resolved the issue (resultCode is RESULT_OK).
     **********************************************************************************************/
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());

    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            Toast.makeText(this,
                    "Geofences added",
                    Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Successfully added activity detection.");

        } else {
            String errorMessage = GeofenceErrorMessages.getErrorString(this, status.getStatusCode());
            Log.e(TAG, "Error adding or removing activity detection: " + status.getStatusMessage());
        }
    }

    /***********************************************************************************************
     * Add Geofence Button handler
     **********************************************************************************************/
    public void addGeofencesButtonHandler(View view) {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
        }
    }
}
