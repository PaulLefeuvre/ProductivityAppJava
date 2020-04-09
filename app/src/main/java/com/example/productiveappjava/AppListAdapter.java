package com.example.productiveappjava;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;


public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {
    private List<String> appTitleDataset;
    private List<Integer> appImageDataset;

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

    public AppListAdapter(List<String> textDataset, List<Integer> imageDataset) {
        appTitleDataset = textDataset;
        appImageDataset = imageDataset;
    }


    // Create new views (invoked by the layout manager)
    @Override
    public AppListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(
                parent.getContext());
        View v = inflater.inflate(R.layout.app_settings_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // For some reason this function links together every 11th switch. Fix later

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final String title = appTitleDataset.get(position);
        final Integer image = appImageDataset.get(position);
        holder.textTitle.setText(title);
        //holder.logoImage.setImageResource(image);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return appTitleDataset.size();
    }

}
