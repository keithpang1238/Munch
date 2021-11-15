package com.example.munch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

class DBHelper extends SQLiteOpenHelper {

    private Context context;

    private static final String DATABASE_NAME = "Munch.db";
    private static final int DATABASE_VERSION = 10;
    private static final String IMAGE_CONFIG_TABLE_NAME = "image_config";
    private static final String TV_GENRE_TABLE_NAME = "tv_genre";
    private static final String MOVIE_GENRE_TABLE_NAME = "movie_genre";
    private static final String LAST_UPDATE_TABLE_NAME = "last_update";
    private static final String PROVIDER_TABLE_NAME = "provider";

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_LAST_UPDATE = "last_update";
    private static final String COLUMN_IMAGE_LABEL = "image_label";
    private static final String COLUMN_IMAGE_VALUE = "image_values";
    private static final String COLUMN_GENRE_NAME = "genre_name";
    private static final String COLUMN_PROVIDER_NAME = "provider_name";

    private static final String[] IMAGE_LABELS = {"secure_base_url", "backdrop_sizes", "poster_sizes"};


    public DBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createImageConfigTableQuery =
                "CREATE TABLE IF NOT EXISTS " + IMAGE_CONFIG_TABLE_NAME +
                        " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_IMAGE_LABEL + " TEXT, " +
                        COLUMN_IMAGE_VALUE + " TEXT);";
        db.execSQL(createImageConfigTableQuery);

        String createTVGenreTableQuery =
                "CREATE TABLE IF NOT EXISTS " + TV_GENRE_TABLE_NAME +
                        " (" + COLUMN_ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_GENRE_NAME + " TEXT);";
        db.execSQL(createTVGenreTableQuery);

        String createMovieGenreTableQuery =
                "CREATE TABLE IF NOT EXISTS " + MOVIE_GENRE_TABLE_NAME +
                        " (" + COLUMN_ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_GENRE_NAME + " TEXT);";
        db.execSQL(createMovieGenreTableQuery);

        String createLastUpdateTableQuery =
                "CREATE TABLE IF NOT EXISTS " + LAST_UPDATE_TABLE_NAME +
                        " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_LAST_UPDATE + " INTEGER);";
        db.execSQL(createLastUpdateTableQuery);

        String createProviderTableQuery =
                "CREATE TABLE IF NOT EXISTS " + PROVIDER_TABLE_NAME +
                        " (" + COLUMN_ID + " TEXT PRIMARY KEY, " +
                        COLUMN_PROVIDER_NAME + " TEXT);";
        db.execSQL(createProviderTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + IMAGE_CONFIG_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TV_GENRE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MOVIE_GENRE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LAST_UPDATE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PROVIDER_TABLE_NAME);
        this.onCreate(db);
    }

    Boolean shouldUpdate() {
        SQLiteDatabase db = this.getReadableDatabase();
        // get last update
        Cursor cursor = db.rawQuery("SELECT * FROM " + LAST_UPDATE_TABLE_NAME, null);
        if (cursor.moveToNext()) {
            try {
                long lastUpdateString = cursor.getLong(1);
                System.out.println(System.currentTimeMillis() - lastUpdateString);
                cursor.close();
                db.close();
                return System.currentTimeMillis() - lastUpdateString > 1000 * 60 * 60 * 24 * 7;
            } catch (Error e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        cursor.close();
        db.close();
        return true;
    }

    HashMap<String, String> getImageConfigs() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM " + IMAGE_CONFIG_TABLE_NAME, null);

        HashMap<String, String> imageConfigs = new HashMap<>();
        while (cursor.moveToNext()) {
            imageConfigs.put(cursor.getString(1), cursor.getString(2));
        }

        cursor.close();
        db.close();
        return imageConfigs;
    }

    HashMap<String, String> getProviders() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM " + PROVIDER_TABLE_NAME, null);

        HashMap<String, String> providers = new HashMap<>();
        while (cursor.moveToNext()) {
            providers.put(cursor.getString(0), cursor.getString(1));
        }

        cursor.close();
        db.close();
        return providers;
    }

    HashMap<String, String> getGenres(Boolean isMovie) {
        String tableName;
        if (isMovie) {
            tableName = MOVIE_GENRE_TABLE_NAME;
        } else {
            tableName = TV_GENRE_TABLE_NAME;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM " + tableName, null);

        HashMap<String, String> genres = new HashMap<>();
        while (cursor.moveToNext()) {
            genres.put(cursor.getString(0), cursor.getString(1));
        }
        cursor.close();
        db.close();
        return genres;
    }

    void recordUpdate() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DELETE FROM " + LAST_UPDATE_TABLE_NAME);
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_LAST_UPDATE, System.currentTimeMillis());
        db.insert(LAST_UPDATE_TABLE_NAME, null, cv);
        db.close();
    }

    Boolean addImageConfigs(Map<String, String> imageConfigs) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        long count = DatabaseUtils.queryNumEntries(db, IMAGE_CONFIG_TABLE_NAME);
        if (count > 0) {
            db.execSQL("DELETE FROM " + IMAGE_CONFIG_TABLE_NAME);
        }
        for (String key : IMAGE_LABELS) {  // loop through your records
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_IMAGE_LABEL, key);
            cv.put(COLUMN_IMAGE_VALUE, imageConfigs.get(key));
            db.insert(IMAGE_CONFIG_TABLE_NAME, null, cv);
        }
        db.setTransactionSuccessful();
        db.endTransaction();

        recordUpdate();

        db.close();
        return true;
    }

    Boolean addGenres(Map<String, String> genres, Boolean isMovie) {
        String tableName;
        if (isMovie) {
            tableName = MOVIE_GENRE_TABLE_NAME;
        } else {
            tableName = TV_GENRE_TABLE_NAME;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        long count = DatabaseUtils.queryNumEntries(db, tableName);
        if (count > 0) {
            db.execSQL("DELETE FROM " + tableName);
        }
        for (String key : genres.keySet()) {  // loop through your records
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_ID, key);
            cv.put(COLUMN_GENRE_NAME, genres.get(key));
            db.insert(tableName, null, cv);
        }
        db.setTransactionSuccessful();
        db.endTransaction();

        db.close();
        return true;
    }

    Boolean addProviders(Map<String, String> providers) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        long count = DatabaseUtils.queryNumEntries(db, PROVIDER_TABLE_NAME);
        if (count > 0) {
            db.execSQL("DELETE FROM " + PROVIDER_TABLE_NAME);
        }
        for (String key : providers.keySet()) {  // loop through your records
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_ID, key);
            cv.put(COLUMN_PROVIDER_NAME, providers.get(key));
            db.insert(PROVIDER_TABLE_NAME, null, cv);
        }
        db.setTransactionSuccessful();
        db.endTransaction();

        db.close();
        return true;
    }
}

