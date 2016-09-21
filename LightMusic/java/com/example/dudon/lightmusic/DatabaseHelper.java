package com.example.dudon.lightmusic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by dudon on 2016/5/2.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    //创建 favor 数据表，name歌名，singer歌手，path路径
    public static final String CREATE_FAVOR = "CREATE TABLE favor ( "
            + " _id integer primary key autoincrement ,"
            + " name text, "
            + " singer text, "
            + " path text, "
            + " date text )";

    private Context context;

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_FAVOR);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            default:
        }
    }
}
