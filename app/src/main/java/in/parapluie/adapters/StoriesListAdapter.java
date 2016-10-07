package in.parapluie.adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import in.parapluie.model.Carousel;
import in.parapluie.model.Social;
import in.parapluie.model.Story;
import in.parapluie.app.R;
import in.parapluie.utils.AndroidUtils;
import in.parapluie.utils.Constants;
import in.parapluie.utils.CustomViewPager;
import in.parapluie.utils.FirebaseUtils;
import in.parapluie.utils.Global;
import in.parapluie.utils.UsageAnalytics;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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
    private static final int sharesIndex = 0;
    private static final int lovesIndex = 1;
    private boolean lovedBefore = false;

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

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //Colors
        final int primaryColor = ContextCompat.getColor(mActivity, R.color.colorPrimary);
        final int grayColor = ContextCompat.getColor(mActivity, R.color.colorDivider);
        final int disabledColor = ContextCompat.getColor(mActivity, R.color.colorDisabled);

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
            storyViewHolder.setTextInitialVisibility(View.VISIBLE);
        }else{
            storyViewHolder.setTextInitialVisibility(View.GONE);
        }


        //Populating shares and loves buttons
        final Drawable lovedIconDrawable = ContextCompat.getDrawable(mActivity,R.drawable.ic_favorite_primary_24dp);
        final Drawable unlovedIconDrawable = ContextCompat.getDrawable(mActivity,R.drawable.ic_favorite_black_24dp);
        //Getting social data from firebase
        DatabaseReference socialReference = mFirebaseDatabaseRef.child(Constants.F_NODE_SOCIAL).child(Constants.F_NODE_STORIES).child(key);
        ValueEventListener socialListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Social social = dataSnapshot.getValue(Social.class);

                SparseArray<Integer> socialCount = new SparseArray<>(2);
                lovedBefore = false;
                if(social!=null){
                    if(social.getShares()!=null) {
                        int sharesCount = 0;
                        for(String userKey: social.getShares().keySet()){
                            sharesCount += social.getShares().get(userKey);
                        }
                        socialCount.put(sharesIndex, sharesCount);
                    }else
                        socialCount.put(sharesIndex,0);

                    if(social.getLoves()!=null) {
                        int lovesCount = 0;
                        for(String userKey: social.getLoves().keySet()){
                            if(social.getLoves().get(userKey))
                                lovesCount += 1;
                        }
                        socialCount.put(lovesIndex, lovesCount);
                        if (firebaseUser != null && social.getLoves().get(firebaseUser.getUid()) != null && social.getLoves().get(firebaseUser.getUid()))
                            lovedBefore = true;
                    }else
                        socialCount.put(lovesIndex,0);
                }else{
                    socialCount.put(sharesIndex,0);
                    socialCount.put(lovesIndex,0);
                }
                String shares = socialCount.get(sharesIndex) + "";
                String loves = socialCount.get(lovesIndex) + "";
                storyViewHolder.setShareButton(shares);
                storyViewHolder.setLoveButton(loves,lovedBefore,primaryColor,disabledColor,lovedIconDrawable,unlovedIconDrawable);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.d(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        socialReference.addValueEventListener(socialListener);


        storyViewHolder.loadStorySnapshot(mActivity, story.getStorySnapshot());
        storyViewHolder.setShareButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(androidUtils.checkPermission(mActivity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},Constants.PERMISSIONS_REQUEST_STORAGE_SHARE)) {
                    ImageView snapshotImageView = storyViewHolder.getSnapshotImageView();
                    Log.d(TAG,"snapshotImageView:"+snapshotImageView);
                    boolean shared = androidUtils.shareStory(mActivity, androidUtils.getShareStoryBody(mActivity, story, key, true), snapshotImageView);
                    if (shared){
                        FirebaseUtils.getInstance().increaseShareCount(key);
                        mUsageAnalytics.trackShareEvent(key, story.getTitle());
                    }else
                        Toast.makeText(mActivity,mActivity.getResources().getString(R.string.story_share_error_generic),Toast.LENGTH_SHORT).show();
                }else{
                    SharedPreferences sharedPreferences = mActivity.getSharedPreferences(Constants.SHARED_PREFERENCE_STORY, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Constants.STORY_PREF_KEY, key);
                    editor.putString(Constants.STORY_PREF_TITLE, story.getTitle());
                    editor.putString(Constants.STORY_PREF_SHARE_TEXT, androidUtils.getShareStoryBody(mActivity,story,key,true));
                    //editor.putString(Constants.STORY_PREF_SNAPSHOT, story.getStorySnapshot());
                    Global.currentStorySnapshot = storyViewHolder.getSnapshotImageView();
                    editor.apply();
                }
            }
        });

        storyViewHolder.setLoveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                //Get User ID

                boolean loved = false;

                Log.d(TAG,"LoveButton Tag:" + v.getTag());
                if(v.getTag()!=null && (boolean)v.getTag()){
                    loved = false;
                    Toast.makeText(mActivity,mActivity.getString(R.string.toast_unlove_button),Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onCLick lovebutton unselected");
                    ((Button)v).setTextColor(disabledColor);
                    ((Button)v).setCompoundDrawablesWithIntrinsicBounds(null,null,unlovedIconDrawable,null);
                }else{
                    loved = true;
                    Toast.makeText(mActivity,mActivity.getString(R.string.toast_love_button),Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onCLick lovebutton selected");
                    ((Button)v).setTextColor(primaryColor);
                    ((Button)v).setCompoundDrawablesWithIntrinsicBounds(null,null,lovedIconDrawable,null);
                }
                v.setTag(loved);

                if(firebaseUser!=null){
                    mFirebaseDatabaseRef.child(Constants.F_NODE_SOCIAL).child(Constants.F_NODE_STORIES).child(key)
                            .child(Constants.F_KEY_STORIES_LOVES)
                            .child(firebaseUser.getUid()).setValue(loved);
                    mFirebaseDatabaseRef.child(Constants.F_NODE_USER).child(firebaseUser.getUid())
                            .child(Constants.F_KEY_USER_ACTIVITY).child(Constants.F_KEY_STORIES_LOVES)
                            .child(key).setValue(loved);
                }
                mUsageAnalytics.trackLikeEvent(story);
            }
        });

        final CustomViewPager tabsViewPager = (CustomViewPager)mActivity.findViewById(R.id.container);

        storyViewHolder.setChatRelatedButton(null,new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUtils.getInstance().sendMessageChatRelatedtoStories(mActivity,story,key);
                tabsViewPager.setCurrentItem(1);
                //Toast.makeText(mActivity,mActivity.getString(R.string.toast_chat_related_to_story),Toast.LENGTH_SHORT).show();
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
            if(!title.equals(titleTextView.getText()))
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

        public void setShareButton(String shareButtonText){
            Button shareButton = (Button) mView.findViewById(R.id.story_button_share);
            if(shareButtonText!=null)
                shareButton.setText(shareButtonText);
        }

        public void setShareButtonClickListener(View.OnClickListener onClickListener){
            Button shareButton = (Button) mView.findViewById(R.id.story_button_share);
            if(onClickListener!=null)
                shareButton.setOnClickListener(onClickListener);
        }

        public void setLoveButton(String loveButtonText, boolean lovedTag,int lovedColor, int unlovedColor, Drawable lovedDrawable, Drawable unlovedDrawable){
            Button loveButton = (Button) mView.findViewById(R.id.story_button_love);
            if(loveButtonText!=null)
                loveButton.setText(loveButtonText);
            loveButton.setTag(lovedTag);
            if(lovedTag) {
                loveButton.setTextColor(lovedColor);
                loveButton.setCompoundDrawablesWithIntrinsicBounds(null,null,lovedDrawable,null);
            }else{
                loveButton.setTextColor(unlovedColor);
                loveButton.setCompoundDrawablesWithIntrinsicBounds(null,null,unlovedDrawable,null);
            }
        }

        public void setLoveButtonClickListener(View.OnClickListener onClickListener){
            Button loveButton = (Button) mView.findViewById(R.id.story_button_love);
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

        public void loadStorySnapshot(Activity activity, String snapshot){
            ImageView snapShotImageView = (ImageView) mView.findViewById(R.id.story_snapshot);
            Glide.with(activity).load(snapshot).diskCacheStrategy(DiskCacheStrategy.NONE).into(snapShotImageView);
        }

        public ImageView getSnapshotImageView(){
            return ((ImageView) mView.findViewById(R.id.story_snapshot));
        }
    }


}
