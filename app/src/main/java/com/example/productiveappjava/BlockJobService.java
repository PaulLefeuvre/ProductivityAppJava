package com.example.productiveappjava;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.RequiresApi;

public class BlockJobService extends JobService {
    private static StartServiceReceiver startServiceReceiver;
    private static JobService instance;
    private static JobParameters jobParameters;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onStartJob(JobParameters params) {
        Util utilClass = new Util();
        utilClass.launchService(this);
        registerStartReceiver();
        instance = this;

        return false;
    }

    private void registerStartReceiver() {
        // Context for the StartServiceReciever can be null if this is called from the BlockerService
        // If we try to call it then, it will crash and give an error, so we need to register it
        if (startServiceReceiver == null)
            startServiceReceiver = new StartServiceReceiver();
        else try {
            unregisterReceiver(startServiceReceiver);
        } catch (Exception e){
            // Not registered
        }
        // Give the time to run
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // we register the  receiver that will restart the background service if it is killed
                // see onDestroy of Service
                IntentFilter filter = new IntentFilter();
                filter.addAction(String.valueOf(R.string.broadcastContext));
                try {
                    registerReceiver(startServiceReceiver, filter);
                } catch (Exception e) {
                    try {
                        getApplicationContext().registerReceiver(startServiceReceiver, filter);
                    } catch (Exception ex) {

                    }
                }
            }
        }, 1000);

    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Intent broadcastIntent = new Intent(String.valueOf(R.string.broadcastContext));
        sendBroadcast(broadcastIntent);
        // give the time to run
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                unregisterReceiver(startServiceReceiver);
            }
        }, 1000);

        return true;
    }
}