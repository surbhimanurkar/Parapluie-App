package com.askoliv.adapters;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.askoliv.model.Carousel;
import com.askoliv.app.R;
import com.bumptech.glide.Glide;

import java.util.Random;


/**
 * Created by surbhimanurkar on 07-06-2016.
 * Adapter to render carousel with image and text in stories
 */
public class StoryPagerAdapter extends PagerAdapter{

    private static final String TAG = StoryPagerAdapter.class.getSimpleName();
    private Activity mActivity;
    private SparseArray<Carousel> mResources;
    private View mView;

    public StoryPagerAdapter(Activity activity, SparseArray<Carousel> resources) {
        mActivity = activity;
        mResources = resources;
    }

    @Override
    public int getCount() {
        return mResources.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == (View) object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.d(TAG, "Position:" + position);
        View itemView = mActivity.getLayoutInflater().inflate(R.layout.story_pager_item, container, false);

        Log.d(TAG,"Container Child count:"+container.getChildCount());

        if(mResources.get(position)!=null){
            ImageView imageView = (ImageView) itemView.findViewById(R.id.story_pager_image);
            int[] randomBgColors = mActivity.getResources().getIntArray(R.array.random_bg_colors);
            int selectedRandomColor = randomBgColors[new Random().nextInt(randomBgColors.length)];
            imageView.setBackgroundColor(selectedRandomColor);
            Glide.with(mActivity).load(mResources.get(position).getImage()).centerCrop().into(imageView);
            Log.d(TAG, "Loading Image:" + mResources.get(position).getImage());

            TextView textView = (TextView) itemView.findViewById(R.id.story_pager_text);
            textView.setText(mResources.get(position).getText());
            Log.d(TAG, "Loading Text:" + mResources.get(position).getText());

            container.addView(itemView);
        }

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.d(TAG, "Destroying item at position:" + position);
        container.removeView((LinearLayout) object);
    }
}
