package com.example.travelguide.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelguide.classes.Guide;
import com.example.travelguide.databinding.FragmentLocationGuideListBinding;
import com.example.travelguide.databinding.LocationGuideBinding;
import com.parse.ParseFile;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Guide}.
 * TODO: Replace the implementation with code for your data type.
 */
public class GuidesAdapter extends RecyclerView.Adapter<GuidesAdapter.ViewHolder> {

    private List<Guide> guides;
    private Context context;

    public GuidesAdapter(List<Guide> items, Context context) {
        this.context = context;
        guides = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(LocationGuideBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        // fills ui elements with information from the guide
        Guide guide = guides.get(position);
        holder.tvUsername.setText(guide.getAuthor().getUsername());
        holder.tvDetail.setText(guide.getText());

        // loads user profile image on timeline
        ParseFile profileImg = guide.getAuthor().getParseFile("avatar");

        // handles if image gotten from database
        if (profileImg != null)
            Glide.with(context)
                    .load(profileImg.getUrl()).fitCenter().transform(new RoundedCornersTransformation(50, 0))
                    .override(100, 50).into(holder.ivAvatar);
        else
            (holder.ivAvatar).setVisibility(View.GONE);


    }

    // clear all elements of the RecyclerView
    public void clear() {
        guides.clear();
        notifyDataSetChanged();
    }

    // Add a list of items to the list
    public void addAll(List<Guide> list) {
        guides.addAll(list);
        notifyDataSetChanged();
    }

    // return size of lists for the lcoation
    @Override
    public int getItemCount() {
        return guides.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvUsername;
        public TextView tvDetail;
        public ImageView ivAvatar;

        public ViewHolder(LocationGuideBinding binding) {
            super(binding.getRoot());

            // binds ui elements to variables
            tvUsername = binding.tvUsername;
            tvDetail = binding.tvDetail;
            ivAvatar = binding.ivAvatar;
        }
    }
}