package com.aldo.aget.simuladortracker.Control;

/**
 * Created by Work on 25/11/16.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLHelper extends SQLiteOpenHelper {
    Context contexto = null;

    public static final String DATABASE_NAME = "Simulador.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TIPO_DATO_TEXT = "TEXT";
    public static final String TIPO_DATO_INTEGER= "INTEGER";


    public static final String COLUMNA_GENERICA_ID = "_id";

    public static final String TABLA_USUARIOS = "usuarios";
    public static final String COLUMNA_USUARIO_ID = "usuario_id";
    public static final String COLUMNA_USUARIO_NUMERO = "numero";


    public static final String TABLA_AUTOTRACK = "autotrack";
//    fix030s005n123456
//    nofix
    public static final String COLUMNA_COMANDO = "comando_autotrack";
    public static final String COLUMNA_NUMERO = "numero";


    private static final String SQL_CREAR_TABLA_USUARIOS = "CREATE TABLE IF NOT EXISTS "
            + TABLA_USUARIOS + " ("
            + COLUMNA_GENERICA_ID +" "+ TIPO_DATO_INTEGER + " PRIMARY KEY AUTOINCREMENT, "
            + COLUMNA_USUARIO_NUMERO + " " + TIPO_DATO_TEXT + ")";

    private static final String SQL_CREAR_TABLA_COMANDO = "CREATE TABLE IF NOT EXISTS "
            + TABLA_AUTOTRACK + " ("
            + COLUMNA_GENERICA_ID + " " + TIPO_DATO_INTEGER + " PRIMARY KEY AUTOINCREMENT, "
            + COLUMNA_COMANDO + " " + TIPO_DATO_TEXT +","
            + COLUMNA_NUMERO + " " + TIPO_DATO_TEXT + " )";

    public SQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        contexto = context;
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREAR_TABLA_USUARIOS);
        db.execSQL(SQL_CREAR_TABLA_COMANDO);

        /*Toast t1 = Toast.makeText(contexto, SQL_CREAR_TABLA_AUXILIAR, Toast.LENGTH_LONG);
        t1.setGravity(Gravity.TOP | Gravity.LEFT, 60, 110);
        t1.show();
        Toast t2 = Toast.makeText(contexto,SQL_INSERTAR_DATOS_DEFAULT_AUXILIAR,Toast.LENGTH_LONG);
        t2.setGravity(Gravity.BOTTOM | Gravity.RIGHT,60,100);*/
        // TODO Auto-generated method stub
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        // se elimina la tabla anterior y crearla de nuevo vacía con el nuevo formato.
        //db.execSQL("DROP TABLE IF EXISTS TablaPrueba");
        //db.execSQL(codeSQL);
    }
}