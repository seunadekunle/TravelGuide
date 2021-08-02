package com.example.travelguide.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelguide.R;
import com.example.travelguide.activities.MapsActivity;
import com.example.travelguide.classes.GlideApp;
import com.example.travelguide.classes.Guide;
import com.example.travelguide.databinding.LocationGuideBinding;
import com.example.travelguide.fragments.ProfileFragment;
import com.example.travelguide.helpers.DeviceDimenHelper;
import com.example.travelguide.helpers.HelperClass;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Guide}.
 */
public class GuidesAdapter extends RecyclerView.Adapter<GuidesAdapter.ViewHolder> {

    private static final String TAG = "GuidesAdapter";

    private List<Guide> guides;
    private List<Guide> originalGuides;
    private final Context context;
    private final ImageView expandedImageView;
    private final View expandedImageViewBG;
    private final Activity activity;
    private SimpleExoPlayer exoPlayer;
    private boolean inProfile;
    private FragmentManager fragmentManager;
    private int frameID;

    // Hold a reference to the current animator
    private Animator currentAnimator;
    // The system "short" animation time duration, in milliseconds.
    private final int shortAnimationDuration = 100;
    private final int playerHeightMult = 12;


    public GuidesAdapter(List<Guide> items, Context context, ImageView expandedImageView, View expandedImageViewBG, Activity activity, SimpleExoPlayer exoPlayer, boolean inProfile
            , FragmentManager fragmentManager, int frameID) {
        this.context = context;
        this.expandedImageView = expandedImageView;
        guides = items;
        updateOriginalGuides();
        this.expandedImageViewBG = expandedImageViewBG;
        this.activity = activity;
        this.exoPlayer = exoPlayer;
        this.inProfile = inProfile;
        this.fragmentManager = fragmentManager;
        this.frameID = frameID;

        updateOriginalGuides();
    }

