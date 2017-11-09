package org.sunbird.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by JUSPAY\nikith.shetty on 15/9/17.
 */

public class SQLBlobStore extends SQLiteOpenHelper {

    static SQLBlobStore instance;
    static SQLiteDatabase db;
    private static final String TAG = "SQLBlobStore";

    private static final String DB_NAME = "BlobStoreDB";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "blobDataTable";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TAG = "tag";
    private static final String COLUMN_DATA = "data";

    public static SQLBlobStore getInstance (Context context) {
        if (instance != null) return instance;
        else {
            instance = new SQLBlobStore(context, DB_NAME, null, DB_VERSION);
            return instance;
        }
    }

    private SQLBlobStore(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sDb) {
        sDb.execSQL("CREATE TABLE " + TABLE_NAME +
                "(" + COLUMN_ID + " INTEGER PRIMARY KEY," +
                COLUMN_TAG + " TEXT," +
                COLUMN_DATA + " BLOB )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.d(TAG, "onUpgrade: " + " db upgrade");
    }

    public static void setData (Context context, String tag, String data) {
        Log.d(TAG, "setData: " + data);
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TAG, tag);
        cv.put(COLUMN_DATA, data);
        SQLiteDatabase db = SQLBlobStore.getInstance(context).getWritableDatabase();
        db.insert(TABLE_NAME, null, cv);
        db.update(TABLE_NAME, cv, COLUMN_TAG + " = \"" + tag + "\"", null);
        db.close();
    }

    public static String getData (Context context, String tag) {
        String query = "SELECT " + COLUMN_DATA + " FROM " + TABLE_NAME + " WHERE " + COLUMN_TAG + " = \"" + tag + "\"";
        SQLiteDatabase db = SQLBlobStore.getInstance(context).getReadableDatabase();
        Log.d(TAG, "getData: query:" + query);
        String ret;
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()){
            Log.d(TAG, "getData: " + c.getString(0));
            ret = c.getString(0);
        } else {
            db.close();
            ret = "__failed";
        }
        db.close();
        return ret;
    }
}
