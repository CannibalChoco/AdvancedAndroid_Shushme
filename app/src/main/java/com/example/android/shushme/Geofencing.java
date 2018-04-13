package com.example.android.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class Geofencing {

    private static final String LOG_TAG = Geofencing.class.getSimpleName();

    private static final long DAY = 24 * 60 * 60 * 1000;
    private static final int GEOFENCE_RADIUS = 50;

    private Context context;
    private List<Geofence> geofenceList;
    private GoogleApiClient client;
    private PendingIntent geofencePendingIntent;

    public Geofencing(Context context, GoogleApiClient client) {
        this.context = context;
        this.client = client;
        geofencePendingIntent = null;
        geofenceList = new ArrayList<>();
    }

    public void registerAllGeofences() {
        if (client == null || !client.isConnected() || geofenceList == null ||
                geofenceList.size() == 0) {
            return;
        }

        try {
            LocationServices.getGeofencingClient(context).addGeofences(
                    getGeofencingRequest(),
                    getGeofencePendingIntent())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.v(LOG_TAG, "Success adding geofences!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.v(LOG_TAG, "Failed adding geofences!");
                        }
                    });
        } catch (SecurityException e) {
            Log.v(LOG_TAG, e.getMessage());
        }
    }

    public void unregisterAllGeofences() {
        if (client == null || !client.isConnected()) {
            return;
        }

        try {
            LocationServices.getGeofencingClient(context).removeGeofences(getGeofencePendingIntent())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.v(LOG_TAG, "Success removing geofences!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.v(LOG_TAG, "Failed removing geofences!");
                        }
                    });
        } catch (SecurityException e) {
            Log.v(LOG_TAG, e.getMessage());
        }
    }

    public void updateGeofencesList(PlaceBuffer placeBuffer) {

        if (placeBuffer == null || placeBuffer.getCount() == 0) return;

        for (Place place : placeBuffer) {
            String placeId = place.getId();
            Double lat = place.getLatLng().latitude;
            Double lng = place.getLatLng().longitude;

            Geofence geofence = new Geofence.Builder()
                    .setRequestId(placeId)
                    .setExpirationDuration(DAY)
                    .setCircularRegion(lat, lng, GEOFENCE_RADIUS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();

            geofenceList.add(geofence);
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofenceList)
                .build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }

        Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
