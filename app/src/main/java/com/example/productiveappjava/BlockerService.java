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
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class BlockerService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    Date targetDate;
    long targetTime;
    String currentApp;
    String ignoreApp;
    Integer timeDiff;
    Boolean overlayActivate;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
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

        // Set up the imageView to display later - make this a video view later
        final ImageView blockImageView = new ImageView(getApplicationContext());
        // Set image resource
        blockImageView.setImageResource(R.drawable.green);
        // Set the image position
        blockImageView.setLayoutParams(new ViewGroup.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT));

        // Initialize the WindowManager
        final WindowManager blockWindowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        final WindowManager.LayoutParams blockLayoutParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        blockLayoutParams.gravity = Gravity.CENTER; // Set it so it stays in the center - fix later depending on where the vines come from

        final SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.preference_file_key), 0);
        final SharedPreferences.Editor prefsEditor = sharedPref.edit();

        overlayActivate = false;
        ignoreApp = "";

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
                    targetTime = System.currentTimeMillis() - 10;
                }
                if(currentTime >= targetTime) {
                    // If the target time is reached
                    prefsEditor.putBoolean("blockEnable", false);
                    prefsEditor.apply();
                    stopSelf();
                } else {
                    handler.postDelayed(this, 1000);
                }
                currentApp = null;
                timeDiff = 0;
                while(currentApp == null) {
                    timeDiff += 1000;
                    currentApp = getForegroundApp(getApplicationContext(), timeDiff, currentApp);
                }
                Log.i("BlockerService", "Current App: " + currentApp);

                if(sharedPref.getBoolean(currentApp, false)) { // If the app is supposed to be blocked
                    Log.i("BlockerService", "Gotcha! You're not supposed to be using the app: " + currentApp);
                    if(!overlayActivate) {
                        overlayActivate = true;
                        blockWindowManager.addView(blockImageView, blockLayoutParams);
                        /*if(ignoreApp == "") {
                            findOverlayApp();
                        }*/
                    }
                } else {
                    Log.i("BlockerService", "You're doing alright for now. Keep it up");
                    if(overlayActivate) {
                        overlayActivate = false;
                        blockWindowManager.removeView(blockImageView);
                    }
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
        final SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.preference_file_key), 0);
        if(sharedPref.getBoolean("blockEnable", false)) {
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

    public String getForegroundApp(Context context, Integer interval, String previousApp) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) context
                .getSystemService(Context.USAGE_STATS_SERVICE);
        long currTime = System.currentTimeMillis();
        List<UsageStats> queryUsageStats = usageStatsManager
                .queryUsageStats(UsageStatsManager.INTERVAL_BEST,
                        currTime - interval, currTime);
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
        if(recentStats.getPackageName().equals("android")) {
            return previousApp;
        } else {
            return recentStats.getPackageName();
        }
    }

    public void findOverlayApp() {
        String previousApp = currentApp = getForegroundApp(getApplicationContext(), 1000, currentApp);
        while(previousApp.equals(currentApp)) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            timeDiff = 0;
            currentApp = null;
            while(currentApp == null) {
                timeDiff += 1000;
                currentApp = getForegroundApp(getApplicationContext(), timeDiff, currentApp);
            }
            Log.i("BlockerService", "Current app: " + currentApp);
        }
        ignoreApp = currentApp;
    }
}
