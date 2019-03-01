package com.example.bhati.routeapplication.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.example.bhati.routeapplication.Rest.Album;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "Route_application.db";
    public static final String TABLE_NAME = "Route_informations";
    public static final String ID = "id";
    public static final String LATLNG = "latlng";
    public static final String SPEECH = "speech";
    public static final String DURATION = "duration";
    public static final String VIDEO = "video";
    public static final String VIDEO_NAME = "video_name";
    public static final String TIME = "time";
    public static final String DATE = "date";
    public static final String SIZE = "size";
    public static final String CITY = "city";
    public static final String USERNAME = "USERNAME";
    private static final String TAG = "DBHelper";

    public DBHelper (Context context)
    {
        super(context , DATABASE_NAME , null , DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME +"(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                ""+VIDEO+" text," +
                ""+VIDEO_NAME+" text," +
                ""+DATE+" text," +
                ""+TIME+" text," +
                ""+LATLNG+" text," +
                ""+SPEECH+" text," +
                ""+SIZE+" text," +
                ""+CITY+" text," +
                ""+USERNAME+" text," +
                ""+DURATION+")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(Uri video_url,String video_name, String speech , String latLngs ,
                              String DURATION , String size , String time ,
                              String date , String city , String username)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.VIDEO, String.valueOf(video_url));
        contentValues.put(DBHelper.VIDEO_NAME,video_name);
        contentValues.put(DBHelper.SPEECH, speech);
        contentValues.put(DBHelper.SIZE, size);
        contentValues.put(DBHelper.TIME, time);
        contentValues.put(DBHelper.DATE, date);
        contentValues.put(DBHelper.CITY, city);
        contentValues.put(DBHelper.USERNAME, username);
        contentValues.put(DBHelper.LATLNG, String.valueOf(latLngs));
        contentValues.put(DBHelper.DURATION, DURATION);
        long i = db.insert(TABLE_NAME , null , contentValues);

        if (i > 0) {
            Log.d(TAG, "insert_data: data inserted successfully: "+contentValues.toString());
            return true;
        }
        else {
            Log.e(TAG, "insert_data: error while inserting data into database ");
            return false;
        }
    }
    public Cursor getData(int id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + "="+id , null);
        return res;
    }

    public List<Album> getAllData()
    {
        List<Album> albums  = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM "+ TABLE_NAME +" ORDER by "+ ID + " DESC", null);
        StringBuffer stringBuffer = new StringBuffer();
        Album album  = null;
        while (res.moveToNext())
        {
            album = new Album();

            String video_name = res.getString(res.getColumnIndexOrThrow(DBHelper.VIDEO_NAME));
            String video_url = res.getString(res.getColumnIndex(DBHelper.VIDEO));
            String id = res.getString(res.getColumnIndex(DBHelper.ID));
            String speech = res.getString(res.getColumnIndex(DBHelper.SPEECH));
            String latLngs  = res.getString(res.getColumnIndex(DBHelper.LATLNG));
            String time = res.getString(res.getColumnIndex(DBHelper.TIME));
            String duration = res.getString(res.getColumnIndex(DBHelper.DURATION));
            String date = res.getString(res.getColumnIndex(DBHelper.DATE));
            String size = res.getString(res.getColumnIndex(DBHelper.SIZE));
            String city = res.getString(res.getColumnIndex(DBHelper.CITY));
            String name = res.getString(res.getColumnIndex(DBHelper.USERNAME));

            album.setLatLngs(latLngs);
            album.setId(id);
            album.setSpeech(speech);
            album.setVideo_name(video_name);
            album.setVideo_url(video_url);
            album.setTime(time);
            album.setDate(date);
            album.setDuration(duration);
            album.setCity(city);
            album.setSize(size);
            album.setName(name);
            //album.setLatLngs(latLngs);
            //album.setLatLngs(latLngs);

            stringBuffer.append(album);
            // stringBuffer.append(dataModel);
            albums.add(album);
        }

        for (Album mo: albums)
        {
            Log.d(TAG, "getAllData: "+mo.getTime());
        }
        return albums;
    }
    public Cursor getAllSize()
    {
        //List<String> size = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM "+ TABLE_NAME , null);
//        size.add(res.getString(res.getColumnIndex(DBHelper.SIZE)));
        return res;
    }
}
