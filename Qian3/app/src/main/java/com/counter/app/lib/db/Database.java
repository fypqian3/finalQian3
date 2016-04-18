package com.counter.app.lib.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

public class Database extends SQLiteOpenHelper {

    private final static String DB_NAME = "steps_database";
    private final static int DB_VERSION = 1;

    // form field name
    private static final String DATE = "date";
    private static final String STEPS = "steps";

    private static Database instance;
    private static final AtomicInteger openCounter = new AtomicInteger();

    private Database(final Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static synchronized Database getInstance(final Context c) {
        if (instance == null) {
            instance = new Database(c.getApplicationContext());
        }
        openCounter.incrementAndGet();
        return instance;
    }

    @Override
    public void close() {
        if (openCounter.decrementAndGet() == 0) {
            super.close();
        }
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + DB_NAME +
                        " (" + DATE + " INTEGER, " + STEPS + " INTEGER)"
        );
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static int cvtCalendarToID (Calendar date) {
        int y = date.get(Calendar.YEAR);
        int m = date.get(Calendar.MONTH) + 1;
        int d = date.get(Calendar.DATE);
        return y*10000 + m*100 + d;
    }

    private DatabaseItem insert(DatabaseItem dbItem) {
        ContentValues cv = new ContentValues();
        cv.put(DATE, dbItem.id);
        cv.put(STEPS, dbItem.steps);
        getWritableDatabase().insert(DB_NAME, null, cv);
        return dbItem;
    }

    private boolean update(DatabaseItem dbItem) {
        ContentValues cv = new ContentValues();
        cv.put(DATE, dbItem.id);
        cv.put(STEPS, dbItem.steps);
        String where = DATE + "=" + String.valueOf(dbItem.id);
        return getWritableDatabase().update(DB_NAME, cv, where, null) > 0;
    }

    private boolean delete(int id) {
        String where = DATE + "=" + String.valueOf(id);
        return getWritableDatabase().delete(DB_NAME, where, null) > 0;
    }

    private void replace(DatabaseItem dbItem) {
        ContentValues cv = new ContentValues();
        cv.put(DATE, dbItem.id);
        cv.put(STEPS, dbItem.steps);
        getWritableDatabase().replace(DB_NAME, null, cv);
    }

    public DatabaseItem setSteps(Calendar date, int steps) {
        return setSteps(cvtCalendarToID(date), steps);
    }

    public DatabaseItem setSteps(int id, int steps) {
        DatabaseItem dbi = new DatabaseItem(id, steps);
        if (!update(dbi)) {
            insert(dbi);
        }
        //replace(dbi);
        Log.i("Database", "Input: " + String.valueOf(steps) + " " + String.valueOf(id));
        Log.i("Database", "getSteps: " + String.valueOf(getSteps(id)));
        Log.i("Database", "getDatabaseItem: " + String.valueOf(getDatabaseItem(id, 1)[0].steps) + " " + String.valueOf(getDatabaseItem(id, 1)[0].getMonth())
                + String.valueOf(getDatabaseItem(id, 1)[0].getDay()));
        return dbi;
    }

    /**
     * @param date       Date represented by id
     * @return Steps
     */
    public int getSteps(Calendar date) {
        return getSteps(cvtCalendarToID(date));
    }

    /**
     * @param id       Date represented by Calendar
     * @return Steps
     */
    public int getSteps(int id) {
        int steps = 0;
        String where = DATE + "=" + String.valueOf(id);
        Cursor cursor = getReadableDatabase().query(
                DB_NAME, null, where, null, null, null, null, null
        );
        if (cursor.moveToFirst()) {
            steps = cursor.getInt(1);
        }
        cursor.close();
        return steps;
    }

    /**
     * @param endID       End date represented by id
     * @param number     Amount of days that you want to get (Pass 1 means get the data of end date only,
     *                                    2 means get the data of end date and the last date of end date, etc.)
     * @return DatabaseItem array
     */
    public DatabaseItem[] getDatabaseItem(int endID, int number) {
        // Index of dbarray
        int i = 0;
        DatabaseItem[] dbarray = new DatabaseItem[number];

        Cursor cursor = getReadableDatabase().query(
                DB_NAME, null, DATE+" <= "+String.valueOf(endID),
                null, null, null, DATE+" DESC", String.valueOf(number)
        );

        if (cursor.moveToNext()) {
            do {
                dbarray[i] = new DatabaseItem(cursor.getInt(0), cursor.getInt(1));
                i++;
            } while (cursor.moveToNext());
        }
        // If require date is not existed, return DatabaseItem(0, 0)
        for (; i<number; i++) {
            dbarray[i] = null;
        }
        cursor.close();
        return dbarray;
    }

    /**
     * @param endC       End date represented by Calendar
     * @param number     Amount of days that you want to get (Pass 1 means get the data of end date only,
     *                                    2 means get the data of end date and the last date of end date, etc.)
     * @return DatabaseItem array
     */
    public DatabaseItem[] getDatabaseItem(Calendar endC, int number) {
        return getDatabaseItem(cvtCalendarToID(endC), number);
    }

    /**
     * @param endID       End date represented by id
     * @param number     Amount of days that you want to get (Pass 1 means get the data of end date only,
     *                                    2 means get the data of end date and the last date of end date, etc.)
     * @return Sum of steps
     */
    public int getStepSumWithNum(int endID, int number) {
        int stepSum = 0;
        Cursor cursor = getReadableDatabase().query(
                DB_NAME, new String[]{"sum("+STEPS+")"}, DATE+" <= "+String.valueOf(endID),
                null, null, null, DATE+" DESC", String.valueOf(number)
        );
        if (cursor.moveToFirst()) {
            stepSum = cursor.getInt(0);
        }
        cursor.close();
        return stepSum;
    }

    /**
     * @param endC       End date represented by Calendar
     * @param number     Amount of days that you want to get (Pass 1 means get the data of end date only,
     *                                    2 means get the data of end date and the last date of end date, etc.)
     * @return Sum of steps
     */
    public int getStepSumWithNum(Calendar endC, int number) {
        return getStepSumWithNum(cvtCalendarToID(endC), number);
    }

    /**
     * @param startID   Start date represented by id
     * @param endID     End date represented by id
     * @return Sum of steps
     */
    public int getStepSum(int startID, int endID) {
        int stepSum = 0;
        Cursor cursor = getReadableDatabase().query(
                DB_NAME, new String[]{"sum("+STEPS+")"}, DATE+" >= ? AND "+DATE+" <= ?",
                new String[]{String.valueOf(startID), String.valueOf(endID)}, null, null, null
        );
        if (cursor.moveToFirst()) {
            stepSum = cursor.getInt(0);
        }
        cursor.close();
        return stepSum;
    }

    /**
     * @param startC   Start date represented by Calendar
     * @param endC     End date represented by Calendar
     * @return Sum of steps
     */
    public int getStepSum(Calendar startC, Calendar endC) {
        return getStepSum(cvtCalendarToID(startC), cvtCalendarToID(endC));
    }

    /**
     * @param number     Amount of days that you want to get
     * @return DatabaseItem array
     */
    public DatabaseItem[] getMaxSteps(int number) {
        // Index of dbarray
        int i = 0;
        DatabaseItem[] dbarray = new DatabaseItem[number];

        Cursor cursor = getReadableDatabase().query(
                DB_NAME, null, null, null, null, null, STEPS + " DESC", String.valueOf(number)
        );
        if (cursor.moveToFirst()) {
            do {
                dbarray[i] = new DatabaseItem(cursor.getInt(0), cursor.getInt(1));
                i++;
            } while (cursor.moveToNext());
        }
        // If require date is not existed, return DatabaseItem(0, 0)
        for (; i<number; i++) {
            dbarray[i] = null;
        }
        cursor.close();
        return dbarray;
    }


    public Cursor query(final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        return getReadableDatabase()
                .query(DB_NAME, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }
}