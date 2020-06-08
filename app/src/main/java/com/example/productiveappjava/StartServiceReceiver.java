package com.example.productiveappjava;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class StartServiceReceiver extends BroadcastReceiver {
    private static JobScheduler jobScheduler;
    private static ComponentName componentName;
    private StartServiceReceiver startServiceReceiver;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        scheduleJob(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void scheduleJob(Context context) {
        if (jobScheduler == null) {
            jobScheduler = (JobScheduler) context
                    .getSystemService(JOB_SCHEDULER_SERVICE);
        }
        componentName = new ComponentName(context,
                JobService.class);
        JobInfo jobInfo = new JobInfo.Builder(1, componentName)
                // setOverrideDeadline runs it immediately - you must have at least one constraint
                .setOverrideDeadline(0)
                // make sure the service persists despite device reboots
                .setPersisted(true)
                .build();
        jobScheduler.schedule(jobInfo);
    }
}
