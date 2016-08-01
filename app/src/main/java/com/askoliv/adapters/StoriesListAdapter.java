package com.askoliv.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.askoliv.model.Carousel;
import com.askoliv.model.Social;
import com.askoliv.model.Story;
import com.askoliv.app.R;
import com.askoliv.utils.Constants;
import com.askoliv.utils.FirebaseUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Map;

/**
 * Created by surbhimanurkar on 19-05-2016.
 * Adapter gets list of stories from firebase and populates them in the view
 */
public class StoriesListAdapter extends FirebaseListAdapter<Story>{

    private static final String TAG = StoriesListAdapter.class.getSimpleName();
    private Activity mActivity;
    private LinearLayout mDotsLayout;
    private DatabaseReference mFirebaseDatabaseRef;

    public StoriesListAdapter(Query ref, Activity activity, int layout) {
        super(ref, Story.class, layout, activity);
        this.mActivity = activity;
        mFirebaseDatabaseRef = FirebaseDatabase.getInstance().getReference();
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
    protected void populateView(View view, final Story story, final String key) {
        Log.d(TAG, "Populating view for:" + story.getTitle());
        //Colors
        final int primaryColor = ContextCompat.getColor(mActivity, R.color.colorPrimary);
        final int grayColor = ContextCompat.getColor(mActivity, R.color.colorDisabled);
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
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getShareBody(story,key));
                mActivity.startActivity(Intent.createChooser(sharingIntent, "Share"));
            }
        });

        chatRelatedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUtils.getInstance().sendMessage(getShareBody(story,key),null,null);
            }
        });



        //Populating carousel
        Map<String, Carousel> carouselItems = story.getCarousel();
        SparseArray<Carousel> resources = new SparseArray<>();
        mDotsLayout = (LinearLayout) view.findViewById(R.id.story_carousel_pagination);
        final Drawable dotDrawable = ContextCompat.getDrawable(mActivity, R.drawable.dot);
        final Drawable dotSelectedDrawable = ContextCompat.getDrawable(mActivity, R.drawable.dot_selected);

        for (String index: carouselItems.keySet()) {
            Carousel carousel = carouselItems.get(index);
            resources.put(carousel.getPosition(),carousel);
        }

        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        final int paddingDot = mActivity.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin_very_narrow);
        mDotsLayout.removeAllViews();
        for(int p=0; p < resources.size(); p++){
            //Log.d(TAG, "Adding dot for story with author: "+authorText + " at position:"+p + " DotsLayout contains:"+ mDotsLayout.getChildCount());
            //Adding pagination dots for carousel
            if(mDotsLayout.getChildAt(p)==null){
                ImageView dot = new ImageView(mActivity);
                dot.setTag(authorText);
                dot.setPadding(paddingDot,0,paddingDot,0);
                if(p==0){
                    dot.setImageDrawable(dotSelectedDrawable);
                }else{
                    dot.setImageDrawable(dotDrawable);
                }
                mDotsLayout.addView(dot,p,params);
            }
        }


        ViewPager storyViewPager = (ViewPager) view.findViewById(R.id.story_viewpager);
        StoryPagerAdapter storyPagerAdapter = new StoryPagerAdapter(mActivity, resources);
        storyViewPager.setAdapter(storyPagerAdapter);
        storyViewPager.setCurrentItem(0);
        storyViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected: Count:"+ mDotsLayout.getChildCount()+" position:"+position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private String getShareBody(Story story, String key){
        String shareLink = mActivity.getResources().getString(R.string.firebase_dynamic_link_domain) + mActivity.getResources().getString(R.string.deep_link_prefix) + key + mActivity.getResources().getString(R.string.deep_link_suffix);
        String shareBody = story.getTitle() + "\n" + shareLink + "\n\n" + mActivity.getResources().getString(R.string.share_message_app_name);
        return shareBody;
    }

}
