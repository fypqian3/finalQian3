package fyp.qian3.lib.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by user on 11/4/2016.
 */
public class Database extends SQLiteOpenHelper {

    private final static String DB_NAME = "steps";
    private final static int DB_VERSION = 2;

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
        db.execSQL("CREATE TABLE " + DB_NAME + " (date INTEGER, steps INTEGER)");
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            // drop PRIMARY KEY constraint
            db.execSQL("CREATE TABLE " + DB_NAME + "2 (date INTEGER, steps INTEGER)");
            db.execSQL("INSERT INTO " + DB_NAME + "2 (date, steps) SELECT date, steps FROM " +
                    DB_NAME);
            db.execSQL("DROP TABLE " + DB_NAME);
            db.execSQL("ALTER TABLE " + DB_NAME + "2 RENAME TO " + DB_NAME + "");
        }
    }

    /**
     * Query the 'steps' table. Remember to close the cursor!
     *
     * @param columns       the colums
     * @param selection     the selection
     * @param selectionArgs the selction arguments
     * @param groupBy       the group by statement
     * @param having        the having statement
     * @param orderBy       the order by statement
     * @return the cursor
     */
    public Cursor query(final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        return getReadableDatabase()
                .query(DB_NAME, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public void updateEverySteps(Calendar date, int steps) {
        //date = Calendar.getInstance();
        int month = date.get(Calendar.MONTH) + 1;
        int dates = date.get(Calendar.DATE);
        int dbDate = month * 100 + dates;
        getWritableDatabase().execSQL("UPDATE " + DB_NAME + " SET steps = steps + " + steps + " WHERE date = " + dbDate);
    }

    //insert  a new row
    //not get current date
    public void setDate(Calendar date, int steps){
        getWritableDatabase().beginTransaction();
        //Calendar date = Calendar.getInstance();


        int month = date.get(Calendar.MONTH) + 1;
        int dates = date.get(Calendar.DATE);
        int dbDate = month * 100 + dates;
        ContentValues values = new ContentValues();
        values.put("date", dbDate);
        values.put("steps", steps);
        getWritableDatabase().insert(DB_NAME, null, values);
        getWritableDatabase().setTransactionSuccessful();
        getWritableDatabase().endTransaction();
    }

    public int getDateStep(Calendar date){
        //date = Calendar.getInstance();
        int month = date.get(Calendar.MONTH) + 1;
        int dates = date.get(Calendar.DATE);
        int dbDate = month * 100 + dates;
        Cursor c = getReadableDatabase().query(DB_NAME, new String[]{"steps"}, "date = ?",
                new String[]{String.valueOf(dbDate)}, null, null, null);
        c.moveToFirst();
        int re;
        if (c.getCount() == 0) re = Integer.MIN_VALUE;
        else re = c.getInt(0);
        c.close();
        return re;
    }

    public int getWeekOrMonthStep(Calendar start, Calendar end){
        // start = Calendar.getInstance();
        //end = Calendar.getInstance();
        int month = start.get(Calendar.MONTH) + 1;
        int dates = start.get(Calendar.DATE);
        int dbDate = month * 100 + dates;

        int month1 = start.get(Calendar.MONTH) + 1;
        int dates1 = start.get(Calendar.DATE);
        int dbDate1 = month1 * 100 + dates1;

        Cursor c = getReadableDatabase()
                .query(DB_NAME, new String[]{"SUM(steps)"}, "date >= ? AND date <= ?",
                        new String[]{String.valueOf(dbDate), String.valueOf(dbDate1)}, null, null, null);
        int re;
        if (c.getCount() == 0) {
            re = 0;
        } else {
            c.moveToFirst();
            re = c.getInt(0);
        }
        c.close();
        return re;
    }

    public Pair<Integer, Integer> getMonthDate(int date){

        int month = date / 100;
        int day = date % 100;

        Pair<Integer, Integer> p = new Pair<Integer, Integer>( month, day);
        return p;
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
   /* public void insertNewDay(long date, int steps) {
        getWritableDatabase().beginTransaction();
        try {
            Cursor c = getReadableDatabase().query(DB_NAME, new String[]{"date"}, "date = ?",
                    new String[]{String.valueOf(date)}, null, null, null);
            if (c.getCount() == 0 && steps >= 0) {
                ContentValues values = new ContentValues();
                values.put("date", date);
                // use the negative steps as offset
                values.put("steps", -steps);
                getWritableDatabase().insert(DB_NAME, null, values);

                // add 'steps' to yesterdays count

            }
            c.close();

            getWritableDatabase().setTransactionSuccessful();
        } finally {
            getWritableDatabase().endTransaction();
        }
    }

    public int getRecord() {
        Cursor c = getReadableDatabase()
                .query(DB_NAME, new String[]{"MAX(steps)"}, "date > 0", null, null, null, null);
        c.moveToFirst();
        int re = c.getInt(0);
        c.close();
        return re;
    }*/


}
