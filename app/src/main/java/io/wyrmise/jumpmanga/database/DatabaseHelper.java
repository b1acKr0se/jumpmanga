package io.wyrmise.jumpmanga.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import io.wyrmise.jumpmanga.model.Chapter;
import io.wyrmise.jumpmanga.model.Manga;

/**
 * Created by Thanh on 7/3/2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    //Log tag
    private static final String LOG = "DatabaseHelper";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "JumpManga";

    private static final String TABLE_MANGA = "manga";
    private static final String TABLE_CHAPTER = "chapter";


    private static final String KEY_NAME = "name";
    private static final String KEY_ID = "id";

    private static final String KEY_MANGA_URL = "url";
    private static final String KEY_MANGA_IMAGE = "image";
    private static final String KEY_MANGA_LATEST = "latest";

    private static final String KEY_MANGA_NAME = "manga_name";

    private static final String CREATE_TABLE_MANGA = "CREATE TABLE " + TABLE_MANGA +
            "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_NAME + " TEXT," + KEY_MANGA_IMAGE + " TEXT," + KEY_MANGA_URL + " TEXT,"
            + KEY_MANGA_LATEST + " TEXT)";

    private static final String CREATE_TABLE_CHAPTER = "CREATE TABLE " + TABLE_CHAPTER +
            "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_NAME + " TEXT, " + KEY_MANGA_NAME + " TEXT)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        db.execSQL(CREATE_TABLE_MANGA);
        db.execSQL(CREATE_TABLE_CHAPTER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MANGA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAPTER);

        // create new tables
        onCreate(db);
    }

    public boolean insertManga(Manga manga) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_NAME, manga.getName());
        contentValues.put(KEY_MANGA_URL, manga.getUrl());
        contentValues.put(KEY_MANGA_IMAGE, manga.getImage());
        contentValues.put(KEY_MANGA_LATEST, manga.getLatest());
        try {
            db.insertOrThrow(TABLE_MANGA, null, contentValues);
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
            System.out.println("Failed to insert" );
            return false;
        }
        System.out.println("inserted");
        return true;
    }

    public boolean insertChapter(Chapter chapter, String mangaName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_NAME, chapter.getName());
        contentValues.put(KEY_MANGA_NAME, mangaName);
        try {
            db.insertOrThrow(TABLE_CHAPTER, null, contentValues);
        } catch (SQLiteConstraintException e) {
            return false;
        }
        return true;
    }

    public boolean updateMangaInfo(Manga manga) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_NAME, manga.getName());
        contentValues.put(KEY_MANGA_URL, manga.getUrl());
        contentValues.put(KEY_MANGA_IMAGE, manga.getImage());
        contentValues.put(KEY_MANGA_LATEST, manga.getLatest());

        int rowsAffected = db.update(TABLE_MANGA, contentValues, KEY_NAME + " = ?",
                new String[]{manga.getName()});

        if (rowsAffected > 0) return true;
        return false;

    }

    public boolean unfavoritedManga(String mangaName) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_MANGA, KEY_NAME + " = ?",
                new String[]{mangaName});
        if (rowsAffected > 0){
            System.out.println("unfavorited");
            return true;
        }
        return false;
    }


    public boolean isMangaFavorited(String mangaName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_MANGA + " WHERE "
                + KEY_NAME + " = '" + mangaName + "'";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public ArrayList<Manga> getAllFavoritedMangas() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_MANGA;

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            ArrayList<Manga> mangas = new ArrayList<>();
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                String image = cursor.getString(cursor.getColumnIndex(KEY_MANGA_IMAGE));
                String url = cursor.getString(cursor.getColumnIndex(KEY_MANGA_URL));
                String latest = cursor.getString(cursor.getColumnIndex(KEY_MANGA_LATEST));
                Manga manga = new Manga(name, url, image, latest);
                manga.setIsFav(true);
                mangas.add(manga);
            }
            return mangas;
        }
        return null;
    }

    public boolean isChapterRead(Chapter chapter, String mangaName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_CHAPTER + " WHERE " + KEY_NAME + " = '" + chapter.getName()
                + "' AND " + KEY_MANGA_NAME + " = '" + mangaName + "'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public ArrayList<Chapter> getAllReadChapters(Manga manga) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_CHAPTER + " WHERE " + KEY_MANGA_NAME + " = '" + manga.getName() + "'";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            ArrayList<Chapter> chapters = new ArrayList<>();
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                Chapter chapter = new Chapter(name);
                chapters.add(chapter);
            }
            return chapters;
        }
        return null;
    }
}
