package com.example.productiveappjava;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;


public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {
    private List<String> appTitleDataSet;
    private List<Drawable> appImageDataSet;
    private List<String> appPackageDataSet;
    private Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textTitle;
        public ImageView logoImage;
        public CompoundButton enableButton;
        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            textTitle = (TextView) v.findViewById(R.id.app_title);
            logoImage = (ImageView) v.findViewById(R.id.logo_picture);
            enableButton = (CompoundButton) v.findViewById(R.id.enable_switch);
        }
    }

    public AppListAdapter(List<String> textDataSet, List<Drawable> imageDataSet, List<String> packageDataSet, Context inputContext) {
        appTitleDataSet = textDataSet;
        appImageDataSet = imageDataSet;
        appPackageDataSet = packageDataSet;
        context = inputContext;
    }


    // Create new views (invoked by the layout manager)
    @Override
    public AppListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(
                parent.getContext());
        View v = inflater.inflate(R.layout.app_settings_layout, parent, false);
        // set the view's size, margins, padding and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // Get the SharedPreferences editor
        SharedPreferences sharedPref = context.getSharedPreferences(String.valueOf(R.string.preference_file_key), Context.MODE_PRIVATE);
        final SharedPreferences.Editor prefsEditor = sharedPref.edit();

        // - get element from your data set at this position
        // - replace the contents of the view with that element
        final String title = appTitleDataSet.get(position);
        final Drawable image = appImageDataSet.get(position);
        holder.textTitle.setText(title);
        holder.logoImage.setImageDrawable(image);

        boolean switchChecked = sharedPref.getBoolean(appPackageDataSet.get(position), false);
        if(switchChecked) {
            holder.enableButton.setChecked(true);
            // Setting switch to true
        }
        else {
            holder.enableButton.setChecked(false);
            // Setting switch to false
        }
        holder.enableButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("BlockerService", "Package name " + appPackageDataSet.get(position) + " has been toggled to: " + isChecked);
                if (isChecked) {
                    // The toggle is enabled
                    prefsEditor.putBoolean(appPackageDataSet.get(position), true);
                    prefsEditor.apply();
                    // Commit 'true' to the SharedPreferences
                } else {
                    // The toggle is disabled
                    prefsEditor.putBoolean(appPackageDataSet.get(position), false);
                    prefsEditor.apply();
                    // Commit 'false' to SharedPreferences
                }
            }
        });
    }

    // Return the size of the data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return appTitleDataSet.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
