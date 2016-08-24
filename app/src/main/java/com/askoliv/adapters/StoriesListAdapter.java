package com.askoliv.adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.askoliv.model.Carousel;
import com.askoliv.model.Social;
import com.askoliv.model.Story;
import com.askoliv.app.R;
import com.askoliv.utils.AndroidUtils;
import com.askoliv.utils.Constants;
import com.askoliv.utils.CustomViewPager;
import com.askoliv.utils.FirebaseUtils;
import com.askoliv.utils.UsageAnalytics;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.io.File;
import java.util.Map;

/**
 * Created by surbhimanurkar on 19-05-2016.
 * Adapter gets list of stories from firebase and populates them in the view
 */
public class StoriesListAdapter extends FirebaseRecyclerAdapter<Story,StoriesListAdapter.StoryViewHolder> {

    private static final String TAG = StoriesListAdapter.class.getSimpleName();
    private Activity mActivity;
    private DatabaseReference mFirebaseDatabaseRef;

    private UsageAnalytics mUsageAnalytics;

    private static final int carouselMaxCount = 10;

    public StoriesListAdapter(Query ref, Activity activity, int layout) {
        super(Story.class, layout, StoryViewHolder.class, ref);
        this.mActivity = activity;
        mFirebaseDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mUsageAnalytics = new UsageAnalytics();
        mUsageAnalytics.initTracker(activity);
    }


