package com.aldo.aget.simuladortracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.aldo.aget.simuladortracker.Control.ManagerDB;
import com.aldo.aget.simuladortracker.Control.SQLHelper;

import java.util.ArrayList;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public static  ListView lista;
    public static TextView comando;
    public static ArrayAdapter<String> adapter;

ArrayList numeros = null;

    ArrayList comaand = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lista = (ListView) findViewById(R.id.lista);
        comando = (TextView) findViewById(R.id.comando);

        ManagerDB db = new ManagerDB(this);
        numeros = db.obtenerDatos(SQLHelper.TABLA_USUARIOS, new String[]{SQLHelper.COLUMNA_USUARIO_NUMERO},null,null);

        comaand = db.obtenerDatos(SQLHelper.TABLA_AUTOTRACK, new String[]{SQLHelper.COLUMNA_COMANDO},null,null);

        if ( comaand != null )
            comando.setText("" + (Integer) comaand.get(0));
        else
            comando.setText("No hay comando");
        adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                numeros);
        if ( numeros != null && numeros.size() > 0) {
            adapter.notifyDataSetChanged();
            lista.setAdapter(adapter);
        }
    }
}
