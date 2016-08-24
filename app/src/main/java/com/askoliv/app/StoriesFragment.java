package com.askoliv.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.askoliv.adapters.StoriesListAdapter;
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
    private RecyclerView storiesRecyclerView;
    private StoriesListAdapter storiesAdapter;
    private LinearLayoutManager mLayoutManager;

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

        storiesRecyclerView = (RecyclerView) rootView.findViewById(R.id.storiesList);
        mLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,true);
        storiesRecyclerView.setLayoutManager(mLayoutManager);
        Log.d(TAG, "onCreateView storiesRecyclerView:" + storiesRecyclerView);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();


        //Attaching adapter to view
        //TODO Need to add sorting logic to this

        storiesAdapter = new StoriesListAdapter(mStoriesRef.orderByChild(Constants.F_KEY_STORIES_TIMEPUBLISHED).limitToLast(Constants.NUM_STORIES_LOADED), getActivity(), R.layout.story);
        storiesAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = storiesAdapter.getItemCount();
                int lastVisiblePosition =
                        mLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    storiesRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        storiesRecyclerView.setAdapter(storiesAdapter);

        Log.d(TAG, "onStart StoriesAdapter:" + storiesRecyclerView.getAdapter());

    }

}
