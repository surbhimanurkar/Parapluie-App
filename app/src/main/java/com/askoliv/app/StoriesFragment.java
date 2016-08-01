package com.askoliv.app;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.askoliv.adapters.StoriesListAdapter;
import com.askoliv.model.Social;
import com.askoliv.model.Story;
import com.askoliv.utils.Constants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 */
public class StoriesFragment extends Fragment {

    private static final String TAG = StoriesFragment.class.getSimpleName();

    //Firebase References
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRootFirebaseRef;
    private DatabaseReference mStoriesRef;

    //Defining UI
    private ListView storiesListView;
    private StoriesListAdapter storiesAdapter;

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
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRootFirebaseRef = mFirebaseDatabase.getReference();
        mStoriesRef = mRootFirebaseRef.child(Constants.F_NODE_STORIES);

        storiesListView = (ListView) rootView.findViewById(android.R.id.list);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();


        //Attaching adapter to view
        //TODO Need to add sorting logic to this

        storiesAdapter = new StoriesListAdapter(mStoriesRef.orderByChild(Constants.F_KEY_STORIES_INVERSETIMEPUBLISHED).limitToFirst(Constants.NUM_STORIES_LOADED), getActivity(), R.layout.story);
        storiesListView.setAdapter(storiesAdapter);

    }

}
