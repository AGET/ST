package com.aldo.aget.simuladortracker.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.aldo.aget.simuladortracker.Control.Ext;
import com.aldo.aget.simuladortracker.Control.ManagerDB;
import com.aldo.aget.simuladortracker.Control.SQLHelper;
import com.aldo.aget.simuladortracker.MainActivity;
import com.aldo.aget.simuladortracker.Service.ServicioTrack;
import com.aldo.aget.simuladortracker.Ubicacion;

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
            // Obtenemos la información del SMS recibido.
            // El campo PDU significa “Protocol Description Unit”
            // y es el formato estándar de los mensajes cortos
            // SMS
            Object[] pdus = (Object[]) bundle.get("pdus");
            // Pasamos todos los mensajes recibidos en formato
            // pdu a una matriz del tipo SmsMessage que contendrá
            // los mensajes en un formato interno y accesible por
            // Android.
            // Definimos el tamaño de la matriz con el no de
            // mensaje recibidos
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
            // Mostramos un mensaje indicando que se ha recibido
            // el mensaje
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
            Log.d(TAGLOG, "info:" + str);

            parse();
        } // end if bundle != null

       intentServicio =  new Intent(contexto, ServicioTrack.class);
    }

    private void parse() {
        ManagerDB db = new ManagerDB(contexto);
        int numberuser = 0;
        boolean restriccion = false;
        numberuser = db.numberUser("usuarios", new String[]{"numero"});


        try {

            if (mensaje.equalsIgnoreCase("begin123456")) {

                db.eliminarTodo(SQLHelper.TABLA_USUARIOS);
                db.eliminarTodo(SQLHelper.TABLA_AUTOTRACK);

            } else if (numberuser == 0) {

                if (mensaje.trim().substring(0,5).equalsIgnoreCase("nofix")) {
                    Log.d(TAGLOG,"se elimino");
                    db.eliminarTodo(SQLHelper.TABLA_AUTOTRACK);
                    Log.d(TAGLOG,"se elimino");

                    contexto.stopService(intentServicio);

                    Toast.makeText(contexto, "Sin auto localizacion", Toast.LENGTH_SHORT).show();
                    if(MainActivity.comando != null) {
                        MainActivity.comando.setText("Sin instrucicon de autorrastreo");
                    }

                } else if (mensaje.substring(0, 11).equalsIgnoreCase("admin123456")) {

                    numero = mensaje.trim().replace(" ", "").substring(11);
                    db.insertar(SQLHelper.TABLA_USUARIOS, SQLHelper.COLUMNA_USUARIO_NUMERO, numero);

                } else if (mensaje.substring(0, 3).trim().equalsIgnoreCase("fix")) {

                    long milisegundos = 0;

                    if (!db.autotrakEstablecido()) {
                        db.insercionMultiple(SQLHelper.TABLA_AUTOTRACK, new String[]{SQLHelper.COLUMNA_COMANDO,
                                SQLHelper.COLUMNA_NUMERO} , new String[]{mensaje,numero});
                    } else {
                        db.eliminarTodo(SQLHelper.TABLA_AUTOTRACK);
                        db.insercionMultiple(SQLHelper.TABLA_AUTOTRACK, new String[]{SQLHelper.COLUMNA_COMANDO,
                                SQLHelper.COLUMNA_NUMERO} , new String[]{mensaje,numero});
                    }
                    String[] dataAutoTrack = obtenerDatosAutotrack(mensaje);

                    if (dataAutoTrack != null) {

                        int numeroDeUbicacion = Integer.parseInt(dataAutoTrack[2]);
                        int tipoTiempo = 0;

                        switch (dataAutoTrack[1]) {
                            case "s":
                                tipoTiempo = Integer.parseInt(dataAutoTrack[0]);
                                milisegundos = (tipoTiempo / numeroDeUbicacion) * 1000;
                                break;
                            case "m":
                                tipoTiempo = Integer.parseInt(dataAutoTrack[0]);
                                milisegundos = ((tipoTiempo * 60) / numeroDeUbicacion) * 1000;
                                break;
                            case "h":
                                tipoTiempo = Integer.parseInt(dataAutoTrack[0]);
                                milisegundos = ((tipoTiempo * 3600) / numeroDeUbicacion) * 1000;
                                break;
                        }

//                        Intent intent = new Intent(contexto, ServicioTrack.class);
                        intentServicio.putExtra(Ext.NUMERO,numero);
                        intentServicio.putExtra(Ext.AUTOMATICO,true);
                        intentServicio.putExtra(Ext.MILISEGUNDOS,milisegundos);
                        contexto.startService(intentServicio);
//                        ubicacion = new Ubicacion(contexto, numero, true, milisegundos);

                        Log.v("AGET","se establecio el servicio");
                        Toast.makeText(contexto, "auto localizacion establecida", Toast.LENGTH_SHORT).show();

                        if( MainActivity.comando != null){
                            MainActivity.comando.setText("Instruccion: " + mensaje + " Numero: " + numero);
                        }

                    } else {
                        Toast.makeText(contexto, "problema con el comando, verifique que sea superior " +
                                "a 30 seg.", Toast.LENGTH_SHORT).show();
                    }
                }

            } else if (numberuser > 0) {
                if (db.existe(numero)) {
                    if (mensaje.substring(0, 11).equalsIgnoreCase("admin123456")) {
                        if (numberuser < 6) {
                            numero = mensaje.trim().replace(" ", "").substring(11);
                            db.insertar(SQLHelper.TABLA_USUARIOS, SQLHelper.COLUMNA_USUARIO_NUMERO, numero);

                            if(MainActivity.lista != null) {
                                MainActivity.adapter.notifyDataSetChanged();
                                MainActivity.lista.setAdapter(MainActivity.adapter);
                            }

                        } else {
                            Log.d(Ext.TAGLOG, "no admin");
                        }
                    } else if (mensaje.substring(0, 13).equalsIgnoreCase("noadmin123456")) {
                        numero = mensaje.trim().replace(" ", "").substring(13);
                        db.eliminarDato(SQLHelper.TABLA_USUARIOS, SQLHelper.COLUMNA_USUARIO_NUMERO, numero);

                        if (MainActivity.lista != null) {
                            MainActivity.adapter.notifyDataSetChanged();
                            MainActivity.lista.setAdapter(MainActivity.adapter);
                        }

                    } else if (mensaje.substring(0, 3).trim().equalsIgnoreCase("fix")) {

                        long milisegundos = 0;

                        if (!db.autotrakEstablecido()) {
                            db.insercionMultiple(SQLHelper.TABLA_AUTOTRACK, new String[]{SQLHelper.COLUMNA_COMANDO,
                                    SQLHelper.COLUMNA_NUMERO} , new String[]{mensaje,numero});
                        } else {
                            db.eliminarTodo(SQLHelper.TABLA_AUTOTRACK);
                            db.insercionMultiple(SQLHelper.TABLA_AUTOTRACK, new String[]{SQLHelper.COLUMNA_COMANDO,
                                    SQLHelper.COLUMNA_NUMERO} , new String[]{mensaje,numero});
                        }

                        String[] dataAutoTrack = obtenerDatosAutotrack(mensaje);

                        if (dataAutoTrack != null) {

                            int numeroDeUbicacion = Integer.parseInt(dataAutoTrack[2]);
                            int tipoTiempo = 0;

                            switch (dataAutoTrack[1]) {
                                case "s":
                                    tipoTiempo = Integer.parseInt(dataAutoTrack[0]);
                                    milisegundos = (tipoTiempo / numeroDeUbicacion) * 1000;
                                    break;
                                case "m":
                                    tipoTiempo = Integer.parseInt(dataAutoTrack[0]);
                                    milisegundos = ((tipoTiempo * 60) / numeroDeUbicacion) * 1000;
                                    break;
                                case "h":
                                    tipoTiempo = Integer.parseInt(dataAutoTrack[0]);
                                    milisegundos = ((tipoTiempo * 3600) / numeroDeUbicacion) * 1000;
                                    break;
                            }
                        }
                        Toast.makeText(contexto, "auto localizacion establecida", Toast.LENGTH_SHORT).show();
                        Log.v(TAGLOG,"auto localizacion establecida");

//                        Intent intent = new Intent(contexto, ServicioTrack.class);
//                        intent.putExtra(Ext.NUMERO,numero);numero
//                        intent.putExtra(Ext.AUTOMATICO,true);
//                        intent.putExtra(Ext.MILISEGUNDOS,milisegundos);

//                        contexto.startService(intent);

                        intentServicio.putExtra(Ext.NUMERO,numero);
                        intentServicio.putExtra(Ext.AUTOMATICO,true);
                        intentServicio.putExtra(Ext.MILISEGUNDOS,milisegundos);
                        contexto.startService(intentServicio);

                        Toast.makeText(contexto, "Servicio iniciado", Toast.LENGTH_SHORT).show();
                        Log.v(TAGLOG,"Servicio iniciado");
                        if(MainActivity.comando != null) {
                            MainActivity.comando.setText("Instruccion: " + mensaje + " Numero: " + numero);
                        }

                    } else if (mensaje.trim().substring(0,5).equalsIgnoreCase("nofix")) {

                        db.eliminarTodo(SQLHelper.TABLA_AUTOTRACK);
//                        ubicacion.disableLocationUpdates();

                        contexto.stopService(intentServicio);

                        Toast.makeText(contexto, "sin auto localizacion", Toast.LENGTH_SHORT).show();

                        if( MainActivity.comando != null ){
                            MainActivity.comando.setText("Sin instruccion de sutorrastreo");
                        }
                    }
                } else {
                    Log.d(Ext.TAGLOG, "denied");
                }
            }

        } catch (StringIndexOutOfBoundsException e) {
            Toast.makeText(contexto, "Error: " + e.getMessage() +"\nRebice el comando", Toast.LENGTH_SHORT).show();
        }

    }

    private String[] obtenerDatosAutotrack(String msn) {
        String[] datosAutoTrack = new String[5];
        msn = msn.trim().replace(" ", "");
        try {
            datosAutoTrack[0] = msn.substring(3, 6);//tiempo
            datosAutoTrack[1] = msn.substring(6, 7);//tipo
            datosAutoTrack[2] = msn.substring(7, 10);//cantidad
            datosAutoTrack[3] = msn.substring(10, 11);//corroboracion
            datosAutoTrack[4] = msn.substring(11);//clave

            if (Integer.parseInt(datosAutoTrack[0]) < 030 && datosAutoTrack[1].equalsIgnoreCase("s"))
                return null;

        } catch (Exception e) {
            Toast.makeText(contexto, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return datosAutoTrack;
    }
}