package io.wyrmise.jumpmanga.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import java.util.ArrayList;

import io.wyrmise.jumpmanga.model.Category;
import io.wyrmise.jumpmanga.model.Chapter;
import io.wyrmise.jumpmanga.model.Manga;


public class JumpDatabaseHelper extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "jump_database.db";
    private static final int DATABASE_VERSION = 2;

    public JumpDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //table name
    private static final String TABLE_MANGA = "manga";
    private static final String TABLE_CHAPTER = "chapter";
    private static final String TABLE_RECENT = "recent";
    private static final String TABLE_FAV_CHAPTER = "fav_chapter";
    private static final String TABLE_ALL_MANGA = "all_manga";
    private static final String TABLE_CATEGORY = "category";

    //TABLE_MANGA COLUMNS
    private static final String TABLE_MANGA_ID = "_id";
    private static final String TABLE_MANGA_NAME = "name";
    private static final String TABLE_MANGA_IMAGE = "image";
    private static final String TABLE_MANGA_URL = "url";
    private static final String TABLE_MANGA_LATEST = "latest";

    //TABLE_CHAPTER COLUMNS
    private static final String TABLE_CHAPTER_ID = "_id";
    private static final String TABLE_CHAPTER_NAME = "name";
    private static final String TABLE_CHAPTER_MANGA_NAME = "manga_name";

    //TABLE_RECENT COLUMNS
    private static final String TABLE_RECENT_ID = "_id";
    private static final String TABLE_RECENT_MANGA_NAME = "manga_name";
    private static final String TABLE_RECENT_MANGA_IMAGE = "manga_image";
    private static final String TABLE_RECENT_MANGA_URL = "manga_url";
    private static final String TABLE_RECENT_CHAPTER_NAME = "chapter_name";
    private static final String TABLE_RECENT_CHAPTER_URL = "chapter_url";

    //TABLE_FAV_CHAPTER COLUMNS
    private static final String TABLE_FAV_CHAPTER_ID = "_id";
    private static final String TABLE_FAV_CHAPTER_NAME = "name";
    private static final String TABLE_FAV_CHAPTER_MANGA_NAME = "manga_name";

    //TABLE_ALL_MANGA COLUMNS
    private static final String TABLE_ALL_MANGA_ID = "_id";
    private static final String TABLE_ALL_MANGA_NAME = "name";
    private static final String TABLE_ALL_MANGA_URL = "url";
    private static final String TABLE_ALL_MANGA_IMAGE = "image";

    //TABLE_CATEGORY COLUMNS
    private static final String TABLE_CATEGORY_ID = "_id";
    private static final String TABLE_CATEGORY_NAME = "name";
    private static final String TABLE_CATEGORY_URL = "url";
    private static final String TABLE_CATEGORY_PAGE = "page";

    public boolean insertManga(Manga manga) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(TABLE_MANGA_NAME, manga.getName());
        contentValues.put(TABLE_MANGA_URL, manga.getUrl());
        contentValues.put(TABLE_MANGA_IMAGE, manga.getImage());
        contentValues.put(TABLE_MANGA_LATEST, manga.getLatest());
        try {
            db.insertOrThrow(TABLE_MANGA, null, contentValues);
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
            System.out.println("Failed to insert");
            return false;
        }
        System.out.println("inserted");
        return true;
    }

    public boolean insertChapter(Chapter chapter, String mangaName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_CHAPTER_NAME, chapter.getName());
        contentValues.put(TABLE_CHAPTER_MANGA_NAME, mangaName);
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

        contentValues.put(TABLE_MANGA_NAME, manga.getName());
        contentValues.put(TABLE_MANGA_URL, manga.getUrl());
        contentValues.put(TABLE_MANGA_IMAGE, manga.getImage());
        contentValues.put(TABLE_MANGA_LATEST, manga.getLatest());

        int rowsAffected = db.update(TABLE_MANGA, contentValues, TABLE_MANGA_NAME + " = ?",
                new String[]{manga.getName()});

        if (rowsAffected > 0) return true;
        return false;

    }

    public boolean unfavoritedManga(String mangaName) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_MANGA, TABLE_MANGA_NAME + " = ?",
                new String[]{mangaName});
        if (rowsAffected > 0) {
            return true;
        }
        return false;
    }

    public boolean isMangaFavorited(String mangaName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_MANGA + " WHERE "
                + TABLE_MANGA_NAME + " = '" + mangaName + "'";

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
                String name = cursor.getString(cursor.getColumnIndex(TABLE_MANGA_NAME));
                String image = cursor.getString(cursor.getColumnIndex(TABLE_MANGA_IMAGE));
                String url = cursor.getString(cursor.getColumnIndex(TABLE_MANGA_URL));
                String latest = cursor.getString(cursor.getColumnIndex(TABLE_MANGA_LATEST));
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
        String selectQuery = "SELECT  * FROM " + TABLE_CHAPTER + " WHERE " + TABLE_CHAPTER_NAME + " = '" + chapter.getName()
                + "' AND " + TABLE_CHAPTER_MANGA_NAME + " = '" + mangaName + "'";
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

        String selectQuery = "SELECT * FROM " + TABLE_CHAPTER + " WHERE " + TABLE_CHAPTER_MANGA_NAME + " = '" + manga.getName() + "'";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            ArrayList<Chapter> chapters = new ArrayList<>();
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(TABLE_CHAPTER_NAME));
                Chapter chapter = new Chapter(name);
                chapters.add(chapter);
            }
            return chapters;
        }
        return null;
    }

    public boolean insertRecentChapter(Manga manga, Chapter chapter) {
        SQLiteDatabase db = this.getWritableDatabase();
        deleteRecentChapter(manga.getName());

        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_RECENT_MANGA_NAME, manga.getName());
        contentValues.put(TABLE_RECENT_MANGA_IMAGE, manga.getImage());
        contentValues.put(TABLE_RECENT_MANGA_URL, manga.getUrl());
        contentValues.put(TABLE_RECENT_CHAPTER_NAME, chapter.getName());
        contentValues.put(TABLE_RECENT_CHAPTER_URL, chapter.getUrl());

        try {
            db.insertOrThrow(TABLE_RECENT, null, contentValues);
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
            System.out.println("recent not inserted");
            return false;
        }
        System.out.println("recent inserted");
        return true;
    }

    public boolean deleteRecentChapter(String mangaName) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_RECENT, TABLE_RECENT_MANGA_NAME + " = ?", new String[]{mangaName});
        return rowsAffected > 0;
    }

    public ArrayList<Manga> getRecentChapters() {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_RECENT + " ORDER BY " + TABLE_RECENT_ID + " DESC LIMIT 10";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            ArrayList<Manga> mangas = new ArrayList<>();
            while (cursor.moveToNext()) {
                String manga_name = cursor.getString(cursor.getColumnIndex(TABLE_RECENT_MANGA_NAME));
                String manga_image = cursor.getString(cursor.getColumnIndex(TABLE_RECENT_MANGA_IMAGE));
                String manga_url = cursor.getString(cursor.getColumnIndex(TABLE_RECENT_MANGA_URL));
                String chapter_name = cursor.getString(cursor.getColumnIndex(TABLE_RECENT_CHAPTER_NAME));
                String chapter_url = cursor.getString(cursor.getColumnIndex(TABLE_RECENT_CHAPTER_URL));
                Chapter c = new Chapter(chapter_name, chapter_url);
                Manga m = new Manga(manga_name, manga_image, c);
                m.setUrl(manga_url);
                mangas.add(m);
            }

            return mangas;
        }
        return null;
    }

    public ArrayList<Chapter> getAllFavChapters(Manga manga) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_FAV_CHAPTER + " WHERE " + TABLE_FAV_CHAPTER_MANGA_NAME + " = '" + manga.getName() + "'";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            ArrayList<Chapter> chapters = new ArrayList<>();
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(TABLE_FAV_CHAPTER_NAME));
                Chapter chapter = new Chapter(name);
                chapters.add(chapter);
            }
            return chapters;
        }
        return null;
    }

    public boolean isChapterFav(Chapter chapter, String mangaName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_FAV_CHAPTER + " WHERE " + TABLE_FAV_CHAPTER_NAME + " = '" + chapter.getName()
                + "' AND " + TABLE_FAV_CHAPTER_MANGA_NAME + " = '" + mangaName + "'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public boolean favChapter(Chapter chapter, String mangaName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_FAV_CHAPTER_NAME, chapter.getName());
        contentValues.put(TABLE_FAV_CHAPTER_MANGA_NAME, mangaName);
        try {
            db.insertOrThrow(TABLE_FAV_CHAPTER, null, contentValues);
        } catch (SQLiteConstraintException e) {
            return false;
        }
        System.out.println("successfully favorited chapter");
        return true;
    }

    public boolean unfavChapter(Chapter chapter, String mangaName) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_FAV_CHAPTER, TABLE_FAV_CHAPTER_NAME + " = ?"
                        + " AND " + TABLE_FAV_CHAPTER_MANGA_NAME + " = ?",
                new String[]{chapter.getName(), mangaName});
        if (rowsAffected > 0) {
            System.out.println("successfully unfav chapter");
            return true;
        }
        return false;
    }

    public ArrayList<Manga> getAllMangas() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_ALL_MANGA;

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            ArrayList<Manga> mangas = new ArrayList<>();
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(TABLE_ALL_MANGA_NAME));
                String image = cursor.getString(cursor.getColumnIndex(TABLE_ALL_MANGA_IMAGE));
                String url = cursor.getString(cursor.getColumnIndex(TABLE_ALL_MANGA_URL));
                Manga manga = new Manga();
                manga.setName(name);
                manga.setUrl(url);
                manga.setImage(image);
                mangas.add(manga);
            }
            return mangas;
        }
        return null;
    }

    public ArrayList<Category> getAllCategories() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_CATEGORY;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            ArrayList<Category> categories = new ArrayList<>();
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(TABLE_CATEGORY_NAME));
                String url = cursor.getString(cursor.getColumnIndex(TABLE_CATEGORY_URL));
                int page = cursor.getInt(cursor.getColumnIndex(TABLE_CATEGORY_PAGE));
                Category c = new Category();
                c.setName(name);
                c.setUrl(url);
                c.setPage(page);
                categories.add(c);
            }
            return categories;
        }
        return null;
    }

}
