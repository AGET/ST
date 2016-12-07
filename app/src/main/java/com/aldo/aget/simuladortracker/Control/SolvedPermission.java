package com.aldo.aget.simuladortracker.Control;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import com.aldo.aget.simuladortracker.Ubicacion;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Work on 07/12/16.
 */

public class SolvedPermission extends Activity {
    private static final int PETICION_PERMISO_LOCALIZACION = 101;
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PETICION_PERMISO_LOCALIZACION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Permiso concedido
//                @SuppressWarnings("MissingPermission")
//                Location lastLocation =
//                        LocationServices.FusedLocationApi.getLastLocation(Ubicacion.apiClient);

            } else {
                //Permiso denegado:
                //Deberíamos deshabilitar toda la funcionalidad relativa a la localización.
                Log.e("AGET", "Permiso denegado");
            }
        }
    }
}
