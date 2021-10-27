package com.example.productiveappjava;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Change the Action Bar title to 'App Settings'
        ActionBar actionBar = getSupportActionBar(); // or getActionBar();
        getSupportActionBar().setTitle("App Settings"); // set the top title

        final PackageManager pm = getPackageManager();
        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        recyclerView = (RecyclerView) findViewById(R.id.AppList);
        // use this setting to
        // improve performance if you know that changes
        // in content do not change the layout size
        // of the RecyclerView
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        List<String> textInput = new ArrayList<>(); // Create app name list
        List<Drawable> imageInput = new ArrayList<>(); // Create app icon list
        List<String> packageName = new ArrayList<>();
        packages.sort(new NameSorter()); // Sort the list of applications

        for (ApplicationInfo packageInfo : packages) { // Loop through all installed apps
            ApplicationInfo ai = null;
            try {
                ai = pm.getApplicationInfo(packageInfo.packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                continue;
            }
            if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                // Check that it's not our own application we're adding to the list, so the user can't block the app itself
                if(!packageInfo.packageName.equals(this.getPackageName())) {
                    textInput.add(packageInfo.loadLabel(getPackageManager()).toString()); // Get the package name
                    imageInput.add(packageInfo.loadIcon(getPackageManager())); // Get the package icon
                    packageName.add(packageInfo.packageName);
                }
            }
        }
        mAdapter = new AppListAdapter(textInput, imageInput, packageName, getApplicationContext());
        recyclerView.setAdapter(mAdapter);
    }

    private class NameSorter implements Comparator<ApplicationInfo> {
        @Override
        public int compare(ApplicationInfo App1, ApplicationInfo App2) {
            return App1.loadLabel(getPackageManager()).toString().compareToIgnoreCase(App2.loadLabel(getPackageManager()).toString());
        }
    }
}

