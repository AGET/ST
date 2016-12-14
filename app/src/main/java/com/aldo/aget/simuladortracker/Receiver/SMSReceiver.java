package com.aldo.aget.simuladortracker.Receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.aldo.aget.simuladortracker.Control.Ext;
import com.aldo.aget.simuladortracker.Control.ManagerDB;
import com.aldo.aget.simuladortracker.Control.SQLHelper;
import com.aldo.aget.simuladortracker.MainActivity;
import com.aldo.aget.simuladortracker.R;
import com.aldo.aget.simuladortracker.Service.ServicioTrack;
import com.aldo.aget.simuladortracker.Ubicacion;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Work on 24/11/16.
 */

public class SMSReceiver extends BroadcastReceiver {
    public static final String TAGLOG = "AGET";

    Context contexto;
    public static Ubicacion ubicacion;

    private static String numero = "";
    private static String mensaje = "";

    Intent intentServicio;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAGLOG, "Deteccion de receptor SMS");

        contexto = context;

        // Obtenemos el contenido del mensaje SMS
        Bundle bundle = intent.getExtras();
        SmsMessage[] mensjs = null;
        String str = "";
        // Si el mensaje contiene algo
        if (bundle != null) {
            // Obtenemos la información del SMS recibido. El campo PDU significa “Protocol Description Unit”
            // y es el formato estándar de los mensajes cortos SMS
            Object[] pdus = (Object[]) bundle.get("pdus");
            // Pasamos todos los mensajes recibidos en formato pdu a una matriz del tipo SmsMessage que contendrá
            // los mensajes en un formato interno y accesible por Android.
            // Definimos el tamaño de la matriz con el no de mensaje recibidos
            mensjs = new SmsMessage[pdus.length];
            // Recorremos todos los mensajes recibidos
            for (int i = 0; i < mensjs.length; i++) {
                // Pasamos el elemento i a la matriz de mensajes
                mensjs[i] =
                        SmsMessage.createFromPdu((byte[]) pdus[i]);
                numero = mensjs[i].getOriginatingAddress();
                // Guardamos en texto plano el mensaje
                str += "SMS de" + numero;

                mensaje = mensjs[i].getMessageBody().toString();
                str += ".Mensaje:";
                str += mensaje;
                str += "\n";
            } // end for
            // Mostramos un mensaje indicando que se ha recibido el mensaje
            Log.d(TAGLOG, "info:" + str + "  tamanio: " + mensaje.length());
            if (mensaje.length() < 23) {
                String posibles = mensaje.substring(0, 3).toUpperCase();
                Log.v(TAGLOG, posibles);
                switch (posibles) {

                    case "BEG":
                    case "ADM":
                    case "NOA":
                    case "FIX":
                    case "NOF":
                        parse();
                        break;

                }
            }
        } // end if bundle != null
    }

    private void parse() {

        intentServicio = new Intent(contexto, ServicioTrack.class);

        numero = numero.replaceAll("\\s", "");
        if (numero.substring(0, 1).equalsIgnoreCase("+")) {
            numero = numero.substring(3);
        }

        ManagerDB db = new ManagerDB(contexto);

        final String COMMAND_RESTART = "begin123456";
        final String COMMAND_ADD_ADMIN = "admin123456";
        final String COMMAND_REMOV_ADMIN = "noadmin123456";
        final String COMMAND_REM_AUTO = "nofix";
        final String COMMAND_ADD_AUTO = "fix";

        int numberuser = 0;
        boolean restriccion = false;

        numberuser = db.numberUser(SQLHelper.TABLA_USUARIOS, new String[]{SQLHelper.COLUMNA_NUMERO});

        //Eliminamos espacios, abiladore y retornos
        mensaje = mensaje.replaceAll("\\s", "");
        try {

            if (mensaje.length() == 11 && mensaje.equalsIgnoreCase(COMMAND_RESTART)) {

                begin(db);

            } else if (numberuser == 0) {

                if (mensaje.length() == 5 && mensaje.substring(0, 5).equalsIgnoreCase(COMMAND_REM_AUTO)) {

                    removeAutoTack(db);

                } else if (mensaje.length() == 21 && mensaje.substring(0, 11).equalsIgnoreCase(COMMAND_ADD_ADMIN)) {

                    addAdmin(db);

                } else if (mensaje.length() == 17 && mensaje.substring(0, 3).equalsIgnoreCase(COMMAND_ADD_AUTO)) {
//
                    if (cumpleSMSAutoTrack()) {
                        addAuto(db);
                    } else {
                        Toast.makeText(contexto, "No cumple, debe ser mayor a 30s. y menor a 60s/m/h", Toast.LENGTH_SHORT).show();
                        Log.v(TAGLOG, "No cumple, debe ser mayor a 30s. y menor a 60s/m/h");
                    }

                } else {
                    Toast.makeText(contexto, "A punto de coincidir con las instruciones de Tracker", Toast.LENGTH_SHORT).show();
                }

            } else if (numberuser > 0) {

                if (db.existe(numero)) {

                    if (mensaje.length() == 21 && mensaje.substring(0, 11).equalsIgnoreCase(COMMAND_ADD_ADMIN)) {
                        if (numberuser < 6) {

                            addAdmin(db);

                        } else {
                            Toast.makeText(contexto, "Cantidad de usuarios superada", Toast.LENGTH_SHORT).show();
                        }

                    } else if (mensaje.length() == 23 && mensaje.substring(0, 13).equalsIgnoreCase(COMMAND_REMOV_ADMIN)) {

                        removeAdmin(db);

                    } else if (mensaje.length() == 17 && mensaje.substring(0, 3).equalsIgnoreCase(COMMAND_ADD_AUTO)) {

                        if (cumpleSMSAutoTrack()) {
                            addAuto(db);
                        } else {
                            Toast.makeText(contexto, "No cumple, debe ser mayor a 30s. y menor a 60s/m/h", Toast.LENGTH_SHORT).show();
                            Log.v(TAGLOG, "No cumple, debe ser mayor a 30s. y menor a 60s/m/h");
                        }

                    } else if (mensaje.trim().substring(0, 5).equalsIgnoreCase("nofix")) {

                        removeAutoTack(db);
                    }
                } else {
                    Log.d(Ext.TAGLOG, "denied");
                }

            }

        } catch (StringIndexOutOfBoundsException e) {
            Toast.makeText(contexto, "Error: " + e.getMessage() + "\nRebice el comando", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean cumpleSMSAutoTrack() {
        //fix002n222n123456
        boolean res = false;
        int timeAux = Integer.parseInt(mensaje.substring(3, 6));
        char tipoTimeAux = mensaje.toUpperCase().charAt(6);
        Log.d(TAGLOG, "time: " + timeAux + "  tipo: " + tipoTimeAux);
        if (timeAux < 60) {
            switch (tipoTimeAux) {
                case 'S':
                    if (timeAux > 30)
                        res = true;
                    break;
                case 'M':
                case 'H':
                    res = true;
                    break;
                default:
                    break;
            }
        } else
            res = false;
        return res;
    }

    private void addAuto(ManagerDB db) {

        long milisegundos = 0;

        String[] dataAutoTrack = obtenerDatosAutotrack(mensaje);

        if (dataAutoTrack != null) {

            int numeroDeUbicacion = Integer.parseInt(dataAutoTrack[2]);
            int tipoTiempo = 0;

            switch (dataAutoTrack[1]) {
                case "S":
                    tipoTiempo = Integer.parseInt(dataAutoTrack[0]);
                    milisegundos = (tipoTiempo / numeroDeUbicacion) * 1000;
                    break;
                case "M":
                    tipoTiempo = Integer.parseInt(dataAutoTrack[0]);
                    milisegundos = ((tipoTiempo * 60) / numeroDeUbicacion) * 1000;
                    break;
                case "H":
                    tipoTiempo = Integer.parseInt(dataAutoTrack[0]);
                    milisegundos = ((tipoTiempo * 3600) / numeroDeUbicacion) * 1000;
                    break;
            }

//                        Intent intent = new Intent(contexto, ServicioTrack.class);
            intentServicio.putExtra(Ext.NUMERO, numero);
            intentServicio.putExtra(Ext.AUTOMATICO, true);
            intentServicio.putExtra(Ext.MILISEGUNDOS, milisegundos);

            if (!db.autotrakEstablecido()) {
                //se comprueba que se ahia inseado
                if (db.insercionMultiple(SQLHelper.TABLA_AUTOTRACK, new String[]{SQLHelper.COLUMNA_COMANDO,
                        SQLHelper.COLUMNA_NUMERO}, new String[]{mensaje, numero}) != -1) {
                    //se establecio el comando en la base de datos y se ejecuta el servicio


                    if (ServicioTrack.isInstanceCreated()) {
                        contexto.stopService(intentServicio);
                    }
                    contexto.startService(intentServicio);
                }
            } else {
                //se comprueba que se ahia eliminado
                if (db.eliminarTodo(SQLHelper.TABLA_AUTOTRACK) == 1) {
                    //se comprueba que se ahia inseado
                    if (db.insercionMultiple(SQLHelper.TABLA_AUTOTRACK, new String[]{SQLHelper.COLUMNA_COMANDO,
                            SQLHelper.COLUMNA_NUMERO}, new String[]{mensaje, numero}) != -1) {

                        //se establecio el comando en la base de datos y se ejecuta el servicio
                        if (ServicioTrack.isInstanceCreated()) {
                            contexto.stopService(intentServicio);
                        }
                        contexto.startService(intentServicio);


                    } else {
                        Toast.makeText(contexto, "Problema al insertar", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(contexto, "Problema a eliminar", Toast.LENGTH_SHORT).show();
                }

            }

        }
    }

    private void addAdmin(ManagerDB db) {

        String numeroAgregar = mensaje.substring(11);
        db.insertar(SQLHelper.TABLA_USUARIOS, SQLHelper.COLUMNA_USUARIO_NUMERO, numeroAgregar);

        getNotification("Nuevo usuario", "Hay un nuevo usuario", "",
                "", "El numero: " + numero + "ha agregado un nuevo usuario al servicio de ubicaciones.\n" +
                        "Abra la aplicacion para observarlo", R.drawable.ic_person_add_white);
    }

    private void removeAdmin(ManagerDB db) {
        numero = mensaje.substring(13);
        db.eliminarItem(SQLHelper.TABLA_USUARIOS, SQLHelper.COLUMNA_USUARIO_NUMERO, numero);
    }

    private void removeAutoTack(ManagerDB db) {

        db.eliminarTodo(SQLHelper.TABLA_AUTOTRACK);
        Log.d(TAGLOG, "se elimino");

        if (ServicioTrack.isInstanceCreated()) {
            contexto.stopService(intentServicio);
        }

//        getNotification("Se detuvo la autoubicacion", "Se ha detenido la autoubucacion", "el servicio ha sido detenido",
//                "Comando enviado del numero: " + numero, "", R.drawable.ic_location_off_blue);

    }

    private void begin(ManagerDB db) {

//        getNotification("Se restauro el GPS", "El GPS a sido restaurado", "El numero: " + numero + "ha enviado el comando begin",
//                        "", "", R.drawable.ic_reset);
        db.eliminarTodo(SQLHelper.TABLA_USUARIOS);
        db.eliminarTodo(SQLHelper.TABLA_AUTOTRACK);
    }

    private String[] obtenerDatosAutotrack(String msn) {

        String[] datosAutoTrack = new String[5];
        try {
            msn = msn.toUpperCase();
            datosAutoTrack[0] = msn.substring(3, 6);//tiempo
            datosAutoTrack[1] = msn.substring(6, 7);//tipo
            datosAutoTrack[2] = msn.substring(7, 10);//cantidad
            datosAutoTrack[3] = msn.substring(10, 11);//corroboracion
            datosAutoTrack[4] = msn.substring(11);//clave
//            if (Integer.parseInt(datosAutoTrack[0]) < 030 && datosAutoTrack[1].equalsIgnoreCase("s"))
//                return null;
        } catch (Exception e) {
            Toast.makeText(contexto, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return datosAutoTrack;
    }

    private void getNotification(String tiker, String contentTitle, String contentText, String subText,
                                 String bigText, int icono) {
//        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri defaultSound = RingtoneManager.getDefaultUri(R.raw.sound);
        long[] pattern = new long[]{1000, 500, 1000};
        NotificationManager nm = (NotificationManager) contexto.getSystemService(NOTIFICATION_SERVICE);
        ;
        final int ID_NOTIFICACION_CREAR = 1;
        Notification.Builder builder = new Notification.Builder(contexto);
        builder.setAutoCancel(false);
        builder.setTicker(tiker);
        builder.setContentTitle(contentTitle);
        builder.setContentText(contentText);
        builder.setSubText(subText);
        builder.setStyle(new Notification.BigTextStyle()
                .bigText(bigText));
        builder.setSmallIcon(icono);
        builder.setAutoCancel(true);
        builder.setSound(defaultSound);        // Uso en API 11 o mayor
        builder.setVibrate(pattern);
        nm.notify(ID_NOTIFICACION_CREAR, builder.build());
    }
}