package com.teamtreehouse.mememaker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.teamtreehouse.mememaker.models.Meme;
import com.teamtreehouse.mememaker.models.MemeAnnotation;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Boban Talevski on 10/19/2017.
 */

public class MemeDataSource {

    private Context context;
    private MemeSQLiteHelper memeSQLiteHelper;

    public MemeDataSource(Context context) {
        this.context = context;
        memeSQLiteHelper = new MemeSQLiteHelper(context);
    }

    private SQLiteDatabase open() {
        return memeSQLiteHelper.getWritableDatabase();
    }

    private void close(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.close();
    }

    public void delete(int memeId){
        SQLiteDatabase database = open();
        database.beginTransaction();
        // boilerplate above

        // implementation details
        // deleting record(s) from the annotation table first cause of FK contraint
        database.delete(MemeSQLiteHelper.ANNOTATIONS_TABLE,
                String.format("%s=%s", MemeSQLiteHelper.COLUMN_FOREIGN_KEY_MEME, String.valueOf(memeId)),
                null);
        // deleting the record from the meme table
        database.delete(MemeSQLiteHelper.MEMES_TABLE,
                String.format(("%s=%s"), BaseColumns._ID, String.valueOf(memeId)),
                null);

        // boilerplate below
        database.setTransactionSuccessful();
        database.endTransaction();
        close(database);
    }

    // method for combining the results in one complete Meme ArrayList model object containing all
    // the memes with their appropriate lists of annotations.
    // Calls both readMemes() and addMemeAnnotations(ArrayList<Meme> meme) methods
    public ArrayList<Meme> read() {
        ArrayList<Meme> memes = readMemes();
        addMemeAnnotations(memes);
        return memes;
    }

