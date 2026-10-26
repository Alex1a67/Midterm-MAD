package com.example.midterm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StudentDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "student.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "students";
    public static final String COL_ID = "_id";
    public static final String COL_NAME = "student_name";
    public static final String COL_STUDENT_ID = "student_id";
    public static final String COL_ENROLLED = "enrolled";

    public StudentDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_STUDENT_ID + " TEXT NOT NULL UNIQUE, "
                + COL_ENROLLED + " INTEGER NOT NULL DEFAULT 0)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Insert a new student. Returns -1 if failed (e.g. duplicate student_id).
    public long insertStudent(String name, String studentId, boolean enrolled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_STUDENT_ID, studentId);
        values.put(COL_ENROLLED, enrolled ? 1 : 0);
        long result = db.insert(TABLE_NAME, null, values);
        return result;
    }

    // Update an existing student by its row _id.
    public int updateStudent(long id, String name, String studentId, boolean enrolled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_STUDENT_ID, studentId);
        values.put(COL_ENROLLED, enrolled ? 1 : 0);
        return db.update(TABLE_NAME, values, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Delete a student by its row _id.
    public int deleteStudent(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Get all students.
    public Cursor getAllStudents() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_ID + " ASC", null);
    }

    // Check if a student_id already exists (optionally excluding a row _id for edit).
    public boolean isStudentIdExists(String studentId, long excludeRowId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query;
        String[] args;
        if (excludeRowId >= 0) {
            query = "SELECT 1 FROM " + TABLE_NAME + " WHERE " + COL_STUDENT_ID + "=? AND " + COL_ID + "!=?";
            args = new String[]{studentId, String.valueOf(excludeRowId)};
        } else {
            query = "SELECT 1 FROM " + TABLE_NAME + " WHERE " + COL_STUDENT_ID + "=?";
            args = new String[]{studentId};
        }
        Cursor c = db.rawQuery(query, args);
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }
}
