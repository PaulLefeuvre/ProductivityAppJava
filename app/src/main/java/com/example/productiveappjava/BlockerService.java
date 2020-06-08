package com.example.productiveappjava;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class BlockerService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    Date targetDate;
    long targetTime;
    String currentApp;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.notif_title))
                .setContentText(getString(R.string.notif_text))
                .setSmallIcon(R.drawable.eco)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setShowWhen(false)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        startForeground(1, notification);

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor prefsEditor = sharedPref.edit();

        final Handler handler = new Handler();
        final Runnable checkTime = new Runnable() {
            // The runnable that runs every second and checks if the timer is over,
            // as well as if the currently running app is to be blocked
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                String dateStr = sharedPref.getInt("chosenDay", 0) + "/" + (sharedPref.getInt("chosenMonth", 0)+1) + "/" + sharedPref.getInt("chosenYear", 0) + " " + sharedPref.getInt("chosenHour", 0) + ":" + sharedPref.getInt("chosenMinute", 0);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                try {
                    targetDate = sdf.parse(dateStr);
                    targetTime = targetDate.getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                    targetTime = System.currentTimeMillis();
                }
                if(currentTime >= targetTime) {
                    // If the target time is reached
                    prefsEditor.putBoolean("blockEnable", false);
                    prefsEditor.apply();
                    stopSelf();
                } else {
                    handler.postDelayed(this, 1000);
                }
                currentApp = getForegroundApp(getApplicationContext());
                if(currentApp != null) {
                    prefsEditor.putString(String.valueOf(R.string.currentAppKey), currentApp);
                    prefsEditor.commit();
                }
                Log.i("BlockerService", "currentApp value: " + currentApp);
                Log.i("BlockerService", "sharedPref value: " + sharedPref.getString(String.valueOf(R.string.currentAppKey), ""));
                if(sharedPref.getBoolean(String.valueOf(sharedPref.getString(String.valueOf(R.string.currentAppKey), "")), false)) {
                    Log.i("BlockerService", "Gotcha! You're not supposed to be using the app: " + currentApp);
                } else {
                    Log.i("BlockerService", "You're doing alright for now. Keep it up");
                }
            }
        };

        handler.post(checkTime);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // If the block is still on, restart the service. If not, do nothing and let the service be destroyed
        if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("blockEnable", false)) {
            Intent broadcastIntent = new Intent(this, StartServiceReceiver.class);
            sendBroadcast(broadcastIntent);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public static String getForegroundApp(Context context) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) context
                .getSystemService(Context.USAGE_STATS_SERVICE);
        long currTime = System.currentTimeMillis();
        List<UsageStats> queryUsageStats = usageStatsManager
                .queryUsageStats(UsageStatsManager.INTERVAL_BEST,
                        currTime - 1000, currTime);
        if (queryUsageStats == null || queryUsageStats.isEmpty()) {
            return null;
        }
        UsageStats recentStats = null;
        for (UsageStats usageStats : queryUsageStats) {
            if (recentStats == null
                    || recentStats.getLastTimeUsed() < usageStats.getLastTimeUsed()) {
                recentStats = usageStats;
            }
        }
        return recentStats.getPackageName();
    }
}
