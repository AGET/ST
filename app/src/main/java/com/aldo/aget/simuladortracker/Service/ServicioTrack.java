package com.aldo.aget.simuladortracker.Service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.aldo.aget.simuladortracker.Control.Ext;
import com.aldo.aget.simuladortracker.MainActivity;
import com.aldo.aget.simuladortracker.R;
import com.aldo.aget.simuladortracker.Ubicacion;

/**
 * Created by Work on 05/12/16.
 */

public class ServicioTrack extends Service {

    private NotificationManager nm;
    public static final int ID_NOTIFICACION_CREAR = 1;
    Notification notificacion;
    Ubicacion ubicacion;

    String numero = "";
    Boolean auto = false;
    long milisegundos = 0;
    int IDService = 0;

    private static ServicioTrack instance = null;

    public static boolean isInstanceCreated() {
        return instance != null;
    }

    public static ServicioTrack instance() {
        return instance ;
    }





    public ServicioTrack(){
    }

    public void onCreate(){
        instance = this;
        Log.v(Ext.TAGLOG,"Servicio creado");
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    public int onStartCommand(Intent intent, int flags, int idArranque){
        Log.v(Ext.TAGLOG,"**Servicio Arrancado: " +idArranque);
        this.IDService = idArranque;

        Bundle extras = intent.getExtras();
        if(extras != null){
            numero = (String) extras.getString(Ext.NUMERO);
            auto = (Boolean) extras.getBoolean(Ext.AUTOMATICO);
            milisegundos = (long) extras.getLong(Ext.MILISEGUNDOS);
        }

        Log.v(Ext.TAGLOG,"Datos recividos:  numero "+numero+"  auto "+auto + "miliseg." + milisegundos);

        // Sonido por defecto de notificaciones, podemos usar otro
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

//        Vibracion
        long[] pattern = new long[]{1000,500,1000};

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, new Intent(this,MainActivity.class), 0);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setAutoCancel(false);
        builder.setTicker( "Creando servicio de Ubicacion");
        builder.setContentTitle("Autotrack activo");
        builder.setContentText("El autotrack es un servicio de ubicaciones");
        builder.setSmallIcon(R.drawable.ic_location_on_blue);
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
        builder.setSubText("");
        builder.setSound(defaultSound);        // Uso en API 11 o mayor
        builder.setVibrate(pattern);
        builder.setStyle(new Notification.BigTextStyle()
                .bigText("Este servicioenviara cada: "+ milisegundos/1000 +
                        " segundos una ubicacion. \nPara desactivarlo envie el mensage: \nnofix " +
                        "desde cualquier disitivo enlazado"));

        nm.notify(ID_NOTIFICACION_CREAR, builder.build());

        ubicacion = new Ubicacion(this, numero, auto, milisegundos);

//        reproductor.start();
//        reproductor.setAudioStreamType(AudioManager.STREAM_MUSIC);

        return START_REDELIVER_INTENT;
    }

    public void onDestroy(){
        Log.v(Ext.TAGLOG,"**Servicio Detetenido: " + IDService);

        nm.cancel(ID_NOTIFICACION_CREAR);

        ubicacion.disableLocationUpdates();

        instance = null;

        Toast.makeText(this, "Servicio Detenido: " + IDService, Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
