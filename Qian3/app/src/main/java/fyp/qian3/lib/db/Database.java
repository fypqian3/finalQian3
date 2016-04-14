package fyp.qian3.lib.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;

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
        db.execSQL("CREATE TABLE " + DB_NAME + " (" + DATE + " INTEGER, " + STEPS + " INTEGER)");
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
        Log.i("Database", "getMultiDatabaseItem: " + String.valueOf(getMultiDatabaseItem(id, 1)[0].steps) + " " + String.valueOf(getMultiDatabaseItem(id, 1)[0].getMonth())
        + String.valueOf(getMultiDatabaseItem(id, 1)[0].getDay()));
        return dbi;
    }

    public int getSteps(Calendar date) {
        return getSteps(cvtCalendarToID(date));
    }

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

    /**
    * @param startID       Start date represented by id
    * @param number     Amount of days that you want to get (Pass 1 means get the data of start date only)
    * @return DatabaseItem array
    */
    public DatabaseItem[] getMultiDatabaseItem(int startID, int number) {
        // Index of dbarray
        int i = 0;
        DatabaseItem[] dbarray = new DatabaseItem[number];

        Cursor cursor = getReadableDatabase().query(
                DB_NAME, null, DATE+" >= "+String.valueOf(startID), null, null, null, null, String.valueOf(number)
        );

        if (cursor.moveToNext()) {
            do {
                dbarray[i] = new DatabaseItem(cursor.getInt(0), cursor.getInt(1));
                i++;
            } while (cursor.moveToNext());
        }
        // If require date is not existed, return DatabaseItem(0, 0)
        for (; i<number; i++) {
            dbarray[i] = new DatabaseItem(0, 0);
        }
        cursor.close();
        return dbarray;
    }

    /**
     * @param startID       Start date represented by id
     * @param number     Amount of days that you want to get (Pass 1 means get the data of start date only)
     * @return Sum of steps
     */
    public int getStepsSum (int startID, int number) {
        int stepSum = 0;
        Cursor cursor = getReadableDatabase().query(
                DB_NAME, null, DATE+" >= "+String.valueOf(startID), null, null, null, null, String.valueOf(number)
        );
        if (cursor.moveToFirst()) {
            do {
                stepSum += cursor.getInt(1);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return stepSum;
    }

    //Get the highest record
    public Pair<Integer, Integer> getRecordData() {
        Cursor c = getReadableDatabase()
                .query(DB_NAME, new String[]{"date, steps"}, "date > 0", null, null, null,
                        "steps DESC", "1");
        c.moveToFirst();
        Pair<Integer, Integer> p = new Pair<Integer, Integer>(c.getInt(0), c.getInt(1));
        c.close();
        return p;
    }

}