    // read only the memes without annotations from the memes table
    public ArrayList<Meme> readMemes() {
        SQLiteDatabase database = open();

        Cursor cursor = database.query(
                MemeSQLiteHelper.MEMES_TABLE,
                new String[] {MemeSQLiteHelper.COLUMN_MEME_NAME, BaseColumns._ID, MemeSQLiteHelper.COLUMN_MEME_ASSET},
                null, // selection
                null, // selection args
                null, // group by
                null, // having
                MemeSQLiteHelper.COLUMN_MEME_CREATE_DATE + " DESC"); // order
        // there's another: limit, but not used

        ArrayList<Meme> memes =  new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Meme meme = new Meme(getIntFromColumnName(cursor, BaseColumns._ID),
                        getStringFromColumnName(cursor, MemeSQLiteHelper.COLUMN_MEME_ASSET),
                        getStringFromColumnName(cursor, MemeSQLiteHelper.COLUMN_MEME_NAME),
                        null);
                memes.add(meme);
            } while (cursor.moveToNext());
        }
        cursor.close();
        close(database);
        return memes;
    }

    // read annotations from the annotations table for each of the memes passed in the
    // ArrayList parameter and add them inside that object to have a complete list of memes with
    // respective annotations from the annotations table
    public void addMemeAnnotations(ArrayList<Meme> memes) {
        SQLiteDatabase database = open();

        for (Meme meme : memes) {
            ArrayList<MemeAnnotation> annotaions = new ArrayList<>();
            Cursor cursor = database.rawQuery(
                    "SELECT * FROM " + MemeSQLiteHelper.ANNOTATIONS_TABLE +
                            " WHERE MEME_ID = " + meme.getId(), null);
            if (cursor.moveToFirst()) {
                do {
                    MemeAnnotation annotation = new MemeAnnotation(
                            getIntFromColumnName(cursor, BaseColumns._ID),
                            getStringFromColumnName(cursor, MemeSQLiteHelper.COLUMN_ANNOTATION_COLOR),
                            getStringFromColumnName(cursor, MemeSQLiteHelper.COLUMN_ANNOTATION_TITLE),
                            getIntFromColumnName(cursor, MemeSQLiteHelper.COLUMN_ANNOTATION_X),
                            getIntFromColumnName(cursor, MemeSQLiteHelper.COLUMN_ANNOTATION_Y)
                    );
                    annotaions.add(annotation);
                } while (cursor.moveToNext());
            }
            meme.setAnnotations(annotaions);
            cursor.close();
        }
        close(database);
    }

    public void update(Meme meme) {
        SQLiteDatabase database = open();
        database.beginTransaction();

        ContentValues updateMemeValues = new ContentValues();
        updateMemeValues.put(MemeSQLiteHelper.COLUMN_MEME_NAME, meme.getName());
        database.update(MemeSQLiteHelper.MEMES_TABLE,
                updateMemeValues,
                String.format("%s=%d", BaseColumns._ID, meme.getId()),
                null);
        for (MemeAnnotation annotation : meme.getAnnotations()) {
            ContentValues updateAnnotations = new ContentValues();
            updateAnnotations.put(MemeSQLiteHelper.COLUMN_ANNOTATION_TITLE, annotation.getTitle());
            updateAnnotations.put(MemeSQLiteHelper.COLUMN_ANNOTATION_X, annotation.getLocationX());
            updateAnnotations.put(MemeSQLiteHelper.COLUMN_ANNOTATION_Y, annotation.getLocationY());
            updateAnnotations.put(MemeSQLiteHelper.COLUMN_FOREIGN_KEY_MEME, meme.getId());
            updateAnnotations.put(MemeSQLiteHelper.COLUMN_ANNOTATION_COLOR, annotation.getColor());

            // checking to see if we are to update an existing annotation or add a new annotation
            // for the meme that was passed in this method.
            // The method is called with a Meme object and we are only using its meme_id to update
            // the database records with all the values in the said object,
            // including the list of annotations. So we might have updates to some annotations
            // or we might have added a new annotation from scratch for the said meme object.
            if(annotation.hasBeenSaved()) {
                // if we are updating an annotation
                database.update(MemeSQLiteHelper.ANNOTATIONS_TABLE,
                        updateAnnotations,
                        String.format("%S=%d", BaseColumns._ID, annotation.getId()),
                        null);
            } else { // if we are creating a new annotation for the same meme
                database.insert(MemeSQLiteHelper.ANNOTATIONS_TABLE,
                        null,
                        updateAnnotations);
            }
        }

        database.setTransactionSuccessful();
        database.endTransaction();
        close(database);
    }

    private int getIntFromColumnName(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getInt(columnIndex);
    }

    private String getStringFromColumnName(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getString(columnIndex);
    }

    public void create(Meme meme) {
        SQLiteDatabase database = open();
        database.beginTransaction();
        // boilerplate above

        // implementation details
        ContentValues memeValues = new ContentValues();
        memeValues.put(MemeSQLiteHelper.COLUMN_MEME_NAME, meme.getName());
        memeValues.put(MemeSQLiteHelper.COLUMN_MEME_ASSET, meme.getAssetLocation());
        memeValues.put(MemeSQLiteHelper.COLUMN_MEME_CREATE_DATE, new Date().getTime());
        long memeId = database.insert(MemeSQLiteHelper.MEMES_TABLE, null, memeValues);

        for (MemeAnnotation annotation : meme.getAnnotations()) {
            ContentValues annotationValues = new ContentValues();
            annotationValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_COLOR, annotation.getColor());
            annotationValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_TITLE, annotation.getTitle());
            annotationValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_X, annotation.getLocationX());
            annotationValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_Y, annotation.getLocationY());
            annotationValues.put(MemeSQLiteHelper.COLUMN_FOREIGN_KEY_MEME, memeId);

            database.insert(MemeSQLiteHelper.ANNOTATIONS_TABLE, null, annotationValues);
        }

        // boilerplate below
        database.setTransactionSuccessful();
        database.endTransaction();
        close(database);
    }
}
