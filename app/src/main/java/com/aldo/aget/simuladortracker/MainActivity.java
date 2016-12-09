package com.aldo.aget.simuladortracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.aldo.aget.simuladortracker.Control.Ext;
import com.aldo.aget.simuladortracker.Control.ManagerDB;
import com.aldo.aget.simuladortracker.Control.SQLHelper;
import com.aldo.aget.simuladortracker.Receiver.SMSReceiver;
import com.aldo.aget.simuladortracker.Service.ServicioTrack;

import java.util.ArrayList;

import android.util.Log;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static ListView lista;
    public static TextView comando;
    public static ArrayAdapter<String> adapter;

    ArrayList numeros = null;

    ArrayList command = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lista = (ListView) findViewById(R.id.lista);
        comando = (TextView) findViewById(R.id.comando);

        ManagerDB db = new ManagerDB(this);
        numeros = db.obtenerDatos(SQLHelper.TABLA_USUARIOS, new String[]{SQLHelper.COLUMNA_USUARIO_NUMERO}, null, null);

        command = db.obtenerDatos(SQLHelper.TABLA_AUTOTRACK, new String[]{SQLHelper.COLUMNA_COMANDO, SQLHelper.COLUMNA_NUMERO}, null, null);

        Log.v(Ext.TAGLOG, "SERVICIO CREADO: " + ServicioTrack.isInstanceCreated());
//        Log.v(Ext.TAGLOG, "Google conectado: " + Ubicacion.isConnectApi());
//        Log.v(Ext.TAGLOG, "Clase que contiene Google nulo: " + Ubicacion.isNullApi());
        Intent intent = new Intent(this, ServicioTrack.class);

//        if (!ServicioTrack.isInstanceCreated() && Ubicacion.isConnectApi()) {
//            Ubicacion.apiClient.disconnect();
//        }

        if (command != null) {

            if (!ServicioTrack.isInstanceCreated()) {

                Log.v(Ext.TAGLOG, "Comando:" + command.get(0).toString());
                Log.v(Ext.TAGLOG, "NumeroTAG:" + command.get(1).toString());
                long milisegundos = 0;
//                comando.setText("" + command.get(0));

                String[] dataAutoTrack = obtenerDatosAutotrack(command.get(0).toString());

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

                    intent.putExtra(Ext.NUMERO, command.get(1).toString());
                    intent.putExtra(Ext.AUTOMATICO, true);
                    intent.putExtra(Ext.MILISEGUNDOS, milisegundos);

                    if (!ServicioTrack.isInstanceCreated()) {
                        startService(intent);
                    }
                }

                Log.v("AGET", "se establecio el servicio");
                Toast.makeText(this, "auto localizacion establecida", Toast.LENGTH_SHORT).show();

            }

            if (comando != null) {
                comando.setText("Instruccion: " + command.get(0) + " Numero: " + command.get(1));
            }

        } else if (command == null && ServicioTrack.isInstanceCreated()) {
            stopService(intent);
//            if (Ubicacion.isConnectApi()) {
//                Ubicacion.apiClient.disconnect();
//            }
            comando.setText("No hay instruccion de autorrastreo");
        } else {
            comando.setText("No hay instruccion de autorrastreo");
            if (SMSReceiver.ubicacion != null) {
                if (SMSReceiver.ubicacion.apiClient != null) {
                    if (SMSReceiver.ubicacion.apiClient.isConnected()) {
                        SMSReceiver.ubicacion.apiClient.disconnect();
                    }
                }
            }
        }

        adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                numeros);
        if (numeros != null && numeros.size() > 0)

        {
            adapter.notifyDataSetChanged();
            lista.setAdapter(adapter);
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
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return datosAutoTrack;
    }

}
