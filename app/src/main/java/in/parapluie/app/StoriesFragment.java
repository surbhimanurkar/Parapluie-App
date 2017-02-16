package in.parapluie.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.parapluie.adapters.StoriesListAdapter;
import in.parapluie.utils.Constants;
import in.parapluie.utils.FirebaseUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


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
    private RecyclerView mStoriesRecyclerView;
    private StoriesListAdapter mStoriesAdapter;
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
        mStoriesRef = mRootFirebaseRef.child(Constants.F_NODE_STORIES).child(Constants.F_NODE_STORIES_PUBLISHED);
        //mStoriesRef = mRootFirebaseRef.child(Constants.F_NODE_STORIES).child(Constants.F_NODE_STORIES_UNPUBLISHED);
        mStoriesRecyclerView = (RecyclerView) rootView.findViewById(R.id.storiesList);
        mLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,true);
        mLayoutManager.setStackFromEnd(true);
        mStoriesRecyclerView.setLayoutManager(mLayoutManager);
        Log.d(TAG, "onCreateView mStoriesRecyclerView:" + mStoriesRecyclerView);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        //Attaching adapter to view
        //TODO Need to add sorting logic to this
        //Setting adapter for StoriesList
        Query ref = mStoriesRef.orderByChild(Constants.F_KEY_STORIES_TIMEPUBLISHED).limitToLast(Constants.NUM_STORIES_LOADED);
        mStoriesAdapter = new StoriesListAdapter(ref, getActivity(), R.layout.story);
        mStoriesAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mStoriesAdapter.getItemCount();
                Log.d(TAG,"Tracker itemCount" + itemCount + " friendlymessagecount:" + friendlyMessageCount);
                int lastVisiblePosition =
                        mLayoutManager.findLastCompletelyVisibleItemPosition();

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCE_HISTORY,Context.MODE_PRIVATE);
                String selectedStoryKey = sharedPreferences.getString(Constants.HISTORY_PREF_STORY_KEY,null);

                if(selectedStoryKey!=null && lastVisiblePosition == -1){
                    int positionFromKey = FirebaseUtils.getInstance().getStoryPositionfromKey(mStoriesAdapter,selectedStoryKey);
                    if(positionFromKey == positionStart)
                        mStoriesRecyclerView.scrollToPosition(positionStart);
                }else if(lastVisiblePosition == -1 || ((positionStart >= (friendlyMessageCount - 1)) &&
                        (lastVisiblePosition == (positionStart - 1)))){
                    mStoriesRecyclerView.scrollToPosition(positionStart);
                    Log.d(TAG,"Scrolling to position start:"+positionStart);
                }

                if(lastVisiblePosition != -1){
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove(Constants.HISTORY_PREF_STORY_KEY);
                    editor.apply();
                }
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
            }
        });

        mStoriesRecyclerView.setAdapter(mStoriesAdapter);

        Log.d(TAG, "onStart StoriesAdapter:" + mStoriesRecyclerView.getAdapter());
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCE_HISTORY,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(Constants.HISTORY_PREF_STORY_KEY);
        editor.apply();
    }
}
