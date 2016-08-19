package com.askoliv.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
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
import com.askoliv.utils.Constants;
import com.askoliv.utils.CustomViewPager;
import com.askoliv.utils.FirebaseUtils;
import com.askoliv.utils.UsageAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by surbhimanurkar on 19-05-2016.
 * Adapter gets list of stories from firebase and populates them in the view
 */
public class StoriesListAdapter extends FirebaseListAdapter<Story>{

    private static final String TAG = StoriesListAdapter.class.getSimpleName();
    private Activity mActivity;
    private DatabaseReference mFirebaseDatabaseRef;

    private UsageAnalytics mUsageAnalytics;
    private Set<String> mStoryPopulatedOnce = new LinkedHashSet<>();

    private static final int carouselMaxCount = 10;

    public StoriesListAdapter(Query ref, Activity activity, int layout) {
        super(ref, Story.class, layout, activity);
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
     * @param view A view instance corresponding to the layout we passed to the constructor.
     * @param story An instance representing the current state of a story
     */
    @Override
    @SuppressLint("NewApi")
    protected void populateView(final View view, final Story story, final String key) {
        Log.d(TAG, "Populating view for:" + story.getTitle());


        //Colors
        final int primaryColor = ContextCompat.getColor(mActivity, R.color.colorPrimary);
        final int grayColor = ContextCompat.getColor(mActivity, R.color.colorDivider);
        final int secondaryColor = ContextCompat.getColor(mActivity, R.color.colorSecondary);

        //Defining views
        TextView titleTextView = (TextView) view.findViewById(R.id.story_title);
        titleTextView.setText(story.getTitle());
        TextView subtitleTextView = (TextView) view.findViewById(R.id.story_subtitle);
        subtitleTextView.setText(story.getSubtitle());
        TextView authorTextView = (TextView) view.findViewById(R.id.story_author);
        String authorText = mActivity.getResources().getString(R.string.author_prefix) + " " + story.getAuthor();
        Log.d(TAG, "AuthorText: "+authorText);
        authorTextView.setText(authorText);

        //Text Initial
        TextView textInitial = (TextView) view.findViewById(R.id.story_text_initial);
        if(story.getTextInitial()!=null){
            textInitial.setText(story.getTextInitial());
        }else{
            textInitial.setVisibility(View.GONE);
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
        Button shareButton = (Button) view.findViewById(R.id.story_button_share);
        shareButton.setText(shares);
        Button loveButton = (Button) view.findViewById(R.id.story_button_love);
        loveButton.setText(loves);
        Button chatRelatedButton = (Button) view.findViewById(R.id.story_related_chat_button);

        loveButton.setOnClickListener(new View.OnClickListener() {
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


        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("image/*");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, getShareBody(story,key,true));
                File imageFile = FirebaseUtils.getInstance().downloadFilefromFirebaseURL(story.getStorySnapshot());
                Uri imageUri = Uri.fromFile(imageFile);
                sharingIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                mActivity.startActivity(Intent.createChooser(sharingIntent, "Share"));
                mUsageAnalytics.trackShareEvent(story);
            }
        });

        final CustomViewPager tabsViewPager = (CustomViewPager)mActivity.findViewById(R.id.container);

        chatRelatedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUtils firebaseUtils = FirebaseUtils.getInstance();
                firebaseUtils.sendMessage(getShareBody(story,key,false),story.getStorySnapshot(),null);
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


        ViewPager storyViewPager = (ViewPager) view.findViewById(R.id.story_viewpager);
        StoryPagerAdapter storyPagerAdapter = new StoryPagerAdapter(mActivity, resources);
        storyViewPager.setAdapter(storyPagerAdapter);


        //Styling DotList
        Log.d(TAG, "Story Populated once:" + mStoryPopulatedOnce.toString());
        for(int p=0; p < carouselMaxCount; p++){
            int size = resources.size();
            ImageView dotImage;
            Log.d(TAG, "Size:" + size + " p:" + p);
            dotImage = getDotImageView(p,view);
            if(p<size){
                if(p==storyViewPager.getCurrentItem()){
                    dotImage.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN);
                }
                dotImage.setVisibility(View.VISIBLE);
            }else{
                dotImage.setVisibility(View.GONE);
            }
        }

        storyViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d(TAG, "onPageScrolled:" + position);
            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected:" + position);
                for(int p=0; p< carouselMaxCount; p++){
                    if(p==position){
                        getDotImageView(p,view).setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN);
                    }else{
                        getDotImageView(p,view).setColorFilter(grayColor, PorterDuff.Mode.SRC_IN);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //Tracking first loading of story
        if(!mStoryPopulatedOnce.contains(key)) {
            mStoryPopulatedOnce.add(key);
            Log.d(TAG, "Populating mStoryPopulatedOnce:" + key);
        }
    }

    private String getShareBody(Story story, String key, boolean external){
        String shareBody;
        if(external){
            String shareLink = mActivity.getResources().getString(R.string.mobile_web_link);
            shareBody = story.getTitle() + "\n" + shareLink + "\n\n" + mActivity.getResources().getString(R.string.share_message_app_name);
        }else{
            shareBody = mActivity.getResources().getString(R.string.text_seeking_help_with_story) + " \"" + story.getTitle() + "\"";
        }
        return shareBody;
    }

    private ImageView getDotImageView(int position, View view){
        ImageView dotImageView = null;
        switch (position){
            case 0: dotImageView = (ImageView) view.findViewById(R.id.dot1);
                break;
            case 1: dotImageView = (ImageView) view.findViewById(R.id.dot2);
                break;
            case 2: dotImageView = (ImageView) view.findViewById(R.id.dot3);
                break;
            case 3: dotImageView = (ImageView) view.findViewById(R.id.dot4);
                break;
            case 4: dotImageView = (ImageView) view.findViewById(R.id.dot5);
                break;
            case 5: dotImageView = (ImageView) view.findViewById(R.id.dot6);
                break;
            case 6: dotImageView = (ImageView) view.findViewById(R.id.dot7);
                break;
            case 7: dotImageView = (ImageView) view.findViewById(R.id.dot8);
                break;
            case 8: dotImageView = (ImageView) view.findViewById(R.id.dot9);
                break;
            case 9: dotImageView = (ImageView) view.findViewById(R.id.dot10);
                break;
        }
        return dotImageView;
    }


}
