package com.example.travelguide.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelguide.databinding.SearchviewItemBinding;
import com.google.android.libraries.places.api.model.AutocompletePrediction;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/*
 * {@link RecyclerView.Adapter} that can display a {@link Guide}.
 */
public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.ViewHolder> {

    public interface onItemClickListener {
        void onItemClick(AutocompletePrediction prediction);
    }

    private static final String TAG = "SearchListAdapter";

    private List<AutocompletePrediction> locationPredictions;
    private Context context;
    private SearchListAdapter.onItemClickListener onItemClickListener;

    public SearchListAdapter(List<AutocompletePrediction> locationPredictions, Context context, onItemClickListener onItemClickListener) {
        this.locationPredictions = locationPredictions;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new ViewHolder(SearchviewItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SearchListAdapter.ViewHolder holder, int position) {

        AutocompletePrediction prediction = locationPredictions.get(position);
        holder.tvPlace.setText(prediction.getPrimaryText(null));

        holder.searchViewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // if the click was a valid part of the ui
                if(position != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemClick(locationPredictions.get(position));
                }

            }
        });
    }

    // clear all elements of the RecyclerView
    public void clear() {
        locationPredictions.clear();
        notifyDataSetChanged();
    }

    // Add a list of items to the list
    public void addAll(List<AutocompletePrediction> list) {
        locationPredictions.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return locationPredictions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvPlace;
        public View searchViewLayout;

        public ViewHolder(SearchviewItemBinding binding) {
            super(binding.getRoot());

            // binds ui elements to variables
            tvPlace = binding.tvPlace;
            searchViewLayout = binding.searchViewLayout;
        }
    }
}
