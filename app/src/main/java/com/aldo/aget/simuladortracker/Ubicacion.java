package com.aldo.aget.simuladortracker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.aldo.aget.simuladortracker.Control.Ext;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class Ubicacion implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private static final String LOGTAG = "AGET-Localizacion";

    private static final int PETICION_PERMISO_LOCALIZACION = 101;
    private static final int PETICION_CONFIG_UBICACION = 201;

    private GoogleApiClient apiClient;
    Context ctx;
    String telefono;
    Boolean autotrack= false;
    long miliSegundos = 0;

    private TextView lblLatitud;
    private TextView lblLongitud;

    private LocationRequest locRequest;

    public Ubicacion(Context contexto, String numero, boolean auto, long milSegundos) {
        Log.d(Ext.TAGLOG, "Constructor");
        ctx = contexto;
        telefono = numero;
        autotrack = auto;
        miliSegundos = milSegundos;

        //Construcción cliente API Google
        if (apiClient == null) {
            apiClient = new GoogleApiClient.Builder(ctx)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
            onStart();
        }
    }

    protected void onStart() {
        apiClient.connect();
    }

    public void enableLocationUpdates() {

        locRequest = new LocationRequest();
        locRequest.setInterval(miliSegundos);
        locRequest.setFastestInterval(miliSegundos+100);
        locRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest locSettingsRequest =
                new LocationSettingsRequest.Builder()
                        .addLocationRequest(locRequest)
                        .build();

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        apiClient, locSettingsRequest);
        Log.d(LOGTAG, "enableLocationUpdates");

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:

                        Log.d(LOGTAG, "Configuración correcta");
                        startLocationUpdates();

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                        try {
                        Log.d(LOGTAG, "Se requiere actuación del usuario");
//                            status.startResolutionForResult(new Activity(), PETICION_CONFIG_UBICACION);
//                            status.startResolutionForResult(Ubicacion.this, PETICION_CONFIG_UBICACION);
                        Toast.makeText(ctx, "Debe de encender el GPS", Toast.LENGTH_SHORT).show();
//                        } catch (IntentSender.SendIntentException e) {
                        Log.i(LOGTAG, "Error al intentar solucionar configuración de ubicación");
//                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.d(LOGTAG, "No se puede cumplir la configuración de ubicación necesaria");

                        break;
                }
            }
        });
    }


    private void disableLocationUpdates() {

        LocationServices.FusedLocationApi.removeLocationUpdates(
                apiClient, this);
        apiClient.disconnect();
    }


    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(ctx,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (apiClient.isConnected()) {
                Log.d(LOGTAG, "Inicio de recepción de ubicaciones");
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        apiClient, locRequest, this);
            } else {
                Log.d(LOGTAG, "No se ha conectado el api");
                apiClient.connect();
                Log.d(LOGTAG, "Segundo intento");

                if (apiClient.isConnected()) {
                    Log.d(LOGTAG, "Inicio de recepción de ubicaciones");
                    LocationServices.FusedLocationApi.requestLocationUpdates(
                            apiClient, locRequest, this);
                } else {
                    Log.d(LOGTAG, "No se ha conectado el api");
                }
            }
        } else {
            Log.d(LOGTAG, "No estan los permisos establecidos ponga en el manifieto los permisos: ACCESS_FINE_LOCATION");
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        //Se ha producido un error que no se puede resolver automáticamente
        //y la conexión con los Google Play Services no se ha establecido.

        Log.e(LOGTAG, "Error grave al conectar con Google Play Services");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Conectado correctamente a Google Play Services

        if (ActivityCompat.checkSelfPermission(ctx,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(ctx,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    PETICION_PERMISO_LOCALIZACION);
        } else {
            Location lastLocation =
                    LocationServices.FusedLocationApi.getLastLocation(apiClient);
            updateUI(lastLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Se ha interrumpido la conexión con Google Play Services
        Log.e(LOGTAG, "Se ha interrumpido la conexión con Google Play Services");
    }

    private void updateUI(Location loc) {
        if (loc != null) {
            String latitud = String.valueOf(loc.getLatitude());
            String longitud = String.valueOf(loc.getLongitude());
            SmsManager msms = SmsManager.getDefault();
            String msn = "lat:"+latitud+"\nlon:"+longitud+"\nspeedt:0.00"+"\nbat:100%"+"\nhttp://maps.google.com/maps?f=q&q="+latitud+","+longitud;
            msms.sendTextMessage(telefono, null, msn, null, null);
            Toast.makeText(ctx, "Latitud: " + latitud, Toast.LENGTH_SHORT).show();
            Toast.makeText(ctx, "Longitud: " + longitud, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ctx, "Latitud: (desconocida)", Toast.LENGTH_SHORT).show();
            Toast.makeText(ctx, "Longitud: (desconocida)", Toast.LENGTH_SHORT).show();
        }
        if(!autotrack){
            disableLocationUpdates();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOGTAG, "Recibida nueva ubicación!");
        //Mostramos la nueva ubicación recibida
        updateUI(location);
    }
}