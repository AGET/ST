package com.aldo.aget.simuladortracker.Control;

/**
 * Created by Work on 25/11/16.
 */


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by workstation on 10/11/15.
 */
public class ManagerDB {

    SQLHelper sqlhelper;
    SQLiteDatabase db;

    public ManagerDB(Context contexto) {
        sqlhelper = new SQLHelper(contexto);
    }

    public int numberUser(String tabla, String[] columna) {
        abrirEscrituraBD();
        Cursor c;
        c = db.query(tabla, columna, null, null, null, null, null);

        Log.d(Ext.TAGLOG,"getColumnCount="+c.getColumnCount());
        Log.d(Ext.TAGLOG,"getCount="+c.getCount());

        return c.getCount();
    }



    public boolean existe(String numero) {
        abrirEscrituraBD();

        Cursor c;
        c = db.query(SQLHelper.TABLA_USUARIOS, new String[]{SQLHelper.COLUMNA_USUARIO_NUMERO},
                SQLHelper.COLUMNA_USUARIO_NUMERO+"=?", new String[]{numero}, null, null, null);


        Log.d(Ext.TAGLOG,"getColumnCount="+c.getColumnCount());
        Log.d(Ext.TAGLOG,"getCount="+c.getCount());
        Log.d(Ext.TAGLOG,"numero="+numero);

        return c.getCount() > 0;
    }

    public boolean autotrakEstablecido() {
        abrirEscrituraBD();
        Cursor c;
        c = db.query(SQLHelper.TABLA_AUTOTRACK, new String[]{SQLHelper.COLUMNA_COMANDO}, null, null, null, null, null);

        Log.d(Ext.TAGLOG,"Autotrack getColumnCount="+c.getColumnCount());
        Log.d(Ext.TAGLOG,"Autotrack getCount="+c.getCount());

        return c.getCount() > 0;
    }

    public boolean eliminarDato(String tabla, String columna, String dato) {
        abrirEscrituraBD();
        db.delete(tabla, columna + "=" + dato, null);
        return true;
    }

    public boolean eliminarTodo(String tabla) {
        abrirEscrituraBD();
        db.delete(tabla, null, null);
        return true;
    }

    public void abrirEscrituraBD() {
        db = sqlhelper.getWritableDatabase();
    }

    public void cerrarBD() {
        db.close();
    }


    public boolean actualizarUnDato(String tabla, String columna, String dato, String condicion, String valorCondicion) {
        abrirEscrituraBD();
        ContentValues valores = new ContentValues();
        valores.put(columna, dato);
        db.update(tabla, valores, condicion + "=" + valorCondicion, null);
        return true;
    }

    public ArrayList obtenerDatos(String tabla, String[] campos, String where, String[] datosWhere) {
        abrirEscrituraBD();
        ArrayList datosCursor = new ArrayList();
        Cursor c;
        if (where != null) {
            c = db.query(tabla, campos, where + "=?", datosWhere, null, null, null);
        } else {
            c = db.query(tabla, campos, null, null, null, null, null);
        }
        if (c.moveToFirst()) {
            do {
                for (int i = 0; i < c.getColumnCount(); i++) {
                    datosCursor.add(c.getString(i));
                }
            } while (c.moveToNext());
        } else {
            datosCursor = null;
        }
        return datosCursor;
    }


    public void insertar(String tabla, String columna, String datosColumnas) {
        abrirEscrituraBD();
        ContentValues nuevoRegistro = new ContentValues();
        switch (tabla) {
            case SQLHelper.TABLA_USUARIOS:
                nuevoRegistro.put(columna, datosColumnas);
                break;
            case SQLHelper.TABLA_AUTOTRACK:
                nuevoRegistro.put(columna, datosColumnas);
                break;
        }
        db.insert(tabla, null, nuevoRegistro);
        cerrarBD();
    }
}