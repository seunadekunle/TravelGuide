package com.example.travelguide.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.example.travelguide.classes.GlideApp;
import com.example.travelguide.classes.Location;
import com.example.travelguide.databinding.RecommendedLocationsItemBinding;
import com.example.travelguide.databinding.TopLocationsItemBinding;
import com.example.travelguide.helpers.DeviceDimenHelper;
import com.example.travelguide.helpers.HelperClass;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link com.example.travelguide.classes.Location}.
 */
public class TopLocationAdapter extends RecyclerView.Adapter<TopLocationAdapter.ViewHolder> {

    private static final String TAG = "TopLocationAdapter";

    // interface for item click
    public interface OnItemClickListener {
        void onItemClick(Location location);
    }

    private Context context;
    private List<Location> locations = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private int viewType;

    public TopLocationAdapter(Context context, List<Location> locations, int viewType, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.locations = locations;
        this.onItemClickListener = onItemClickListener;
        this.viewType = viewType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // returns separate view based on viewtypes

        if (viewType == 1) {
            return new ViewHolder(RecommendedLocationsItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        return new ViewHolder(TopLocationsItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), context);
    }

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // get current location
        Location location = locations.get(position);

        // callback to set ui text
        OnSuccessListener<FetchPlaceResponse> locationSuccess = fetchPlaceResponse -> holder.locationName.setText(fetchPlaceResponse.getPlace().getName());
        OnSuccessListener<FetchPlaceResponse> imageCallback = new OnSuccessListener<FetchPlaceResponse>() {
            @Override
            public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                final Place place = fetchPlaceResponse.getPlace();

                // Get the photo metadata.
                final List<PhotoMetadata> metadata = place.getPhotoMetadatas();
                if (metadata == null || metadata.isEmpty()) {
                    Log.w(TAG, "No photo metadata.");
                    return;
                }
                final PhotoMetadata photoMetadata = metadata.get(0);
                // Get the attribution text.
                final String attributions = photoMetadata.getAttributions();

                // Create a FetchPhotoRequest.
                final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                        .setMaxWidth(500)
                        .setMaxHeight(300)
                        .build();

                HelperClass.getPlacesClient().fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                    Bitmap bitmap = fetchPhotoResponse.getBitmap();

                    // loads bitmap into image preview
                    GlideApp.with(context).asBitmap().override(1000, 1000).load(bitmap).centerCrop()
                            .into(new BitmapImageViewTarget(holder.ivBackground) {
                                @Override
                                protected void setResource(Bitmap resource) {
                                    super.setResource(resource);
                                    RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                                    roundedBitmapDrawable.setCornerRadius(15);
                                    holder.ivBackground.setImageDrawable(roundedBitmapDrawable);
                                }
                            });
                }).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        final ApiException apiException = (ApiException) exception;
                        Log.e(TAG, "Place not found: " + exception.getMessage());
                        final int statusCode = apiException.getStatusCode();
                    }
                });
            }
        };

        // handles different view holder types
        if (holder.ivBackground != null) {
            HelperClass.fetchPlaceImage(context, location.getPlaceID(), imageCallback);
        }

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
        private Context context;
        public ImageView ivBackground;

        ViewHolder(RecommendedLocationsItemBinding binding) {
            super(binding.getRoot());

            locationName = binding.locationName;
            cvLocations = binding.cvLocations;
            this.context = context;
            ivBackground = null;
        }

        ViewHolder(TopLocationsItemBinding binding, Context context) {
            super(binding.getRoot());

            locationName = binding.locationName;
            cvLocations = binding.cvLocations;
            ivBackground = binding.ivBackground;

            int cardWidth = (int) (DeviceDimenHelper.getDisplayWidthPixels(context) / 2.05);
            int cardHeight = (int) (DeviceDimenHelper.getDisplayHeightPixels(context) / 3.425);

            // adjust card dimension based on layout
            cvLocations.getLayoutParams().width = cardWidth;
            cvLocations.getLayoutParams().height = cardHeight;

            // adjust height of text
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins((int) (cardWidth / 15)
                    , (int) (cardHeight / 1.375)
                    , (int) (cardWidth / 15)
                    , (int) (cardWidth / 40));
            locationName.setLayoutParams(params);
        }

    }

}