package com.example.productiveappjava;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/*
IMPORTANT!!!
This file needs to be completely changed to increase its accuracy in tracking user app usage.
The function queryAndAggregateUsageStats() is very inconsistent in its data and therefore is not a good idea for the intended purpose
Research needs to be done into the function queryEvents() which seems to be more consistent, as per this Stack Overflow question:
https://stackoverflow.com/questions/36238481/android-usagestatsmanager-not-returning-correct-daily-results
Improve in the near future
 */



public class ChartActivity extends AppCompatActivity {
    private HorizontalBarChart usageChart;
    private ArrayList<String> appNames = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        // Change the Action Bar title to 'Usage Statistics'
        ActionBar actionBar = getSupportActionBar(); // or getActionBar();
        getSupportActionBar().setTitle("Usage Statistics"); // set the top title

        usageChart = (HorizontalBarChart) findViewById(R.id.usage_barchart);

        BarData dailyData = getDailyValues();
        configureChartAppearance();
        prepareChartData(dailyData);
    }

    private void configureChartAppearance() {
        usageChart.setDrawGridBackground(false);
        usageChart.setDrawValueAboveBar(false);

        usageChart.getDescription().setEnabled(false);

        XAxis xAxis = usageChart.getXAxis();
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                String[] convertedArray = appNames.toArray(new String[0]);
                return convertedArray[(int) value];
            }
        });


        YAxis leftAxis = usageChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = usageChart.getAxisRight();
        rightAxis.setDrawGridLines(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private BarData getDailyValues() {
        final PackageManager pm = getPackageManager();
        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        // The timezones we'll need
        ZoneId utc = ZoneId.of("UTC");
        ZoneId defaultZone = ZoneId.systemDefault();

        LocalDate date = LocalDate.now();

        // Set the starting and ending times to be midnight in UTC time
        ZonedDateTime startDate = date.atStartOfDay(defaultZone).withZoneSameInstant(utc);
        long start = startDate.toInstant().toEpochMilli();
        long end = startDate.plusDays(1).toInstant().toEpochMilli();

        // This will keep a map of all of the events per package name
        HashMap<String, ArrayList<UsageEvents.Event>> sortedEvents = new HashMap<>();

        UsageStatsManager usageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);

        ArrayList<BarEntry> values = new ArrayList<>();

        int timeVal;
        int counter = 0;

        // Query the list of events that has happened within that time frame
        UsageEvents systemEvents = usageStatsManager.queryEvents(start, end);
        while (systemEvents.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            systemEvents.getNextEvent(event);

            // Get the list of events for the package name, create one if it doesn't exist
            ArrayList<UsageEvents.Event> packageEvents = ((packageEvents = sortedEvents.get(event.getPackageName())) != null) ? packageEvents : new ArrayList<>();
            packageEvents.add(event);
            sortedEvents.put(event.getPackageName(), packageEvents);
        }

        // This will keep a list of our final stats
        HashMap<String, Long> stats = new HashMap<>();

        long startTime;
        long endTime;
        long totalTime;

        // Go through the events by package name
        Iterator it = sortedEvents.entrySet().iterator();
        for(Map.Entry<String, ArrayList<UsageEvents.Event>> entry : sortedEvents.entrySet()) {
            String packageName = entry.getKey();
            ArrayList<UsageEvents.Event> events = entry.getValue();
            // Keep track of the current start and end times
            startTime = 0;
            endTime = 0;
            // Keep track of the total usage time for this app
            totalTime = 0;
            // Keep track of the start times for this app
            ArrayList<ZonedDateTime> startTimes = new ArrayList<>();

            UsageEvents.Event e = new UsageEvents.Event();
            for(int i = 0; i < events.size(); i++) {
                e = events.get(i);

                if (e.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    // App was moved to the foreground: set the start time
                    startTime = e.getTimeStamp();
                } else if (e.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                    // App was moved to background: set the end time
                    endTime = e.getTimeStamp();
                }

                // If there's an end time with no start time, this might mean that
                //  The app was started on the previous day, so take midnight
                //  As the start time
                if (startTime == 0 && endTime != 0) {
                    startTime = start;
                }

                // If both start and end are defined, we have a session
                if (startTime != 0 && endTime != 0) {
                    // Add the session time to the total time
                    totalTime += endTime - startTime;
                    // Reset the start/end times to 0
                    startTime = 0;
                    endTime = 0;
                }
            }

            // If there is a start time without an end time, this might mean that
            //  the app was used past midnight, so take (midnight - 1 second) 
            //  as the end time
            if (startTime != 0 && endTime == 0) {
                totalTime += end - 1000 - startTime;
            }
            stats.put(packageName, totalTime);
        }

        for (ApplicationInfo packageInfo : packages) {
            if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                if(stats.get(packageInfo.packageName) != null) {
                    timeVal = (int) Math.floor(stats.get(packageInfo.packageName) / 60000.0);
                } else {timeVal = 0;}

                if(timeVal > 0) {
                    appNames.add(String.valueOf(packageInfo.loadLabel(pm)));
                    values.add(new BarEntry(counter, timeVal));
                    counter++;
                }
            }
        }

        BarDataSet dailySet = new BarDataSet(values, "Daily Usage");
        dailySet.setColors(ColorTemplate.MATERIAL_COLORS);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(dailySet);

        return new BarData(dataSets);
    }

    private void prepareChartData(BarData data) {
        data.setValueTextSize(12f);
        usageChart.setData(data);
        usageChart.setVisibleXRangeMaximum(6);
        usageChart.animateY(2000);
        usageChart.invalidate();
    }
}
