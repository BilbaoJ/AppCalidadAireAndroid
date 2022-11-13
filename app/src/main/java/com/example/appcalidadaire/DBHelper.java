package com.example.appcalidadaire;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    String tablaEstaciones = "CREATE TABLE Estaciones (id integer primary key autoincrement, " +
            "nombre_estacion text not null," +
            "ciudad text not null," +
            "longitud double not null," +
            "latitud double not null," +
            "pm double not null)";

    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(tablaEstaciones);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("Drop table Estaciones");
        db.execSQL(tablaEstaciones);
    }
}
