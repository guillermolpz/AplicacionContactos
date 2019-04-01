package com.example.aplicacioncontactos;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class SQLiteHelper extends SQLiteOpenHelper {
    //constructor
    SQLiteHelper(Context context,
                 String name,
                 SQLiteDatabase.CursorFactory factory,
                 int version){
        super(context, name, factory, version);
    }

    public void queryData(String sql){
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL(sql);
    }

    //insertData
    public void insertData(String nombre, String edad, String telefono, byte[] image){
        SQLiteDatabase database = getWritableDatabase();
        //consulta para insertar registro en la tabla de base de datos
        String sql = "INSERT INTO RECORD VALUES(NULL, ?, ?, ?, ?)"; //where "RECORD" is table name in database we will create in mainActivity

        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();

        statement.bindString(1, nombre);
        statement.bindString(2, edad);
        statement.bindString(3, telefono);
        statement.bindBlob(4, image);

        statement.executeInsert();
    }

    //updateData
    public void updateData(String nombre, String edad, String telefono, byte[] image, int id){
        SQLiteDatabase database = getWritableDatabase();
        //consulta para actualizar el registro
        String sql = "UPDATE RECORD SET nombre=?, edad=?, telefono=?, image=? WHERE id=?";

        SQLiteStatement statement = database.compileStatement(sql);

        statement.bindString(1, nombre);
        statement.bindString(2, edad);
        statement.bindString(3, telefono);
        statement.bindBlob(4, image);
        statement.bindDouble(5, (double)id);

        statement.execute();
        database.close();
    }

    //deleteData
    public void deleteData(int id){
        SQLiteDatabase database = getWritableDatabase();
        //consulta para borrar el registro utilizando id
        String sql = "DELETE FROM RECORD WHERE id=?";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();
        statement.bindDouble(1, (double)id);

        statement.execute();
        database.close();
    }

    public Cursor getData(String sql){
        SQLiteDatabase database = getReadableDatabase();
        return database.rawQuery(sql, null);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
