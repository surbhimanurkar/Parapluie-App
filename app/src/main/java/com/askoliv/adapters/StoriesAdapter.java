package com.askoliv.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import com.askoliv.model.Story;
import com.askoliv.oliv.R;
import com.firebase.client.Query;

/**
 * Created by surbhimanurkar on 19-05-2016.
 * Adapter gets list of stories from firebase and populates them in the view
 */
public class StoriesAdapter extends FirebaseListAdapter<Story>{

    private Activity activity;

    public StoriesAdapter(Query ref, Activity activity, int layout) {
        super(ref, Story.class, layout, activity);
        this.activity = activity;
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
    protected void populateView(View view, Story story) {
        //TODO populate story in the list
        TextView titleTextView = (TextView) view.findViewById(R.id.story_title);
        titleTextView.setText(story.getTitle());
        TextView subtitleTextView = (TextView) view.findViewById(R.id.story_subtitle);
        subtitleTextView.setText(story.getSubtitle());
    }
}
