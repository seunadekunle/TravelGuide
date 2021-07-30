package com.example.travelguide.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelguide.classes.Location;
import com.example.travelguide.databinding.TopLocationsItemBinding;
import com.example.travelguide.helpers.HelperClass;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link com.example.travelguide.classes.Location}.
 */
public class TopLocationAdapter extends RecyclerView.Adapter<TopLocationAdapter.ViewHolder> {

    // interface for item click
    public interface OnItemClickListener {
        void onItemClick(Location location);
    }

    private Context context;
    private List<Location> locations = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    public TopLocationAdapter(Context context, List<Location> locations, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.locations = locations;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(TopLocationsItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // callback to set ui text
        OnSuccessListener<FetchPlaceResponse> locationSuccess = fetchPlaceResponse -> holder.locationName.setText(fetchPlaceResponse.getPlace().getName());
        Location location = locations.get(position);

        // sets the item name based on place id
        if (location.getPlaceID().equals(HelperClass.defaultPlaceID)) {
            holder.locationName.setText(HelperClass.getAddress(context, location.getCoord().latitude, location.getCoord().longitude));
        } else {
            HelperClass.fetchPlacesName(locationSuccess, location.getPlaceID());
        }

        holder.cvLocations.setOnClickListener((v -> {
            onItemClickListener.onItemClick(location);
        }));
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView locationName;
        public CardView cvLocations;

        ViewHolder(TopLocationsItemBinding binding) {
            super(binding.getRoot());

            locationName = binding.locationName;
            cvLocations = binding.cvLocations;
        }

    }

}