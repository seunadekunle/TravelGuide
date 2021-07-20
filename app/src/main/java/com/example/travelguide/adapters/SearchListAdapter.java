package com.example.travelguide.adapters;

import android.content.Context;
import android.text.style.CharacterStyle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelguide.classes.Guide;
import com.example.travelguide.databinding.LocationGuideBinding;
import com.example.travelguide.databinding.SearchviewItemBinding;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.libraries.places.api.model.AutocompletePrediction;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/*
 * {@link RecyclerView.Adapter} that can display a {@link Guide}.
 */
public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.ViewHolder> {

    private static final String TAG = "SearchListAdapter";

    private List<AutocompletePrediction> locationPredictions;
    private Context context;

    public SearchListAdapter(List<AutocompletePrediction> locationPredictions, Context context) {
        this.locationPredictions = locationPredictions;
        this.context = context;
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

        public ViewHolder(SearchviewItemBinding binding) {
            super(binding.getRoot());

            // binds ui elements to variables
            tvPlace = binding.tvPlace;
        }
    }
}