    /**
     * Bind an instance of the <code>Stories</code> class to our view. This method is called by <code>FirebaseListAdapter</code>
     * when there is a data change, and we are given an instance of a View that corresponds to the layout that we passed
     * to the constructor, as well as a single <code>Stories</code> instance that represents the current data to bind.
     *
     * @param storyViewHolder A viewholder instance corresponding to the layout we passed to the constructor.
     * @param story An instance representing the current state of a story
     * @param position Position of the model in the list
     */
    @Override
    protected void populateViewHolder(final StoryViewHolder storyViewHolder, final Story story, int position) {

        final String key = this.getRef(position).getKey();

        //Colors
        final int primaryColor = ContextCompat.getColor(mActivity, R.color.colorPrimary);
        final int grayColor = ContextCompat.getColor(mActivity, R.color.colorDivider);
        final int secondaryColor = ContextCompat.getColor(mActivity, R.color.colorSecondary);

        //Defining Auxiliary objects
        final AndroidUtils androidUtils = new AndroidUtils();

        //Defining views
        storyViewHolder.setTitle(story.getTitle());
        storyViewHolder.setSubtitle(story.getSubtitle());
        String authorText = mActivity.getResources().getString(R.string.author_prefix) + " " + story.getAuthor();
        storyViewHolder.setAuthor(authorText);

        //Text Initial
        if(story.getTextInitial()!=null){
            storyViewHolder.setTextInitial(story.getTextInitial());
        }else{
            storyViewHolder.setTextInitialVisibility(View.GONE);
        }


        String shares = "";
        String loves = "";
        if(story.getSocial()!=null){
           shares = story.getSocial().getShares() + shares;
            loves = story.getSocial().getLoves() + loves;
        }else{
            shares = "0" + shares;
            loves = "0" + loves;
        }

        storyViewHolder.setShareButton(shares, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(androidUtils.checkPermission(mActivity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},Constants.PERMISSIONS_REQUEST_STORAGE_SHARE)){
                    androidUtils.shareStory(mActivity,androidUtils.getShareStoryBody(mActivity,story,key,true),story.getStorySnapshot());
                    mUsageAnalytics.trackShareEvent(key,story.getTitle());
                }else{
                    SharedPreferences sharedPreferences = mActivity.getSharedPreferences(Constants.SHARED_PREFERENCE_STORY, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Constants.STORY_PREF_KEY, key);
                    editor.putString(Constants.STORY_PREF_TITLE, story.getTitle());
                    editor.putString(Constants.STORY_PREF_SHARE_TEXT, androidUtils.getShareStoryBody(mActivity,story,key,true));
                    editor.putString(Constants.STORY_PREF_SNAPSHOT, story.getStorySnapshot());
                    editor.apply();
                }
            }
        });
        storyViewHolder.setLoveButton(loves,new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(v.getId() == R.id.story_button_love){
                    //Get User ID
                    String userID = null;
                    if(FirebaseAuth.getInstance()!=null && FirebaseAuth.getInstance().getCurrentUser()!=null){
                        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    }

                    Social social = story.getSocial();
                    int newLoves = social.getLoves();
                    int color;
                    Drawable iconDrawable;

                    if(v.getTag()!=null && ((int)v.getTag())==1){
                        v.setTag(0);
                        Log.d(TAG, "onCLick lovebutton unselected");
                        newLoves = newLoves - 1;
                        color = secondaryColor;
                        iconDrawable = ContextCompat.getDrawable(mActivity,R.drawable.ic_favorite_black_24dp);
                    }else{
                        v.setTag(1);
                        Log.d(TAG, "onCLick lovebutton selected");
                        newLoves = newLoves + 1;
                        color = primaryColor;
                        iconDrawable = ContextCompat.getDrawable(mActivity,R.drawable.ic_favorite_primary_24dp);
                    }
                    //Update UI
                    ((Button)v).setTextColor(color);
                    ((Button)v).setCompoundDrawablesWithIntrinsicBounds(null,null,iconDrawable,null);
                    if(userID!=null){
                        mFirebaseDatabaseRef.child(Constants.F_NODE_STORIES).child(key).child(Constants.F_KEY_STORIES_SOCIAL).child(Constants.F_KEY_STORIES_LOVES).setValue(newLoves);
                        mFirebaseDatabaseRef.child(Constants.F_NODE_USER).child(userID).child(Constants.F_KEY_USER_ACTIVITY).child(Constants.F_KEY_STORIES_LOVES).child(key).setValue(true);
                    }
                }
                mUsageAnalytics.trackLikeEvent(story);
            }
        });

        final CustomViewPager tabsViewPager = (CustomViewPager)mActivity.findViewById(R.id.container);

        storyViewHolder.setChatRelatedButton(null,new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUtils firebaseUtils = FirebaseUtils.getInstance();
                firebaseUtils.sendMessage(androidUtils.getShareStoryBody(mActivity,story,key,false),story.getStorySnapshot(),null);
                tabsViewPager.setCurrentItem(1);
            }
        });

        //Populating carousel
        Map<String, Carousel> carouselItems = story.getCarousel();
        SparseArray<Carousel> resources = new SparseArray<>();
        //LinearLayout dotLayout = (LinearLayout) view.findViewById(R.id.story_carousel_pagination);
        //Drawable dotDrawable = ContextCompat.getDrawable(mActivity, R.drawable.dot);
        //final Drawable dotSelectedDrawable = ContextCompat.getDrawable(mActivity, R.drawable.dot_selected);

        for (String index: carouselItems.keySet()) {
            Carousel carousel = carouselItems.get(index);
            resources.put(carousel.getPosition(),carousel);
        }



        //Styling DotList
        for(int p=0; p < carouselMaxCount; p++){
            int size = resources.size();
            Log.d(TAG, "Size:" + size + " p:" + p);
            if(p<size){
                if(p==storyViewHolder.getCurrentStoryPagerItem()){
                    storyViewHolder.setDotColorFilter(p,primaryColor);
                }
                storyViewHolder.setDotVisibility(p,View.VISIBLE);
            }else{
                storyViewHolder.setDotVisibility(p,View.GONE);
            }
        }

        StoryPagerAdapter storyPagerAdapter = new StoryPagerAdapter(mActivity, resources);

        storyViewHolder.setStoryPager(storyPagerAdapter,new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d(TAG, "onPageScrolled:" + position);
            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected:" + position);
                for(int p=0; p< carouselMaxCount; p++){
                    if(p==position){
                        storyViewHolder.setDotColorFilter(p,primaryColor);
                    }else{
                        storyViewHolder.setDotColorFilter(p,grayColor);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }


    public static class StoryViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public StoryViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTitle(String title){
            TextView titleTextView = (TextView) mView.findViewById(R.id.story_title);
            titleTextView.setText(title);
        }

        public void setSubtitle(String subtitle){
            TextView subtitleTextView = (TextView) mView.findViewById(R.id.story_subtitle);
            subtitleTextView.setText(subtitle);
        }

        public void setAuthor(String author){
            TextView authorTextView = (TextView) mView.findViewById(R.id.story_author);
            authorTextView.setText(author);
        }

        public void setTextInitial(String textInitial){
            TextView textInitialTextView = (TextView) mView.findViewById(R.id.story_text_initial);
            textInitialTextView.setText(textInitial);
        }

        public void setTextInitialVisibility(int visibility){
            TextView textInitialTextView = (TextView) mView.findViewById(R.id.story_text_initial);
            textInitialTextView.setVisibility(visibility);
        }

        public void setShareButton(String shareButtonText, View.OnClickListener onClickListener){
            Button shareButton = (Button) mView.findViewById(R.id.story_button_share);
            if(shareButtonText!=null)
                shareButton.setText(shareButtonText);
            if(onClickListener!=null)
                shareButton.setOnClickListener(onClickListener);
        }

        public void setLoveButton(String loveButtonText, View.OnClickListener onClickListener){
            Button loveButton = (Button) mView.findViewById(R.id.story_button_love);
            if(loveButtonText!=null)
                loveButton.setText(loveButtonText);
            if(onClickListener!=null)
                loveButton.setOnClickListener(onClickListener);
        }

        public void setChatRelatedButton(String chatRelatedButtonText, View.OnClickListener onClickListener){
            Button chatRelatedButton = (Button) mView.findViewById(R.id.story_related_chat_button);
            if(chatRelatedButtonText!=null)
                chatRelatedButton.setText(chatRelatedButtonText);
            if(onClickListener!=null)
                chatRelatedButton.setOnClickListener(onClickListener);
        }

        public void setStoryPager(StoryPagerAdapter storyPagerAdapter, ViewPager.OnPageChangeListener onPageChangeListener){
            ViewPager storyPager = (ViewPager) mView.findViewById(R.id.story_pager);
            storyPager.setAdapter(storyPagerAdapter);
            storyPager.addOnPageChangeListener(onPageChangeListener);
        }

        public int getCurrentStoryPagerItem(){
            ViewPager storyPager = (ViewPager) mView.findViewById(R.id.story_pager);
            return storyPager.getCurrentItem();
        }

        public void setDotColorFilter(int position, int color){
            switch(position){
                case 0: ((ImageView) mView.findViewById(R.id.dot1)).setColorFilter(color, PorterDuff.Mode.SRC_IN);
                    break;
                case 1: ((ImageView) mView.findViewById(R.id.dot2)).setColorFilter(color, PorterDuff.Mode.SRC_IN);
                    break;
                case 2: ((ImageView) mView.findViewById(R.id.dot3)).setColorFilter(color, PorterDuff.Mode.SRC_IN);
                    break;
                case 3: ((ImageView) mView.findViewById(R.id.dot4)).setColorFilter(color, PorterDuff.Mode.SRC_IN);
                    break;
                case 4: ((ImageView) mView.findViewById(R.id.dot5)).setColorFilter(color, PorterDuff.Mode.SRC_IN);
                    break;
                case 5: ((ImageView) mView.findViewById(R.id.dot6)).setColorFilter(color, PorterDuff.Mode.SRC_IN);
                    break;
                case 6: ((ImageView) mView.findViewById(R.id.dot7)).setColorFilter(color, PorterDuff.Mode.SRC_IN);
                    break;
                case 7: ((ImageView) mView.findViewById(R.id.dot8)).setColorFilter(color, PorterDuff.Mode.SRC_IN);
                    break;
                case 8: ((ImageView) mView.findViewById(R.id.dot9)).setColorFilter(color, PorterDuff.Mode.SRC_IN);
                    break;
                case 9: ((ImageView) mView.findViewById(R.id.dot10)).setColorFilter(color, PorterDuff.Mode.SRC_IN);
                    break;
            }
        }

        public void setDotVisibility(int position, int visibility){
            switch(position){
                case 0: (mView.findViewById(R.id.dot1)).setVisibility(visibility);
                    break;
                case 1: (mView.findViewById(R.id.dot2)).setVisibility(visibility);
                    break;
                case 2: (mView.findViewById(R.id.dot3)).setVisibility(visibility);
                    break;
                case 3: (mView.findViewById(R.id.dot4)).setVisibility(visibility);
                    break;
                case 4: (mView.findViewById(R.id.dot5)).setVisibility(visibility);
                    break;
                case 5: (mView.findViewById(R.id.dot6)).setVisibility(visibility);
                    break;
                case 6: (mView.findViewById(R.id.dot7)).setVisibility(visibility);
                    break;
                case 7: (mView.findViewById(R.id.dot8)).setVisibility(visibility);
                    break;
                case 8: (mView.findViewById(R.id.dot9)).setVisibility(visibility);
                    break;
                case 9: (mView.findViewById(R.id.dot10)).setVisibility(visibility);
                    break;
            }
        }
    }


}