    public void updateOriginalGuides() {
        originalGuides = guides;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(LocationGuideBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    // fills ui elements with information from the guide
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        // if the click doesn't relate to an item
        if (position == RecyclerView.NO_POSITION)
            return;

        // get current guide at position
        Guide guide = guides.get(position);

        if (guide == null)
            return;


        String profileUrl = null;
        try {
            profileUrl = ((ParseFile) guide.getAuthor().fetchIfNeeded().get("avatar")).getUrl();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (profileUrl != null)
            HelperClass.loadCircularImage(profileUrl, context, 100, 100, holder.ivAvatar);


        try {
            setTextViewText(holder.tvUsername, guide.getAuthor().fetchIfNeeded().getUsername());
        } catch (ParseException e) {
            setTextViewText(holder.tvUsername, "");
            e.printStackTrace();
        }
        setTextViewText(holder.tvDetail, guide.getText());
        setTextViewText(holder.tvCreatedAt, guide.getTimeStamp());
        setTextViewText(holder.tvLikes, String.valueOf(guide.getLikes()));

        holder.tvUsername.setOnClickListener(v -> {
            goToProfile(guide);
        });

        holder.ivAvatar.setOnClickListener(v -> {
            goToProfile(guide);
        });

        // if there is any media
        fillMediaLayout(holder, guide);
        handleLikeButton(holder, guide, position);
    }

    private void goToProfile(Guide guide) {
        ProfileFragment userProfile = ProfileFragment.newInstance(guide.getAuthor().getObjectId());
        HelperClass.showFragment(fragmentManager, frameID, userProfile, ProfileFragment.TAG);
    }

    // clear all elements of the RecyclerView
    public void clear() {
        guides.clear();
        notifyDataSetChanged();

        updateOriginalGuides();
    }

    // Add a list of items to the list
    public void addAll(List<Guide> list) {
        guides.addAll(list);
        notifyDataSetChanged();

        updateOriginalGuides();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // filters the recyclerview
    public void filter(String newText) {

        List<Guide> filteredList = new ArrayList<>();

        // add guides whose text fit it
        for (Guide guide : guides) {
            if (guide.getText().toLowerCase().contains(newText.toLowerCase()))
                filteredList.add(guide);
        }

        updateList(filteredList);
    }

    // updates the list that the adapter uses
    private void updateList(List<Guide> list) {
        guides = list;
        notifyDataSetChanged();

        // call onBindViewHolder for each of the items to update the like button
        for (int i = 0; i < list.size(); i++) {
            notifyItemChanged(i);
        }
    }

    // goes back to the default list of guides
    public void resetFilter() {
        updateList(originalGuides);
    }


    // return size of lists for the lcoation
    @Override
    public int getItemCount() {
        return guides.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvUsername;
        public TextView tvDetail;
        public ImageView ivAvatar;
        public TextView tvCreatedAt;
        public TextView tvLikes;
        public ImageButton ibLikes;


        // media ui elements
        private final ConstraintLayout mediaLayout;
        private final ImageButton ibThumb;
        public PlayerView epPlayerView;

        public ViewHolder(LocationGuideBinding binding) {
            super(binding.getRoot());

            // binds ui elements to variables
            tvUsername = binding.tvUsername;
            tvDetail = binding.tvDetail;
            ivAvatar = binding.ivAvatar;
            tvCreatedAt = binding.tvCreatedAt;
            tvLikes = binding.tvLikes;
            ibLikes = binding.ibLikes;

            mediaLayout = binding.mediaContainer.mediaLayout;
            ibThumb = binding.mediaContainer.ibThumb;
            epPlayerView = binding.mediaContainer.epPlayer;
        }
    }


    // handle click for like button
    private void handleLikeButton(ViewHolder holder, Guide guide, int pos) {

        // callback for getting like data
        FindCallback<ParseObject> findCallback = (objects, e) -> {

            if (!guide.isGuideLiked() && e == null && objects.size() >= 1) {
                guide.setGuideLiked(true);
                holder.ibLikes.setSelected(guide.isGuideLiked());
            }
        };

        isGuideLiked(guide, findCallback);

        // callback for deleting like data
        FindCallback<ParseObject> deleteCallback = (objects, e) -> {

            if (guide.isGuideLiked() && e == null && objects.size() >= 1) {

                for (ParseObject object : objects) {
                    try {
                        object.delete();
                    } catch (ParseException parseException) {
                        parseException.printStackTrace();
                    }
                    object.saveInBackground();
                }

                guide.setGuideLiked(false);
                guide.saveInBackground();
                setTextViewText(holder.tvLikes, String.valueOf(guide.getLikes()));

                // if list is in profile update liked list
                if (inProfile) {
                    guides.remove(pos);
                    notifyDataSetChanged();

                    updateOriginalGuides();
                }
            }
        };

        // click listener for like button
        holder.ibLikes.setOnClickListener(v -> {

            if (holder.ibLikes.isSelected()) {
                isGuideLiked(guide, deleteCallback);
            } else {
                // creates a like row and updates Guide text
                com.example.travelguide.classes.Activity likeActivity = new com.example.travelguide.classes.Activity();

                likeActivity.put(com.example.travelguide.classes.Activity.getKeyUserId(), ParseUser.getCurrentUser());
                likeActivity.put(com.example.travelguide.classes.Activity.getKeyGuideId(), guide);

                likeActivity.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null)
                            Log.i(TAG, e.getMessage());
                        else {

                            // if guide isn't liked
                            if (!guide.isGuideLiked()) {

                                // update ui state and save like
                                guide.setGuideLiked(true);
                                guide.setLikes(guide.getLikes() + 1);
                                guide.saveInBackground();

                                setTextViewText(holder.tvLikes, String.valueOf(guide.getLikes()));
                            }
                        }
                    }
                });
            }

            // toggle view state
            holder.ibLikes.setSelected(!holder.ibLikes.isSelected());
        });
    }

    private void setTextViewText(TextView tvChanged, String s) {
        tvChanged.setText(s);
    }

    private void fillMediaLayout(ViewHolder holder, Guide guide) {
        if (guide.getPhoto() != null || guide.getVideo() != null || guide.getAudio() != null) {

            Log.i(TAG, guide.getText());
            holder.mediaLayout.setVisibility(View.VISIBLE);

            if (guide.getPhoto() != null) {

                String photoUrl = guide.getPhoto().getUrl();

                if (photoUrl != null) {

                    // sets view to be visible
                    holder.ibThumb.setVisibility(View.VISIBLE);
                    holder.epPlayerView.setVisibility(View.GONE);

                    GlideApp.with(context)
                            .load(photoUrl).centerCrop()
                            .override((int) (HelperClass.detailImgDimen * 1.7), (int) (HelperClass.detailImgDimen * 1.7))
                            .transform(new RoundedCornersTransformation(HelperClass.picRadius, 0)).into(holder.ibThumb);

                    holder.ibThumb.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // shows background
                            expandedImageViewBG.setVisibility(View.VISIBLE);
                            zoomImageFromThumb(holder.ibThumb, photoUrl, expandedImageView, expandedImageViewBG);
                        }
                    });
                }

                return;
            }

            // if there is audio or video
            if (guide.getVideo() != null || guide.getAudio() != null) {

                Uri mediaUri;

                // shows the media view
                holder.epPlayerView.setVisibility(View.VISIBLE);
                holder.ibThumb.setVisibility(View.GONE);

                // creates a track selector an pick media that is only sd quality or lower
                DefaultTrackSelector trackSelector = new DefaultTrackSelector(context);
                trackSelector.setParameters(
                        trackSelector
                                .buildUponParameters()
                                .setMaxVideoSizeSd());


                // creates new exo player
                exoPlayer = new SimpleExoPlayer.Builder(context)
                        .setTrackSelector(trackSelector)
                        .build();

                if (guide.getAudio() != null) {
                    // shortens the player height if it is video
                    holder.epPlayerView.getLayoutParams().height = DeviceDimenHelper.getDisplayHeight(context) / playerHeightMult;

                    mediaUri = Uri.parse(guide.getAudio().getUrl());
                    holder.epPlayerView.setShutterBackgroundColor(R.color.black);

                    // set audio attributes for guide audio
                    AudioAttributes audioAttributes = new AudioAttributes.Builder()
                            .setUsage(C.USAGE_MEDIA)
                            .setContentType(C.CONTENT_TYPE_MUSIC)
                            .build();

                    exoPlayer.setAudioAttributes(audioAttributes, true);
                } else {
                    mediaUri = Uri.parse(guide.getVideo().getUrl());
                }

                // creates a new MediaItem
                MediaItem mediaItem = MediaItem.fromUri(mediaUri);

                // sets exoPlayer media item
                exoPlayer.setMediaItem(mediaItem);

                // sets playerView player
                holder.epPlayerView.setPlayer(exoPlayer);

                // prepares the media
                exoPlayer.prepare();
                exoPlayer.setPlayWhenReady(false);
                return;
            }
        }
    }

    // uses a join table to check if the user likes the post
    private void isGuideLiked(Guide guide, FindCallback<ParseObject> findCallback) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Activity");

        query.whereEqualTo(com.example.travelguide.classes.Activity.getKeyUserId(), ParseUser.getCurrentUser());
        query.whereEqualTo(com.example.travelguide.classes.Activity.getKeyGuideId(), guide);

        query.findInBackground(findCallback);
    }

    /* creates an expanded view after clicking on thumbnail
     * ref: https://developer.android.com/training/animation/zoom.html
     */
    private void zoomImageFromThumb(final View thumbView, String imgUrl, ImageView expandedImageView, View expandedImageViewBG) {

        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        // shows background if it isn't in profile
        if (!inProfile) {
            expandedImageViewBG.setVisibility(View.VISIBLE);
        }

        // Load the high-resolution "zoomed-in" image.
        GlideApp.with(context).load(imgUrl).into(expandedImageView);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        expandedImageView.getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimator = null;
            }
        });
        set.start();
        currentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // if it isn't in profile
                if (!inProfile) {
                    if (currentAnimator != null) {
                        currentAnimator.cancel();
                    }

                    // removes background
                    expandedImageViewBG.setVisibility(View.INVISIBLE);

                    // Animate the four positioning/sizing properties in parallel,
                    // back to their original values.
                    AnimatorSet set = new AnimatorSet();
                    set.play(ObjectAnimator
                            .ofFloat(expandedImageView, View.X, startBounds.left))
                            .with(ObjectAnimator
                                    .ofFloat(expandedImageView,
                                            View.Y, startBounds.top))
                            .with(ObjectAnimator
                                    .ofFloat(expandedImageView,
                                            View.SCALE_X, startScaleFinal))
                            .with(ObjectAnimator
                                    .ofFloat(expandedImageView,
                                            View.SCALE_Y, startScaleFinal));
                    set.setDuration(shortAnimationDuration);
                    set.setInterpolator(new DecelerateInterpolator());
                    set.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            thumbView.setAlpha(1f);
                            expandedImageView.setVisibility(View.INVISIBLE);
                            currentAnimator = null;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            thumbView.setAlpha(1f);
                            expandedImageView.setVisibility(View.INVISIBLE);
                            currentAnimator = null;
                        }
                    });
                    set.start();
                    currentAnimator = set;
                } else {
                    thumbView.setAlpha(1f);
                    expandedImageView.setVisibility(View.INVISIBLE);
                    expandedImageViewBG.setVisibility(View.INVISIBLE);
                }

//                // show add button
//                ((MapsActivity) activity).showOverlayBtns();
            }
        });
    }
}