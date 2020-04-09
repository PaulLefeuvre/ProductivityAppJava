package com.example.productiveappjava;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Change the Action Bar title to 'App Settings'
        ActionBar actionBar = getSupportActionBar(); // or getActionBar();
        getSupportActionBar().setTitle("App Settings"); // set the top title

        // Error is between these brackets: {
        recyclerView = (RecyclerView) findViewById(R.id.AppList);
        // use this setting to
        // improve performance if you know that changes
        // in content do not change the layout size
        // of the RecyclerView
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        List<String> textInput = new ArrayList<>();
        List<Integer> imageInput = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            textInput.add("Test" + i);
            imageInput.add(R.id.icon);
        }// define an adapter
        mAdapter = new AppListAdapter(textInput, imageInput);
        recyclerView.setAdapter(mAdapter);
        // }
    }
}

