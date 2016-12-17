package com.codeoregonapp.patrickleonard.mememaker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.codeoregonapp.patrickleonard.mememaker.models.Meme;
import com.codeoregonapp.patrickleonard.mememaker.models.MemeAnnotation;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Evan Anger on 8/17/14.
 */
public class MemeDatasource {

    private Context mContext;
    private MemeSQLiteHelper mMemeSqlLiteHelper;

    public MemeDatasource(Context context) {
        mContext = context;
        mMemeSqlLiteHelper = new MemeSQLiteHelper(context);
    }

    private SQLiteDatabase open() {
        return mMemeSqlLiteHelper.getWritableDatabase();
    }

    private void close(SQLiteDatabase db) {
        db.close();
    }

    public void create(Meme meme) {
        SQLiteDatabase db = open();

        db.beginTransaction();

        ContentValues memeValues = new ContentValues();

        memeValues.put(MemeSQLiteHelper.COLUMN_MEME_NAME,meme.getName());
        memeValues.put(MemeSQLiteHelper.COLUMN_MEME_ASSET, meme.getAssetLocation());
        memeValues.put(MemeSQLiteHelper.COLUMN_MEME_CREATE_DATE,new Date().getTime());
        long memeId = db.insert(MemeSQLiteHelper.MEMES_TABLE,null,memeValues);

        for(MemeAnnotation annotation: meme.getAnnotations()) {
            ContentValues annotationValues = new ContentValues();
            annotationValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_TITLE,annotation.getTitle());
            annotationValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_COLOR,annotation.getColor());
            annotationValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_X,annotation.getLocationX());
            annotationValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_Y,annotation.getLocationY());
            annotationValues.put(MemeSQLiteHelper.COLUMN_FOREIGN_KEY_MEME,memeId);

            db.insert(MemeSQLiteHelper.ANNOTATIONS_TABLE,null,annotationValues);
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        close(db);
    }

    public void delete(int id) {
        SQLiteDatabase db = open();
        db.beginTransaction();
        db.delete(MemeSQLiteHelper.ANNOTATIONS_TABLE,
                String.format("%s=%s", MemeSQLiteHelper.COLUMN_FOREIGN_KEY_MEME, String.valueOf(id))
                , null);
        db.delete(MemeSQLiteHelper.MEMES_TABLE,
                String.format("%s=%s",BaseColumns._ID,String.valueOf(id))
                ,null);
        db.setTransactionSuccessful();
        db.endTransaction();
        close(db);
    }

    public void update(Meme meme) {
        SQLiteDatabase db = open();
        db.beginTransaction();

        ContentValues updateMemeValues = new ContentValues();

        updateMemeValues.put(MemeSQLiteHelper.COLUMN_MEME_NAME, meme.getName());
        db.update(MemeSQLiteHelper.MEMES_TABLE,
                updateMemeValues,
                String.format("%s=%d", BaseColumns._ID, meme.getId())
                , null);

        for(MemeAnnotation annotation: meme.getAnnotations()) {
            ContentValues updateAnnotations = new ContentValues();
            updateAnnotations.put(MemeSQLiteHelper.COLUMN_ANNOTATION_TITLE,annotation.getTitle());
            updateAnnotations.put(MemeSQLiteHelper.COLUMN_ANNOTATION_COLOR,annotation.getColor());
            updateAnnotations.put(MemeSQLiteHelper.COLUMN_FOREIGN_KEY_MEME,meme.getId());
            updateAnnotations.put(MemeSQLiteHelper.COLUMN_ANNOTATION_X,annotation.getLocationX());
            updateAnnotations.put(MemeSQLiteHelper.COLUMN_ANNOTATION_Y,annotation.getLocationY());

            if(annotation.hasBeenSaved()) {
                db.update(MemeSQLiteHelper.ANNOTATIONS_TABLE,
                        updateAnnotations,
                        String.format("%s=%d",BaseColumns._ID,annotation.getId()),
                        null);
            }
            else {
                db.insert(MemeSQLiteHelper.ANNOTATIONS_TABLE,
                        null,updateAnnotations);
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        close(db);
    }

    public ArrayList<Meme> read() {
        ArrayList<Meme> memes = readMemes();
        addMemeAnnotations(memes);
        return memes;
    }

    public ArrayList<Meme> readMemes() {
        SQLiteDatabase db = open();

        Cursor cursor = db.query(MemeSQLiteHelper.MEMES_TABLE,
                new String[] {MemeSQLiteHelper.COLUMN_MEME_NAME, BaseColumns._ID,MemeSQLiteHelper.COLUMN_MEME_ASSET},
                null,  //Selection
                null,  //Selection args
                null,  //Group By
                null,  //Having
                MemeSQLiteHelper.COLUMN_MEME_CREATE_DATE + " DESC",  //Order
                null); //Limit

        ArrayList<Meme> memes = new ArrayList<>();

        if(cursor.moveToFirst()) {
            do{
                Meme meme = new Meme(getIntFromColumnName(cursor,BaseColumns._ID),
                                     getStringFromColumnName(cursor,MemeSQLiteHelper.COLUMN_MEME_ASSET),
                                     getStringFromColumnName(cursor,MemeSQLiteHelper.COLUMN_MEME_NAME),
                                    null);

                memes.add(meme);
            }while(cursor.moveToNext());
        }
        cursor.close();
        close(db);
        return memes;
    }

    public void addMemeAnnotations(ArrayList<Meme> memes) {
        SQLiteDatabase db = open();

        for(Meme meme: memes) {
            ArrayList<MemeAnnotation> memeAnnotations = new ArrayList<>();
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM " + MemeSQLiteHelper.ANNOTATIONS_TABLE +
                    " WHERE MEME_ID  = " + meme.getId(),null);

            if(cursor.moveToFirst()) {
                do {
                    MemeAnnotation annotation = new MemeAnnotation(
                            getIntFromColumnName(cursor,BaseColumns._ID),
                            getStringFromColumnName(cursor,MemeSQLiteHelper.COLUMN_ANNOTATION_COLOR),
                            getStringFromColumnName(cursor,MemeSQLiteHelper.COLUMN_ANNOTATION_TITLE),
                            getIntFromColumnName(cursor,MemeSQLiteHelper.COLUMN_ANNOTATION_X),
                            getIntFromColumnName(cursor,MemeSQLiteHelper.COLUMN_ANNOTATION_Y));
                    memeAnnotations.add(annotation);
                }while(cursor.moveToNext());
            }
            meme.setAnnotations(memeAnnotations);
            cursor.close();
        }
        close(db);
    }

    private int getIntFromColumnName(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getInt(columnIndex);
    }

    private String getStringFromColumnName(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getString(columnIndex);
    }

}
