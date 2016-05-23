package com.askoliv.oliv;

import android.content.Context;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.askoliv.adapters.StoriesAdapter;
import com.askoliv.utils.Constants;
import com.firebase.client.Firebase;


/**
 * A simple {@link Fragment} subclass.
 */
public class StoriesFragment extends Fragment {

    private static final String TAG = StoriesFragment.class.getSimpleName();

    //Firebase References
    private Firebase mRootFirebaseRef;
    private Firebase mStoriesRef;

    //Defining UI
    private ListView storiesListView;
    private StoriesAdapter storiesAdapter;

    public StoriesFragment() {
        super();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_stories, container, false);

         /* Create the Firebase ref that is used for all authentication with Firebase */
        mRootFirebaseRef = new Firebase(getResources().getString(R.string.firebase_url));
        mStoriesRef = mRootFirebaseRef.child(Constants.FIREBASE_STORIES_NODE);

        storiesListView = (ListView) rootView.findViewById(R.id.listview_stories);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        //Attaching adapter to view
        //TODO Need to add sorting logic to this

        storiesAdapter = new StoriesAdapter(mStoriesRef, this.getActivity(), R.layout.story);
        storiesListView.setAdapter(storiesAdapter);
        storiesAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                storiesListView.setSelection(storiesAdapter.getCount() - 1);
            }
        });
    }

}
