package fyp.qian3.lib.db;


import android.text.format.Time;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by user on 8/4/2016.
 */
public abstract class Util {

    public static int getDateBaseToday() {
        Calendar c = Calendar.getInstance();
        int month = c.get(Calendar.MONTH) + 1;           //取出月，月份的編號是由0~11 故+1
        int day = c.get(Calendar.DAY_OF_MONTH);        //取出日
        int dbDate = month * 100 + day;
        return dbDate;
    }



}
