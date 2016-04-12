package fyp.qian3.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import fyp.qian3.R;
import fyp.qian3.lib.db.Database;
import fyp.qian3.lib.srv.PedoEvent;
import fyp.qian3.lib.srv.PedoEventService;

public class StatsAct extends Activity {

    boolean mPedoSrvBound = false;
    PedoEventService.PedoSrvBinder mPedoSrvBinder;
    ServiceConnection mConnection;
    private TextView record, totalThisWeek, totalThisMonth, averageThisWeek, averageThisMonth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        record = (TextView) findViewById(R.id.record);
        totalThisWeek = (TextView) findViewById(R.id.totalthisweek);
        totalThisMonth = (TextView) findViewById(R.id.totalthismonth);
        averageThisWeek = (TextView) findViewById(R.id.averagethisweek);
        averageThisMonth = (TextView) findViewById(R.id.averagethismonth);



        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                mPedoSrvBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mPedoSrvBound = false;
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind with the service
        bindService(new Intent(this, PedoEventService.class), mConnection, Context.BIND_AUTO_CREATE);


        int stepFromToday = mPedoSrvBinder.getCurrStep();

        Database db = Database.getInstance(this);



        //test
        Calendar c = Calendar.getInstance();
        db.setDate(100);


        Calendar fromDay = Calendar.getInstance();
        fromDay.add(Calendar.DATE, -6);
        Calendar endDay = Calendar.getInstance();


        // int a = 11;

        // record.setText(String.valueOf(a));
       int daysThisMonth = endDay.get(Calendar.DAY_OF_MONTH);

        int thisWeek = db.getWeekOrMonthStep(fromDay, endDay) + stepFromToday;




        Pair<Integer, Integer> highestRecord = db.getRecordData();
        Pair<Integer, Integer> highestDay = db.getMonthDate(highestRecord.first.intValue());

        record.setText(String.valueOf(highestRecord.second.intValue())+ " steps at " +
                        String.valueOf(highestDay.second.intValue() + " / " +
                                String.valueOf(highestDay.first.intValue()))
        );

        totalThisWeek.setText(String.valueOf(thisWeek) + " steps");

        fromDay.set(Calendar.DATE, 1);
        int thisMonth =  db.getWeekOrMonthStep(fromDay, endDay) + stepFromToday;

        totalThisMonth.setText(String.valueOf(thisMonth) + " steps");

        averageThisWeek.setText(String.valueOf(thisWeek/7) + " steps");
        averageThisMonth.setText(String.valueOf(thisMonth / daysThisMonth) + " steps");

        db.close();




        barChart();


    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unbind from the service
        if (mPedoSrvBound) {
            unbindService(mConnection);
            mPedoSrvBound = false;
        }
    }

    private void barChart(){
        Database db = Database.getInstance(this);
        Calendar sevenDayBefore = Calendar.getInstance();
        sevenDayBefore.add(Calendar.DATE, -7);

        int step;
        BarModel bm;
        BarChart barChart = (BarChart) findViewById(R.id.bargraph);
        if (barChart.getData().size() > 0)
            barChart.clearChart();

        for(int i = 0; i < 7; i++){
            step = db.getDateStep(sevenDayBefore);
            float date;

            if (step > 0) {

                int day = sevenDayBefore.get(Calendar.DATE);
                int month = sevenDayBefore.get(Calendar.MONTH);

                date = day *1000 +  month;
                bm = new BarModel(date, 0);
                bm.setValue(step);
                barChart.addBar(bm);
            }
            sevenDayBefore.add(Calendar.DATE, 1);
        }

        if(barChart.getData().size() > 0){
            barChart.startAnimation();
        }
        else
        {
            barChart.setVisibility(View.GONE);
        }

        db.close();


    }
}
